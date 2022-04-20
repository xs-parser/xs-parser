package xs.parser;

import java.util.*;
import java.util.function.*;
import javax.xml.*;
import org.w3c.dom.*;
import xs.parser.Annotation.*;
import xs.parser.ModelGroup.*;
import xs.parser.Schema.*;
import xs.parser.internal.*;
import xs.parser.internal.SequenceParser.*;

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

	public enum Variety {

		EMPTY,
		SIMPLE,
		ELEMENT_ONLY,
		MIXED;

		private boolean isMixedOrElementOnly() {
			return EMPTY.equals(this) || ELEMENT_ONLY.equals(this);
		}

	}

	public enum DerivationMethod {

		EXTENSION,
		RESTRICTION;

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
		private final Particle<Term> particle;
		private final OpenContent openContent;
		private final SimpleType simpleType;

		private ContentType(final DefaultOpenContent defaultOpenContent, final Variety variety, final Particle<Term> particle, final OpenContent openContent, final SimpleType simpleType) {
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

		public Particle<Term> particle() {
			return particle;
		}

		public OpenContent openContent() {
			return openContent;
		}

		public SimpleType simpleType() {
			return simpleType;
		}

	}

	private static final ComplexType xsAnyType;
	static final String ANYTYPE_NAME = "anyType";
	protected static final SequenceParser parser = new SequenceParser()
			.optionalAttributes(AttributeValue.ID, AttributeValue.ABSTRACT, AttributeValue.BLOCK, AttributeValue.FINAL, AttributeValue.MIXED, AttributeValue.NAME, AttributeValue.DEFAULTATTRIBUTESAPPLY)
			.elements(0, 1, ElementValue.ANNOTATION)
			.elements(0, 1, ElementValue.COMPLEXCONTENT, ElementValue.SIMPLECONTENT, ElementValue.OPENCONTENT)
			.elements(0, 1, ElementValue.GROUP, ElementValue.ALL, ElementValue.CHOICE, ElementValue.SEQUENCE)
			.elements(0, Integer.MAX_VALUE, ElementValue.ATTRIBUTE, ElementValue.ATTRIBUTEGROUP)
			.elements(0, 1, ElementValue.ANYATTRIBUTE)
			.elements(0, Integer.MAX_VALUE, ElementValue.ASSERT);

	static {
		final Document xsAnyTypeDoc = NodeHelper.newDocument();
		final Node xsAnyTypeNode = NodeHelper.newNode(xsAnyTypeDoc, ElementValue.COMPLEXTYPE, "xs", XMLConstants.W3C_XML_SCHEMA_NS_URI, ANYTYPE_NAME);
		xsAnyType = new ComplexType(xsAnyTypeNode, Deques.emptyDeque(), ANYTYPE_NAME, XMLConstants.W3C_XML_SCHEMA_NS_URI, Deques.emptyDeque(), Deferred.none(), Deferred.value(Deques.emptyDeque()), null, null, false, DerivationMethod.RESTRICTION, Deferred.value(new ContentType(null, Variety.EMPTY, null, null, null)), Deques.emptyDeque(), Deques.emptyDeque()) {
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

	@SuppressWarnings("unchecked")
	protected static ComplexType parse(final Result result) {
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
				: mixed != null ? mixed : false;
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
			final Particle<ModelGroup> group;
			final Particle<ModelGroup> all;
			final Particle<ModelGroup> choice;
			final Particle<ModelGroup> sequence;
			if (complexContent != null) {
				annotations.add(complexContent::annotations);
				openContent = complexContent.derivation().openContent();
				group = complexContent.derivation().group();
				all = complexContent.derivation().all();
				choice = complexContent.derivation().choice();
				sequence = complexContent.derivation().sequence();
				assertions = complexContent.derivation().asserts();
				baseType = Deferred.of(complexContent.derivation()::base);
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
				baseType = Deferred.value(xsAnyType);
				final Deque<AttributeGroup> attributeGroups = result.parseAll(ElementValue.ATTRIBUTEGROUP);
				annotations.add(attributeGroups);
				attributeUses = AttributeGroup.findAttributeUses(result.parseAll(ElementValue.ATTRIBUTE), attributeGroups);
				attributeWildcard = result.parse(ElementValue.ANYATTRIBUTE);
				derivationMethod = DerivationMethod.RESTRICTION;
			}
			final Particle<ModelGroup> particle = group != null ? group : all != null ? all : choice != null ? choice : sequence != null ? sequence : null;
			final Particle<?> explicitContent;
			if (particle == null) {
				explicitContent = null;
			} else {
				if ((all != null && all.term().particles().isEmpty()) || (sequence != null && sequence.term().particles().isEmpty())) {
					explicitContent = null;
				} else {
					final boolean choiceMinOccurs0 = choice != null && "0".equals(choice.minOccurs()) && (choice.term() == null || choice.term().particles().isEmpty());
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
			final Particle<?> effectiveContent;
			if (explicitContent == null) {
				if (effectiveMixed) {
					effectiveContent = new Particle<>(result.node(), result.annotations(), AttributeValue.MAXOCCURS.defaultValue(), AttributeValue.MINOCCURS.defaultValue(), ModelGroup.synthetic(result.node(), result.annotations(), Compositor.SEQUENCE, Deques.emptyDeque()));
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
						return new ContentType(result.schema().defaultOpenContent(), effectiveMixed ? Variety.MIXED : Variety.ELEMENT_ONLY, (Particle<Term>) effectiveContent, openContent, null);
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
							final Particle<Term> baseParticle = complexBase.contentType().particle();
							final Particle<Term> effectiveParticle;
							if (baseParticle != null && baseParticle.term() instanceof ModelGroup && Compositor.ALL.equals(((ModelGroup) baseParticle.term()).compositor()) && effectiveContent == null) { // 4.2.3.1
								effectiveParticle = baseParticle;
							} else if (baseParticle != null && baseParticle.term() instanceof ModelGroup && Compositor.ALL.equals(((ModelGroup) baseParticle.term()).compositor()) && effectiveContent != null && effectiveContent.term() instanceof ModelGroup && Compositor.ALL.equals(((ModelGroup) effectiveContent.term()).compositor())) { // 4.2.3.2
								final Deque<Particle<Term>> particles = new ArrayDeque<>(((ModelGroup) baseParticle.term()).particles());
								particles.addAll(((ModelGroup) effectiveContent.term()).particles());
								effectiveParticle = new Particle<>(result.node(), result.annotations(), "1", baseParticle.minOccurs(), ModelGroup.synthetic(result.node(), result.annotations(), Compositor.ALL, particles));
							} else { // 4.2.3.3
								final Deque<Particle<Term>> particles = new ArrayDeque<>();
								if (baseParticle != null) {
									particles.add(baseParticle);
								}
								if (effectiveContent != null) {
									particles.add((Particle<Term>) effectiveContent);
								}
								effectiveParticle = new Particle<>(result.node(), result.annotations(), "1", "1", ModelGroup.synthetic(result.node(), result.annotations(), Compositor.SEQUENCE, particles));
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