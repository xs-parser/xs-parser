package xs.parser;

import java.util.*;
import javax.xml.namespace.*;
import org.w3c.dom.*;
import xs.parser.Attribute.*;
import xs.parser.ComplexType.*;
import xs.parser.Schema.*;
import xs.parser.TypeDefinition.*;
import xs.parser.internal.*;
import xs.parser.internal.SequenceParser.*;

/**
 * <pre>
 * &lt;element
 *   abstract = boolean : false
 *   block = (#all | List of (extension | restriction | substitution))
 *   default = string
 *   final = (#all | List of (extension | restriction))
 *   fixed = string
 *   form = (qualified | unqualified)
 *   id = ID
 *   maxOccurs = (nonNegativeInteger | unbounded)  : 1
 *   minOccurs = nonNegativeInteger : 1
 *   name = NCName
 *   nillable = boolean : false
 *   ref = QName
 *   substitutionGroup = List of QName
 *   targetNamespace = anyURI
 *   type = QName
 *   {any attributes with non-schema namespace . . .}&gt;
 *   Content: (annotation?, ((simpleType | complexType)?, alternative*, (unique | key | keyref)*))
 * &lt;/element&gt;
 * </pre>
 *
 * <table>
 *   <caption style="font-size: large; text-align: left">Schema Component: Element Declaration, a kind of Term</caption>
 *   <thead>
 *     <tr>
 *       <th style="text-align: left">Method</th>
 *       <th style="text-align: left">Property</th>
 *       <th style="text-align: left">Representation</th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <td>{@link Element#annotations()}</td>
 *       <td>{annotations}</td>
 *       <td>A sequence of Annotation components.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link Element#name()}</td>
 *       <td>{name}</td>
 *       <td>An xs:NCName value. Required.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link Element#targetNamespace()}</td>
 *       <td>{target namespace}</td>
 *       <td>An xs:anyURI value. Optional.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link Element#type()}</td>
 *       <td>{type definition}</td>
 *       <td>A Type Definition component. Required.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link Element#typeTable()}</td>
 *       <td>{type table}</td>
 *       <td>A Type Table property record. Optional.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link Element#scope()}</td>
 *       <td>{scope}</td>
 *       <td>A Scope property record. Required.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link Element#valueConstraint()}</td>
 *       <td>{value constraint}</td>
 *       <td>A Value Constraint property record. Optional.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link Element#nillable()}</td>
 *       <td>{nillable}</td>
 *       <td>An xs:boolean value. Required.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link Element#identityConstraints()}</td>
 *       <td>{identity-constraint definitions}</td>
 *       <td>A set of Identity-Constraint Definition components.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link Element#substitutionGroupAffiliations()}</td>
 *       <td>{substitution group affiliations}</td>
 *       <td>A set of Element Declaration components.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link Element#substitutionGroupExclusions()}</td>
 *       <td>{substitution group exclusions}</td>
 *       <td>A subset of {extension, restriction}.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link Element#disallowedSubstitutions()}</td>
 *       <td>{disallowed substitutions}</td>
 *       <td>A subset of {substitution, extension, restriction}.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link Element#isAbstract()}</td>
 *       <td>{abstract}</td>
 *       <td>An xs:boolean value. Required.</td>
 *     </tr>
 *   </tbody>
 * </table>
 */
public class Element implements Term {

	/**
	 * <table>
	 *   <caption style="font-size: large; text-align: left">Property Record: Type Table</caption>
	 *   <thead>
	 *     <tr>
	 *       <th style="text-align: left">Method</th>
	 *       <th style="text-align: left">Property</th>
	 *       <th style="text-align: left">Representation</th>
	 *     </tr>
	 *   </thead>
	 *   <tbody>
	 *     <tr>
	 *       <td>{@link TypeTable#alternatives()}</td>
	 *       <td>{alternatives}</td>
	 *       <td>A sequence of Type Alternative components.</td>
	 *     </tr>
	 *     <tr>
	 *       <td>{@link TypeTable#defaultType()}</td>
	 *       <td>{default type definition}</td>
	 *       <td>A Type Alternative component. Required.</td>
	 *     </tr>
	 *   </tbody>
	 * </table>
	 */
	public static class TypeTable {

		private final Deque<Alternative> alternatives;
		private final Alternative defaultType;

		private TypeTable(final Deque<Alternative> alternatives, final Element parent) {
			if (alternatives.isEmpty()) {
				throw new IllegalArgumentException("TypeTable must have at least one alternative");
			}
			this.alternatives = Objects.requireNonNull(alternatives);
			final Alternative last = alternatives.getFirst();
			this.defaultType = last.test() == null
					? last
					: new Alternative(parent.node(), Deques.emptyDeque(), null, Deferred.of(last::type));
		}

		public Deque<Alternative> alternatives() {
			return alternatives;
		}

		public Alternative defaultType() {
			return defaultType;
		}

	}

	protected static final SequenceParser parser = new SequenceParser()
			.optionalAttributes(AttributeValue.ID, AttributeValue.ABSTRACT, AttributeValue.BLOCK, AttributeValue.DEFAULT, AttributeValue.FINAL, AttributeValue.FIXED, AttributeValue.FORM, AttributeValue.MAXOCCURS, AttributeValue.MINOCCURS, AttributeValue.NAME, AttributeValue.NILLABLE, AttributeValue.REF, AttributeValue.SUBSTITUTIONGROUP, AttributeValue.TARGETNAMESPACE, AttributeValue.TYPE)
			.elements(0, 1, ElementValue.ANNOTATION)
			.elements(0, 1, ElementValue.COMPLEXTYPE, ElementValue.SIMPLETYPE)
			.elements(0, Integer.MAX_VALUE, ElementValue.ALTERNATIVE)
			.elements(0, Integer.MAX_VALUE, ElementValue.KEY, ElementValue.KEYREF, ElementValue.UNIQUE);
	private static final Deque<Block> ALLOWED_BLOCK = Deques.asDeque(Block.EXTENSION, Block.RESTRICTION, Block.SUBSTITUTION);
	private static final Deque<Final> ALLOWED_FINAL = Deques.asDeque(Final.EXTENSION, Final.RESTRICTION);

	private final Node node;
	private final Deque<Annotation> annotations;
	private final String name;
	private final String targetNamespace;
	private final Deferred<? extends TypeDefinition> type;
	private final TypeTable typeTable;
	private final Scope scope;
	private final boolean nillable;
	private final Deferred<ValueConstraint> valueConstraint;
	private final Deque<IdentityConstraint> identityConstraints;
	private final Deque<Element> substitutionGroupAffiliations;
	private final Set<Block> disallowedSubstitutions;
	private final Set<Final> substitutionGroupExclusions;
	private final boolean isAbstract;

	private Element(final Node node, final Deque<Annotation> annotations, final String name, final String targetNamespace, final Deferred<? extends TypeDefinition> type, final Deque<Alternative> alternatives, final Scope scope, final boolean nillable, final Deferred<ValueConstraint> valueConstraint, final Deque<IdentityConstraint> identityConstraints, final Deque<Element> substitutionGroupAffiliations, final Set<Block> disallowedSubstitutions, final Set<Final> substitutionGroupExclusions, final boolean isAbstract) {
		this.node = Objects.requireNonNull(node);
		this.annotations = Objects.requireNonNull(annotations);
		this.name = name;
		this.targetNamespace = NodeHelper.validateTargetNamespace(targetNamespace);
		this.type = Objects.requireNonNull(type);
		this.typeTable = alternatives.isEmpty() ? null : new TypeTable(alternatives, this);
		this.scope = scope;
		this.nillable = nillable;
		this.valueConstraint = Objects.requireNonNull(valueConstraint);
		this.identityConstraints = Objects.requireNonNull(identityConstraints);
		this.substitutionGroupAffiliations = Objects.requireNonNull(substitutionGroupAffiliations);
		this.disallowedSubstitutions = Objects.requireNonNull(disallowedSubstitutions);
		this.substitutionGroupExclusions = Objects.requireNonNull(substitutionGroupExclusions);
		this.isAbstract = isAbstract;
	}

	protected static Element parseDecl(final Result result) {
		final String defaultValue = result.value(AttributeValue.DEFAULT);
		final String fixedValue = result.value(AttributeValue.FIXED);
		Deque<Block> effectiveBlockValue = result.value(AttributeValue.BLOCK);
		if (effectiveBlockValue.isEmpty()) {
			effectiveBlockValue = Deques.singletonDeque(result.schema().blockDefault());
		}
		final Set<Block> disallowedSubstitutions = new HashSet<>();
		for (final Block b : effectiveBlockValue) {
			if (Block.ALL.equals(b)) {
				disallowedSubstitutions.addAll(ALLOWED_BLOCK);
				break;
			} else if (ALLOWED_BLOCK.contains(b)) {
				disallowedSubstitutions.add(b);
			}
		}
		Deque<Final> effectiveFinalValue = result.value(AttributeValue.FINAL);
		if (effectiveFinalValue.isEmpty()) {
			effectiveFinalValue = Deques.singletonDeque(result.schema().finalDefault());
		}
		final Set<Final> substitutionGroupExclusions = new HashSet<>();
		for (final Final f : effectiveFinalValue) {
			if (Final.ALL.equals(f)) {
				substitutionGroupExclusions.addAll(ALLOWED_FINAL);
				break;
			} else if (ALLOWED_FINAL.contains(f)) {
				substitutionGroupExclusions.add(f);
			}
		}
		final boolean nillable = result.value(AttributeValue.NILLABLE);
		final boolean isAbstract = result.value(AttributeValue.ABSTRACT);
		final Deque<Alternative> alternatives = result.parseAll(ElementValue.ALTERNATIVE);
		final Deque<IdentityConstraint> identityConstraints = result.parseAll(ElementValue.KEY, ElementValue.KEYREF, ElementValue.UNIQUE);
		final Form form = result.value(AttributeValue.FORM);
		final boolean isGlobal = NodeHelper.isParentSchemaElement(result);
		final Scope scope;
		String targetNamespace;
		if (isGlobal) {
			targetNamespace = result.schema().targetNamespace();
			scope = new Scope(Scope.Variety.GLOBAL, null);
		} else {
			targetNamespace = result.value(AttributeValue.TARGETNAMESPACE);
			if (targetNamespace == null && (Form.QUALIFIED.equals(form) || (form == null && Form.QUALIFIED.equals(result.schema().elementFormDefault())))) { // 3.3.2.3
				targetNamespace = result.schema().targetNamespace();
			}
			Node n = result.parent().node();
			while (!ElementValue.COMPLEXTYPE.equalsName(n) && !ElementValue.GROUP.equalsName(n)) {
				if ((n = n.getParentNode()) == null) {
					throw new SchemaParseException(result.node(), ElementValue.ELEMENT.getName() + " must be a descendent of " + ElementValue.COMPLEXTYPE.getName() + " or " + ElementValue.GROUP.getName());
				}
			}
			scope = new Scope(Scope.Variety.LOCAL, n);
		}
		Deque<QName> substitutionGroup = result.value(AttributeValue.SUBSTITUTIONGROUP);
		if (substitutionGroup != null && !isGlobal) {
			throw new SchemaParseException(result.node(), "@substitutionGroup is only valid for global elements");
		} else if (substitutionGroup == null) {
			substitutionGroup = Deques.emptyDeque();
		}
		final DeferredArrayDeque<Element> substitutionGroupAffiliations = new DeferredArrayDeque<>(substitutionGroup.size());
		final Deferred<? extends TypeDefinition> type;
		final QName typeName = result.value(AttributeValue.TYPE);
		if (typeName != null) {
			type = result.schema().find(typeName, TypeDefinition.class);
		} else {
			final SimpleType simpleType = result.parse(ElementValue.SIMPLETYPE);
			if (simpleType != null) {
				type = Deferred.value(simpleType);
			} else {
				final ComplexType complexType = result.parse(ElementValue.COMPLEXTYPE);
				type = complexType != null ? Deferred.value(complexType)
						: !substitutionGroupAffiliations.isEmpty() ? substitutionGroupAffiliations.getFirst().type
						: Deferred.value(ComplexType.xsAnyType());
			}
		}
		final Deferred<ValueConstraint> valueConstraint;
		if (defaultValue != null || fixedValue != null) {
			valueConstraint = type.map(t -> {
				final Deferred<SimpleType> effectiveSimpleType;
				final ContentType c;
				if (t instanceof SimpleType) {
					effectiveSimpleType = Deferred.value((SimpleType) t);
				} else if (ComplexType.Variety.SIMPLE.equals((c = ((ComplexType) t).contentType()).variety())) {
					effectiveSimpleType = Deferred.value(c.simpleType());
				} else {
					effectiveSimpleType = Deferred.value(SimpleType.xsString());
				}
				if (defaultValue != null) {
					return new ValueConstraint(effectiveSimpleType, ValueConstraint.Variety.DEFAULT, defaultValue);
				} else {
					return new ValueConstraint(effectiveSimpleType, ValueConstraint.Variety.FIXED, fixedValue);
				}
			});
		} else {
			valueConstraint = Deferred.none();
		}
		substitutionGroup.forEach(s -> substitutionGroupAffiliations.add(result.schema().find(s, Element.class))); // TODO (???)
		final String name = result.value(AttributeValue.NAME);
		return new Element(result.node(), result.annotations(), name, targetNamespace, type, alternatives, scope, nillable, valueConstraint, identityConstraints, substitutionGroupAffiliations, disallowedSubstitutions, substitutionGroupExclusions, isAbstract);
	}

	protected static Particle<Element> parse(final Result result) {
		final String maxOccurs = result.value(AttributeValue.MAXOCCURS);
		final String minOccurs = result.value(AttributeValue.MINOCCURS);
		final QName refName = result.value(AttributeValue.REF);
		final Deferred<Element> decl = refName != null
				? result.schema().find(refName, Element.class)
				: Deferred.value(parseDecl(result));
		return new Particle<>(result.node(), result.annotations(), maxOccurs, minOccurs, decl);
	}

	public String name() {
		return name;
	}

	public String targetNamespace() {
		return targetNamespace;
	}

	public TypeDefinition type() {
		return type.get();
	}

	public TypeTable typeTable() {
		return typeTable;
	}

	public Scope scope() {
		return scope;
	}

	public boolean nillable() {
		return nillable;
	}

	public ValueConstraint valueConstraint() {
		return valueConstraint.get();
	}

	public Deque<IdentityConstraint> identityConstraints() {
		return Deques.unmodifiableDeque(identityConstraints);
	}

	public Deque<Element> substitutionGroupAffiliations() {
		return Deques.unmodifiableDeque(substitutionGroupAffiliations);
	}

	public Set<Block> disallowedSubstitutions() {
		return Collections.unmodifiableSet(disallowedSubstitutions);
	}

	public Set<Final> substitutionGroupExclusions() {
		return Collections.unmodifiableSet(substitutionGroupExclusions);
	}

	public boolean isAbstract() {
		return isAbstract;
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