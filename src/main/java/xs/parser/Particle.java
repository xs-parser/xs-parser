package xs.parser;

import java.math.*;
import java.util.*;
import java.util.function.*;
import org.w3c.dom.*;
import xs.parser.ModelGroup.*;
import xs.parser.Wildcard.*;
import xs.parser.internal.*;
import xs.parser.internal.util.*;
import xs.parser.internal.util.SequenceParser.*;
import xs.parser.v.*;

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

	private final Deferred<? extends AnnotatedComponent> context;
	private final Node node;
	private final Deque<Annotation> annotations;
	private final Number maxOccurs;
	private final Number minOccurs;
	private final Deferred<? extends Term> term;

	Particle(final Deferred<? extends AnnotatedComponent> context, final Node node, final Deque<Annotation> annotations, final Number maxOccurs, final Number minOccurs, final Term term) {
		this(context, node, annotations, maxOccurs, minOccurs, new DeferredValue<>(Objects.requireNonNull(term)));
	}

	Particle(final Deferred<? extends AnnotatedComponent> context, final Node node, final Deque<Annotation> annotations, final Number maxOccurs, final Number minOccurs, final Deferred<? extends Term> term) {
		this.context = Objects.requireNonNull(context);
		this.node = Objects.requireNonNull(node);
		this.annotations = Objects.requireNonNull(annotations);
		this.maxOccurs = Objects.requireNonNull(maxOccurs);
		this.minOccurs = Objects.requireNonNull(minOccurs);
		this.term = Objects.requireNonNull(term);
	}

	private static Particle parse(final Result result) {
		final Deferred<? extends AnnotatedComponent> context = result.context();
		final Node node = result.node();
		final Deque<Annotation> annotations = Annotation.of(result).resolve(node);
		final Number maxOccurs = result.value(AttrParser.MAX_OCCURS);
		final Number minOccurs = result.value(AttrParser.MIN_OCCURS);
		final Compositor compositor = TagParser.ALL.equalsName(node) ? Compositor.ALL
				: TagParser.CHOICE.equalsName(node) ? Compositor.CHOICE
				: TagParser.SEQUENCE.equalsName(node) ? Compositor.SEQUENCE
				: null;
		final Deque<Particle> particles = result.parseAll(TagParser.ELEMENT.use(), TagParser.GROUP.use(), TagParser.ALL, TagParser.CHOICE, TagParser.SEQUENCE, TagParser.ANY);
		final DeferredValue<Particle> self = new DeferredValue<>();
		final ModelGroup term = ModelGroup.synthetic(self, node, annotations, compositor, particles);
		return self.set(new Particle(context, node, annotations, maxOccurs, minOccurs, term));
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
		TagParser.register(TagParser.Names.ALL, parser, Particle.class, Particle::parse);
		TagParser.register(TagParser.Names.CHOICE, parser, Particle.class, Particle::parse);
		TagParser.register(TagParser.Names.SEQUENCE, parser, Particle.class, Particle::parse);
		VisitorHelper.register(Particle.class, Particle::visit);
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
		return minOccurs.intValue() == 0 || (term() instanceof ModelGroup && BigInteger.ZERO.equals(effectiveTotalRangeMinimum()));
	}

	void visit(final Visitor visitor) {
		if (visitor.visit(context.get(), node, this)) {
			visitor.onParticle(context.get(), node, this);
			annotations.forEach(a -> a.visit(visitor));
			final Term t = term();
			if (t instanceof ModelGroup) {
				((ModelGroup) t).visit(visitor);
			} else if (t instanceof Element) {
				((Element) t).visit(visitor);
			} else if (t instanceof Any) {
				((Any) t).visit(visitor);
			} else {
				((AnyAttribute) t).visit(visitor);
			}
		}
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
	public Deque<Annotation> annotations() {
		return Deques.unmodifiableDeque(annotations);
	}

}
