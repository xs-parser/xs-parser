package xs.parser;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.stream.*;
import javax.xml.*;
import javax.xml.namespace.*;
import org.w3c.dom.*;
import xs.parser.Annotation.*;
import xs.parser.Assertion.*;
import xs.parser.ModelGroup.*;
import xs.parser.Schema.*;
import xs.parser.Wildcard.*;
import xs.parser.internal.*;
import xs.parser.internal.util.*;
import xs.parser.internal.util.SequenceParser.*;
import xs.parser.v.*;

/**
 * A complex type definition is a set of attribute declarations and a content type, applicable to the [attributes] and [children] of an element information item respectively. The content type may require the [children] to contain neither element nor character information items (that is, to be empty), or to be a string which belongs to a particular simple type, or to contain a sequence of element information items which conforms to a particular model group, with or without character information items as well.
 * <p>
 * Each complex type definition other than ·xs:anyType· is either a restriction of a complex ·base type definition· or an ·extension· of a simple or complex ·base type definition·.
 * <p>
 * A complex type which extends another does so by having additional content model particles at the end of the other definition's content model, or by having additional attribute declarations, or both.
 * <p>
 * <i>Note: For the most part, this specification allows only appending, and not other kinds of extensions. This decision simplifies application processing required to cast instances from the derived type to the base type. One special case allows the extension of all-groups in ways that do not guarantee that the new material occurs only at the end of the content. Another special case is extension via Open Contents in interleave mode.</i>
 *
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
 *       <td>{@link ComplexType#baseTypeDefinition()}</td>
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

	private static class Def {

		private final TypeDefinition baseTypeDefinition;
		private final Deque<Assert> asserts;
		private final Deque<AttributeUse> attributeUses;
		private final Deferred<Wildcard> attributeWildcard;
		private final DerivationMethod derivationMethod;
		private final Deferred<ContentType> contentType;

		Def(final TypeDefinition baseTypeDefinition, final Deque<Assert> asserts, final Deque<AttributeUse> attributeUses, final Deferred<Wildcard> attributeWildcard, final DerivationMethod derivationMethod, final Deferred<ContentType> contentType) {
			this.baseTypeDefinition = Objects.requireNonNull(baseTypeDefinition);
			this.asserts = Objects.requireNonNull(asserts);
			this.attributeUses = Objects.requireNonNull(attributeUses);
			this.attributeWildcard = attributeWildcard;
			this.derivationMethod = Objects.requireNonNull(derivationMethod);
			this.contentType = Objects.requireNonNull(contentType);
		}

		private Wildcard attributeWildcard() {
			return attributeWildcard != null ? attributeWildcard.get() : null;
		}

		private ContentType explicitContentType() {
			return contentType.get();
		}

	}

	/** Complex type derivation method */
	public enum DerivationMethod {

		/** Complex type derivation by extension */
		EXTENSION,
		/** Complex type derivation by restriction */
		RESTRICTION;

	}

	/** Complex type content type variety */
	public enum Variety {

		/** Complex type content type variety empty */
		EMPTY,
		/** Complex type content type variety simple */
		SIMPLE,
		/** Complex type content type variety element only */
		ELEMENT_ONLY,
		/** Complex type content type variety simple mixed */
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
	public static class Assert {

		private static final SequenceParser parser = new SequenceParser()
				.requiredAttributes(AttrParser.TEST)
				.optionalAttributes(AttrParser.ID, AttrParser.XPATH_DEFAULT_NAMESPACE)
				.elements(0, 1, TagParser.ANNOTATION);

		private final AnnotationSet annotations;
		private final Assertion assertion;

		Assert(final Deferred<? extends AnnotatedComponent> context, final Node node, final AnnotationSet annotations, final XPathExpression test) {
			this.annotations = Objects.requireNonNull(annotations);
			this.assertion = new Assertion(context, node, annotations.resolve(node), test);
		}

		private static Assert parse(final Result result) {
			final Deferred<? extends AnnotatedComponent> context = result.context();
			final Node node = result.node();
			final AnnotationSet annotations = Annotation.of(result);
			final String expression = result.value(AttrParser.TEST);
			final String xpathDefaultNamespace = result.value(AttrParser.XPATH_DEFAULT_NAMESPACE);
			final XPathExpression test = new XPathExpression(result, xpathDefaultNamespace, expression);
			return new Assert(context, node, annotations, test);
		}

		private AnnotationSet annotations() {
			return annotations;
		}

		private Assertion assertion() {
			return assertion;
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

			private static final SequenceParser parser = new SequenceParser()
					.requiredAttributes(AttrParser.BASE)
					.optionalAttributes(AttrParser.ID)
					.elements(0, 1, TagParser.ANNOTATION)
					.elements(0, 1, TagParser.COMPLEX_TYPE.openContent())
					.elements(0, 1, TagParser.GROUP.use(), TagParser.ALL, TagParser.CHOICE, TagParser.SEQUENCE)
					.elements(0, Integer.MAX_VALUE, TagParser.ATTRIBUTE.use(), TagParser.ATTRIBUTE_GROUP)
					.elements(0, 1, TagParser.ANY_ATTRIBUTE)
					.elements(0, Integer.MAX_VALUE, TagParser.COMPLEX_TYPE.asserts());

			private final AnnotationSet annotations;
			private final Deferred<ComplexType> base;
			private final Deferred<OpenContent> openContent;
			private final Deferred<Particle> group;
			private final Deferred<Particle> all;
			private final Deferred<Particle> choice;
			private final Deferred<Particle> sequence;
			private final Deque<AttributeUse> attributeUses;
			private final Deferred<Wildcard> attributeWildcard;
			private final Deque<Assert> asserts;

			Derivation(final AnnotationSet annotations, final Deferred<ComplexType> base, final Deferred<Particle> group, final Deferred<Particle> all, final Deferred<Particle> choice, final Deferred<Particle> sequence, final Deferred<OpenContent> openContent, final Deque<AttributeUse> attributeUses, final Deferred<Wildcard> attributeWildcard, final Deque<Assert> asserts) {
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

			private static Derivation parse(final Result result) {
				final QName baseType = result.value(AttrParser.BASE);
				final Deferred<ComplexType> baseTypeDefinition = result.schema().find(baseType, ComplexType.class);
				final Deferred<Particle> group = result.parse(TagParser.GROUP.use());
				final Deferred<Particle> all = result.parse(TagParser.ALL);
				final Deferred<Particle> choice = result.parse(TagParser.CHOICE);
				final Deferred<Particle> sequence = result.parse(TagParser.SEQUENCE);
				final Deferred<OpenContent> openContent = result.parse(TagParser.COMPLEX_TYPE.openContent());
				final Deque<AttributeGroup> attributeGroups = result.parseAll(TagParser.ATTRIBUTE_GROUP);
				final Deque<AttributeUse> attributeUses = AttributeGroup.findAttributeUses(result.parseAll(TagParser.ATTRIBUTE.use()), attributeGroups);
				final AnnotationSet annotations = Annotation.of(result).addAll(attributeGroups);
				if (openContent != null) {
					annotations.add(openContent, OpenContent::annotations);
				}
				final Deferred<Wildcard> attributeWildcard = result.parse(TagParser.ANY_ATTRIBUTE);
				final Deque<Assert> asserts = result.parseAll(TagParser.COMPLEX_TYPE.asserts());
				return new Derivation(annotations, baseTypeDefinition, group, all, choice, sequence, openContent, attributeUses, attributeWildcard, asserts);
			}

			private AnnotationSet annotations() {
				return annotations;
			}

			private ComplexType base() {
				return base.get();
			}

			private Deferred<OpenContent> openContent() {
				return openContent;
			}

			private Deferred<Particle> group() {
				return group;
			}

			private Deferred<Particle> all() {
				return all;
			}

			private Deferred<Particle> choice() {
				return choice;
			}

			private Deferred<Particle> sequence() {
				return sequence;
			}

			private Deque<AttributeUse> attributeUses() {
				return attributeUses;
			}

			private Deferred<Wildcard> attributeWildcard() {
				return attributeWildcard;
			}

			private Deque<Assert> asserts() {
				return asserts;
			}

		}

		private static final SequenceParser parser = new SequenceParser()
				.optionalAttributes(AttrParser.ID, AttrParser.MIXED)
				.elements(0, 1, TagParser.ANNOTATION)
				.elements(1, 1, TagParser.COMPLEX_TYPE.complexContent().extension(), TagParser.COMPLEX_TYPE.complexContent().restriction());

		private final AnnotationSet annotations;
		private final Boolean mixed;
		private final DerivationMethod derivationMethod;
		private final Deferred<Derivation> derivation;

		ComplexContent(final AnnotationSet annotations, final Boolean mixed, final DerivationMethod derivationMethod, final Deferred<Derivation> derivation) {
			this.annotations = Objects.requireNonNull(annotations);
			this.mixed = mixed;
			this.derivationMethod = Objects.requireNonNull(derivationMethod);
			this.derivation = Objects.requireNonNull(derivation);
		}

		private static ComplexContent parse(final Result result) {
			final Boolean mixed = result.value(AttrParser.MIXED);
			final DerivationMethod derivationMethod;
			final Deferred<Derivation> derivation;
			final Deferred<Derivation> restriction = result.parse(TagParser.COMPLEX_TYPE.complexContent().restriction());
			if (restriction != null) {
				derivationMethod = DerivationMethod.RESTRICTION;
				derivation = restriction;
			} else {
				derivationMethod = DerivationMethod.EXTENSION;
				derivation = result.parse(TagParser.COMPLEX_TYPE.complexContent().extension());
			}
			final AnnotationSet annotations = Annotation.of(result).add(derivation, Derivation::annotations);
			return new ComplexContent(annotations, mixed, derivationMethod, derivation);
		}

		private AnnotationSet annotations() {
			return annotations;
		}

		private Derivation derivation() {
			return derivation.get();
		}

		private Boolean mixed() {
			return mixed;
		}

		private DerivationMethod derivationMethod() {
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
	 *       <td>{@link ContentType#simpleTypeDefinition()}</td>
	 *       <td>{simple type definition}</td>
	 *       <td>A Simple Type Definition component. Required if {variety} is simple, otherwise must be ·absent·.</td>
	 *     </tr>
	 *   </tbody>
	 * </table>
	 */
	public static class ContentType {

		private final Deferred<Variety> variety;
		private final Deferred<Particle> particle;
		private final Deferred<OpenContent> openContent;
		private final Deferred<SimpleType> simpleTypeDefinition;

		private ContentType(final Deferred<Variety> variety, final Deferred<Particle> particle, final Deferred<OpenContent> openContent, final Deferred<SimpleType> simpleTypeDefinition) {
			this.variety = Objects.requireNonNull(variety);
			this.particle = particle;
			this.openContent = openContent;
			this.simpleTypeDefinition = simpleTypeDefinition;
		}

		/** @return One of {empty, simple, element-only, mixed}. Required. */
		public Variety variety() {
			return variety.get();
		}

		/** @return A Particle component. Required if {variety} is element-only or mixed, otherwise must be ·absent·. */
		public Particle particle() {
			return particle != null ? particle.get() : null;
		}

		/** @return An Open Content property record. Optional if {variety} is element-only or mixed, otherwise must be ·absent·. */
		public OpenContent openContent() {
			return openContent != null ? openContent.get() : null;
		}

		/** @return A Simple Type Definition component. Required if {variety} is simple, otherwise must be ·absent·. */
		public SimpleType simpleTypeDefinition() {
			return simpleTypeDefinition != null ? simpleTypeDefinition.get() : null;
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

		/** Complex type open content mode */
		public enum Mode {

			/** Complex type open content mode none */
			NONE("none"),
			/** Complex type open content mode interleave */
			INTERLEAVE("interleave"),
			/** Complex type open content mode suffix */
			SUFFIX("suffix");

			private final String name;

			private Mode(final String name) {
				this.name = name;
			}

			private static Mode getAttrValueAsMode(final Attr attr) {
				final String value = NodeHelper.collapseWhitespace(attr.getValue());
				Mode mode = null;
				for (final Mode m : values()) {
					if (m.name.equals(value)) {
						mode = m;
					}
				}
				if (mode == null || (Mode.NONE.equals(mode) && !TagParser.Names.OPEN_CONTENT.equals(attr.getOwnerElement().getLocalName()))) {
					throw NodeHelper.newFacetException(attr, value, AttrParser.Names.MODE.getLocalPart());
				}
				return mode;
			}

			/**
			 * Returns the name of this complex type open content mode, i.e. {@code "none"}, {@code "interleave"}, or {@code "suffix"} as appropriate.
			 * @return The name of this complex type open content mode
			 */
			@Override
			public String toString() {
				return name;
			}

		}

		private static final SequenceParser parser = new SequenceParser()
				.optionalAttributes(AttrParser.ID, AttrParser.MODE)
				.elements(0, 1, TagParser.ANNOTATION)
				.elements(0, 1, TagParser.ANY);

		private final AnnotationSet annotations;
		private final Mode mode;
		private final Deferred<Any> wildcard;

		OpenContent(final AnnotationSet annotations, final Mode mode, final Deferred<Any> wildcard) {
			this.annotations = Objects.requireNonNull(annotations);
			this.mode = mode;
			this.wildcard = wildcard;
		}

		private static OpenContent parse(final Result result) {
			final AnnotationSet annotations = Annotation.of(result);
			final Mode mode = result.value(AttrParser.MODE);
			final Deferred<Any> wildcard = result.parse(TagParser.ANY);
			return new OpenContent(annotations, mode, wildcard);
		}

		AnnotationSet annotations() {
			return annotations;
		}

		/** @return The ·actual value· of the mode [attribute] of the ·wildcard element·, if present, otherwise interleave. */
		public Mode mode() {
			return mode;
		}

		/** @return Let W be the wildcard corresponding to the &lt;any&gt; [child] of the ·wildcard element·. If the {open content} of the ·explicit content type· is ·absent·, then W; otherwise a wildcard whose {process contents} and {annotations} are those of W, and whose {namespace constraint} is the wildcard union of the {namespace constraint} of W and of {open content}.{wildcard} of the ·explicit content type·, as defined in Attribute Wildcard Union (§3.10.6.3). */
		public Any wildcard() {
			return wildcard != null ? wildcard.get() : null;
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

			private static final SequenceParser parser = new SequenceParser()
					.requiredAttributes(AttrParser.BASE)
					.optionalAttributes(AttrParser.ID)
					.elements(0, 1, TagParser.ANNOTATION)
					.elements(0, Integer.MAX_VALUE, TagParser.ATTRIBUTE.use(), TagParser.ATTRIBUTE_GROUP)
					.elements(0, 1, TagParser.ANY_ATTRIBUTE)
					.elements(0, Integer.MAX_VALUE, TagParser.COMPLEX_TYPE.asserts());

			private final AnnotationSet annotations;
			private final Deferred<? extends TypeDefinition> base;
			private final Deque<AttributeUse> attributeUses;
			private final Deferred<Wildcard> attributeWildcard;
			private final Deque<Assert> asserts;

			Extension(final AnnotationSet annotations, final Deferred<? extends TypeDefinition> base, final Deque<AttributeUse> attributeUses, final Deferred<Wildcard> attributeWildcard, final Deque<Assert> asserts) {
				this.annotations = Objects.requireNonNull(annotations);
				this.base = Objects.requireNonNull(base);
				this.attributeUses = Objects.requireNonNull(attributeUses);
				this.attributeWildcard = attributeWildcard;
				this.asserts = Objects.requireNonNull(asserts);
			}

			private static Extension parse(final Result result) {
				final QName baseType = result.value(AttrParser.BASE);
				final Deferred<? extends TypeDefinition> baseTypeDefinition = result.schema().find(baseType, TypeDefinition.class);
				final Deque<AttributeGroup> attributeGroups = result.parseAll(TagParser.ATTRIBUTE_GROUP);
				final Deque<AttributeUse> attributeUses = AttributeGroup.findAttributeUses(result.parseAll(TagParser.ATTRIBUTE.use()), attributeGroups);
				final AnnotationSet annotations = Annotation.of(result).addAll(attributeGroups);
				final Deferred<Wildcard> attributeWildcard = result.parse(TagParser.ANY_ATTRIBUTE);
				final Deque<Assert> asserts = result.parseAll(TagParser.COMPLEX_TYPE.asserts());
				return new Extension(annotations, baseTypeDefinition, attributeUses, attributeWildcard, asserts);
			}

			private AnnotationSet annotations() {
				return annotations;
			}

			private TypeDefinition base() {
				return base.get();
			}

			private Deque<AttributeUse> attributeUses() {
				return attributeUses;
			}

			private Deferred<Wildcard> attributeWildcard() {
				return attributeWildcard;
			}

			private Deque<Assert> asserts() {
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

			private static final SequenceParser parser = new SequenceParser()
					.requiredAttributes(AttrParser.BASE)
					.optionalAttributes(AttrParser.ID)
					.elements(0, 1, TagParser.ANNOTATION)
					.elements(0, 1, TagParser.SIMPLE_TYPE)
					.elements(0, Integer.MAX_VALUE, TagParser.FACETS.length(), TagParser.FACETS.maxLength(), TagParser.FACETS.minLength(), TagParser.FACETS.pattern(), TagParser.FACETS.enumeration(), TagParser.FACETS.whiteSpace(), TagParser.FACETS.maxInclusive(), TagParser.FACETS.maxExclusive(), TagParser.FACETS.minExclusive(), TagParser.FACETS.minInclusive(), TagParser.FACETS.totalDigits(), TagParser.FACETS.fractionDigits(), TagParser.FACETS.assertion())
					.elements(0, Integer.MAX_VALUE, TagParser.ATTRIBUTE.use(), TagParser.ATTRIBUTE_GROUP)
					.elements(0, 1, TagParser.ANY_ATTRIBUTE)
					.elements(0, Integer.MAX_VALUE, TagParser.COMPLEX_TYPE.asserts());

			private final AnnotationSet annotations;
			private final Deferred<ComplexType> base;
			private final Deque<ConstrainingFacet> facets;
			private final Deferred<SimpleType> simpleType;
			private final Deque<AttributeUse> attributeUses;
			private final Deferred<Wildcard> attributeWildcard;
			private final Deque<Assert> asserts;

			private Restriction(final AnnotationSet annotations, final Deferred<ComplexType> base, final Deque<ConstrainingFacet> facets, final Deferred<SimpleType> simpleType, final Deque<AttributeUse> attributeUses, final Deferred<Wildcard> attributeWildcard, final Deque<Assert> asserts) {
				this.annotations = Objects.requireNonNull(annotations);
				this.base = Objects.requireNonNull(base);
				this.facets = Objects.requireNonNull(facets);
				this.simpleType = simpleType;
				this.attributeUses = Objects.requireNonNull(attributeUses);
				this.attributeWildcard = attributeWildcard;
				this.asserts = Objects.requireNonNull(asserts);
			}

			private static Restriction parse(final Result result) {
				final QName baseType = result.value(AttrParser.BASE);
				final Deferred<ComplexType> baseTypeDefinition = result.schema().find(baseType, ComplexType.class);
				final Deferred<SimpleType> simpleType = result.parse(TagParser.SIMPLE_TYPE);
				final Deque<ConstrainingFacet> facets = result.parseAll(TagParser.FACETS.length(), TagParser.FACETS.maxLength(), TagParser.FACETS.minLength(), TagParser.FACETS.pattern(), TagParser.FACETS.enumeration(), TagParser.FACETS.whiteSpace(), TagParser.FACETS.maxInclusive(), TagParser.FACETS.maxExclusive(), TagParser.FACETS.minExclusive(), TagParser.FACETS.minInclusive(), TagParser.FACETS.totalDigits(), TagParser.FACETS.fractionDigits(), TagParser.FACETS.assertion());
				final Deque<AttributeGroup> attributeGroups = result.parseAll(TagParser.ATTRIBUTE_GROUP);
				final Deque<AttributeUse> attributeUses = AttributeGroup.findAttributeUses(result.parseAll(TagParser.ATTRIBUTE.use()), attributeGroups);
				final AnnotationSet annotations = Annotation.of(result).addAll(attributeGroups);
				final Deferred<Wildcard> attributeWildcard = result.parse(TagParser.ANY_ATTRIBUTE);
				final Deque<Assert> asserts = result.parseAll(TagParser.COMPLEX_TYPE.asserts());
				return new Restriction(annotations, baseTypeDefinition, facets, simpleType, attributeUses, attributeWildcard, asserts);
			}

			private AnnotationSet annotations() {
				return annotations;
			}

			private ComplexType base() {
				return base.get();
			}

			private Deque<ConstrainingFacet> facets() {
				return facets;
			}

			private Deferred<SimpleType> simpleType() {
				return simpleType;
			}

			private Deque<AttributeUse> attributeUses() {
				return attributeUses;
			}

			private Deferred<Wildcard> attributeWildcard() {
				return attributeWildcard;
			}

			private Deque<Assert> asserts() {
				return asserts;
			}

		}

		private static final SequenceParser parser = new SequenceParser()
				.optionalAttributes(AttrParser.ID)
				.elements(0, 1, TagParser.ANNOTATION)
				.elements(1, 1, TagParser.COMPLEX_TYPE.simpleContent().extension(), TagParser.COMPLEX_TYPE.simpleContent().restriction());

		private final DerivationMethod derivationMethod;
		private final Deferred<Restriction> restriction;
		private final Deferred<Extension> extension;

		private SimpleContent(final DerivationMethod derivationMethod, final Deferred<Restriction> restriction, final Deferred<Extension> extension) {
			this.derivationMethod = Objects.requireNonNull(derivationMethod);
			this.restriction = restriction;
			this.extension = extension;
		}

		private static SimpleContent parse(final Result result) {
			final Deferred<Restriction> restriction = result.parse(TagParser.COMPLEX_TYPE.simpleContent().restriction());
			if (restriction != null) {
				return new SimpleContent(DerivationMethod.RESTRICTION, restriction, null);
			}
			final Deferred<Extension> extension = result.parse(TagParser.COMPLEX_TYPE.simpleContent().extension());
			return new SimpleContent(DerivationMethod.EXTENSION, null, extension);
		}

		private AnnotationSet annotations() {
			return restriction != null ? restriction.get().annotations() : extension.get().annotations();
		}

		private DerivationMethod derivationMethod() {
			return derivationMethod;
		}

		private TypeDefinition base() {
			return restriction != null ? restriction.get().base() : extension.get().base();
		}

		private Deque<AttributeUse> attributeUses() {
			return restriction != null ? restriction.get().attributeUses() : extension.get().attributeUses();
		}

		private Deferred<Wildcard> attributeWildcard() {
			return restriction != null ? restriction.get().attributeWildcard() : extension.get().attributeWildcard();
		}

		private Deque<Assert> asserts() {
			return restriction != null ? restriction.get().asserts() : extension.get().asserts();
		}

		private Restriction restriction() {
			return restriction.get();
		}

	}

	private static final Deferred<ComplexType> xsAnyType;
	private static final SequenceParser parser = new SequenceParser()
			.optionalAttributes(AttrParser.ID, AttrParser.ABSTRACT, AttrParser.BLOCK, AttrParser.FINAL, AttrParser.MIXED, AttrParser.NAME, AttrParser.DEFAULT_ATTRIBUTES_APPLY)
			.elements(0, 1, TagParser.ANNOTATION)
			.elements(0, 1, TagParser.COMPLEX_TYPE.complexContent(), TagParser.COMPLEX_TYPE.simpleContent(), TagParser.COMPLEX_TYPE.openContent())
			.elements(0, 1, TagParser.GROUP.use(), TagParser.ALL, TagParser.CHOICE, TagParser.SEQUENCE)
			.elements(0, Integer.MAX_VALUE, TagParser.ATTRIBUTE.use(), TagParser.ATTRIBUTE_GROUP)
			.elements(0, 1, TagParser.ANY_ATTRIBUTE)
			.elements(0, Integer.MAX_VALUE, TagParser.COMPLEX_TYPE.asserts());
	static final String ANYTYPE_NAME = "anyType";

	static {
		xsAnyType = Deferred.of(() -> {
			final Node xsAnyTypeNode = NodeHelper.newGlobalNode(Schema.XSD, TagParser.Names.COMPLEX_TYPE, ANYTYPE_NAME);
			final ContentType xsAnyTypeContentType = new ContentType(() -> Variety.EMPTY, null, null, null);
			return new ComplexType(() -> Schema.XSD, xsAnyTypeNode, Deques.emptyDeque(), ANYTYPE_NAME, XMLConstants.W3C_XML_SCHEMA_NS_URI, Deques.emptyDeque(), () -> {
				throw new AssertionError();
			}, Deques.emptyDeque(), () -> null, false, () -> DerivationMethod.RESTRICTION, () -> xsAnyTypeContentType, Deques.emptyDeque(), Deques.emptyDeque()) {

				@Override
				public TypeDefinition baseTypeDefinition() {
					return this;
				}

			};
		});
	}

	private final Deferred<? extends AnnotatedComponent> context;
	private final Node node;
	private final Deque<Annotation> annotations;
	private final String name;
	private final String targetNamespace;
	private final Deque<Final> finals;
	private final Deferred<? extends TypeDefinition> baseTypeDefinition;
	private final Deque<AttributeUse> attributeUses;
	private final Deferred<Wildcard> attributeWildcard;
	private final boolean isAbstract;
	private final Deferred<DerivationMethod> derivationMethod;
	private final Deferred<ContentType> contentType;
	private final Deque<Block> prohibitedSubstitutions;
	private final Deque<Assertion> assertions;

	private ComplexType(final Deferred<? extends AnnotatedComponent> context, final Node node, final Deque<Annotation> annotations, final String name, final String targetNamespace, final Deque<Final> finals, final Deferred<? extends TypeDefinition> baseTypeDefinition, final Deque<AttributeUse> attributeUses, final Deferred<Wildcard> attributeWildcard, final boolean isAbstract, final Deferred<DerivationMethod> derivationMethod, final Deferred<ContentType> contentType, final Deque<Block> prohibitedSubstitutions, final Deque<Assertion> assertions) {
		this.context = Objects.requireNonNull(context);
		this.node = Objects.requireNonNull(node);
		this.annotations = Objects.requireNonNull(annotations);
		this.name = name;
		this.targetNamespace = NodeHelper.requireNonEmpty(node, targetNamespace);
		this.finals = Objects.requireNonNull(finals);
		this.baseTypeDefinition = Objects.requireNonNull(baseTypeDefinition);
		this.attributeUses = Objects.requireNonNull(attributeUses);
		this.attributeWildcard = Objects.requireNonNull(attributeWildcard);
		this.isAbstract = isAbstract;
		this.derivationMethod = Objects.requireNonNull(derivationMethod);
		this.contentType = Objects.requireNonNull(contentType);
		this.prohibitedSubstitutions = Objects.requireNonNull(prohibitedSubstitutions);
		this.assertions = Objects.requireNonNull(assertions);
	}

	private static boolean nonNull(final Deferred<?> def) {
		return def != null && def.get() != null;
	}

	private static ComplexType parse(final Result result) {
		final Deferred<? extends AnnotatedComponent> context = result.context();
		final Node node = result.node();
		final AnnotationSet annotations = Annotation.of(result);
		final boolean isAbstract = result.value(AttrParser.ABSTRACT);
		Deque<Block> block = result.value(AttrParser.BLOCK);
		if (block.isEmpty()) {
			block = Deques.singletonDeque(result.schema().blockDefault());
		}
		Deque<Final> finals = result.value(AttrParser.FINAL);
		if (finals.isEmpty()) {
			finals = Deques.singletonDeque(result.schema().finalDefault());
		}
		final Boolean mixed = result.value(AttrParser.MIXED);
		final String name = result.value(AttrParser.NAME);
		final boolean defaultAttributesApply = result.value(AttrParser.DEFAULT_ATTRIBUTES_APPLY);
		final String targetNamespace = result.schema().targetNamespace();
		final Deferred<ComplexContent> complexContent = result.parse(TagParser.COMPLEX_TYPE.complexContent());
		final Deferred<SimpleContent> simpleContent = result.parse(TagParser.COMPLEX_TYPE.simpleContent());
		final Deferred<Boolean> effectiveMixed = Deferred.of(() ->
				complexContent != null && complexContent.get().mixed() != null
						? complexContent.get().mixed()
						: mixed != null && mixed);
		final Deferred<Def> def;
		final DeferredValue<ComplexType> self = new DeferredValue<>();
		if (simpleContent != null) {
			def = simpleContent.map(s -> {
				// https://www.w3.org/TR/xmlschema11-1/#dcl.ctd.ctsc
				annotations.add(s.annotations());
				final SimpleType simpleBase;
				final TypeDefinition typeDefinition = s.base();
				if (typeDefinition instanceof ComplexType) {
					final Schema schema = result.schema();
					final ComplexType complexType = (ComplexType) typeDefinition;
					if (complexType.contentType().variety() == Variety.SIMPLE && DerivationMethod.RESTRICTION.equals(s.derivationMethod())) { // 1
						if (s.restriction().simpleType() != null) { // 1.1
							simpleBase = s.restriction().simpleType().get();
						} else { // 1.2
							simpleBase = SimpleType.wrap(schema, self, node, Deques.emptyDeque(), null, result.schema().targetNamespace(), Deques.emptyDeque(), complexType.contentType().simpleTypeDefinition(), s.restriction().facets());
						}
					} else if (complexType.contentType().variety() == Variety.MIXED && DerivationMethod.RESTRICTION.equals(s.derivationMethod()) && complexType.contentType().particle().isEmptiable()) { // 2
						final SimpleType sb = s.restriction().simpleType() != null ? s.restriction().simpleType().get() : SimpleType.xsAnySimpleType();
						simpleBase = SimpleType.wrap(schema, self, node, Deques.emptyDeque(), null, result.schema().targetNamespace(), Deques.emptyDeque(), sb, sb.facets());
					} else if (complexType.contentType().variety() == Variety.SIMPLE && DerivationMethod.EXTENSION.equals(s.derivationMethod())) { // 3
						simpleBase = complexType.contentType().simpleTypeDefinition();
					} else {
						simpleBase = SimpleType.xsAnySimpleType(); // 5
					}
				} else if (DerivationMethod.EXTENSION.equals(s.derivationMethod()) && typeDefinition instanceof SimpleType) { // 4
					simpleBase = (SimpleType) typeDefinition;
				} else {
					simpleBase = SimpleType.xsAnySimpleType(); // 5
				}
				final ContentType explicitContentType = new ContentType(() -> Variety.SIMPLE, null, null, () -> simpleBase);
				return new Def(simpleBase, s.asserts(), s.attributeUses(), s.attributeWildcard(), s.derivationMethod(), () -> explicitContentType);
			});
		} else {
			def = Deferred.of(() -> {
				final Deferred<OpenContent> openContent;
				final Deferred<Particle> group;
				final Deferred<Particle> all;
				final Deferred<Particle> choice;
				final Deferred<Particle> sequence;
				final Deque<Assert> asserts;
				final TypeDefinition baseTypeDefinition;
				final Deque<AttributeUse> attributeUses;
				final Deferred<Wildcard> attributeWildcard;
				final DerivationMethod derivationMethod;
				if (complexContent != null) {
					final ComplexContent c = complexContent.get();
					annotations.add(complexContent, ComplexContent::annotations);
					openContent = c.derivation().openContent();
					group = c.derivation().group();
					all = c.derivation().all();
					choice = c.derivation().choice();
					sequence = c.derivation().sequence();
					asserts = c.derivation().asserts();
					baseTypeDefinition = c.derivation().base();
					attributeUses = c.derivation().attributeUses();
					attributeWildcard = c.derivation().attributeWildcard();
					derivationMethod = c.derivationMethod();
				} else {
					openContent = result.parse(TagParser.COMPLEX_TYPE.openContent());
					group = result.parse(TagParser.GROUP.use());
					all = result.parse(TagParser.ALL);
					choice = result.parse(TagParser.CHOICE);
					sequence = result.parse(TagParser.SEQUENCE);
					asserts = result.parseAll(TagParser.COMPLEX_TYPE.asserts());
					baseTypeDefinition = xsAnyType();
					final Deque<AttributeGroup> attributeGroups = result.parseAll(TagParser.ATTRIBUTE_GROUP);
					annotations.addAll(attributeGroups);
					attributeUses = AttributeGroup.findAttributeUses(result.parseAll(TagParser.ATTRIBUTE.use()), attributeGroups);
					attributeWildcard = result.parse(TagParser.ANY_ATTRIBUTE);
					derivationMethod = DerivationMethod.RESTRICTION;
				}
				final Deferred<Particle> particle = Deferred.of(() ->
						nonNull(group) ? group.get()
						: nonNull(all) ? all.get()
						: nonNull(choice) ? choice.get()
						: nonNull(sequence) ? sequence.get()
						: null);
				final Deferred<Particle> explicitContent = Deferred.of(() -> {
					if (particle == null) {
						return null;
					} else if ((nonNull(all) && ((ModelGroup) all.get().term()).particles().isEmpty()) || (nonNull(sequence) && ((ModelGroup) sequence.get().term()).particles().isEmpty())) {
						return null;
					} else {
						final boolean choiceMinOccurs0 = nonNull(choice) && choice.get().minOccurs().intValue() == 0 && (choice.get().term() == null || ((ModelGroup) choice.get().term()).particles().isEmpty());
						if (choiceMinOccurs0) {
							return null;
						} else {
							final Number particleMaxOccurs = nonNull(group) ? group.get().maxOccurs()
									: nonNull(all) ? all.get().maxOccurs()
									: nonNull(choice) ? choice.get().maxOccurs()
									: nonNull(sequence) ? sequence.get().maxOccurs()
									: null;
							return particleMaxOccurs != null && particleMaxOccurs.intValue() == 0 ? null : particle.get();
						}
					}
				});
				final Deferred<Particle> effectiveContent = explicitContent.map(e -> {
					if (e == null) {
						if (Boolean.TRUE.equals(effectiveMixed.get())) {
							return new Particle(self, node, Deques.emptyDeque(), 1, 1, ModelGroup.synthetic(self, node, Deques.emptyDeque(), Compositor.SEQUENCE, Deques.emptyDeque()));
						} else {
							return null;
						}
					} else {
						return e;
					}
				});
				final Deferred<ContentType> explicitContentType = effectiveContent.map(ef -> {
					final Supplier<ContentType> handleRestriction = () -> {
						if (ef == null) { // 4.1.1
							return new ContentType(() -> Variety.EMPTY, null, openContent, null);
						} else { // 4.1.2
							return new ContentType(effectiveMixed.map(e -> Boolean.TRUE.equals(e) ? Variety.MIXED : Variety.ELEMENT_ONLY), effectiveContent, openContent, null);
						}
					};
					switch (derivationMethod) {
					case RESTRICTION: // 4.1
						return handleRestriction.get();
					case EXTENSION: // 4.2
						if (baseTypeDefinition instanceof SimpleType) {
							return handleRestriction.get(); // 4.2.1
						} else {
							final ComplexType complexBase = (ComplexType) baseTypeDefinition;
							if (ef == null && complexBase.contentType().variety().isMixedOrElementOnly()) {
								return complexBase.contentType(); // 4.2.2
							} else if (!complexBase.contentType().variety().isMixedOrElementOnly()) {
								return handleRestriction.get(); // 4.2.1
							} else { // 4.2.3
								final Particle baseParticle = complexBase.contentType().particle();
								final Particle effectiveParticle;
								if (baseParticle != null && baseParticle.term() instanceof ModelGroup && Compositor.ALL.equals(((ModelGroup) baseParticle.term()).compositor()) && ef == null) { // 4.2.3.1
									effectiveParticle = baseParticle;
								} else if (baseParticle != null && baseParticle.term() instanceof ModelGroup && Compositor.ALL.equals(((ModelGroup) baseParticle.term()).compositor()) && ef != null && ef.term() instanceof ModelGroup && Compositor.ALL.equals(((ModelGroup) ef.term()).compositor())) { // 4.2.3.2
									final Deque<Particle> particles = new ConcurrentLinkedDeque<>(((ModelGroup) baseParticle.term()).particles());
									particles.addAll(((ModelGroup) ef.term()).particles());
									effectiveParticle = new Particle(self, node, Deques.emptyDeque(), 1, baseParticle.minOccurs(), ModelGroup.synthetic(self, node, Deques.emptyDeque(), Compositor.ALL, particles));
								} else { // 4.2.3.3
									final Deque<Particle> particles = new ConcurrentLinkedDeque<>();
									if (baseParticle != null) {
										particles.add(baseParticle);
									}
									if (ef != null) {
										particles.add(ef);
									}
									effectiveParticle = new Particle(self, node, Deques.emptyDeque(), 1, 1, ModelGroup.synthetic(self, node, Deques.emptyDeque(), Compositor.SEQUENCE, particles));
								}
								return new ContentType(effectiveMixed.map(e -> Boolean.TRUE.equals(e) ? Variety.MIXED : Variety.ELEMENT_ONLY), () -> effectiveParticle, complexBase.contentType()::openContent, null);
							}
						}
					default:
						throw new AssertionError(derivationMethod.toString());
					}
				});
				final Deferred<OpenContent> wildcardElement = Deferred.of(() -> {
					if (openContent != null) { // 5.1
						return openContent.get();
					} else {
						final Deferred<DefaultOpenContent> d = result.schema().defaultOpenContent();
						if (d != null && (!Variety.EMPTY.equals(explicitContentType.get().variety()) || (Variety.EMPTY.equals(explicitContentType.get().variety()) && d.get().appliesToEmpty()))) { // 5.2
							return d.get();
						} else { // 5.3
							return null;
						}
					}
				});
				final Deferred<ContentType> contentType = wildcardElement.map(wc -> {
					if (wc == null || OpenContent.Mode.NONE.equals(wc.mode())) { // 6.1
						return explicitContentType.get();
					} else { // 6.2
						final Deferred<Variety> v = explicitContentType.map(e -> Variety.EMPTY.equals(e.variety()) ? Variety.ELEMENT_ONLY : e.variety());
						final Deferred<Particle> p = explicitContentType.map(e -> Variety.EMPTY.equals(e.variety()) ? new Particle(context, result.node(), Deques.emptyDeque(), 1, 1, () -> ModelGroup.synthetic(context, node, Deques.emptyDeque(), Compositor.SEQUENCE, Deques.emptyDeque())) : e.particle());
						final OpenContent o = new OpenContent(AnnotationSet.EMPTY, wc != null ? wc.mode : OpenContent.Mode.INTERLEAVE, explicitContentType.map(e -> {
							if (nonNull(e.openContent)) {
								return nonNull(wc.wildcard) ? wc.wildcard().union(e.openContent().wildcard()) : e.openContent().wildcard();
							} else {
								return wc.wildcard();
							}
						}));
						return new ContentType(v, p, () -> o, null);
					}
				});
				return new Def(baseTypeDefinition, asserts, attributeUses, attributeWildcard, derivationMethod, contentType);
			});
		}
		annotations.addAll(def.mapToDeque(d -> d.asserts), Assert::annotations);
		return self.set(new ComplexType(context, node, def.mapToDeque(d -> annotations.resolve(node)), name, targetNamespace, finals, def.map(d -> d.baseTypeDefinition), def.mapToDeque(d -> {
			if (defaultAttributesApply && result.schema().defaultAttributes() != null) {
				d.attributeUses.addAll(result.schema().defaultAttributes().mapToDeque(AttributeGroup::attributeUses));
			}
			return d.attributeUses;
		}), def.map(Def::attributeWildcard), isAbstract, def.map(d -> d.derivationMethod), def.map(Def::explicitContentType), block, def.mapToDeque(d -> d.asserts.stream().map(Assert::assertion).collect(Collectors.toCollection(ConcurrentLinkedDeque::new)))));
	}

	static void register() {
		AttrParser.register(AttrParser.Names.ABSTRACT, false);
		AttrParser.register(AttrParser.Names.BASE, QName.class, NodeHelper::getAttrValueAsQName);
		AttrParser.register(AttrParser.Names.DEFAULT_ATTRIBUTES_APPLY, true);
		AttrParser.register(AttrParser.Names.MIXED, (Boolean) null);
		AttrParser.register(AttrParser.Names.MODE, OpenContent.Mode.class, OpenContent.Mode.INTERLEAVE, OpenContent.Mode::getAttrValueAsMode);
		TagParser.register(TagParser.Names.ASSERT, Assert.parser, Assert.class, Assert::parse);
		TagParser.register(TagParser.Names.RESTRICTION, ComplexContent.Derivation.parser, ComplexContent.Derivation.class, ComplexContent.Derivation::parse);
		TagParser.register(TagParser.Names.EXTENSION, ComplexContent.Derivation.parser, ComplexContent.Derivation.class, ComplexContent.Derivation::parse);
		TagParser.register(TagParser.Names.COMPLEX_CONTENT, ComplexContent.parser, ComplexContent.class, ComplexContent::parse);
		TagParser.register(TagParser.Names.RESTRICTION, SimpleContent.Restriction.parser, SimpleContent.Restriction.class, SimpleContent.Restriction::parse);
		TagParser.register(TagParser.Names.EXTENSION, SimpleContent.Extension.parser, SimpleContent.Extension.class, SimpleContent.Extension::parse);
		TagParser.register(TagParser.Names.SIMPLE_CONTENT, SimpleContent.parser, SimpleContent.class, SimpleContent::parse);
		TagParser.register(TagParser.Names.OPEN_CONTENT, OpenContent.parser, OpenContent.class, OpenContent::parse);
		TagParser.register(TagParser.Names.COMPLEX_TYPE, ComplexType.parser, ComplexType.class, ComplexType::parse);
		VisitorHelper.register(ComplexType.class, ComplexType::visit);
	}

	/** @return The xs:anyType complex type */
	public static ComplexType xsAnyType() {
		return xsAnyType.get();
	}

	static void visitTypeDefinition(final TypeDefinition typeDefinition, final Visitor visitor) {
		if (typeDefinition instanceof ComplexType) {
			((ComplexType) typeDefinition).visit(visitor);
		} else {
			((SimpleType) typeDefinition).visit(visitor);
		}
	}

	void visit(final Visitor visitor) {
		if (visitor.visit(context.get(), node, this)) {
			visitor.onComplexType(context.get(), node, this);
			annotations.forEach(a -> a.visit(visitor));
			visitTypeDefinition(baseTypeDefinition(), visitor);
			attributeUses.forEach(a -> a.visit(visitor));
			if (attributeWildcard() != null) {
				((AnyAttribute) attributeWildcard()).visit(visitor);
			}
			final ContentType content = contentType();
			if (content.particle != null) {
				content.particle().visit(visitor);
			}
			if (content.openContent() != null && content.openContent().wildcard != null) {
				content.openContent().wildcard().visit(visitor);
			}
			if (content.simpleTypeDefinition != null) {
				content.simpleTypeDefinition().visit(visitor);
			}
			assertions.forEach(a -> a.visit(visitor));
		}
	}

	/** @return A Content Type as follows */
	public ContentType contentType() {
		return contentType.get();
	}

	/** @return If the &lt;restriction&gt; alternative is chosen, then restriction, otherwise (the &lt;extension&gt; alternative is chosen) extension. */
	public DerivationMethod derivationMethod() {
		return derivationMethod.get();
	}

	/** @return The ·actual value· of the abstract [attribute], if present, otherwise false. */
	public boolean isAbstract() {
		return isAbstract;
	}

	/**
	 * @return If the &lt;schema&gt; ancestor has a defaultAttributes attribute, and the &lt;complexType&gt; element does not have defaultAttributesApply = false, then the {attribute uses} property is computed as if there were an &lt;attributeGroup&gt; [child] with empty content and a ref [attribute] whose ·actual value· is the same as that of the defaultAttributes [attribute] appearing after any other &lt;attributeGroup&gt; [children]. Otherwise proceed as if there were no such &lt;attributeGroup&gt; [child].
	 * <p>
	 * Then the value is a union of sets of attribute uses as follows
	 * <ol>
	 *   <li>The set of attribute uses corresponding to the &lt;attribute&gt; [children], if any.</li>
	 *   <li>The {attribute uses} of the attribute groups ·resolved· to by the ·actual value·s of the ref [attribute] of the &lt;attributeGroup&gt; [children], if any.</li>
	 *   <li>The attribute uses "inherited" from the {base type definition} T, as described by the appropriate case among the following:
	 *     <ol>
	 *       <li>If T is a complex type definition and {derivation method} = extension, then the attribute uses in T.{attribute uses} are inherited.</li>
	 *       <li>If T is a complex type definition and {derivation method} = restriction, then the attribute uses in T.{attribute uses} are inherited, with the exception of those with an {attribute declaration} whose expanded name is one of the following:
	 *         <ol>
	 *           <li>the expanded name of the {attribute declaration} of an attribute use which has already been included in the set, following the rules in clause 1 or clause 2 above;</li>
	 *           <li>the expanded name of the {attribute declaration} of what would have been an attribute use corresponding to an &lt;attribute&gt; [child], if the &lt;attribute&gt; had not had use = prohibited. <i>Note: This sub-clause handles the case where the base type definition T allows the attribute in question, but the restriction prohibits it.</i></li>
	 *         </ol>
	 *       </li>
	 *       <li>otherwise no attribute use is inherited.</li>
	 *     </ol>
	 *   </li>
	 * </ol>
	 */
	public Deque<AttributeUse> attributeUses() {
		return Deques.unmodifiableDeque(attributeUses);
	}

	/**
	 * @return If the &lt;schema&gt; ancestor has a defaultAttributes attribute, and the &lt;complexType&gt; element does not have defaultAttributesApply = false, then the {attribute wildcard} property is computed as if there were an &lt;attributeGroup&gt; [child] with empty content and a ref [attribute] whose ·actual value· is the same as that of the defaultAttributes [attribute] appearing after any other &lt;attributeGroup&gt; [children]. Otherwise proceed as if there were no such &lt;attributeGroup&gt; [child].
	 * <ol>
	 *   <li>Let the complete wildcard be the Wildcard computed as described in Common Rules for Attribute Wildcards (§3.6.2.2).</li>
	 *   <li>The value is then determined by the appropriate case among the following:
	 *     <ol>
	 *       <li>If {derivation method} = restriction, then the ·complete wildcard·;</li>
	 *       <li>If {derivation method} = extension, then
	 *         <ol>
	 *           <li>let the base wildcard be defined as the appropriate case among the following:
	 *             <ol>
	 *               <li>If the {base type definition} is a complex type definition with an {attribute wildcard}, then that {attribute wildcard}.</li>
	 *               <li>otherwise ·absent·.</li>
	 *             </ol>
	 *           </li>
	 *           <li>The value is then determined by the first case among the following which applies:
	 *             <ol>
	 *               <li>If the ·base wildcard· is ·absent·, then the ·complete wildcard·;</li>
	 *               <li>If the ·complete wildcard· is ·absent·, then the ·base wildcard·;</li>
	 *               <li>otherwise a wildcard whose {process contents} and {annotations} are those of the ·complete wildcard·, and whose {namespace constraint} is the wildcard union of the {namespace constraint} of the ·complete wildcard· and of the ·base wildcard·, as defined in Attribute Wildcard Union (§3.10.6.3).</li>
	 *             </ol>
	 *           </li>
	 *         </ol>
	 *       </li>
	 *     </ol>
	 *   </li>
	 * </ol>
	 */
	public Wildcard attributeWildcard() {
		return attributeWildcard.get();
	}

	/**
	 * @return A set corresponding to the ·actual value· of the block [attribute], if present, otherwise to the ·actual value· of the blockDefault [attribute] of the ancestor &lt;schema&gt; element information item, if present, otherwise on the empty string. Call this the EBV (for effective block value). Then the value of this property is the appropriate case among the following:
	 * <ol>
	 *   <li>If the EBV is the empty string, then the empty set;</li>
	 *   <li>If the EBV is #all, then {extension, restriction};</li>
	 *   <li>otherwise a set with members drawn from the set above, each being present or absent depending on whether the ·actual value· (which is a list) contains an equivalently named item.</li>
	 * </ol>
	 *
	 * <i>Note: Although the blockDefault [attribute] of &lt;schema&gt; may include values other than restriction or extension, those values are ignored in the determination of {prohibited substitutions} for complex type definitions (they are used elsewhere).</i>
	 */
	public Deque<Block> prohibitedSubstitutions() {
		return Deques.unmodifiableDeque(prohibitedSubstitutions);
	}

	/**
	 * @return A sequence whose members are Assertions drawn from the following sources, in order:
	 * <ol>
	 *   <li>The {assertions} of the {base type definition}.</li>
	 *   <li>Assertions corresponding to all the &lt;assert&gt; element information items among the [children] of &lt;complexType&gt;, &lt;restriction&gt; and &lt;extension&gt;, if any, in document order.</li>
	 * </ol>
	 */
	public Deque<Assertion> assertions() {
		return assertions;
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
	public TypeDefinition baseTypeDefinition() {
		return baseTypeDefinition.get();
	}

	@Override
	public Deque<Final> finals() {
		return Deques.unmodifiableDeque(finals);
	}

	/** @return If the name [attribute] is present, then ·absent·, otherwise (among the ancestor element information items there will be a nearest &lt;element&gt;), the Element Declaration corresponding to the nearest &lt;element&gt; information item among the the ancestor element information items. */
	@Override
	public AnnotatedComponent context() {
		return name == null ? context.get() : null;
	}

}
