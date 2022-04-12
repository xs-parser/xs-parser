package xs.parser;

import java.util.*;
import javax.xml.namespace.*;
import xs.parser.internal.*;
import xs.parser.internal.SequenceParser.*;

/**
 * <pre>
 * &lt;restriction
 *   base = QName
 *   id = ID
 *   {any attributes with non-schema namespace . . .}&gt;
 *   Content: (annotation?, (simpleType?, (minExclusive | minInclusive | maxExclusive | maxInclusive | totalDigits | fractionDigits | length | minLength | maxLength | enumeration | whiteSpace | pattern | assertion | explicitTimezone | {any with namespace: ##other})*))
 * &lt;/restriction&gt;
 * </pre>
 */
public class SimpleRestriction {

	protected static final SequenceParser parser = new SequenceParser()
			.optionalAttributes(AttributeValue.BASE, AttributeValue.ID)
			.elements(0, 1, ElementValue.ANNOTATION)
			.elements(0, 1, ElementValue.SIMPLETYPE)
			.elements(0, Integer.MAX_VALUE, ElementValue.MINEXCLUSIVE, ElementValue.MININCLUSIVE, ElementValue.MAXEXCLUSIVE, ElementValue.MAXINCLUSIVE, ElementValue.TOTALDIGITS, ElementValue.FRACTIONDIGITS, ElementValue.LENGTH, ElementValue.MINLENGTH, ElementValue.MAXLENGTH, ElementValue.ENUMERATION, ElementValue.WHITESPACE, ElementValue.PATTERN, ElementValue.ASSERTION, ElementValue.EXPLICITTIMEZONE, ElementValue.ANY);

	private final Deque<Annotation> annotations;
	private final Deferred<SimpleType> base;
	private final Deque<ConstrainingFacet<?>> facets;
	private final Deque<Particle<Wildcard>> wildcard;

	SimpleRestriction(final Deque<Annotation> annotations, final Deferred<SimpleType> base, final Deque<ConstrainingFacet<?>> facets, final Deque<Particle<Wildcard>> wildcard) {
		this.annotations = Objects.requireNonNull(annotations);
		this.base = Objects.requireNonNull(base);
		this.facets = Objects.requireNonNull(facets);
		this.wildcard = Objects.requireNonNull(wildcard);
	}

	protected static SimpleRestriction parse(final Result result) {
		final QName baseType = result.value(AttributeValue.BASE);
		final Deferred<SimpleType> base = baseType == null
				? Deferred.of(() -> result.parse(ElementValue.SIMPLETYPE))
				: result.schema().find(baseType, SimpleType.class);
		final Deque<ConstrainingFacet<?>> facets = result.parseAll(ElementValue.MINEXCLUSIVE, ElementValue.MININCLUSIVE, ElementValue.MAXEXCLUSIVE, ElementValue.MAXINCLUSIVE, ElementValue.TOTALDIGITS, ElementValue.FRACTIONDIGITS, ElementValue.LENGTH, ElementValue.MINLENGTH, ElementValue.MAXLENGTH, ElementValue.ENUMERATION, ElementValue.WHITESPACE, ElementValue.PATTERN, ElementValue.ASSERTION, ElementValue.EXPLICITTIMEZONE);
		final Deque<Particle<Wildcard>> wildcard = result.parseAll(ElementValue.ANY);
		return new SimpleRestriction(result.annotations(), base, facets, wildcard);
	}

	protected Deque<Annotation> annotations() {
		return annotations;
	}

	protected Deferred<SimpleType> base() {
		return base;
	}

	protected Deque<ConstrainingFacet<?>> facets() {
		return facets;
	}

	protected Deque<Particle<Wildcard>> wildcard() {
		return wildcard;
	}

}