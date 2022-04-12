package xs.parser;

import java.util.*;
import org.w3c.dom.*;
import xs.parser.ModelGroup.*;
import xs.parser.internal.*;
import xs.parser.internal.SequenceParser.*;

/**
 * <pre>
 * &lt;all
 *   id = ID
 *   maxOccurs = (0 | 1) : 1
 *   minOccurs = (0 | 1) : 1
 *   {any attributes with non-schema namespace . . .}&gt;
 *   Content: (annotation?, (element | any | group)*)
 * &lt;/all&gt;
 *
 * &lt;choice
 *   id = ID
 *   maxOccurs = (nonNegativeInteger | unbounded)  : 1
 *   minOccurs = nonNegativeInteger : 1
 *   {any attributes with non-schema namespace . . .}&gt;
 *   Content: (annotation?, (element | group | choice | sequence | any)*)
 * &lt;/choice&gt;
 *
 * &lt;sequence
 *   id = ID
 *   maxOccurs = (nonNegativeInteger | unbounded)  : 1
 *   minOccurs = nonNegativeInteger : 1
 *   {any attributes with non-schema namespace . . .}&gt;
 *   Content: (annotation?, (element | group | choice | sequence | any)*)
 * &lt;/sequence&gt;
 * </pre>
 */
public class Particle<T extends Term> implements AnnotatedComponent {

	protected static final String UNBOUNDED = "unbounded";
	protected static final SequenceParser parser = new SequenceParser()
			.optionalAttributes(AttributeValue.ID, AttributeValue.MAXOCCURS, AttributeValue.MINOCCURS)
			.elements(0, 1, ElementValue.ANNOTATION)
			.elements(0, Integer.MAX_VALUE, ElementValue.ELEMENT, ElementValue.GROUP, ElementValue.ALL, ElementValue.CHOICE, ElementValue.SEQUENCE, ElementValue.ANY);

	private final Node node;
	private final Deque<Annotation> annotations;
	private final String maxOccurs;
	private final String minOccurs;
	private final Deferred<T> term;

	Particle(final Node node, final Deque<Annotation> annotations, final String maxOccurs, final String minOccurs, final T term) {
		this(node, annotations, maxOccurs, minOccurs, Deferred.value(term));
	}

	Particle(final Node node, final Deque<Annotation> annotations, final String maxOccurs, final String minOccurs, final Deferred<T> term) {
		this.node = Objects.requireNonNull(node);
		this.annotations = Objects.requireNonNull(annotations);
		this.maxOccurs = maxOccurs;
		this.minOccurs = minOccurs;
		this.term = term;
	}

	@SuppressWarnings("unchecked")
	protected static Particle<ModelGroup> parse(final Result result) {
		final String maxOccurs = result.value(AttributeValue.MAXOCCURS);
		final String minOccurs = result.value(AttributeValue.MINOCCURS);
		final Deque<Particle<?>> elements = (Deque<Particle<?>>) (Object) result.parseAll(ElementValue.ELEMENT);
		final Deque<Particle<?>> modelGroups = (Deque<Particle<?>>) (Object) result.parseAll(ElementValue.ALL, ElementValue.CHOICE, ElementValue.SEQUENCE);
		final Deque<Particle<?>> any = (Deque<Particle<?>>) (Object) result.parseAll(ElementValue.ANY);
		final Compositor compositor = ElementValue.ALL.equalsName(result.node()) ? Compositor.ALL
				: ElementValue.CHOICE.equalsName(result.node()) ? Compositor.CHOICE
				: ElementValue.SEQUENCE.equalsName(result.node()) ? Compositor.SEQUENCE
				: null;
		final Deque<Particle<?>> particles = new DeferredArrayDeque<>(modelGroups.size() + elements.size() + any.size());
		particles.addAll(modelGroups);
		particles.addAll(elements);
		particles.addAll(any);
		final ModelGroup term = ModelGroup.synthetic(result.node(), result.annotations(), compositor, particles);
		return new Particle<>(result.node(), result.annotations(), maxOccurs, minOccurs, term);
	}

	/**
	 * The effective total range algorithm defined at <a href="https://www.w3.org/TR/xmlschema11-1/#cos-seq-range">https://www.w3.org/TR/xmlschema11-1/#cos-seq-range</a> and <a href="https://www.w3.org/TR/xmlschema11-1/#cos-choice-range">https://www.w3.org/TR/xmlschema11-1/#cos-choice-range</a>.
	 * @return the maximum portion of the effective total range
	 */
	protected String effectiveMaximum() {
		if (term() instanceof Element || term() instanceof Wildcard) {
			return maxOccurs;
		} else if (term() instanceof ModelGroup) {
			final ModelGroup g = (ModelGroup) term();
			final boolean isChoice = Compositor.CHOICE.equals(g.compositor());
			long cumulative = 0L; // unsigned
			for (final Particle<?> p : g.particles()) {
				final String pEffectiveMax = p.effectiveMaximum();
				if (UNBOUNDED.equals(pEffectiveMax)) {
					return UNBOUNDED;
				} else {
					if (UNBOUNDED.equals(maxOccurs) && !"0".equals(pEffectiveMax)) {
						return UNBOUNDED;
					} else {
						final long pMaxOccurs = Long.parseUnsignedLong(pEffectiveMax);
						// TODO unsignedMax
						cumulative = isChoice ? Long.max(cumulative, pMaxOccurs) : cumulative + pMaxOccurs;
					}
				}
			}
			return UNBOUNDED.equals(maxOccurs)
					? Long.toUnsignedString(0L)
					: Long.toUnsignedString(Long.parseUnsignedLong(maxOccurs) * cumulative);
		}
		throw new IllegalStateException(String.valueOf(term()));
	}

	/**
	 * The effective total range algorithm defined at <a href="https://www.w3.org/TR/xmlschema11-1/#cos-seq-range">https://www.w3.org/TR/xmlschema11-1/#cos-seq-range</a> and <a href="https://www.w3.org/TR/xmlschema11-1/#cos-choice-range">https://www.w3.org/TR/xmlschema11-1/#cos-choice-range</a>.
	 * @return the minimum portion of the effective total range
	 */
	protected String effectiveMinimum() {
		if (term() instanceof Element || term() instanceof Wildcard) {
			return minOccurs;
		} else if (term() instanceof ModelGroup) {
			final ModelGroup g = (ModelGroup) term();
			final boolean isChoice = Compositor.CHOICE.equals(g.compositor());
			long cumulative = 0L;
			for (final Particle<?> p : g.particles()) {
				final long pEffectiveMin = Long.parseUnsignedLong(p.effectiveMinimum());
				// TODO unsignedMin
				cumulative = isChoice ? Long.min(cumulative, pEffectiveMin) : cumulative + pEffectiveMin;
			}
			return Long.toUnsignedString(Long.parseUnsignedLong(minOccurs) * cumulative);
		}
		throw new IllegalStateException(String.valueOf(term()));
	}

	/**
	 * Particle emptiable algorithm defined at <a href="https://www.w3.org/TR/xmlschema11-1/#cos-group-emptiable">https://www.w3.org/TR/xmlschema11-1/#cos-group-emptiable</a>.
	 * @return {@code true} if this {@code Particle} is emptiable
	 */
	protected boolean isEmptiable() {
		if ("0".equals(minOccurs)) {
			return true;
		} else if (term() instanceof ModelGroup) {
			return "0".equals(effectiveMinimum());
		}
		return false;
	}

	public String maxOccurs() {
		return maxOccurs;
	}

	public String minOccurs() {
		return minOccurs;
	}

	public T term() {
		return term.get();
	}

	@Override
	public Node node() {
		return node;
	}

	@Override
	public Deque<Annotation> annotations() {
		return Deques.unmodifiableDeque(annotations);
	}

}