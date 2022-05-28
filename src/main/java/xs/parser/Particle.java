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

	private static final SequenceParser parser = new SequenceParser()
			.optionalAttributes(AttrParser.ID, AttrParser.MAX_OCCURS, AttrParser.MIN_OCCURS)
			.elements(0, 1, TagParser.ANNOTATION)
			.elements(0, Integer.MAX_VALUE, TagParser.ELEMENT.use(), TagParser.GROUP.use(), TagParser.ALL, TagParser.CHOICE, TagParser.SEQUENCE, TagParser.ANY);
	static final String UNBOUNDED = "unbounded";

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
	private static Particle parse(final Result result) {
		final String maxOccurs = result.value(AttrParser.MAX_OCCURS);
		final String minOccurs = result.value(AttrParser.MIN_OCCURS);
		final Compositor compositor = TagParser.ALL.equalsName(result.node()) ? Compositor.ALL
				: TagParser.CHOICE.equalsName(result.node()) ? Compositor.CHOICE
				: TagParser.SEQUENCE.equalsName(result.node()) ? Compositor.SEQUENCE
				: null;
		final Deque<Particle> particles = result.parseAll(TagParser.ELEMENT.use(), TagParser.GROUP.use(), TagParser.ALL, TagParser.CHOICE, TagParser.SEQUENCE, TagParser.ANY);
		final ModelGroup term = ModelGroup.synthetic(result.node(), result.annotations(), compositor, (Deque<Particle>) (Object) particles);
		return new Particle(result.node(), result.annotations(), maxOccurs, minOccurs, term);
	}

	private static String getNodeValueAsMaxOccurs(final Node node) {
		return UNBOUNDED.equals(node.getNodeValue())
				? UNBOUNDED
				: NodeHelper.getNodeValueAsNonNegativeInteger(node);
	}

	static void register() {
		AttrParser.register(AttrParser.Names.MAX_OCCURS, String.class, "1", Particle::getNodeValueAsMaxOccurs);
		AttrParser.register(AttrParser.Names.MIN_OCCURS, String.class, "1", NodeHelper::getNodeValueAsNonNegativeInteger);
		TagParser.register(new String[] { TagParser.Names.ALL, TagParser.Names.CHOICE, TagParser.Names.SEQUENCE }, parser, Particle.class, Particle::parse);
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
					if (UNBOUNDED.equals(maxOccurs) && !NodeHelper.isZero(pEffectiveMax)) {
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
		if (NodeHelper.isZero(minOccurs)) {
			return true;
		} else if (term() instanceof ModelGroup) {
			return NodeHelper.isZero(effectiveTotalRangeMinimum());
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
