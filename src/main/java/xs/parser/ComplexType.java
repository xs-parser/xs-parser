package xs.parser;

import java.util.*;
import java.util.function.*;
import javax.xml.*;
import javax.xml.namespace.*;
import org.w3c.dom.*;
import xs.parser.Annotation.*;
import xs.parser.ModelGroup.*;
import xs.parser.Schema.*;
import xs.parser.internal.*;
import xs.parser.internal.util.*;
import xs.parser.internal.util.SequenceParser.*;

/**
 * <pre>
 * &lt;complexType
 *   abstract = boolean : false
 *   block = (#all | List of (extension | restriction))
 *   final = (#all | List of (extension | restriction))
 *   id = ID
 *   mixed = boolean
 *   name = NCName
 *   defaultAttributesApply = boolean : true
 *   {any attributes with non-schema namespace . . .}&gt;
 *   Content: (annotation?, (simpleContent | complexContent | (openContent?, (group | all | choice | sequence)?, ((attribute | attributeGroup)*, anyAttribute?), assert*)))
 * &lt;/complexType&gt;
 * </pre>
 *
 * <table>
 *   <caption style="font-size: large; text-align: left">Schema Component: Complex Type Definition, a kind of Type Definition</caption>
 *   <thead>
 *     <tr>
 *       <th style="text-align: left">Method</th>
 *       <th style="text-align: left">Property</th>
 *       <th style="text-align: left">Representation</th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <td>{@link ComplexType#annotations()}</td>
 *       <td>{annotations}</td>
 *       <td>A sequence of Annotation components.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link ComplexType#name()}</td>
 *       <td>{name}</td>
 *       <td>An xs:NCName value. Optional.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link ComplexType#targetNamespace()}</td>
 *       <td>{target namespace}</td>
 *       <td>An xs:anyURI value. Optional.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link ComplexType#baseType()}</td>
 *       <td>{base type definition}</td>
 *       <td>A type definition component. Required.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link ComplexType#finals()}</td>
 *       <td>{final}</td>
 *       <td>A subset of {extension, restriction}.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link ComplexType#context()}</td>
 *       <td>{context}</td>
 *       <td>Required if {name} is ·absent·, otherwise must be ·absent·. Either an Element Declaration or a Complex Type Definition.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link ComplexType#derivationMethod()}</td>
 *       <td>{derivation method}</td>
 *       <td>One of {extension, restriction}. Required.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link ComplexType#isAbstract()}</td>
 *       <td>{abstract}</td>
 *       <td>An xs:boolean value. Required.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link ComplexType#attributeUses()}</td>
 *       <td>{attribute uses}</td>
 *       <td>A set of Attribute Use components.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link ComplexType#attributeWildcard()}</td>
 *       <td>{attribute wildcard}</td>
 *       <td>A Wildcard component. Optional.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link ComplexType#contentType()}</td>
 *       <td>{content type}</td>
 *       <td>A Content Type property record. Required.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link ComplexType#prohibitedSubstitutions()}</td>
 *       <td>{prohibited substitutions}</td>
 *       <td>A subset of {extension, restriction}.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link ComplexType#assertions()}</td>
 *       <td>{assertions}</td>
 *       <td>A sequence of Assertion components.</td>
 *     </tr>
 *   </tbody>
 * </table>
 */
public class ComplexType implements TypeDefinition {

	public enum DerivationMethod {

		EXTENSION,
		RESTRICTION;

	}

	public enum Variety {

		EMPTY,
		SIMPLE,
		ELEMENT_ONLY,
		MIXED;

		private boolean isMixedOrElementOnly() {
			return EMPTY.equals(this) || ELEMENT_ONLY.equals(this);
		}

	}

	/**
	 * &lt;assert
	 *   id = ID
	 *   test = an XPath expression
	 *   xpathDefaultNamespace = (anyURI | (##defaultNamespace | ##targetNamespace | ##local))
	 *   {any attributes with non-schema namespace . . .}&gt;
	 *   Content: (annotation?)
	 * &lt;/assert&gt;
	 */
	public static class Assert extends Assertion {

		static final SequenceParser parser = new SequenceParser()
				.requiredAttributes(AttributeValue.TEST)
				.optionalAttributes(AttributeValue.ID, AttributeValue.XPATHDEFAULTNAMESPACE)
				.elements(0, 1, ElementValue.ANNOTATION);

		Assert(final Node node, final Deque<Annotation> annotations, final XPathExpression test) {
			super(node, annotations, test);
		}

		static Assert parse(final Result result) {
			final XPathExpression test = XPathExpression.parse(result);
			return new Assert(result.node(), result.annotations(), test);
		}

	}

	/**
	 * <pre>
	 * &lt;complexContent
	 *   id = ID
	 *   mixed = boolean
	 *   {any attributes with non-schema namespace . . .}&gt;
	 *   Content: (annotation?, (restriction | extension))
	 * &lt;/complexContent&gt;
	 * </pre>
	 */
	public static class ComplexContent {

		/**
		 * <pre>
		 * &lt;restriction
		 *   base = QName
		 *   id = ID
		 *   {any attributes with non-schema namespace . . .}&gt;
		 *   Content: (annotation?, openContent?, (group | all | choice | sequence)?, ((attribute | attributeGroup)*, anyAttribute?), assert*)
		 * &lt;/restriction&gt;
		 *
		 * &lt;extension
		 *   base = QName
		 *   id = ID
		 *   {any attributes with non-schema namespace . . .}&gt;
		 *   Content: (annotation?, openContent?, ((group | all | choice | sequence)?, ((attribute | attributeGroup)*, anyAttribute?), assert*))
		 * &lt;/extension&gt;
		 * </pre>
		 */
		public static class Derivation {

			static final SequenceParser parser = new SequenceParser()
					.requiredAttributes(AttributeValue.BASE)
					.optionalAttributes(AttributeValue.ID)
					.elements(0, 1, ElementValue.ANNOTATION)
					.elements(0, 1, ElementValue.OPENCONTENT)
					.elements(0, 1, ElementValue.GROUP, ElementValue.ALL, ElementValue.CHOICE, ElementValue.SEQUENCE)
					.elements(0, Integer.MAX_VALUE, ElementValue.ATTRIBUTE, ElementValue.ATTRIBUTEGROUP)
					.elements(0, 1, ElementValue.ANYATTRIBUTE)
					.elements(0, Integer.MAX_VALUE, ElementValue.ASSERT);

			private final Deque<Annotation> annotations;
			private final Deferred<ComplexType> base;
			private final OpenContent openContent;
			private final Particle group;
			private final Particle all;
			private final Particle choice;
			private final Particle sequence;
			private final Deferred<Deque<AttributeUse>> attributeUses;
			private final Wildcard attributeWildcard;
			private final Deque<Assertion> asserts;

			Derivation(final Deque<Annotation> annotations, final Deferred<ComplexType> base, final Particle group, final Particle all, final Particle 	choice, final Particle sequence, final OpenContent openContent, final Deferred<Deque<AttributeUse>> attributeUses, final Wildcard attributeWildcard, final 	Deque<Assertion> asserts) {
				this.annotations = Objects.requireNonNull(annotations);
				this.base = Objects.requireNonNull(base);
				this.group = group;
				this.all = all;
				this.choice = choice;
				this.sequence = sequence;
				this.openContent = openContent;
				this.attributeUses = Objects.requireNonNull(attributeUses);
				this.attributeWildcard = attributeWildcard;
				this.asserts = Objects.requireNonNull(asserts);
			}

			static Derivation parse(final Result result) {
				final QName baseType = result.value(AttributeValue.BASE);
				final Deferred<ComplexType> base = result.schema().find(baseType, ComplexType.class);
				final Particle group = result.parse(ElementValue.GROUP);
				final Particle all = result.parse(ElementValue.ALL);
				final Particle choice = result.parse(ElementValue.CHOICE);
				final Particle sequence = result.parse(ElementValue.SEQUENCE);
				final OpenContent openContent = result.parse(ElementValue.OPENCONTENT);
				final Deque<AttributeGroup> attributeGroups = result.parseAll(ElementValue.ATTRIBUTEGROUP);
				final Deferred<Deque<AttributeUse>> attributeUses = AttributeGroup.findAttributeUses(result.parseAll(ElementValue.ATTRIBUTE), attributeGroups);
				final AnnotationsBuilder annotations = new AnnotationsBuilder(result).add(attributeGroups);
				if (openContent != null) {
					annotations.add(openContent::annotations);
				}
				final Wildcard attributeWildcard = result.parse(ElementValue.ANYATTRIBUTE);
				final Deque<Assertion> asserts = result.parseAll(ElementValue.ASSERT);
				return new Derivation(annotations.build(), base, group, all, choice, sequence, openContent, attributeUses, attributeWildcard, asserts);
			}

			Deque<Annotation> annotations() {
				return annotations;
			}

			ComplexType base() {
				return base.get();
			}

			OpenContent openContent() {
				return openContent;
			}

			Particle group() {
				return group;
			}

			Particle all() {
				return all;
			}

			Particle choice() {
				return choice;
			}

			Particle sequence() {
				return sequence;
			}

			Deferred<Deque<AttributeUse>> attributeUses() {
				return attributeUses;
			}

			Wildcard attributeWildcard() {
				return attributeWildcard;
			}

			Deque<Assertion> asserts() {
				return asserts;
			}

		}

		static final SequenceParser parser = new SequenceParser()
				.optionalAttributes(AttributeValue.ID, AttributeValue.MIXED)
				.elements(0, 1, ElementValue.ANNOTATION)
				.elements(1, 1, ElementValue.COMPLEXCONTENT_RESTRICTION, ElementValue.COMPLEXCONTENT_EXTENSION);

		private final Deque<Annotation> annotations;
		private final Boolean mixed;
		private final DerivationMethod derivationMethod;
		private final Derivation derivation;

		ComplexContent(final Deque<Annotation> annotations, final Boolean mixed, final DerivationMethod derivationMethod, final Derivation derivation) {
			this.annotations = Objects.requireNonNull(annotations);
			this.mixed = mixed;
			this.derivationMethod = derivationMethod;
			this.derivation = derivation;
		}

		static ComplexContent parse(final Result result) {
			final Boolean mixed = result.value(AttributeValue.MIXED);
			final Derivation restriction = result.parse(ElementValue.COMPLEXCONTENT_RESTRICTION);
			final Derivation extension = result.parse(ElementValue.COMPLEXCONTENT_EXTENSION);
			final DerivationMethod derivationMethod;
			final Deque<Annotation> annotations;
			if (restriction != null) {
				derivationMethod = DerivationMethod.RESTRICTION;
				annotations = new AnnotationsBuilder(result).add(restriction::annotations).build();
			} else {
				derivationMethod = DerivationMethod.EXTENSION;
				annotations = new AnnotationsBuilder(result).add(extension::annotations).build();
			}
			return new ComplexContent(annotations, mixed, derivationMethod, restriction != null ? restriction : extension);
		}

		Deque<Annotation> annotations() {
			return annotations;
		}

		Derivation derivation() {
			return derivation;
		}

		Boolean mixed() {
			return mixed;
		}

		DerivationMethod derivationMethod() {
			return derivationMethod;
		}

	}

	/**
	 * <table>
	 *   <caption style="font-size: large; text-align: left">Property Record: Content Type</caption>
	 *   <thead>
	 *     <tr>
	 *       <th style="text-align: left">Method</th>
	 *       <th style="text-align: left">Property</th>
	 *       <th style="text-align: left">Representation</th>
	 *     </tr>
	 *   </thead>
	 *   <tbody>
	 *     <tr>
	 *       <td>{@link ContentType#variety()}</td>
	 *       <td>{variety}</td>
	 *       <td>One of {empty, simple, element-only, mixed}. Required.</td>
	 *     </tr>
	 *     <tr>
	 *       <td>{@link ContentType#particle()}</td>
	 *       <td>{particle}</td>
	 *       <td>A Particle component. Required if {variety} is element-only or mixed, otherwise must be ·absent·.</td>
	 *     </tr>
	 *     <tr>
	 *       <td>{@link ContentType#openContent()}</td>
	 *       <td>{open content}</td>
	 *       <td>An Open Content property record. Optional if {variety} is element-only or mixed, otherwise must be ·absent·.</td>
	 *     </tr>
	 *     <tr>
	 *       <td>{@link ContentType#simpleType()}</td>
	 *       <td>{simple type definition}</td>
	 *       <td>A Simple Type Definition component. Required if {variety} is simple, otherwise must be ·absent·.</td>
	 *     </tr>
	 *   </tbody>
	 * </table>
	 */
	public static class ContentType {

		private final Variety variety;
		private final Particle particle;
		private final OpenContent openContent;
		private final SimpleType simpleType;

		private ContentType(final DefaultOpenContent defaultOpenContent, final Variety variety, final Particle particle, final OpenContent openContent, final SimpleType simpleType) {
			this.variety = Objects.requireNonNull(variety);
			this.particle = particle;
			if (openContent != null) {
				this.openContent = openContent;
			} else if (!Variety.EMPTY.equals(variety) || (defaultOpenContent != null && defaultOpenContent.appliesToEmpty())) {
				this.openContent = defaultOpenContent;
			} else {
				this.openContent = null;
			}
			this.simpleType = simpleType;
		}

		public Variety variety() {
			return variety;
		}

		public Particle particle() {
			return particle;
		}

		public OpenContent openContent() {
			return openContent;
		}

		public SimpleType simpleType() {
			return simpleType;
		}

	}

	/**
	 * <pre>
	 * &lt;openContent
	 *   id = ID
	 *   mode = (none | interleave | suffix) : interleave
	 *   {any attributes with non-schema namespace . . .}&gt;
	 *   Content: (annotation?, any?)
	 * &lt;/openContent&gt;
	 * </pre>
	 *
	 * <table>
	 *   <caption style="font-size: large; text-align: left">Property Record: Open Content</caption>
	 *   <thead>
	 *     <tr>
	 *       <th style="text-align: left">Method</th>
	 *       <th style="text-align: left">Property</th>
	 *       <th style="text-align: left">Representation</th>
	 *     </tr>
	 *   </thead>
	 *   <tbody>
	 *     <tr>
	 *       <td>{@link OpenContent#mode()}</td>
	 *       <td>{mode}</td>
	 *       <td>One of {interleave, suffix}. Required.</td>
	 *     </tr>
	 *     <tr>
	 *       <td>{@link OpenContent#wildcard()}</td>
	 *       <td>{wildcard}</td>
	 *       <td>A Wildcard component. Required.</td>
	 *     </tr>
	 *   </tbody>
	 * </table>
	 */
	public static class OpenContent {

		public enum Mode {

			NONE("none"),
			INTERLEAVE("interleave"),
			SUFFIX("suffix");

			private final String name;

			private Mode(final String value) {
				this.name = value;
			}

			public static Mode getByName(final Node node) {
				final String name = node.getNodeValue();
				for (final Mode m : values()) {
					if (m.getName().equals(name)) {
						return m;
					}
				}
				throw new IllegalArgumentException(name);
			}

			public String getName() {
				return name;
			}

			@Override
			public String toString() {
				return getName();
			}

		}

		static final SequenceParser parser = new SequenceParser()
				.optionalAttributes(AttributeValue.ID, AttributeValue.MODE)
				.elements(0, 1, ElementValue.ANNOTATION)
				.elements(0, 1, ElementValue.ANY);

		private final Deque<Annotation> annotations;
		private final Mode mode;
		private final Particle wildcard;

		OpenContent(final Deque<Annotation> annotations, final Mode mode, final Particle wildcard) {
			this.annotations = Objects.requireNonNull(annotations);
			this.mode = mode;
			this.wildcard = wildcard;
		}

		static OpenContent parse(final Result result) {
			final Mode mode = result.value(AttributeValue.MODE);
			final Particle wildcard = result.parse(ElementValue.ANY);
			return new OpenContent(result.annotations(), mode, wildcard);
		}

		private Deque<Annotation> annotations() {
			return annotations;
		}

		/** @return The ·actual value· of the mode [attribute] of the ·wildcard element·, if present, otherwise interleave. */
		public Mode mode() {
			return mode;
		}

		/** @return Let W be the wildcard corresponding to the &lt;any&gt; [child] of the ·wildcard element·. If the {open content} of the ·explicit content type· is ·absent·, then W; otherwise a wildcard whose {process contents} and {annotations} are those of W, and whose {namespace constraint} is the wildcard union of the {namespace constraint} of W and of {open content}.{wildcard} of the ·explicit content type·, as defined in Attribute Wildcard Union (§3.10.6.3). */
		public Particle wildcard() {
			return wildcard;
		}

	}

	/**
	 * <pre>
	 * &lt;simpleContent
	 *   id = ID
	 *   {any attributes with non-schema namespace . . .}&gt;
	 *   Content: (annotation?, (restriction | extension))
	 * &lt;/simpleContent&gt;
	 * </pre>
	 */
	public static class SimpleContent {

		/**
		 * <pre>
		 * &lt;extension
		 *   base = QName
		 *   id = ID
		 *   {any attributes with non-schema namespace . . .}&gt;
		 *   Content: (annotation?, ((attribute | attributeGroup)*, anyAttribute?), assert*)
		 * &lt;/extension&gt;
		 * </pre>
		 */
		public static class Extension {

			static final SequenceParser parser = new SequenceParser()
					.requiredAttributes(AttributeValue.BASE)
					.optionalAttributes(AttributeValue.ID)
					.elements(0, 1, ElementValue.ANNOTATION)
					.elements(0, Integer.MAX_VALUE, ElementValue.ATTRIBUTE, ElementValue.ATTRIBUTEGROUP)
					.elements(0, 1, ElementValue.ANYATTRIBUTE)
					.elements(0, Integer.MAX_VALUE, ElementValue.ASSERT);

			private final Deque<Annotation> annotations;
			private final Deferred<? extends TypeDefinition> base;
			private final Deferred<Deque<AttributeUse>> attributeUses;
			private final Wildcard attributeWildcard;
			private final Deque<Assertion> asserts;

			Extension(final Deque<Annotation> annotations, final Deferred<? extends TypeDefinition> base, final Deferred<Deque<AttributeUse>> attributeUses, final Wildcard attributeWildcard, final Deque<Assertion> asserts) {
				this.annotations = Objects.requireNonNull(annotations);
				this.base = Objects.requireNonNull(base);
				this.attributeUses = Objects.requireNonNull(attributeUses);
				this.attributeWildcard = attributeWildcard;
				this.asserts = Objects.requireNonNull(asserts);
			}

			static Extension parse(final Result result) {
				final QName baseType = result.value(AttributeValue.BASE);
				final Deferred<? extends TypeDefinition> base = result.schema().find(baseType, TypeDefinition.class);
				final Deque<AttributeGroup> attributeGroups = result.parseAll(ElementValue.ATTRIBUTEGROUP);
				final Deferred<Deque<AttributeUse>> attributeUses = AttributeGroup.findAttributeUses(result.parseAll(ElementValue.ATTRIBUTE), attributeGroups);
				final Deque<Annotation> annotations = new AnnotationsBuilder(result).add(attributeGroups).build();
				final Wildcard attributeWildcard = result.parse(ElementValue.ANYATTRIBUTE);
				final Deque<Assertion> asserts = result.parseAll(ElementValue.ASSERT);
				return new Extension(annotations, base, attributeUses, attributeWildcard, asserts);
			}

			Deque<Annotation> annotations() {
				return annotations;
			}

			TypeDefinition base() {
				return base.get();
			}

			Deferred<Deque<AttributeUse>> attributeUses() {
				return attributeUses;
			}

			Wildcard attributeWildcard() {
				return attributeWildcard;
			}

			Deque<Assertion> asserts() {
				return asserts;
			}

		}

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
		public static class Restriction {

			static final SequenceParser parser = new SequenceParser()
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
			private final Deque<ConstrainingFacet> facets;
			private final Deque<Particle> wildcard;
			private final SimpleType simpleType;
			private final Deferred<Deque<AttributeUse>> attributeUses;
			private final Wildcard attributeWildcard;
			private final Deque<Assertion> asserts;

			private Restriction(final Deque<Annotation> annotations, final Deferred<ComplexType> base, final Deque<ConstrainingFacet> facets, final Deque<Particle> wildcard, final SimpleType simpleType, final Deferred<Deque<AttributeUse>> attributeUses, final Wildcard attributeWildcard, final Deque<Assertion> asserts) {
				this.annotations = Objects.requireNonNull(annotations);
				this.base = Objects.requireNonNull(base);
				this.facets = Objects.requireNonNull(facets);
				this.wildcard = Objects.requireNonNull(wildcard);
				this.simpleType = simpleType;
				this.attributeUses = Objects.requireNonNull(attributeUses);
				this.attributeWildcard = attributeWildcard;
				this.asserts = Objects.requireNonNull(asserts);
			}

			static Restriction parse(final Result result) {
				final QName baseType = result.value(AttributeValue.BASE);
				final Deferred<ComplexType> base = result.schema().find(baseType, ComplexType.class);
				final SimpleType simpleType = result.parse(ElementValue.SIMPLETYPE);
				final Deque<ConstrainingFacet> facets = result.parseAll(ElementValue.MINEXCLUSIVE, ElementValue.MININCLUSIVE, ElementValue.MAXEXCLUSIVE, ElementValue.MAXINCLUSIVE, ElementValue.TOTALDIGITS, ElementValue.FRACTIONDIGITS, ElementValue.LENGTH, ElementValue.MINLENGTH, ElementValue.MAXLENGTH, ElementValue.ENUMERATION, ElementValue.WHITESPACE, ElementValue.PATTERN, ElementValue.ASSERTION);
				final Deque<Particle> wildcard = result.parseAll(ElementValue.ANY);
				final Deque<AttributeGroup> attributeGroups = result.parseAll(ElementValue.ATTRIBUTEGROUP);
				final Deferred<Deque<AttributeUse>> attributeUses = AttributeGroup.findAttributeUses(result.parseAll(ElementValue.ATTRIBUTE), attributeGroups);
				final Deque<Annotation> annotations = new AnnotationsBuilder(result).add(attributeGroups).build();
				final Wildcard attributeWildcard = result.parse(ElementValue.ANYATTRIBUTE);
				final Deque<Assertion> asserts = result.parseAll(ElementValue.ASSERT);
				return new Restriction(annotations, base, facets, wildcard, simpleType, attributeUses, attributeWildcard, asserts);
			}

			Deque<Annotation> annotations() {
				return annotations;
			}

			Deferred<ComplexType> base() {
				return base;
			}

			Deque<ConstrainingFacet> facets() {
				return facets;
			}

			// TODO: wildcard unused
			Deque<Particle> wildcard() {
				return wildcard;
			}

			SimpleType simpleType() {
				return simpleType;
			}

			Deferred<Deque<AttributeUse>> attributeUses() {
				return attributeUses;
			}

			Wildcard attributeWildcard() {
				return attributeWildcard;
			}

			Deque<Assertion> asserts() {
				return asserts;
			}

		}

		static final SequenceParser parser = new SequenceParser()
				.optionalAttributes(AttributeValue.ID)
				.elements(0, 1, ElementValue.ANNOTATION)
				.elements(1, 1, ElementValue.SIMPLECONTENT_RESTRICTION, ElementValue.SIMPLECONTENT_EXTENSION);

		private final Deque<Annotation> annotations;
		private final DerivationMethod derivationMethod;
		private final Restriction restriction;
		private final Extension extension;

		private SimpleContent(final Deque<Annotation> annotations, final DerivationMethod derivationMethod, final Restriction restriction, final Extension extension) {
			this.annotations = Objects.requireNonNull(annotations);
			this.derivationMethod = derivationMethod;
			this.restriction = restriction;
			this.extension = extension;
		}

		static SimpleContent parse(final Result result) {
			final Restriction restriction = result.parse(ElementValue.SIMPLECONTENT_RESTRICTION);
			final Extension extension = result.parse(ElementValue.SIMPLECONTENT_EXTENSION);
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

		Deque<Annotation> annotations() {
			return annotations;
		}

		DerivationMethod derivationMethod() {
			return derivationMethod;
		}

		TypeDefinition base() {
			return DerivationMethod.RESTRICTION.equals(derivationMethod) ? restriction().base().get() : extension().base();
		}

		Deferred<Deque<AttributeUse>> attributeUses() {
			return DerivationMethod.RESTRICTION.equals(derivationMethod) ? restriction().attributeUses() : extension().attributeUses();
		}

		Wildcard attributeWildcard() {
			return DerivationMethod.RESTRICTION.equals(derivationMethod) ? restriction().attributeWildcard() : extension().attributeWildcard();
		}

		Deque<Assertion> asserts() {
			return DerivationMethod.RESTRICTION.equals(derivationMethod) ? restriction().asserts() : extension().asserts();
		}

		Restriction restriction() {
			return restriction;
		}

		Extension extension() {
			return extension;
		}

	}

	private static final ComplexType xsAnyType;
	static final String ANYTYPE_NAME = "anyType";
	static final SequenceParser parser = new SequenceParser()
			.optionalAttributes(AttributeValue.ID, AttributeValue.ABSTRACT, AttributeValue.BLOCK, AttributeValue.FINAL, AttributeValue.MIXED, AttributeValue.NAME, AttributeValue.DEFAULTATTRIBUTESAPPLY)
			.elements(0, 1, ElementValue.ANNOTATION)
			.elements(0, 1, ElementValue.COMPLEXCONTENT, ElementValue.SIMPLECONTENT, ElementValue.OPENCONTENT)
			.elements(0, 1, ElementValue.GROUP, ElementValue.ALL, ElementValue.CHOICE, ElementValue.SEQUENCE)
			.elements(0, Integer.MAX_VALUE, ElementValue.ATTRIBUTE, ElementValue.ATTRIBUTEGROUP)
			.elements(0, 1, ElementValue.ANYATTRIBUTE)
			.elements(0, Integer.MAX_VALUE, ElementValue.ASSERT);

	static {
		final Document xsAnyTypeDoc = NodeHelper.newDocument();
		final ContentType xsAnyTypeContentType = new ContentType(null, Variety.EMPTY, null, null, null);
		final Node xsAnyTypeNode = NodeHelper.newNode(xsAnyTypeDoc, ElementValue.COMPLEXTYPE, "xs", XMLConstants.W3C_XML_SCHEMA_NS_URI, ANYTYPE_NAME);
		xsAnyType = new ComplexType(xsAnyTypeNode, Deques.emptyDeque(), ANYTYPE_NAME, XMLConstants.W3C_XML_SCHEMA_NS_URI, Deques.emptyDeque(), Deferred.none(), Deques::emptyDeque, null, null, false, DerivationMethod.RESTRICTION, () -> xsAnyTypeContentType, Deques.emptyDeque(), Deques.emptyDeque()) {

			@Override
			public TypeDefinition baseType() {
				return this;
			}

		};
	}

	private final Node node;
	private final Deque<Annotation> annotations;
	private final String name;
	private final String targetNamespace;
	private final Deque<Final> finals;
	private final Deferred<? extends TypeDefinition> baseType;
	private final Deferred<Deque<AttributeUse>> attributeUses;
	private final Wildcard attributeWildcard;
	private final Node context;
	private final boolean isAbstract;
	private final DerivationMethod derivationMethod;
	private final Deferred<ContentType> contentType;
	private final Deque<Block> prohibitedSubstitutions;
	private final Deque<Assertion> assertions;

	private ComplexType(final Node node, final Deque<Annotation> annotations, final String name, final String targetNamespace, final Deque<Final> finals, final Deferred<? extends TypeDefinition> baseType, final Deferred<Deque<AttributeUse>> attributeUses, final Wildcard attributeWildcard, final Node context, final boolean isAbstract, final DerivationMethod derivationMethod, final Deferred<ContentType> contentType, final Deque<Block> prohibitedSubstitutions, final Deque<Assertion> assertions) {
		this.node = Objects.requireNonNull(node);
		this.annotations = Objects.requireNonNull(annotations);
		this.name = name;
		this.targetNamespace = NodeHelper.validateTargetNamespace(targetNamespace);
		this.finals = Objects.requireNonNull(finals);
		this.baseType = Objects.requireNonNull(baseType);
		this.attributeUses = Objects.requireNonNull(attributeUses);
		this.attributeWildcard = attributeWildcard;
		this.context = context;
		this.isAbstract = isAbstract;
		this.derivationMethod = derivationMethod;
		this.contentType = Objects.requireNonNull(contentType);
		this.prohibitedSubstitutions = Objects.requireNonNull(prohibitedSubstitutions);
		this.assertions = Objects.requireNonNull(assertions);
	}

	static ComplexType parse(final Result result) {
		final boolean isAbstract = result.value(AttributeValue.ABSTRACT);
		Deque<Block> block = result.value(AttributeValue.BLOCK);
		if (block.isEmpty()) {
			block = Deques.singletonDeque(result.schema().blockDefault());
		}
		Deque<Final> finals = result.value(AttributeValue.FINAL);
		if (finals.isEmpty()) {
			finals = Deques.singletonDeque(result.schema().finalDefault());
		}
		final Boolean mixed = result.value(AttributeValue.MIXED);
		final String name = result.value(AttributeValue.NAME);
		final boolean defaultAttributesApply = result.value(AttributeValue.DEFAULTATTRIBUTESAPPLY);
		final String targetNamespace = result.schema().targetNamespace();
		final SimpleContent simpleContent = result.parse(ElementValue.SIMPLECONTENT);
		final ComplexContent complexContent = result.parse(ElementValue.COMPLEXCONTENT);
		final boolean effectiveMixed = complexContent != null && complexContent.mixed() != null
				? complexContent.mixed()
				: mixed != null && mixed;
		final AnnotationsBuilder annotations = new AnnotationsBuilder(result);
		final Deferred<? extends TypeDefinition> baseType;
		final Deque<Assertion> assertions;
		Deferred<Deque<AttributeUse>> attributeUses;
		final Wildcard attributeWildcard;
		final DerivationMethod derivationMethod;
		final Deferred<ContentType> explicitContentType;
		if (simpleContent != null) {
			// https://www.w3.org/TR/xmlschema11-1/#dcl.ctd.ctsc
			annotations.add(simpleContent::annotations);
			assertions = simpleContent.asserts();
			final Deferred<SimpleType> simpleBase = Deferred.of(() -> {
				final TypeDefinition type = simpleContent.base();
				if (type instanceof ComplexType) {
					final ComplexType complexType = (ComplexType) type;
					if (complexType.contentType().variety() == Variety.SIMPLE && DerivationMethod.RESTRICTION.equals(simpleContent.derivationMethod())) { // 1
						if (simpleContent.restriction().simpleType() != null) { // 1.1
							return simpleContent.restriction().simpleType();
						} else { // 1.2
							return SimpleType.wrap(result.node(), Deques.emptyDeque(), null, result.schema().targetNamespace(), Deques.emptyDeque(), complexType.node(), complexType.contentType().simpleType(), simpleContent.restriction().facets());
						}
					} else if (complexType.contentType().variety() == Variety.MIXED && DerivationMethod.RESTRICTION.equals(simpleContent.derivationMethod()) && complexType.contentType().particle().isEmptiable()) { // 2
						final SimpleType sb = simpleContent.restriction().simpleType() != null ? simpleContent.restriction().simpleType() : SimpleType.xsAnySimpleType();
						return SimpleType.wrap(result.node(), Deques.emptyDeque(), null, result.schema().targetNamespace(), Deques.emptyDeque(), complexType.node(), sb, sb.facets());
					} else if (complexType.contentType().variety() == Variety.SIMPLE && DerivationMethod.EXTENSION.equals(simpleContent.derivationMethod())) { // 3
						return complexType.contentType().simpleType();
					}
				} else if (DerivationMethod.EXTENSION.equals(simpleContent.derivationMethod()) && type instanceof SimpleType) { // 4
					return (SimpleType) type;
				}
				return SimpleType.xsAnySimpleType(); // 5
			});
			baseType = simpleBase;
			attributeUses = simpleContent.attributeUses();
			attributeWildcard = simpleContent.attributeWildcard();
			derivationMethod = simpleContent.derivationMethod();
			explicitContentType = Deferred.of(() -> new ContentType(result.schema().defaultOpenContent(), Variety.SIMPLE, null, null, simpleBase.get()));
		} else {
			final OpenContent openContent;
			final Particle group;
			final Particle all;
			final Particle choice;
			final Particle sequence;
			if (complexContent != null) {
				annotations.add(complexContent::annotations);
				openContent = complexContent.derivation().openContent();
				group = complexContent.derivation().group();
				all = complexContent.derivation().all();
				choice = complexContent.derivation().choice();
				sequence = complexContent.derivation().sequence();
				assertions = complexContent.derivation().asserts();
				baseType = complexContent.derivation()::base;
				attributeUses = complexContent.derivation().attributeUses();
				attributeWildcard = complexContent.derivation().attributeWildcard();
				derivationMethod = complexContent.derivationMethod();
			} else {
				openContent = result.parse(ElementValue.OPENCONTENT);
				group = result.parse(ElementValue.GROUP);
				all = result.parse(ElementValue.ALL);
				choice = result.parse(ElementValue.CHOICE);
				sequence = result.parse(ElementValue.SEQUENCE);
				assertions = result.parseAll(ElementValue.ASSERT);
				baseType = () -> xsAnyType;
				final Deque<AttributeGroup> attributeGroups = result.parseAll(ElementValue.ATTRIBUTEGROUP);
				annotations.add(attributeGroups);
				attributeUses = AttributeGroup.findAttributeUses(result.parseAll(ElementValue.ATTRIBUTE), attributeGroups);
				attributeWildcard = result.parse(ElementValue.ANYATTRIBUTE);
				derivationMethod = DerivationMethod.RESTRICTION;
			}
			final Particle particle = group != null ? group : all != null ? all : choice != null ? choice : sequence != null ? sequence : null;
			final Particle explicitContent;
			if (particle == null) {
				explicitContent = null;
			} else {
				if ((all != null && ((ModelGroup) all.term()).particles().isEmpty()) || (sequence != null && ((ModelGroup) sequence.term()).particles().isEmpty())) {
					explicitContent = null;
				} else {
					final boolean choiceMinOccurs0 = choice != null && "0".equals(choice.minOccurs()) && (choice.term() == null || ((ModelGroup) choice.term()).particles().isEmpty());
					if (choiceMinOccurs0) {
						explicitContent = null;
					} else {
						final String particleMaxOccurs = group != null ? group.maxOccurs()
								: all != null ? all.maxOccurs()
								: choice != null ? choice.maxOccurs()
								: sequence != null ? sequence.maxOccurs()
								: null;
						explicitContent = "0".equals(particleMaxOccurs) ? null : particle;
					}
				}
			}
			final Particle effectiveContent;
			if (explicitContent == null) {
				if (effectiveMixed) {
					effectiveContent = new Particle(result.node(), result.annotations(), AttributeValue.MAXOCCURS.defaultValue(), AttributeValue.MINOCCURS.defaultValue(), ModelGroup.synthetic(result.node(), result.annotations(), Compositor.SEQUENCE, Deques.emptyDeque()));
				} else {
					effectiveContent = null;
				}
			} else {
				effectiveContent = explicitContent;
			}
			explicitContentType = Deferred.of(() -> {
				final Supplier<ContentType> handleRestriction = () -> {
					if (effectiveContent == null) { // 4.1.1
						return new ContentType(result.schema().defaultOpenContent(), Variety.EMPTY, null, openContent, null);
					} else { // 4.1.2
						return new ContentType(result.schema().defaultOpenContent(), effectiveMixed ? Variety.MIXED : Variety.ELEMENT_ONLY, effectiveContent, openContent, null);
					}
				};
				switch (derivationMethod) {
				case RESTRICTION: // 4.1
					return handleRestriction.get();
				case EXTENSION: // 4.2
					if (baseType.get() instanceof SimpleType) {
						return handleRestriction.get(); // 4.2.1
					} else {
						final ComplexType complexBase = (ComplexType) baseType.get();
						if (effectiveContent == null && complexBase.contentType().variety().isMixedOrElementOnly()) {
							return complexBase.contentType(); // 4.2.2
						} else if (!complexBase.contentType().variety().isMixedOrElementOnly()) {
							return handleRestriction.get(); // 4.2.1
						} else { // 4.2.3
							final Particle baseParticle = complexBase.contentType().particle();
							final Particle effectiveParticle;
							if (baseParticle != null && baseParticle.term() instanceof ModelGroup && Compositor.ALL.equals(((ModelGroup) baseParticle.term()).compositor()) && effectiveContent == null) { // 4.2.3.1
								effectiveParticle = baseParticle;
							} else if (baseParticle != null && baseParticle.term() instanceof ModelGroup && Compositor.ALL.equals(((ModelGroup) baseParticle.term()).compositor()) && effectiveContent != null && effectiveContent.term() instanceof ModelGroup && Compositor.ALL.equals(((ModelGroup) effectiveContent.term()).compositor())) { // 4.2.3.2
								final Deque<Particle> particles = new ArrayDeque<>(((ModelGroup) baseParticle.term()).particles());
								particles.addAll(((ModelGroup) effectiveContent.term()).particles());
								effectiveParticle = new Particle(result.node(), result.annotations(), "1", baseParticle.minOccurs(), ModelGroup.synthetic(result.node(), result.annotations(), Compositor.ALL, particles));
							} else { // 4.2.3.3
								final Deque<Particle> particles = new ArrayDeque<>();
								if (baseParticle != null) {
									particles.add(baseParticle);
								}
								if (effectiveContent != null) {
									particles.add(effectiveContent);
								}
								effectiveParticle = new Particle(result.node(), result.annotations(), "1", "1", ModelGroup.synthetic(result.node(), result.annotations(), Compositor.SEQUENCE, particles));
							}
							return new ContentType(result.schema().defaultOpenContent(), effectiveMixed ? Variety.MIXED : Variety.ELEMENT_ONLY, effectiveParticle, complexBase.contentType().openContent(), null);
						}
					}
				default:
					throw new AssertionError(derivationMethod.toString());
				}
			});
		}
		if (defaultAttributesApply && result.schema().defaultAttributes() != null) {
			attributeUses = attributeUses.map(a -> {
				final DeferredArrayDeque<AttributeUse> ls = new DeferredArrayDeque<>(a.size() + result.schema().defaultAttributes().attributeUses().size());
				ls.addAll(a);
				ls.addAll(result.schema().defaultAttributes().attributeUses());
				return ls;
			});
		}
		final Node context = result.parent() != null ? result.parent().node() : null;
		assert explicitContentType != null : NodeHelper.toString(result.node());
		return new ComplexType(result.node(), annotations.build(), name, targetNamespace, finals, baseType, attributeUses, attributeWildcard, context, isAbstract, derivationMethod, explicitContentType, block, assertions);
	}

	public static ComplexType xsAnyType() {
		return xsAnyType;
	}

	public ContentType contentType() {
		return contentType.get();
	}

	public DerivationMethod derivationMethod() {
		return derivationMethod;
	}

	/** @return The ·actual value· of the abstract [attribute], if present, otherwise false. */
	public boolean isAbstract() {
		return isAbstract;
	}

	public Deque<AttributeUse> attributeUses() {
		return Deques.unmodifiableDeque(attributeUses.get());
	}

	public Wildcard attributeWildcard() {
		return attributeWildcard;
	}

	public Deque<Block> prohibitedSubstitutions() {
		return Deques.unmodifiableDeque(prohibitedSubstitutions);
	}

	public Deque<Assertion> assertions() {
		return assertions;
	}

	@Override
	public Node node() {
		return node;
	}

	@Override
	public Deque<Annotation> annotations() {
		return Deques.unmodifiableDeque(annotations);
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public String targetNamespace() {
		return targetNamespace;
	}

	@Override
	public TypeDefinition baseType() {
		return baseType.get();
	}

	@Override
	public Deque<Final> finals() {
		return Deques.unmodifiableDeque(finals);
	}

	@Override
	public Node context() {
		return context;
	}

}