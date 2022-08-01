package xs.parser;

import java.util.*;
import javax.xml.namespace.*;
import org.w3c.dom.*;
import xs.parser.Attribute.*;
import xs.parser.ComplexType.*;
import xs.parser.Schema.*;
import xs.parser.TypeDefinition.*;
import xs.parser.internal.*;
import xs.parser.internal.util.*;
import xs.parser.internal.util.SequenceParser.*;

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
 *       <td>{@link Element#typeDefinition()}</td>
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
 *       <td>{@link Element#identityConstraintDefinitions()}</td>
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
	 *       <td>{@link TypeTable#defaultTypeDefinition()}</td>
	 *       <td>{default type definition}</td>
	 *       <td>A Type Alternative component. Required.</td>
	 *     </tr>
	 *   </tbody>
	 * </table>
	 */
	public class TypeTable {

		private final Deque<Alternative> alternatives;
		private final Alternative defaultTypeDefinition;

		private TypeTable(final Deque<Alternative> alternatives) {
			if (alternatives.isEmpty()) {
				throw new IllegalArgumentException("TypeTable must have at least one alternative");
			}
			this.alternatives = Objects.requireNonNull(alternatives);
			final Alternative last = alternatives.getLast();
			this.defaultTypeDefinition = last.test() == null
					? last
					: new Alternative(node, Deques.emptyDeque(), null, typeDefinition);
		}

		public Deque<Alternative> alternatives() {
			return alternatives;
		}

		public Alternative defaultTypeDefinition() {
			return defaultTypeDefinition;
		}

	}

	private static final Deque<Block> ALLOWED_BLOCK = Deques.asDeque(Block.EXTENSION, Block.RESTRICTION, Block.SUBSTITUTION);
	private static final Deque<Final> ALLOWED_FINAL = Deques.asDeque(Final.EXTENSION, Final.RESTRICTION);
	private static final SequenceParser parser = new SequenceParser()
			.optionalAttributes(AttrParser.ID, AttrParser.ABSTRACT, AttrParser.BLOCK, AttrParser.DEFAULT, AttrParser.FINAL, AttrParser.FIXED, AttrParser.FORM, AttrParser.MAX_OCCURS, AttrParser.MIN_OCCURS, AttrParser.NAME, AttrParser.NILLABLE, AttrParser.REF, AttrParser.SUBSTITUTION_GROUP, AttrParser.TARGET_NAMESPACE, AttrParser.TYPE)
			.elements(0, 1, TagParser.ANNOTATION)
			.elements(0, 1, TagParser.COMPLEX_TYPE, TagParser.SIMPLE_TYPE)
			.elements(0, Integer.MAX_VALUE, TagParser.ALTERNATIVE)
			.elements(0, Integer.MAX_VALUE, TagParser.KEY, TagParser.KEYREF, TagParser.UNIQUE);

	private final Node node;
	private final Deque<Annotation> annotations;
	private final String name;
	private final String targetNamespace;
	private final Deferred<? extends TypeDefinition> typeDefinition;
	private final TypeTable typeTable;
	private final Scope scope;
	private final boolean nillable;
	private final Deferred<ValueConstraint> valueConstraint;
	private final Deque<IdentityConstraint> identityConstraintDefinitions;
	private final Deque<Element> substitutionGroupAffiliations;
	private final Set<Block> disallowedSubstitutions;
	private final Set<Final> substitutionGroupExclusions;
	private final boolean isAbstract;

	private Element(final Node node, final Deque<Annotation> annotations, final String name, final String targetNamespace, final Deferred<? extends TypeDefinition> typeDefinition, final Deque<Alternative> alternatives, final Scope scope, final boolean nillable, final Deferred<ValueConstraint> valueConstraint, final Deque<IdentityConstraint> identityConstraintDefinitions, final Deque<Element> substitutionGroupAffiliations, final Set<Block> disallowedSubstitutions, final Set<Final> substitutionGroupExclusions, final boolean isAbstract) {
		this.node = Objects.requireNonNull(node);
		this.annotations = Objects.requireNonNull(annotations);
		this.name = name;
		this.targetNamespace = NodeHelper.requireNonEmpty(node, targetNamespace);
		this.typeDefinition = Objects.requireNonNull(typeDefinition);
		this.typeTable = alternatives.isEmpty() ? null : new TypeTable(alternatives);
		this.scope = scope;
		this.nillable = nillable;
		this.valueConstraint = valueConstraint;
		this.identityConstraintDefinitions = Objects.requireNonNull(identityConstraintDefinitions);
		this.substitutionGroupAffiliations = Objects.requireNonNull(substitutionGroupAffiliations);
		this.disallowedSubstitutions = Objects.requireNonNull(disallowedSubstitutions);
		this.substitutionGroupExclusions = Objects.requireNonNull(substitutionGroupExclusions);
		this.isAbstract = isAbstract;
	}

	private static Element parseDecl(final Result result) {
		final Node node = result.node();
		final Deque<Annotation> annotations = Annotation.of(result).resolve(node);
		final String defaultValue = result.value(AttrParser.DEFAULT);
		final String fixedValue = result.value(AttrParser.FIXED);
		Deque<Block> effectiveBlockValue = result.value(AttrParser.BLOCK);
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
		Deque<Final> effectiveFinalValue = result.value(AttrParser.FINAL);
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
		final boolean nillable = result.value(AttrParser.NILLABLE);
		final boolean isAbstract = result.value(AttrParser.ABSTRACT);
		final Deque<Alternative> alternatives = result.parseAll(TagParser.ALTERNATIVE);
		final Deque<IdentityConstraint> identityConstraints = result.parseAll(TagParser.KEY, TagParser.KEYREF, TagParser.UNIQUE);
		final Form form = result.value(AttrParser.FORM);
		final boolean isGlobal = NodeHelper.isParentSchemaElement(result);
		final Scope scope;
		String targetNamespace = result.value(AttrParser.TARGET_NAMESPACE);
		if (isGlobal) {
			if (form != null) {
				throw new Schema.ParseException(node, "'form' attribute is only allowed for local element declarations");
			} else if (targetNamespace != null) {
				throw new Schema.ParseException(node, "'targetNamespace' attribute is only allowed for local element declarations");
			}
			targetNamespace = result.schema().targetNamespace();
			scope = new Scope(Scope.Variety.GLOBAL, null);
		} else {
			if (targetNamespace == null && (Form.QUALIFIED.equals(form) || (form == null && Form.QUALIFIED.equals(result.schema().elementFormDefault())))) { // 3.3.2.3
				targetNamespace = result.schema().targetNamespace();
			}
			Node n = result.parent().node();
			while (!TagParser.COMPLEX_TYPE.equalsName(n) && !TagParser.GROUP.equalsName(n)) {
				if ((n = n.getParentNode()) == null) {
					throw new ParseException(result.node(), TagParser.ELEMENT.getName() + " must be a descendent of " + TagParser.COMPLEX_TYPE.getName() + " or " + TagParser.GROUP.getName());
				}
			}
			scope = new Scope(Scope.Variety.LOCAL, n);
		}
		Deque<QName> substitutionGroup = result.value(AttrParser.SUBSTITUTION_GROUP);
		if (substitutionGroup != null && !isGlobal) {
			throw new ParseException(result.node(), "@substitutionGroup is only valid for global elements");
		} else if (substitutionGroup == null) {
			substitutionGroup = Deques.emptyDeque();
		}
		final DeferredArrayDeque<Element> substitutionGroupAffiliations = new DeferredArrayDeque<>();
		final Deferred<? extends TypeDefinition> typeDefinition;
		final QName typeName = result.value(AttrParser.TYPE);
		if (typeName != null) {
			typeDefinition = result.schema().find(typeName, TypeDefinition.class);
		} else {
			final Deferred<TypeDefinition> type = result.parse(TagParser.COMPLEX_TYPE, TagParser.SIMPLE_TYPE);
			typeDefinition = type != null
					? type
					: substitutionGroup.isEmpty()
							? ComplexType::xsAnyType
							: () -> substitutionGroupAffiliations.getFirst().typeDefinition.get();
		}
		final Deferred<ValueConstraint> valueConstraint;
		if (defaultValue != null || fixedValue != null) {
			valueConstraint = typeDefinition.map(t -> {
				final Deferred<SimpleType> effectiveSimpleType;
				if (t instanceof SimpleType) {
					effectiveSimpleType = () -> (SimpleType) t;
				} else {
					final ContentType contentType = ((ComplexType) t).contentType();
					effectiveSimpleType = ComplexType.Variety.SIMPLE.equals(contentType.variety())
							? contentType::simpleTypeDefinition
							: SimpleType::xsString;
				}
				return defaultValue != null
						? new ValueConstraint(result.schema(), effectiveSimpleType, ValueConstraint.Variety.DEFAULT, defaultValue)
						: fixedValue != null
								? new ValueConstraint(result.schema(), effectiveSimpleType, ValueConstraint.Variety.FIXED, fixedValue)
								: null;
			});
		} else {
			valueConstraint = null;
		}
		substitutionGroup.forEach(s -> substitutionGroupAffiliations.addLast(result.schema().find(s, Element.class)));
		final String name = result.value(AttrParser.NAME);
		return new Element(node, annotations, name, targetNamespace, typeDefinition, alternatives, scope, nillable, valueConstraint, identityConstraints, substitutionGroupAffiliations, disallowedSubstitutions, substitutionGroupExclusions, isAbstract);
	}

	private static Particle parse(final Result result) {
		final Node node = result.node();
		final Deque<Annotation> annotations = Annotation.of(result).resolve(node);
		final Number maxOccurs = result.value(AttrParser.MAX_OCCURS);
		final Number minOccurs = result.value(AttrParser.MIN_OCCURS);
		final QName refName = result.value(AttrParser.REF);
		final Deferred<Element> decl;
		if (refName != null) {
			decl = result.schema().find(refName, Element.class);
		} else {
			final Element elem = parseDecl(result);
			decl = () -> elem;
		}
		return new Particle(node, annotations, maxOccurs, minOccurs, decl);
	}

	static void register() {
		AttrParser.register(AttrParser.Names.NAME, NodeHelper::getAttrValueAsNCName);
		AttrParser.register(AttrParser.Names.NILLABLE, false);
		AttrParser.register(AttrParser.Names.REF, QName.class, NodeHelper::getAttrValueAsQName);
		AttrParser.register(AttrParser.Names.SUBSTITUTION_GROUP, Deque.class, QName.class, null, NodeHelper::getAttrValueAsQNames);
		AttrParser.register(AttrParser.Names.TYPE, QName.class, NodeHelper::getAttrValueAsQName);
		TagParser.register(TagParser.Names.ELEMENT, parser, Element.class, Element::parseDecl);
		TagParser.register(TagParser.Names.ELEMENT, parser, Particle.class, Element::parse);
	}

	public String name() {
		return name;
	}

	public String targetNamespace() {
		return targetNamespace;
	}

	public TypeDefinition typeDefinition() {
		return typeDefinition.get();
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
		return valueConstraint != null ? valueConstraint.get() : null;
	}

	public Deque<IdentityConstraint> identityConstraintDefinitions() {
		return Deques.unmodifiableDeque(identityConstraintDefinitions);
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
