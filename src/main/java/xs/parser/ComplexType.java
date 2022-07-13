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

		private static final SequenceParser parser = new SequenceParser()
				.requiredAttributes(AttrParser.TEST)
				.optionalAttributes(AttrParser.ID, AttrParser.XPATH_DEFAULT_NAMESPACE)
				.elements(0, 1, TagParser.ANNOTATION);

		Assert(final Node node, final Deque<Annotation> annotations, final XPathExpression test) {
			super(node, annotations, test);
		}

		private static Assert parse(final Result result) {
			final XPathExpression test = TagParser.SELECTOR.parse(result);
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

			private static final SequenceParser parser = new SequenceParser()
					.requiredAttributes(AttrParser.BASE)
					.optionalAttributes(AttrParser.ID)
					.elements(0, 1, TagParser.ANNOTATION)
					.elements(0, 1, TagParser.COMPLEX_TYPE.openContent())
					.elements(0, 1, TagParser.GROUP.use(), TagParser.ALL, TagParser.CHOICE, TagParser.SEQUENCE)
					.elements(0, Integer.MAX_VALUE, TagParser.ATTRIBUTE.use(), TagParser.ATTRIBUTE_GROUP)
					.elements(0, 1, TagParser.ANY_ATTRIBUTE)
					.elements(0, Integer.MAX_VALUE, TagParser.COMPLEX_TYPE.asserts());

			private final Deque<Annotation> annotations;
			private final Deferred<ComplexType> base;
			private final OpenContent openContent;
			private final Particle group;
			private final Particle all;
			private final Particle choice;
			private final Particle sequence;
			private final Deferred<Deque<AttributeUse>> attributeUses;
			private final Wildcard attributeWildcard;
			private final Deque<Assert> asserts;

			Derivation(final Deque<Annotation> annotations, final Deferred<ComplexType> base, final Particle group, final Particle all, final Particle choice, final Particle sequence, final OpenContent openContent, final Deferred<Deque<AttributeUse>> attributeUses, final Wildcard attributeWildcard, final Deque<Assert> asserts) {
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
				final Particle group = result.parse(TagParser.GROUP.use());
				final Particle all = result.parse(TagParser.ALL);
				final Particle choice = result.parse(TagParser.CHOICE);
				final Particle sequence = result.parse(TagParser.SEQUENCE);
				final OpenContent openContent = result.parse(TagParser.COMPLEX_TYPE.openContent());
				final Deque<AttributeGroup> attributeGroups = result.parseAll(TagParser.ATTRIBUTE_GROUP);
				final Deferred<Deque<AttributeUse>> attributeUses = AttributeGroup.findAttributeUses(result.parseAll(TagParser.ATTRIBUTE.use()), attributeGroups);
				final AnnotationsBuilder annotations = new AnnotationsBuilder(result).add(attributeGroups);
				if (openContent != null) {
					annotations.add(openContent::annotations);
				}
				final Wildcard attributeWildcard = result.parse(TagParser.ANY_ATTRIBUTE);
				final Deque<Assert> asserts = result.parseAll(TagParser.COMPLEX_TYPE.asserts());
				return new Derivation(annotations.build(), baseTypeDefinition, group, all, choice, sequence, openContent, attributeUses, attributeWildcard, asserts);
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

			Deque<Assert> asserts() {
				return asserts;
			}

		}

		private static final SequenceParser parser = new SequenceParser()
				.optionalAttributes(AttrParser.ID, AttrParser.MIXED)
				.elements(0, 1, TagParser.ANNOTATION)
				.elements(1, 1, TagParser.COMPLEX_TYPE.complexContent().extension(), TagParser.COMPLEX_TYPE.complexContent().restriction());

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

		private static ComplexContent parse(final Result result) {
			final Boolean mixed = result.value(AttrParser.MIXED);
			final Derivation restriction = result.parse(TagParser.COMPLEX_TYPE.complexContent().restriction());
			final DerivationMethod derivationMethod;
			final Deque<Annotation> annotations;
			if (restriction != null) {
				derivationMethod = DerivationMethod.RESTRICTION;
				annotations = new AnnotationsBuilder(result).add(restriction::annotations).build();
				return new ComplexContent(annotations, mixed, derivationMethod, restriction);
			} else {
				final Derivation extension = result.parse(TagParser.COMPLEX_TYPE.complexContent().extension());
				derivationMethod = DerivationMethod.EXTENSION;
				annotations = new AnnotationsBuilder(result).add(extension::annotations).build();
				return new ComplexContent(annotations, mixed, derivationMethod, extension);
			}
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
	 *       <td>{@link ContentType#simpleTypeDefinition()}</td>
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
		private final SimpleType simpleTypeDefinition;

		private ContentType(final DefaultOpenContent defaultOpenContent, final Variety variety, final Particle particle, final OpenContent openContent, final SimpleType simpleTypeDefinition) {
			this.variety = Objects.requireNonNull(variety);
			this.particle = particle;
			if (openContent != null) {
				this.openContent = openContent;
			} else if (!Variety.EMPTY.equals(variety) || (defaultOpenContent != null && defaultOpenContent.appliesToEmpty())) {
				this.openContent = defaultOpenContent;
			} else {
				this.openContent = null;
			}
			this.simpleTypeDefinition = simpleTypeDefinition;
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

		public SimpleType simpleTypeDefinition() {
			return simpleTypeDefinition;
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

			private Mode(final String name) {
				this.name = name;
			}

			private static Mode getAttrValueAsMode(final Attr attr) {
				final String value = NodeHelper.collapseWhitespace(attr.getValue());
				Mode mode = null;
				for (final Mode m : values()) {
					if (m.getName().equals(value)) {
						mode = m;
					}
				}
				if (mode == null || (Mode.NONE.equals(mode) && !TagParser.Names.OPEN_CONTENT.equals(attr.getOwnerElement().getLocalName()))) {
					throw NodeHelper.newFacetException(attr, value, AttrParser.Names.MODE.getLocalPart());
				}
				return mode;
			}

			public String getName() {
				return name;
			}

			@Override
			public String toString() {
				return getName();
			}

		}

		private static final SequenceParser parser = new SequenceParser()
				.optionalAttributes(AttrParser.ID, AttrParser.MODE)
				.elements(0, 1, TagParser.ANNOTATION)
				.elements(0, 1, TagParser.ANY);

		private final Deque<Annotation> annotations;
		private final Mode mode;
		private final Particle wildcard;

		OpenContent(final Deque<Annotation> annotations, final Mode mode, final Particle wildcard) {
			this.annotations = Objects.requireNonNull(annotations);
			this.mode = mode;
			this.wildcard = wildcard;
		}

		private static OpenContent parse(final Result result) {
			final Mode mode = result.value(AttrParser.MODE);
			final Particle wildcard = result.parse(TagParser.ANY);
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

			private static final SequenceParser parser = new SequenceParser()
					.requiredAttributes(AttrParser.BASE)
					.optionalAttributes(AttrParser.ID)
					.elements(0, 1, TagParser.ANNOTATION)
					.elements(0, Integer.MAX_VALUE, TagParser.ATTRIBUTE.use(), TagParser.ATTRIBUTE_GROUP)
					.elements(0, 1, TagParser.ANY_ATTRIBUTE)
					.elements(0, Integer.MAX_VALUE, TagParser.COMPLEX_TYPE.asserts());

			private final Deque<Annotation> annotations;
			private final Deferred<? extends TypeDefinition> base;
			private final Deferred<Deque<AttributeUse>> attributeUses;
			private final Wildcard attributeWildcard;
			private final Deque<Assert> asserts;

			Extension(final Deque<Annotation> annotations, final Deferred<? extends TypeDefinition> base, final Deferred<Deque<AttributeUse>> attributeUses, final Wildcard attributeWildcard, final Deque<Assert> asserts) {
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
				final Deferred<Deque<AttributeUse>> attributeUses = AttributeGroup.findAttributeUses(result.parseAll(TagParser.ATTRIBUTE.use()), attributeGroups);
				final Deque<Annotation> annotations = new AnnotationsBuilder(result).add(attributeGroups).build();
				final Wildcard attributeWildcard = result.parse(TagParser.ANY_ATTRIBUTE);
				final Deque<Assert> asserts = result.parseAll(TagParser.COMPLEX_TYPE.asserts());
				return new Extension(annotations, baseTypeDefinition, attributeUses, attributeWildcard, asserts);
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

			Deque<Assert> asserts() {
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
					.elements(0, Integer.MAX_VALUE, TagParser.FACETS.length(), TagParser.FACETS.maxLength(), TagParser.FACETS.minLength(), TagParser.FACETS.pattern(), TagParser.FACETS.enumeration(), TagParser.FACETS.whiteSpace(), TagParser.FACETS.maxInclusive(), TagParser.FACETS.maxExclusive(), TagParser.FACETS.minExclusive(), TagParser.FACETS.minInclusive(), TagParser.FACETS.totalDigits(), TagParser.FACETS.fractionDigits(), TagParser.FACETS.assertion(), TagParser.ANY)
					.elements(0, Integer.MAX_VALUE, TagParser.ATTRIBUTE.use(), TagParser.ATTRIBUTE_GROUP)
					.elements(0, 1, TagParser.ANY_ATTRIBUTE)
					.elements(0, Integer.MAX_VALUE, TagParser.COMPLEX_TYPE.asserts());

			private final Deque<Annotation> annotations;
			private final Deferred<ComplexType> base;
			private final Deque<ConstrainingFacet> facets;
			private final Deque<Particle> wildcard;
			private final SimpleType simpleType;
			private final Deferred<Deque<AttributeUse>> attributeUses;
			private final Wildcard attributeWildcard;
			private final Deque<Assert> asserts;

			private Restriction(final Deque<Annotation> annotations, final Deferred<ComplexType> base, final Deque<ConstrainingFacet> facets, final Deque<Particle> wildcard, final SimpleType simpleType, final Deferred<Deque<AttributeUse>> attributeUses, final Wildcard attributeWildcard, final Deque<Assert> asserts) {
				this.annotations = Objects.requireNonNull(annotations);
				this.base = Objects.requireNonNull(base);
				this.facets = Objects.requireNonNull(facets);
				this.wildcard = Objects.requireNonNull(wildcard);
				this.simpleType = simpleType;
				this.attributeUses = Objects.requireNonNull(attributeUses);
				this.attributeWildcard = attributeWildcard;
				this.asserts = Objects.requireNonNull(asserts);
			}

			private static Restriction parse(final Result result) {
				final QName baseType = result.value(AttrParser.BASE);
				final Deferred<ComplexType> baseTypeDefinition = result.schema().find(baseType, ComplexType.class);
				final SimpleType simpleType = result.parse(TagParser.SIMPLE_TYPE);
				final Deque<ConstrainingFacet> facets = result.parseAll(TagParser.FACETS.length(), TagParser.FACETS.maxLength(), TagParser.FACETS.minLength(), TagParser.FACETS.pattern(), TagParser.FACETS.enumeration(), TagParser.FACETS.whiteSpace(), TagParser.FACETS.maxInclusive(), TagParser.FACETS.maxExclusive(), TagParser.FACETS.minExclusive(), TagParser.FACETS.minInclusive(), TagParser.FACETS.totalDigits(), TagParser.FACETS.fractionDigits(), TagParser.FACETS.assertion());
				final Deque<Particle> wildcard = result.parseAll(TagParser.ANY);
				final Deque<AttributeGroup> attributeGroups = result.parseAll(TagParser.ATTRIBUTE_GROUP);
				final Deferred<Deque<AttributeUse>> attributeUses = AttributeGroup.findAttributeUses(result.parseAll(TagParser.ATTRIBUTE.use()), attributeGroups);
				final Deque<Annotation> annotations = new AnnotationsBuilder(result).add(attributeGroups).build();
				final Wildcard attributeWildcard = result.parse(TagParser.ANY_ATTRIBUTE);
				final Deque<Assert> asserts = result.parseAll(TagParser.COMPLEX_TYPE.asserts());
				return new Restriction(annotations, baseTypeDefinition, facets, wildcard, simpleType, attributeUses, attributeWildcard, asserts);
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

			Deque<Assert> asserts() {
				return asserts;
			}

		}

		private static final SequenceParser parser = new SequenceParser()
				.optionalAttributes(AttrParser.ID)
				.elements(0, 1, TagParser.ANNOTATION)
				.elements(1, 1, TagParser.COMPLEX_TYPE.simpleContent().extension(), TagParser.COMPLEX_TYPE.simpleContent().restriction());

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

		private static SimpleContent parse(final Result result) {
			final Restriction restriction = result.parse(TagParser.COMPLEX_TYPE.simpleContent().restriction());
			final Extension extension = result.parse(TagParser.COMPLEX_TYPE.simpleContent().extension());
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

		Deque<Assert> asserts() {
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
		final Document xsAnyTypeDoc = NodeHelper.newSchemaDocument(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		final ContentType xsAnyTypeContentType = new ContentType(null, Variety.EMPTY, null, null, null);
		final Node xsAnyTypeNode = NodeHelper.newSchemaNode(xsAnyTypeDoc, TagParser.Names.COMPLEX_TYPE, ANYTYPE_NAME);
		xsAnyType = new ComplexType(xsAnyTypeNode, Deques.emptyDeque(), ANYTYPE_NAME, XMLConstants.W3C_XML_SCHEMA_NS_URI, Deques.emptyDeque(), Deferred.none(), Deques::emptyDeque, null, null, false, DerivationMethod.RESTRICTION, () -> xsAnyTypeContentType, Deques.emptyDeque(), Deques.emptyDeque()) {

			@Override
			public TypeDefinition baseTypeDefinition() {
				return this;
			}

		};
	}

	private final Node node;
	private final Deque<Annotation> annotations;
	private final String name;
	private final String targetNamespace;
	private final Deque<Final> finals;
	private final Deferred<? extends TypeDefinition> baseTypeDefinition;
	private final Deferred<Deque<AttributeUse>> attributeUses;
	private final Wildcard attributeWildcard;
	private final Node context;
	private final boolean isAbstract;
	private final DerivationMethod derivationMethod;
	private final Deferred<ContentType> contentType;
	private final Deque<Block> prohibitedSubstitutions;
	private final Deque<Assertion> assertions;

	private ComplexType(final Node node, final Deque<Annotation> annotations, final String name, final String targetNamespace, final Deque<Final> finals, final Deferred<? extends TypeDefinition> baseTypeDefinition, final Deferred<Deque<AttributeUse>> attributeUses, final Wildcard attributeWildcard, final Node context, final boolean isAbstract, final DerivationMethod derivationMethod, final Deferred<ContentType> contentType, final Deque<Block> prohibitedSubstitutions, final Deque<Assertion> assertions) {
		this.node = Objects.requireNonNull(node);
		this.annotations = Objects.requireNonNull(annotations);
		this.name = name;
		this.targetNamespace = NodeHelper.requireNonEmpty(node, targetNamespace);
		this.finals = Objects.requireNonNull(finals);
		this.baseTypeDefinition = Objects.requireNonNull(baseTypeDefinition);
		this.attributeUses = Objects.requireNonNull(attributeUses);
		this.attributeWildcard = attributeWildcard;
		this.context = context;
		this.isAbstract = isAbstract;
		this.derivationMethod = derivationMethod;
		this.contentType = Objects.requireNonNull(contentType);
		this.prohibitedSubstitutions = Objects.requireNonNull(prohibitedSubstitutions);
		this.assertions = Objects.requireNonNull(assertions);
	}

	@SuppressWarnings("unchecked")
	private static ComplexType parse(final Result result) {
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
		final ComplexContent complexContent = result.parse(TagParser.COMPLEX_TYPE.complexContent());
		final SimpleContent simpleContent = result.parse(TagParser.COMPLEX_TYPE.simpleContent());
		final boolean effectiveMixed = complexContent != null && complexContent.mixed() != null
				? complexContent.mixed()
				: mixed != null && mixed;
		final AnnotationsBuilder annotations = new AnnotationsBuilder(result);
		final Deferred<? extends TypeDefinition> baseTypeDefinition;
		final Deque<Assertion> assertions;
		Deferred<Deque<AttributeUse>> attributeUses;
		final Wildcard attributeWildcard;
		final DerivationMethod derivationMethod;
		final Deferred<ContentType> explicitContentType;
		if (simpleContent != null) {
			// https://www.w3.org/TR/xmlschema11-1/#dcl.ctd.ctsc
			annotations.add(simpleContent::annotations);
			assertions = (Deque<Assertion>) (Object) simpleContent.asserts();
			final Deferred<SimpleType> simpleBase = Deferred.of(() -> {
				final TypeDefinition typeDefinition = simpleContent.base();
				if (typeDefinition instanceof ComplexType) {
					final ComplexType complexType = (ComplexType) typeDefinition;
					if (complexType.contentType().variety() == Variety.SIMPLE && DerivationMethod.RESTRICTION.equals(simpleContent.derivationMethod())) { // 1
						if (simpleContent.restriction().simpleType() != null) { // 1.1
							return simpleContent.restriction().simpleType();
						} else { // 1.2
							return SimpleType.wrap(result.node(), Deques.emptyDeque(), null, result.schema().targetNamespace(), Deques.emptyDeque(), complexType.node(), complexType.contentType().simpleTypeDefinition(), simpleContent.restriction().facets());
						}
					} else if (complexType.contentType().variety() == Variety.MIXED && DerivationMethod.RESTRICTION.equals(simpleContent.derivationMethod()) && complexType.contentType().particle().isEmptiable()) { // 2
						final SimpleType sb = simpleContent.restriction().simpleType() != null ? simpleContent.restriction().simpleType() : SimpleType.xsAnySimpleType();
						return SimpleType.wrap(result.node(), Deques.emptyDeque(), null, result.schema().targetNamespace(), Deques.emptyDeque(), complexType.node(), sb, sb.facets());
					} else if (complexType.contentType().variety() == Variety.SIMPLE && DerivationMethod.EXTENSION.equals(simpleContent.derivationMethod())) { // 3
						return complexType.contentType().simpleTypeDefinition();
					}
				} else if (DerivationMethod.EXTENSION.equals(simpleContent.derivationMethod()) && typeDefinition instanceof SimpleType) { // 4
					return (SimpleType) typeDefinition;
				}
				return SimpleType.xsAnySimpleType(); // 5
			});
			baseTypeDefinition = simpleBase;
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
			final Deque<Assert> asserts;
			if (complexContent != null) {
				annotations.add(complexContent::annotations);
				openContent = complexContent.derivation().openContent();
				group = complexContent.derivation().group();
				all = complexContent.derivation().all();
				choice = complexContent.derivation().choice();
				sequence = complexContent.derivation().sequence();
				asserts = complexContent.derivation().asserts();
				baseTypeDefinition = complexContent.derivation()::base;
				attributeUses = complexContent.derivation().attributeUses();
				attributeWildcard = complexContent.derivation().attributeWildcard();
				derivationMethod = complexContent.derivationMethod();
			} else {
				openContent = result.parse(TagParser.COMPLEX_TYPE.openContent());
				group = result.parse(TagParser.GROUP.use());
				all = result.parse(TagParser.ALL);
				choice = result.parse(TagParser.CHOICE);
				sequence = result.parse(TagParser.SEQUENCE);
				asserts = result.parseAll(TagParser.COMPLEX_TYPE.asserts());
				baseTypeDefinition = () -> xsAnyType;
				final Deque<AttributeGroup> attributeGroups = result.parseAll(TagParser.ATTRIBUTE_GROUP);
				annotations.add(attributeGroups);
				attributeUses = AttributeGroup.findAttributeUses(result.parseAll(TagParser.ATTRIBUTE.use()), attributeGroups);
				attributeWildcard = result.parse(TagParser.ANY_ATTRIBUTE);
				derivationMethod = DerivationMethod.RESTRICTION;
			}
			assertions = (Deque<Assertion>) (Object) asserts;
			final Particle particle = group != null ? group : all != null ? all : choice != null ? choice : sequence != null ? sequence : null;
			final Particle explicitContent;
			if (particle == null) {
				explicitContent = null;
			} else {
				if ((all != null && ((ModelGroup) all.term()).particles().isEmpty()) || (sequence != null && ((ModelGroup) sequence.term()).particles().isEmpty())) {
					explicitContent = null;
				} else {
					final boolean choiceMinOccurs0 = choice != null && choice.minOccurs().intValue() == 0 && (choice.term() == null || ((ModelGroup) choice.term()).particles().isEmpty());
					if (choiceMinOccurs0) {
						explicitContent = null;
					} else {
						final Number particleMaxOccurs = group != null ? group.maxOccurs()
								: all != null ? all.maxOccurs()
								: choice != null ? choice.maxOccurs()
								: sequence != null ? sequence.maxOccurs()
								: null;
						explicitContent = particleMaxOccurs.intValue() == 0 ? null : particle;
					}
				}
			}
			final Particle effectiveContent;
			if (explicitContent == null) {
				if (effectiveMixed) {
					effectiveContent = new Particle(result.node(), result.annotations(), 1, 1, ModelGroup.synthetic(result.node(), result.annotations(), Compositor.SEQUENCE, Deques.emptyDeque()));
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
					if (baseTypeDefinition.get() instanceof SimpleType) {
						return handleRestriction.get(); // 4.2.1
					} else {
						final ComplexType complexBase = (ComplexType) baseTypeDefinition.get();
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
								effectiveParticle = new Particle(result.node(), result.annotations(), 1, baseParticle.minOccurs(), ModelGroup.synthetic(result.node(), result.annotations(), Compositor.ALL, particles));
							} else { // 4.2.3.3
								final Deque<Particle> particles = new ArrayDeque<>();
								if (baseParticle != null) {
									particles.add(baseParticle);
								}
								if (effectiveContent != null) {
									particles.add(effectiveContent);
								}
								effectiveParticle = new Particle(result.node(), result.annotations(), 1, 1, ModelGroup.synthetic(result.node(), result.annotations(), Compositor.SEQUENCE, particles));
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
		return new ComplexType(result.node(), annotations.build(), name, targetNamespace, finals, baseTypeDefinition, attributeUses, attributeWildcard, context, isAbstract, derivationMethod, explicitContentType, block, assertions);
	}

	static void register() {
		AttrParser.register(AttrParser.Names.ABSTRACT, false);
		AttrParser.register(AttrParser.Names.BASE, QName.class, NodeHelper::getAttrValueAsQName);
		AttrParser.register(AttrParser.Names.DEFAULT_ATTRIBUTES_APPLY, true);
		AttrParser.register(AttrParser.Names.MIXED, (Boolean) null);
		AttrParser.register(AttrParser.Names.MODE, OpenContent.Mode.class, OpenContent.Mode.INTERLEAVE, OpenContent.Mode::getAttrValueAsMode);
		TagParser.register(TagParser.Names.ASSERT, Assert.parser, Assert.class, Assert::parse);
		TagParser.register(new String[] { TagParser.Names.RESTRICTION, TagParser.Names.EXTENSION }, ComplexContent.Derivation.parser, ComplexContent.Derivation.class, ComplexContent.Derivation::parse);
		TagParser.register(TagParser.Names.COMPLEX_CONTENT, ComplexContent.parser, ComplexContent.class, ComplexContent::parse);
		TagParser.register(TagParser.Names.RESTRICTION, SimpleContent.Restriction.parser, SimpleContent.Restriction.class, SimpleContent.Restriction::parse);
		TagParser.register(TagParser.Names.EXTENSION, SimpleContent.Extension.parser, SimpleContent.Extension.class, SimpleContent.Extension::parse);
		TagParser.register(TagParser.Names.SIMPLE_CONTENT, SimpleContent.parser, SimpleContent.class, SimpleContent::parse);
		TagParser.register(TagParser.Names.OPEN_CONTENT, OpenContent.parser, OpenContent.class, OpenContent::parse);
		TagParser.register(TagParser.Names.COMPLEX_TYPE, ComplexType.parser, ComplexType.class, ComplexType::parse);
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
	public TypeDefinition baseTypeDefinition() {
		return baseTypeDefinition.get();
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