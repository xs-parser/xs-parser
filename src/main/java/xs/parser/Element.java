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
import xs.parser.v.*;

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
	 *   <caption style="font-size: large; text-align: left">Property Record: Scope</caption>
	 *   <thead>
	 *     <tr>
	 *       <th style="text-align: left">Method</th>
	 *       <th style="text-align: left">Property</th>
	 *       <th style="text-align: left">Representation</th>
	 *     </tr>
	 *   </thead>
	 *   <tbody>
	 *     <tr>
	 *       <td>{@link Scope#variety()}</td>
	 *       <td>{variety}</td>
	 *       <td>One of {global, local}. Required.</td>
	 *     </tr>
	 *     <tr>
	 *       <td>{@link Scope#parent()}</td>
	 *       <td>{parent}</td>
	 *       <td>Either a Complex Type Definition or a Model Group Definition. Required if {variety} is local, otherwise must be ·absent·</td>
	 *     </tr>
	 *   </tbody>
	 * </table>
	 */
	public static class Scope {

		public enum Variety {

			GLOBAL,
			LOCAL;

		}

		private final Variety variety;
		private final AnnotatedComponent parent;

		Scope(final Variety variety, final AnnotatedComponent parent, final Node node) {
			if ((Variety.LOCAL.equals(variety) && parent == null) || (Variety.GLOBAL.equals(variety) && parent != null)) {
				throw new IllegalArgumentException(variety.toString());
			}
			this.variety = Objects.requireNonNull(variety);
			Objects.requireNonNull(node);
			if (Variety.GLOBAL.equals(variety)) {
				this.parent = null;
			} else if (parent instanceof ComplexType || parent instanceof ModelGroup) {
				this.parent = parent;
			} else if (parent instanceof Particle && ((Particle) parent).term() instanceof ModelGroup) {
				this.parent = ((Particle) parent).term();
			} else {
				throw new ParseException(node, TagParser.ELEMENT.getName() + " must be a descendent of " + TagParser.COMPLEX_TYPE.getName() + " or " + TagParser.GROUP.getName());
			}
		}

		/** @return either local or global, as appropriate */
		public Variety variety() {
			return variety;
		}

		/** @return If the &lt;element&gt; element information item has &lt;complexType&gt; as an ancestor, the Complex Type Definition corresponding to that item, otherwise (the &lt;element&gt; element information item is within a named &lt;group&gt; element information item), the Model Group Definition corresponding to that item. */
		public AnnotatedComponent parent() {
			return parent;
		}

	}

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
					: new Alternative(() -> Element.this, node, Deques.emptyDeque(), null, typeDefinition);
		}

		/** @return A sequence of Type Alternatives, each corresponding, in order, to one of the &lt;alternative&gt; elements which have a test [attribute]. */
		public Deque<Alternative> alternatives() {
			return alternatives;
		}

		/** @return Depends upon the final &lt;alternative&gt; element among the [children]. If it has no test [attribute], the final &lt;alternative&gt; maps to the {default type definition}; if it does have a test attribute, it is covered by the rule for {alternatives} and the {default type definition} is taken from the declared type of the Element Declaration. So the value of the {default type definition} is given by the appropriate case among the following:
		 * <br>1 If the &lt;alternative&gt; has no test [attribute], then a Type Alternative corresponding to the &lt;alternative&gt;.
		 * <br>2 otherwise (the &lt;alternative&gt; has a test) a Type Alternative with the following properties:
		 * <table>
		 *   <caption style="font-size: large; text-align: left">Type Alternative</caption>
		 *   <thead>
		 *     <tr>
		 *       <th style="text-align: left">Property</th>
		 *       <th style="text-align: left">Value</th>
		 *     </tr>
		 *   </thead>
		 *   <tbody>
		 *     <tr>
		 *       <td>{test}</td>
		 *       <td>·absent·</td>
		 *     </tr>
		 *     <tr>
		 *       <td>{type definition}</td>
		 *       <td>the {type definition} property of the parent Element Declaration.</td>
		 *     </tr>
		 *     <tr>
		 *       <td>{annotations}</td>
		 *       <td>the empty sequence.</td>
		 *     </tr>
		 *   </tbody>
		 * </table> */
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

	private final Deferred<? extends AnnotatedComponent> context;
	private final Node node;
	private final Deque<Annotation> annotations;
	private final String name;
	private final Deferred<String> targetNamespace;
	private final Deferred<? extends TypeDefinition> typeDefinition;
	private final TypeTable typeTable;
	private final Deferred<Scope> scope;
	private final boolean nillable;
	private final Deferred<ValueConstraint> valueConstraint;
	private final Deque<IdentityConstraint> identityConstraintDefinitions;
	private final Deque<Element> substitutionGroupAffiliations;
	private final Set<Block> disallowedSubstitutions;
	private final Set<Final> substitutionGroupExclusions;
	private final boolean isAbstract;

	private Element(final Deferred<? extends AnnotatedComponent> context, final Node node, final Deque<Annotation> annotations, final String name, final Deferred<String> targetNamespace, final Deferred<? extends TypeDefinition> typeDefinition, final Deque<Alternative> alternatives, final Deferred<Scope> scope, final boolean nillable, final Deferred<ValueConstraint> valueConstraint, final Deque<IdentityConstraint> identityConstraintDefinitions, final Deque<Element> substitutionGroupAffiliations, final Set<Block> disallowedSubstitutions, final Set<Final> substitutionGroupExclusions, final boolean isAbstract) {
		this.context = Objects.requireNonNull(context);
		this.node = Objects.requireNonNull(node);
		this.annotations = Objects.requireNonNull(annotations);
		this.name = name;
		this.targetNamespace = targetNamespace.map(tns -> NodeHelper.requireNonEmpty(node, tns));
		this.typeDefinition = Objects.requireNonNull(typeDefinition);
		this.typeTable = alternatives.isEmpty() ? null : new TypeTable(alternatives);
		this.scope = Objects.requireNonNull(scope);
		this.nillable = nillable;
		this.valueConstraint = valueConstraint;
		this.identityConstraintDefinitions = Objects.requireNonNull(identityConstraintDefinitions);
		this.substitutionGroupAffiliations = Objects.requireNonNull(substitutionGroupAffiliations);
		this.disallowedSubstitutions = Objects.requireNonNull(disallowedSubstitutions);
		this.substitutionGroupExclusions = Objects.requireNonNull(substitutionGroupExclusions);
		this.isAbstract = isAbstract;
	}

	private static Element parseDecl(final Result result) {
		final Deferred<AnnotatedComponent> context = result.context();
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
		final Deferred<Scope> scope = context.map(ctx -> {
			if (ctx instanceof Schema) {
				if (form != null) {
					throw new Schema.ParseException(node, "'form' attribute is only allowed for local element declarations");
				}
				return new Scope(Scope.Variety.GLOBAL, null, node);
			} else {
				return new Scope(Scope.Variety.LOCAL, ctx, node);
			}
		});
		final String targetNamespaceValue = result.value(AttrParser.TARGET_NAMESPACE);
		final Deferred<String> targetNamespace = context.map(ctx -> {
			if (ctx instanceof Schema) {
				if (targetNamespaceValue != null) {
					throw new Schema.ParseException(node, "'targetNamespace' attribute is only allowed for local element declarations");
				}
				return result.schema().targetNamespace();
			} else {
				if (targetNamespaceValue == null && (Form.QUALIFIED.equals(form) || (form == null && Form.QUALIFIED.equals(result.schema().elementFormDefault())))) { // 3.3.2.3
					return result.schema().targetNamespace();
				}
				return targetNamespaceValue;
			}
		});
		final Deque<QName> substitutionGroup = result.value(AttrParser.SUBSTITUTION_GROUP);
		final DeferredArrayDeque<Element> substitutionGroupAffiliations = new DeferredArrayDeque<>(() -> {
			if (substitutionGroup != null && !(context.get() instanceof Schema)) {
				throw new ParseException(result.node(), "@substitutionGroup is only valid for global elements");
			} else if (substitutionGroup == null) {
				return Deques.emptyDeque();
			}
			final DeferredArrayDeque<Element> x = new DeferredArrayDeque<>();
			for (final QName s : substitutionGroup) {
				x.addLast(result.schema().find(s, Element.class));
			}
			return x;
		});
		final Deferred<? extends TypeDefinition> typeDefinition;
		final QName typeName = result.value(AttrParser.TYPE);
		if (typeName != null) {
			typeDefinition = result.schema().find(typeName, TypeDefinition.class);
		} else {
			final Deferred<TypeDefinition> type = result.parse(TagParser.COMPLEX_TYPE, TagParser.SIMPLE_TYPE);
			typeDefinition = type != null
					? type
					: () -> substitutionGroupAffiliations.isEmpty()
							? ComplexType.xsAnyType()
							: substitutionGroupAffiliations.getFirst().typeDefinition.get();
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
						? new ValueConstraint(effectiveSimpleType, ValueConstraint.Variety.DEFAULT, defaultValue)
						: fixedValue != null
								? new ValueConstraint(effectiveSimpleType, ValueConstraint.Variety.FIXED, fixedValue)
								: null;
			});
		} else {
			valueConstraint = null;
		}
		final String name = result.value(AttrParser.NAME);
		return new Element(context, node, annotations, name, targetNamespace, typeDefinition, alternatives, scope, nillable, valueConstraint, identityConstraints, substitutionGroupAffiliations, disallowedSubstitutions, substitutionGroupExclusions, isAbstract);
	}

	private static Particle parse(final Result result) {
		final Deferred<? extends AnnotatedComponent> context = result.context();
		final Node node = result.node();
		final Deque<Annotation> annotations = Annotation.of(result).resolve(node);
		final Number maxOccurs = result.value(AttrParser.MAX_OCCURS);
		final Number minOccurs = result.value(AttrParser.MIN_OCCURS);
		final QName refName = result.value(AttrParser.REF);
		final Deferred<Element> term = refName != null
				? result.schema().find(refName, Element.class)
				: new DeferredValue<>(parseDecl(result));
		return new Particle(context, node, annotations, maxOccurs, minOccurs, term);
	}

	static void register() {
		AttrParser.register(AttrParser.Names.NAME, NodeHelper::getAttrValueAsNCName);
		AttrParser.register(AttrParser.Names.NILLABLE, false);
		AttrParser.register(AttrParser.Names.REF, QName.class, NodeHelper::getAttrValueAsQName);
		AttrParser.register(AttrParser.Names.SUBSTITUTION_GROUP, Deque.class, QName.class, null, NodeHelper::getAttrValueAsQNames);
		AttrParser.register(AttrParser.Names.TYPE, QName.class, NodeHelper::getAttrValueAsQName);
		TagParser.register(TagParser.Names.ELEMENT, parser, Element.class, Element::parseDecl);
		TagParser.register(TagParser.Names.ELEMENT, parser, Particle.class, Element::parse);
		VisitorHelper.register(Element.class, Element::visit);
	}

	void visit(final Visitor visitor) {
		if (visitor.visit(context.get(), node, this)) {
			visitor.onElement(context.get(), node, this);
			annotations.forEach(a -> a.visit(visitor));
			ComplexType.visitTypeDefinition(typeDefinition(), visitor);
			if (typeTable != null) {
				typeTable.alternatives.forEach(a -> a.visit(visitor));
				typeTable.defaultTypeDefinition.visit(visitor);
			}
			identityConstraintDefinitions.forEach(i -> i.visit(visitor));
			substitutionGroupAffiliations.forEach(e -> e.visit(visitor));
		}
	}

	public String name() {
		return name;
	}

	public String targetNamespace() {
		return targetNamespace.get();
	}

	public TypeDefinition typeDefinition() {
		return typeDefinition.get();
	}

	public TypeTable typeTable() {
		return typeTable;
	}

	public Scope scope() {
		return scope.get();
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
	public Deque<Annotation> annotations() {
		return Deques.unmodifiableDeque(annotations);
	}

}
