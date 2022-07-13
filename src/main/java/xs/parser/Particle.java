package xs.parser;

import java.math.*;
import java.util.*;
import java.util.function.*;
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
	private final Number maxOccurs;
	private final Number minOccurs;
	private final Deferred<? extends Term> term;

	Particle(final Node node, final Deque<Annotation> annotations, final Number maxOccurs, final Number minOccurs, final Term term) {
		this(node, annotations, maxOccurs, minOccurs, () -> term);
	}

	Particle(final Node node, final Deque<Annotation> annotations, final Number maxOccurs, final Number minOccurs, final Deferred<? extends Term> term) {
		this.node = Objects.requireNonNull(node);
		this.annotations = Objects.requireNonNull(annotations);
		this.maxOccurs = maxOccurs;
		this.minOccurs = minOccurs;
		this.term = term;
	}

	private static Particle parse(final Result result) {
		final Number maxOccurs = result.value(AttrParser.MAX_OCCURS);
		final Number minOccurs = result.value(AttrParser.MIN_OCCURS);
		final Compositor compositor = TagParser.ALL.equalsName(result.node()) ? Compositor.ALL
				: TagParser.CHOICE.equalsName(result.node()) ? Compositor.CHOICE
				: TagParser.SEQUENCE.equalsName(result.node()) ? Compositor.SEQUENCE
				: null;
		final Deque<Particle> particles = result.parseAll(TagParser.ELEMENT.use(), TagParser.GROUP.use(), TagParser.ALL, TagParser.CHOICE, TagParser.SEQUENCE, TagParser.ANY);
		final ModelGroup term = ModelGroup.synthetic(result.node(), result.annotations(), compositor, particles);
		return new Particle(result.node(), result.annotations(), maxOccurs, minOccurs, term);
	}

	private static Number getAttrValueAsMaxOccurs(final Attr attr) {
		final String value = NodeHelper.collapseWhitespace(attr.getValue());
		return UNBOUNDED.equals(value)
				? Unbounded.INSTANCE
				: NodeHelper.getNodeValueAsNonNegativeInteger(attr, value);
	}

	static void register() {
		AttrParser.register(AttrParser.Names.MAX_OCCURS, Number.class, 1, Particle::getAttrValueAsMaxOccurs);
		AttrParser.register(AttrParser.Names.MIN_OCCURS, Number.class, 1, NodeHelper::getAttrValueAsNonNegativeInteger);
		TagParser.register(new String[] { TagParser.Names.ALL, TagParser.Names.CHOICE, TagParser.Names.SEQUENCE }, parser, Particle.class, Particle::parse);
	}

	/**
	 * The minimum portion of the effective total range algorithm defined at <a href="https://www.w3.org/TR/xmlschema11-1/#cos-seq-range">https://www.w3.org/TR/xmlschema11-1/#cos-seq-range</a> and <a href="https://www.w3.org/TR/xmlschema11-1/#cos-choice-range">https://www.w3.org/TR/xmlschema11-1/#cos-choice-range</a>.
	 * @return the minimum portion of the effective total range
	 */
	private BigInteger effectiveTotalRangeMinimum() {
		final Term t = term();
		BigInteger result = minOccurs instanceof BigInteger ? (BigInteger) minOccurs : new BigInteger(minOccurs.toString());
		if (t instanceof ModelGroup) {
			final ModelGroup m = (ModelGroup) t;
			final BinaryOperator<BigInteger> fn;
			switch (m.compositor()) {
			case ALL:
			case SEQUENCE:
				fn = BigInteger::add;
				break;
			case CHOICE:
				fn = BigInteger::min;
				break;
			default:
				throw new AssertionError(m.compositor().toString());
			}
			final Optional<BigInteger> value = m.particles().stream()
					.map(Particle::effectiveTotalRangeMinimum)
					.reduce(fn);
			if (value.isPresent()) {
				result = result.multiply(value.get());
			} else {
				return BigInteger.ZERO;
			}
		}
		return result;
	}

	/**
	 * Particle emptiable algorithm defined at <a href="https://www.w3.org/TR/xmlschema11-1/#cos-group-emptiable">https://www.w3.org/TR/xmlschema11-1/#cos-group-emptiable</a>.
	 * @return {@code true} if this {@code Particle} is emptiable
	 */
	boolean isEmptiable() {
		if (minOccurs.intValue() == 0) {
			return true;
		} else if (term() instanceof ModelGroup) {
			return BigInteger.ZERO.equals(effectiveTotalRangeMinimum());
		}
		return false;
	}

	public Number maxOccurs() {
		return maxOccurs;
	}

	public Number minOccurs() {
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
