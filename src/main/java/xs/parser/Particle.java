package xs.parser;

import java.util.*;
import org.w3c.dom.*;
import xs.parser.ModelGroup.*;
import xs.parser.internal.*;
import xs.parser.internal.util.*;
import xs.parser.internal.util.SequenceParser.*;

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
public class Particle implements AnnotatedComponent {

	static final String UNBOUNDED = "unbounded";
	static final SequenceParser parser = new SequenceParser()
			.optionalAttributes(AttributeValue.ID, AttributeValue.MAXOCCURS, AttributeValue.MINOCCURS)
			.elements(0, 1, ElementValue.ANNOTATION)
			.elements(0, Integer.MAX_VALUE, ElementValue.ELEMENT, ElementValue.GROUP, ElementValue.ALL, ElementValue.CHOICE, ElementValue.SEQUENCE, ElementValue.ANY);

	private final Node node;
	private final Deque<Annotation> annotations;
	private final String maxOccurs;
	private final String minOccurs;
	private final Deferred<? extends Term> term;

	Particle(final Node node, final Deque<Annotation> annotations, final String maxOccurs, final String minOccurs, final Term term) {
		this(node, annotations, maxOccurs, minOccurs, () -> term);
	}

	Particle(final Node node, final Deque<Annotation> annotations, final String maxOccurs, final String minOccurs, final Deferred<? extends Term> term) {
		this.node = Objects.requireNonNull(node);
		this.annotations = Objects.requireNonNull(annotations);
		this.maxOccurs = maxOccurs;
		this.minOccurs = minOccurs;
		this.term = term;
	}

	@SuppressWarnings("unchecked")
	static Particle parse(final Result result) {
		final String maxOccurs = result.value(AttributeValue.MAXOCCURS);
		final String minOccurs = result.value(AttributeValue.MINOCCURS);
		final Compositor compositor = ElementValue.ALL.equalsName(result.node()) ? Compositor.ALL
				: ElementValue.CHOICE.equalsName(result.node()) ? Compositor.CHOICE
				: ElementValue.SEQUENCE.equalsName(result.node()) ? Compositor.SEQUENCE
				: null;
		final Deque<Particle> particles = result.parseAll(ElementValue.ELEMENT, ElementValue.ALL, ElementValue.CHOICE, ElementValue.SEQUENCE, ElementValue.ANY);
		final ModelGroup term = ModelGroup.synthetic(result.node(), result.annotations(), compositor, (Deque<Particle>) (Object) particles);
		return new Particle(result.node(), result.annotations(), maxOccurs, minOccurs, term);
	}

	/**
	 * The effective total range algorithm defined at <a href="https://www.w3.org/TR/xmlschema11-1/#cos-seq-range">https://www.w3.org/TR/xmlschema11-1/#cos-seq-range</a> and <a href="https://www.w3.org/TR/xmlschema11-1/#cos-choice-range">https://www.w3.org/TR/xmlschema11-1/#cos-choice-range</a>.
	 * @return the maximum portion of the effective total range
	 */
	private String effectiveTotalRangeMaximum() {
		if (term() instanceof Element || term() instanceof Wildcard) {
			return maxOccurs;
		} else if (term() instanceof ModelGroup) {
			final ModelGroup g = (ModelGroup) term();
			final boolean isChoice = Compositor.CHOICE.equals(g.compositor());
			long cumulative = 0L; // unsigned
			for (final Particle p : g.particles()) {
				final String pEffectiveMax = p.effectiveTotalRangeMaximum();
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
	private String effectiveTotalRangeMinimum() {
		if (term() instanceof Element || term() instanceof Wildcard) {
			return minOccurs;
		} else if (term() instanceof ModelGroup) {
			final ModelGroup g = (ModelGroup) term();
			final boolean isChoice = Compositor.CHOICE.equals(g.compositor());
			long cumulative = 0L;
			for (final Particle p : g.particles()) {
				final long pEffectiveMin = Long.parseUnsignedLong(p.effectiveTotalRangeMinimum());
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
	boolean isEmptiable() {
		if ("0".equals(minOccurs)) {
			return true;
		} else if (term() instanceof ModelGroup) {
			return "0".equals(effectiveTotalRangeMinimum());
		}
		return false;
	}

	public String maxOccurs() {
		return maxOccurs;
	}

	public String minOccurs() {
		return minOccurs;
	}

	public Term term() {
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
