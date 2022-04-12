package xs.parser;

import java.util.*;
import javax.xml.namespace.*;
import xs.parser.Annotation.*;
import xs.parser.ComplexType.*;
import xs.parser.internal.*;
import xs.parser.internal.SequenceParser.*;

/**
 * <pre>
 * &lt;simpleContent
 *   id = ID
 *   {any attributes with non-schema namespace . . .}&gt;
 *   Content: (annotation?, (restriction | extension))
 * &lt;/simpleContent&gt;
 * </pre>
 */
public class SimpleContent {

	/**
	 * <pre>
	 * &lt;restriction
	 *   base = QName
	 *   id = ID
	 *   {any attributes with non-schema namespace . . .}&gt;
	 *   Content: (annotation?, (simpleType?, (minExclusive | minInclusive | maxExclusive | maxInclusive | totalDigits | fractionDigits | length | minLength | maxLength | enumeration | whiteSpace | pattern | assertion | {any with namespace: ##other})*)?, ((attribute | attributeGroup)*, anyAttribute?), assert*)
	 * &lt;/restriction&gt;
	 * </pre>
	 */
	public static class SimpleContentRestriction {

		protected static final SequenceParser parser = new SequenceParser()
				.requiredAttributes(AttributeValue.BASE)
				.optionalAttributes(AttributeValue.ID)
				.elements(0, 1, ElementValue.ANNOTATION)
				.elements(0, 1, ElementValue.SIMPLETYPE)
				.elements(0, Integer.MAX_VALUE, ElementValue.MINEXCLUSIVE, ElementValue.MININCLUSIVE, ElementValue.MAXEXCLUSIVE, ElementValue.MAXINCLUSIVE, ElementValue.TOTALDIGITS, ElementValue.FRACTIONDIGITS, ElementValue.LENGTH, ElementValue.MINLENGTH, ElementValue.MAXLENGTH, ElementValue.ENUMERATION, ElementValue.WHITESPACE, ElementValue.PATTERN, ElementValue.ASSERTION, ElementValue.ANY)
				.elements(0, Integer.MAX_VALUE, ElementValue.ATTRIBUTE, ElementValue.ATTRIBUTEGROUP)
				.elements(0, 1, ElementValue.ANYATTRIBUTE)
				.elements(0, Integer.MAX_VALUE, ElementValue.ASSERT);

		private final Deque<Annotation> annotations;
		private final Deferred<ComplexType> base;
		private final Deque<ConstrainingFacet<?>> facets;
		private final Deque<Particle<Wildcard>> wildcard;
		private final SimpleType simpleType;
		private final Deferred<Deque<AttributeUse>> attributeUses;
		private final Wildcard attributeWildcard;
		private final Deque<Assertion> asserts;

		private SimpleContentRestriction(final Deque<Annotation> annotations, final Deferred<ComplexType> base, final Deque<ConstrainingFacet<?>> facets, final Deque<Particle<Wildcard>> wildcard, final SimpleType simpleType, final Deferred<Deque<AttributeUse>> attributeUses, final Wildcard attributeWildcard, final Deque<Assertion> asserts) {
			this.annotations = Objects.requireNonNull(annotations);
			this.base = Objects.requireNonNull(base);
			this.facets = Objects.requireNonNull(facets);
			this.wildcard = Objects.requireNonNull(wildcard);
			this.simpleType = simpleType;
			this.attributeUses = Objects.requireNonNull(attributeUses);
			this.attributeWildcard = attributeWildcard;
			this.asserts = Objects.requireNonNull(asserts);
		}

		protected static SimpleContentRestriction parse(final Result result) {
			final QName baseType = result.value(AttributeValue.BASE);
			final Deferred<ComplexType> base = result.schema().find(baseType, ComplexType.class);
			final SimpleType simpleType = result.parse(ElementValue.SIMPLETYPE);
			final Deque<ConstrainingFacet<?>> facets = result.parseAll(ElementValue.MINEXCLUSIVE, ElementValue.MININCLUSIVE, ElementValue.MAXEXCLUSIVE, ElementValue.MAXINCLUSIVE, ElementValue.TOTALDIGITS, ElementValue.FRACTIONDIGITS, ElementValue.LENGTH, ElementValue.MINLENGTH, ElementValue.MAXLENGTH, ElementValue.ENUMERATION, ElementValue.WHITESPACE, ElementValue.PATTERN, ElementValue.ASSERTION);
			final Deque<Particle<Wildcard>> wildcard = result.parseAll(ElementValue.ANY);
			final Deque<AttributeGroup> attributeGroups = result.parseAll(ElementValue.ATTRIBUTEGROUP);
			final Deferred<Deque<AttributeUse>> attributeUses = AttributeGroup.findAttributeUses(result.parseAll(ElementValue.ATTRIBUTE), attributeGroups);
			final Deque<Annotation> annotations = new AnnotationsBuilder(result).add(attributeGroups).build();
			final Wildcard attributeWildcard = result.parse(ElementValue.ANYATTRIBUTE);
			final Deque<Assertion> asserts = result.parseAll(ElementValue.ASSERT);
			return new SimpleContentRestriction(annotations, base, facets, wildcard, simpleType, attributeUses, attributeWildcard, asserts);
		}

		protected Deque<Annotation> annotations() {
			return annotations;
		}

		protected Deferred<ComplexType> base() {
			return base;
		}

		protected Deque<ConstrainingFacet<?>> facets() {
			return facets;
		}

		protected Deque<Particle<Wildcard>> wildcard() {
			return wildcard;
		}

		protected SimpleType simpleType() {
			return simpleType;
		}

		protected Deferred<Deque<AttributeUse>> attributeUses() {
			return attributeUses;
		}

		protected Wildcard attributeWildcard() {
			return attributeWildcard;
		}

		protected Deque<Assertion> asserts() {
			return asserts;
		}

	}

	protected static final SequenceParser parser = new SequenceParser()
			.optionalAttributes(AttributeValue.ID)
			.elements(0, 1, ElementValue.ANNOTATION)
			.elements(1, 1, ElementValue.SIMPLECONTENT_RESTRICTION, ElementValue.SIMPLE_EXTENSION);

	private final Deque<Annotation> annotations;
	private final DerivationMethod derivationMethod;
	private final SimpleContentRestriction restriction;
	private final SimpleExtension extension;

	private SimpleContent(final Deque<Annotation> annotations, final DerivationMethod derivationMethod, final SimpleContentRestriction restriction, final SimpleExtension extension) {
		this.annotations = Objects.requireNonNull(annotations);
		this.derivationMethod = derivationMethod;
		this.restriction = restriction;
		this.extension = extension;
	}

	protected static SimpleContent parse(final Result result) {
		final SimpleContentRestriction restriction = result.parse(ElementValue.SIMPLECONTENT_RESTRICTION);
		final SimpleExtension extension = result.parse(ElementValue.SIMPLE_EXTENSION);
		final DerivationMethod derivationMethod;
		final Deque<Annotation> annotations;
		if (restriction != null) {
			derivationMethod = DerivationMethod.RESTRICTION;
			annotations = new AnnotationsBuilder(result).add(restriction::annotations).build();
		} else {
			derivationMethod = DerivationMethod.EXTENSION;
			annotations = new AnnotationsBuilder(result).add(extension::annotations).build();
		}
		return new SimpleContent(annotations, derivationMethod, restriction, extension);
	}

	protected Deque<Annotation> annotations() {
		return annotations;
	}

	protected DerivationMethod derivationMethod() {
		return derivationMethod;
	}

	protected TypeDefinition base() {
		return DerivationMethod.RESTRICTION.equals(derivationMethod) ? restriction().base().get() : extension().base();
	}

	protected Deferred<Deque<AttributeUse>> attributeUses() {
		return DerivationMethod.RESTRICTION.equals(derivationMethod) ? restriction().attributeUses() : extension().attributeUses();
	}

	protected Wildcard attributeWildcard() {
		return DerivationMethod.RESTRICTION.equals(derivationMethod) ? restriction().attributeWildcard() : extension().attributeWildcard();
	}

	protected Deque<Assertion> asserts() {
		return DerivationMethod.RESTRICTION.equals(derivationMethod) ? restriction().asserts() : extension().asserts();
	}

	protected SimpleContentRestriction restriction() {
		return restriction;
	}

	protected SimpleExtension extension() {
		return extension;
	}

}