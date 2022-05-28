package xs.parser;

import java.util.*;
import java.util.stream.*;
import javax.xml.*;
import javax.xml.namespace.*;
import org.w3c.dom.*;
import xs.parser.ConstrainingFacet.*;
import xs.parser.internal.*;
import xs.parser.internal.util.*;
import xs.parser.internal.util.SequenceParser.*;

/**
 * <pre>
 * &lt;simpleType
 *   final = (#all | List of (list | union | restriction | extension))
 *   id = ID
 *   name = NCName
 *   {any attributes with non-schema namespace . . .}&gt;
 *   Content: (annotation?, (restriction | list | union))
 * &lt;/simpleType&gt;
 * </pre>
 *
 * <table>
 *   <caption style="font-size: large; text-align: left">Schema Component: Simple Type Definition, a kind of Type Definition</caption>
 *   <thead>
 *     <tr>
 *       <th style="text-align: left">Method</th>
 *       <th style="text-align: left">Property</th>
 *       <th style="text-align: left">Representation</th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <td>{@link SimpleType#annotations()}</td>
 *       <td>{annotations}</td>
 *       <td>A sequence of Annotation components.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link SimpleType#name()}</td>
 *       <td>{name}</td>
 *       <td>An xs:NCName value. Optional.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link SimpleType#targetNamespace()}</td>
 *       <td>{target namespace}</td>
 *       <td>An xs:anyURI value. Optional.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link SimpleType#finals()}</td>
 *       <td>{final}</td>
 *       <td>A subset of {extension, restriction, list, union}.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link SimpleType#context()}</td>
 *       <td>{context}</td>
 *       <td>Required if {name} is ·absent·, otherwise must be ·absent·.<br>Either an Attribute Declaration, an Element Declaration, a Complex Type Definition, or a Simple Type Definition.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link SimpleType#baseType()}</td>
 *       <td>{base type definition}</td>
 *       <td>A Type Definition component. Required.<br>With one exception, the {base type definition} of any Simple Type Definition is a Simple Type Definition. The exception is ·xs:anySimpleType·, which has ·xs:anyType·, a Complex Type Definition, as its {base type definition}.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link SimpleType#facets()}</td>
 *       <td>{facets}</td>
 *       <td>A set of Constraining Facet components.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link SimpleType#fundamentalFacets()}</td>
 *       <td>{fundamental facets}</td>
 *       <td>A set of Fundamental Facet components.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link SimpleType#variety()}</td>
 *       <td>{variety}</td>
 *       <td>One of {atomic, list, union}. Required for all Simple Type Definitions except ·xs:anySimpleType·, in which it is ·absent·.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link SimpleType#primitiveType()}</td>
 *       <td>{primitive type definition}</td>
 *       <td>A Simple Type Definition component. With one exception, required if {variety} is atomic, otherwise must be ·absent·. The exception is ·xs:anyAtomicType·, whose {primitive type definition} is ·absent·.<br>If non-·absent·, must be a primitive definition.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link SimpleType#itemType()}</td>
 *       <td>{item type definition}</td>
 *       <td>A Simple Type Definition component. Required if {variety} is list, otherwise must be ·absent·.<br>The value of this property must be a primitive or ordinary simple type definition with {variety} = atomic, or an ordinary simple type definition with {variety} = union whose basic members are all atomic; the value must not itself be a list type (have {variety} = list) or have any basic members which are list types.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link SimpleType#memberTypes()}</td>
 *       <td>{member type definitions}</td>
 *       <td>A sequence of primitive or ordinary Simple Type Definition components.<br>Must be present (but may be empty) if {variety} is union, otherwise must be ·absent·.<br>The sequence may contain any primitive or ordinary simple type definition, but must not contain any special type definitions.</td>
 *     </tr>
 *   </tbody>
 * </table>
 */
public class SimpleType implements TypeDefinition {

	public enum Variety {

		ATOMIC,
		LIST,
		UNION;

	}

	/**
	 * <pre>
	 * &lt;list
	 *   id = ID
	 *   itemType = QName
	 *   {any attributes with non-schema namespace . . .}&gt;
	 *   Content: (annotation?, simpleType?)
	 * &lt;/list&gt;
	 * </pre>
	 */
	public static class List {

		private static final SequenceParser parser = new SequenceParser()
				.optionalAttributes(AttrParser.ID, AttrParser.ITEM_TYPE)
				.elements(0, 1, TagParser.ANNOTATION)
				.elements(0, 1, TagParser.SIMPLE_TYPE);

		private final Deque<Annotation> annotations;
		private final Deferred<SimpleType> itemType;

		List(final Deque<Annotation> annotations, final Deferred<SimpleType> itemType) {
			this.annotations = Objects.requireNonNull(annotations);
			this.itemType = Objects.requireNonNull(itemType);
		}

		private static List parse(final Result result) {
			final QName itemTypeName = result.value(AttrParser.ITEM_TYPE);
			if (itemTypeName != null) {
				return new List(result.annotations(), result.schema().find(itemTypeName, SimpleType.class));
			}
			final SimpleType itemSimpleType = result.parse(TagParser.SIMPLE_TYPE);
			return new List(result.annotations(), () -> itemSimpleType);
		}

		private Deque<Annotation> annotations() {
			return annotations;
		}

		private Deferred<SimpleType> itemType() {
			return itemType;
		}

	}

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
	public static class Restriction {

		private static final SequenceParser parser = new SequenceParser()
				.optionalAttributes(AttrParser.BASE, AttrParser.ID)
				.elements(0, 1, TagParser.ANNOTATION)
				.elements(0, 1, TagParser.SIMPLE_TYPE)
				.elements(0, Integer.MAX_VALUE, TagParser.FACETS.length(), TagParser.FACETS.maxLength(), TagParser.FACETS.minLength(), TagParser.FACETS.pattern(), TagParser.FACETS.enumeration(), TagParser.FACETS.whiteSpace(), TagParser.FACETS.maxInclusive(), TagParser.FACETS.maxExclusive(), TagParser.FACETS.minExclusive(), TagParser.FACETS.minInclusive(), TagParser.FACETS.totalDigits(), TagParser.FACETS.fractionDigits(), TagParser.FACETS.assertion(), TagParser.FACETS.explicitTimezone(), TagParser.ANY);

		private final Deque<Annotation> annotations;
		private final Deferred<SimpleType> base;
		private final Deque<ConstrainingFacet> facets;
		private final Deque<Particle> wildcard;

		private Restriction(final Deque<Annotation> annotations, final Deferred<SimpleType> base, final Deque<ConstrainingFacet> facets, final Deque<Particle> wildcard) {
			this.annotations = Objects.requireNonNull(annotations);
			this.base = Objects.requireNonNull(base);
			this.facets = Objects.requireNonNull(facets);
			this.wildcard = Objects.requireNonNull(wildcard);
		}

		private static Restriction parse(final Result result) {
			final QName baseType = result.value(AttrParser.BASE);
			final Deferred<SimpleType> base = baseType == null
					? Deferred.of(() -> result.parse(TagParser.SIMPLE_TYPE))
					: result.schema().find(baseType, SimpleType.class);
			final Deque<ConstrainingFacet> facets = result.parseAll(TagParser.FACETS.length(), TagParser.FACETS.maxLength(), TagParser.FACETS.minLength(), TagParser.FACETS.pattern(), TagParser.FACETS.enumeration(), TagParser.FACETS.whiteSpace(), TagParser.FACETS.maxInclusive(), TagParser.FACETS.maxExclusive(), TagParser.FACETS.minExclusive(), TagParser.FACETS.minInclusive(), TagParser.FACETS.totalDigits(), TagParser.FACETS.fractionDigits(), TagParser.FACETS.assertion(), TagParser.FACETS.explicitTimezone());
			final Deque<Particle> wildcard = result.parseAll(TagParser.ANY);
			return new Restriction(result.annotations(), base, facets, wildcard);
		}

		private Deque<Annotation> annotations() {
			return annotations;
		}

		private Deferred<SimpleType> base() {
			return base;
		}

		private Deque<ConstrainingFacet> facets() {
			return facets;
		}

		private Deque<Particle> wildcard() {
			return wildcard;
		}

	}

	/**
	 * <pre>
	 * &lt;union
	 *   id = ID
	 *   memberTypes = List of QName
	 *   {any attributes with non-schema namespace . . .}&gt;
	 *   Content: (annotation?, simpleType*)
	 * &lt;/union&gt;
	 * </pre>
	 */
	public static class Union {

		private static final SequenceParser parser = new SequenceParser()
				.optionalAttributes(AttrParser.ID, AttrParser.MEMBER_TYPES)
				.elements(0, 1, TagParser.ANNOTATION)
				.elements(0, Integer.MAX_VALUE, TagParser.SIMPLE_TYPE);

		private final Deque<Annotation> annotations;
		private final Deque<SimpleType> memberTypes;

		private Union(final Deque<Annotation> annotations, final Deque<SimpleType> memberTypes) {
			this.annotations = Objects.requireNonNull(annotations);
			this.memberTypes = Objects.requireNonNull(memberTypes);
		}

		private static Union parse(final Result result) {
			final Deque<QName> memberTypes = result.value(AttrParser.MEMBER_TYPES);
			if (memberTypes != null) {
				final Deque<SimpleType> memberTypesValues = DeferredArrayDeque.of(memberTypes.stream()
						.map(memberType -> result.schema().find(memberType, SimpleType.class))
						.collect(Collectors.toCollection(ArrayDeque::new)));
				return new Union(result.annotations(), memberTypesValues);
			}
			final Deque<SimpleType> memberTypesElem = result.parseAll(TagParser.SIMPLE_TYPE);
			return new Union(result.annotations(), memberTypesElem);
		}

		private Deque<Annotation> annotations() {
			return annotations;
		}

		private Deque<SimpleType> memberTypes() {
			return memberTypes;
		}

	}

	private static final SequenceParser parser = new SequenceParser()
			.optionalAttributes(AttrParser.FINAL, AttrParser.ID, AttrParser.NAME)
			.elements(0, 1, TagParser.ANNOTATION)
			.elements(1, 1, TagParser.SIMPLE_TYPE.restriction(), TagParser.SIMPLE_TYPE.list(), TagParser.SIMPLE_TYPE.union());
	static final String ANYSIMPLETYPE_NAME = "anySimpleType";
	private static final SimpleType xsAnySimpleType;
	static final String ANYATOMICTYPE_NAME = "anyAtomicType";
	private static final SimpleType xsAnyAtomicType;
	private static final Map<String, SimpleType> PRIMITIVE_TYPES;
	static final String STRING_NAME = "string";
	static final String BOOLEAN_NAME = "boolean";
	static final String DECIMAL_NAME = "decimal";
	static final String FLOAT_NAME = "float";
	static final String DOUBLE_NAME = "double";
	static final String DURATION_NAME = "duration";
	static final String DATETIME_NAME = "dateTime";
	static final String TIME_NAME = "time";
	static final String DATE_NAME = "date";
	static final String GYEARMONTH_NAME = "gYearMonth";
	static final String GYEAR_NAME = "gYear";
	static final String GMONTHDAY_NAME = "gMonthDay";
	static final String GDAY_NAME = "gDay";
	static final String GMONTH_NAME = "gMonth";
	static final String HEXBINARY_NAME = "hexBinary";
	static final String BASE64BINARY_NAME = "base64Binary";
	static final String ANYURI_NAME = "anyURI";
	static final String QNAME_NAME = "QName";
	static final String NOTATION_NAME = "NOTATION";
	private static final Map<String, SimpleType> BUILTIN_TYPES;
	static final String NORMALIZEDSTRING_NAME = "normalizedString";
	static final String TOKEN_NAME = "token";
	static final String LANGUAGE_NAME = "language";
	static final String NMTOKEN_NAME = "NMTOKEN";
	static final String NMTOKENS_NAME = "NMTOKENS";
	static final String NAME_NAME = "Name";
	static final String NCNAME_NAME = "NCName";
	static final String ID_NAME = "ID";
	static final String IDREF_NAME = "IDREF";
	static final String IDREFS_NAME = "IDREFS";
	static final String ENTITY_NAME = "ENTITY";
	static final String ENTITIES_NAME = "ENTITIES";
	static final String INTEGER_NAME = "integer";
	static final String NONPOSITIVEINTEGER_NAME = "nonPositiveInteger";
	static final String NEGATIVEINTEGER_NAME = "negativeInteger";
	static final String LONG_NAME = "long";
	static final String INT_NAME = "int";
	static final String SHORT_NAME = "short";
	static final String BYTE_NAME = "byte";
	static final String NONNEGATIVEINTEGER_NAME = "nonNegativeInteger";
	static final String UNSIGNEDLONG_NAME = "unsignedLong";
	static final String UNSIGNEDINT_NAME = "unsignedInt";
	static final String UNSIGNEDSHORT_NAME = "unsignedShort";
	static final String UNSIGNEDBYTE_NAME = "unsignedByte";
	static final String POSITIVEINTEGER_NAME = "positiveInteger";
	static final String YEARMONTHDURATION_NAME = "yearMonthDuration";
	static final String DAYTIMEDURATION_NAME = "dayTimeDuration";
	static final String DATETIMESTAMP_NAME = "dateTimeStamp";

	static {
		final Document doc = NodeHelper.newSchemaDocument(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		final Node xsAnySimpleTypeNode = NodeHelper.newSchemaNode(doc, TagParser.Names.SIMPLE_TYPE, ANYSIMPLETYPE_NAME);
		xsAnySimpleType = new SimpleType(xsAnySimpleTypeNode, Deques.emptyDeque(), ANYSIMPLETYPE_NAME, XMLConstants.W3C_XML_SCHEMA_NS_URI, Deques.emptyDeque(), null, ComplexType::xsAnyType, Deferred.none(), Deques::emptyDeque, null, null, null);
		xsAnyAtomicType = create(doc, ANYATOMICTYPE_NAME, xsAnySimpleType);
		final Map<String, SimpleType> primitiveTypes = new HashMap<>();
		primitiveTypes.put(STRING_NAME, create(doc, STRING_NAME, xsAnyAtomicType));
		primitiveTypes.put(BOOLEAN_NAME, create(doc, BOOLEAN_NAME, xsAnyAtomicType));
		primitiveTypes.put(DECIMAL_NAME, create(doc, DECIMAL_NAME, xsAnyAtomicType));
		primitiveTypes.put(FLOAT_NAME, create(doc, FLOAT_NAME, xsAnyAtomicType));
		primitiveTypes.put(DOUBLE_NAME, create(doc, DOUBLE_NAME, xsAnyAtomicType));
		primitiveTypes.put(DURATION_NAME, create(doc, DURATION_NAME, xsAnyAtomicType));
		primitiveTypes.put(DATETIME_NAME, create(doc, DATETIME_NAME, xsAnyAtomicType));
		primitiveTypes.put(DATE_NAME, create(doc, DATE_NAME, xsAnyAtomicType));
		primitiveTypes.put(TIME_NAME, create(doc, TIME_NAME, xsAnyAtomicType));
		primitiveTypes.put(GYEARMONTH_NAME, create(doc, GYEARMONTH_NAME, xsAnyAtomicType));
		primitiveTypes.put(GYEAR_NAME, create(doc, GYEAR_NAME, xsAnyAtomicType));
		primitiveTypes.put(GMONTHDAY_NAME, create(doc, GMONTHDAY_NAME, xsAnyAtomicType));
		primitiveTypes.put(GDAY_NAME, create(doc, GDAY_NAME, xsAnyAtomicType));
		primitiveTypes.put(GMONTH_NAME, create(doc, GMONTH_NAME, xsAnyAtomicType));
		primitiveTypes.put(HEXBINARY_NAME, create(doc, HEXBINARY_NAME, xsAnyAtomicType));
		primitiveTypes.put(BASE64BINARY_NAME, create(doc, BASE64BINARY_NAME, xsAnyAtomicType));
		primitiveTypes.put(ANYURI_NAME, create(doc, ANYURI_NAME, xsAnyAtomicType));
		primitiveTypes.put(QNAME_NAME, create(doc, QNAME_NAME, xsAnyAtomicType));
		primitiveTypes.put(NOTATION_NAME, create(doc, NOTATION_NAME, xsAnyAtomicType));
		assert primitiveTypes.size() == 19 : primitiveTypes.size() + ", " + primitiveTypes;
		PRIMITIVE_TYPES = Collections.unmodifiableMap(primitiveTypes);
		final Map<String, SimpleType> builtinTypes = new HashMap<>();
		builtinTypes.put(NORMALIZEDSTRING_NAME, create(doc, NORMALIZEDSTRING_NAME, PRIMITIVE_TYPES.get(STRING_NAME)));
		builtinTypes.put(TOKEN_NAME, create(doc, TOKEN_NAME, builtinTypes.get(NORMALIZEDSTRING_NAME)));
		builtinTypes.put(LANGUAGE_NAME, create(doc, LANGUAGE_NAME, builtinTypes.get(TOKEN_NAME)));
		builtinTypes.put(NMTOKEN_NAME, create(doc, NMTOKEN_NAME, builtinTypes.get(TOKEN_NAME)));
		builtinTypes.put(NMTOKENS_NAME, create(doc, NMTOKENS_NAME, xsAnySimpleType));
		builtinTypes.put(NAME_NAME, create(doc, NAME_NAME, builtinTypes.get(TOKEN_NAME)));
		builtinTypes.put(NCNAME_NAME, create(doc, NCNAME_NAME, builtinTypes.get(NAME_NAME)));
		builtinTypes.put(ID_NAME, create(doc, ID_NAME, builtinTypes.get(NCNAME_NAME)));
		builtinTypes.put(IDREF_NAME, create(doc, IDREF_NAME, builtinTypes.get(NCNAME_NAME)));
		builtinTypes.put(IDREFS_NAME, create(doc, IDREFS_NAME, xsAnySimpleType));
		builtinTypes.put(ENTITY_NAME, create(doc, ENTITY_NAME, builtinTypes.get(NCNAME_NAME)));
		builtinTypes.put(ENTITIES_NAME, create(doc, ENTITIES_NAME, xsAnySimpleType));
		builtinTypes.put(INTEGER_NAME, create(doc, INTEGER_NAME, PRIMITIVE_TYPES.get(DECIMAL_NAME)));
		builtinTypes.put(NONPOSITIVEINTEGER_NAME, create(doc, NONPOSITIVEINTEGER_NAME, builtinTypes.get(INTEGER_NAME)));
		builtinTypes.put(NEGATIVEINTEGER_NAME, create(doc, NEGATIVEINTEGER_NAME, builtinTypes.get(NONPOSITIVEINTEGER_NAME)));
		builtinTypes.put(LONG_NAME, create(doc, LONG_NAME, builtinTypes.get(INTEGER_NAME)));
		builtinTypes.put(INT_NAME, create(doc, INT_NAME, builtinTypes.get(LONG_NAME)));
		builtinTypes.put(SHORT_NAME, create(doc, SHORT_NAME, builtinTypes.get(INT_NAME)));
		builtinTypes.put(BYTE_NAME, create(doc, BYTE_NAME, builtinTypes.get(SHORT_NAME)));
		builtinTypes.put(NONNEGATIVEINTEGER_NAME, create(doc, NONNEGATIVEINTEGER_NAME, builtinTypes.get(INTEGER_NAME)));
		builtinTypes.put(UNSIGNEDLONG_NAME, create(doc, UNSIGNEDLONG_NAME, builtinTypes.get(NONNEGATIVEINTEGER_NAME)));
		builtinTypes.put(UNSIGNEDINT_NAME, create(doc, UNSIGNEDINT_NAME, builtinTypes.get(UNSIGNEDLONG_NAME)));
		builtinTypes.put(UNSIGNEDSHORT_NAME, create(doc, UNSIGNEDSHORT_NAME, builtinTypes.get(UNSIGNEDINT_NAME)));
		builtinTypes.put(UNSIGNEDBYTE_NAME, create(doc, UNSIGNEDBYTE_NAME, builtinTypes.get(UNSIGNEDINT_NAME)));
		builtinTypes.put(POSITIVEINTEGER_NAME, create(doc, POSITIVEINTEGER_NAME, builtinTypes.get(NONNEGATIVEINTEGER_NAME)));
		builtinTypes.put(YEARMONTHDURATION_NAME, create(doc, YEARMONTHDURATION_NAME, PRIMITIVE_TYPES.get(DURATION_NAME)));
		builtinTypes.put(DAYTIMEDURATION_NAME, create(doc, DAYTIMEDURATION_NAME, PRIMITIVE_TYPES.get(DURATION_NAME)));
		builtinTypes.put(DATETIMESTAMP_NAME, create(doc, DATETIMESTAMP_NAME, PRIMITIVE_TYPES.get(DATETIME_NAME)));
		assert builtinTypes.size() == 28;
		BUILTIN_TYPES = Collections.unmodifiableMap(builtinTypes);
	}

	private final Node node;
	private final Deque<Annotation> annotations;
	private final String name;
	private final String targetNamespace;
	private final Deferred<? extends TypeDefinition> baseType;
	private final Deque<Final> finals;
	private final Node context;
	private final Deferred<Variety> variety;
	private final Deferred<Deque<Object>> facets;
	private final Deferred<Deque<ConstrainingFacet>> facetValues;
	private final Deferred<Deque<FundamentalFacet>> fundamentalFacets;
	private final Deferred<SimpleType> primitiveType;
	private final Deferred<SimpleType> itemType;
	private final Deferred<Deque<SimpleType>> memberTypes;

	SimpleType(final Node node, final Deque<Annotation> annotations, final String name, final String targetNamespace, final Deque<Final> finals, final Node context, final Deferred<? extends TypeDefinition> baseType, final Deferred<Variety> variety, final Deferred<Deque<Object>> facets, final Restriction restriction, final List list, final Union union) {
		this.node = Objects.requireNonNull(node);
		this.annotations = Objects.requireNonNull(annotations);
		this.name = name;
		this.targetNamespace = NodeHelper.validateTargetNamespace(node, targetNamespace);
		this.baseType = Objects.requireNonNull(baseType);
		this.finals = Objects.requireNonNull(finals);
		this.context = context;
		this.variety = Objects.requireNonNull(variety);
		this.facets = Objects.requireNonNull(facets);
		this.facetValues = facets.map(f -> f.stream().filter(ConstrainingFacet.class::isInstance).map(ConstrainingFacet.class::cast).collect(Collectors.toCollection(ArrayDeque::new)));
		this.fundamentalFacets = Deferred.of(() -> {
			final Deque<FundamentalFacet> f = FundamentalFacet.find(this);
			if (f != null) {
				return f;
			}
			final TypeDefinition base = baseType.get();
			if (base instanceof ComplexType) {
				return Deques.emptyDeque();
			}
			return ((SimpleType) base).fundamentalFacets();
		});
		this.primitiveType = variety.map(v -> {
			if (Variety.ATOMIC.equals(v) && this != xsAnyAtomicType) {
				if (PRIMITIVE_TYPES.containsValue(this)) {
					return this;
				}
				assert baseType() instanceof SimpleType;
				return Objects.requireNonNull(((SimpleType) baseType()).primitiveType());
			}
			return null;
		});
		this.itemType = variety.map(v -> {
			if (Variety.LIST.equals(v)) {
				assert restriction != null || list != null;
				final SimpleType itemSimpleType = restriction != null
						? restriction.base().get().itemType()
						: list.itemType().get();
				assert Variety.ATOMIC.equals(itemSimpleType.variety()) || (Variety.UNION.equals(itemSimpleType.variety()) && itemSimpleType.memberTypes().stream().allMatch(s -> Variety.ATOMIC.equals(s.variety())));
				return itemSimpleType;
			}
			return null;
		});
		this.memberTypes = variety.map(v -> {
			if (Variety.UNION.equals(v)) {
				assert restriction != null || union != null;
				return restriction != null
						? restriction.base().get().memberTypes()
						: union.memberTypes();
			}
			return Deques.emptyDeque();
		});
	}

	private static SimpleType create(final Document doc, final String name, final TypeDefinition base) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(base);
		final Deque<Object> facets = ANYATOMICTYPE_NAME.equals(name) ? Deques.emptyDeque() : ConstrainingFacet.find(name);
		Objects.requireNonNull(facets);
		final Variety variety;
		final List list;
		switch (name) {
		case ENTITIES_NAME:
			list = new List(Deques.emptyDeque(), SimpleType::xsENTITY);
			variety = Variety.LIST;
			break;
		case IDREFS_NAME:
			list = new List(Deques.emptyDeque(), SimpleType::xsIDREF);
			variety = Variety.LIST;
			break;
		case NMTOKENS_NAME:
			list = new List(Deques.emptyDeque(), SimpleType::xsNMTOKEN);
			variety = Variety.LIST;
			break;
		default:
			list = null;
			variety = Variety.ATOMIC;
		}
		final Node node = NodeHelper.newSchemaNode(doc, TagParser.Names.SIMPLE_TYPE, name);
		return new SimpleType(node, Deques.emptyDeque(), name, XMLConstants.W3C_XML_SCHEMA_NS_URI, Deques.emptyDeque(), null, () -> base, () -> variety, () -> facets, null, list, null);
	}

	static void register() {
		AttrParser.register(AttrParser.Names.ITEM_TYPE, QName.class, NodeHelper::getNodeValueAsQName);
		AttrParser.register(AttrParser.Names.MEMBER_TYPES, Deque.class, QName.class, null, NodeHelper::getNodeValueAsQNames);
		TagParser.register(TagParser.Names.RESTRICTION, Restriction.parser, Restriction.class, Restriction::parse);
		TagParser.register(TagParser.Names.LIST, List.parser, List.class, List::parse);
		TagParser.register(TagParser.Names.UNION, Union.parser, Union.class, Union::parse);
		TagParser.register(TagParser.Names.SIMPLE_TYPE, SimpleType.parser, SimpleType.class, SimpleType::parse);
	}

	static SimpleType findPrimitiveOrBuiltinType(final String localName) {
		SimpleType s = PRIMITIVE_TYPES.get(localName);
		if (s != null) {
			return s;
		}
		s = BUILTIN_TYPES.get(localName);
		if (s != null) {
			return s;
		}
		throw new IllegalArgumentException("No primitive or built-in simpleType for name " + localName);
	}

	private static SimpleType parse(final Result result) {
		final String name = result.value(AttrParser.NAME);
		final String targetNamespace = result.schema().targetNamespace();
		Deque<Final> finals = result.value(AttrParser.FINAL);
		if (finals.isEmpty()) {
			finals = Deques.singletonDeque(result.schema().finalDefault());
		}
		final Restriction restriction = result.parse(TagParser.SIMPLE_TYPE.restriction());
		final List list = result.parse(TagParser.SIMPLE_TYPE.list());
		final Union union = result.parse(TagParser.SIMPLE_TYPE.union());
		final Deferred<Variety> variety = list != null ? () -> Variety.LIST
				: union != null ? () -> Variety.UNION
				: restriction.base().map(SimpleType::variety);
		final Deferred<SimpleType> baseType = variety.map(v -> Variety.LIST.equals(v) || Variety.UNION.equals(v) ? xsAnySimpleType : restriction.base().get());
		final Node context;
		final Node parentNode = result.node().getParentNode();
		if (name != null || parentNode == null) {
			context = null;
		} else if (TagParser.ATTRIBUTE.equalsName(parentNode) || TagParser.ELEMENT.equalsName(parentNode)) {
			context = parentNode;
		} else {
			final Node grandParent = parentNode.getParentNode();
			if (grandParent != null && TagParser.SIMPLE_TYPE.equalsName(grandParent)) {
				context = grandParent;
			} else if (grandParent != null && TagParser.COMPLEX_TYPE.simpleContent().equalsName(grandParent) && grandParent.getParentNode() != null && TagParser.COMPLEX_TYPE.equalsName(grandParent.getParentNode())) {
				context = grandParent.getParentNode();
			} else {
				throw new Schema.ParseException(result.node());
			}
		}
		final Deferred<Deque<Object>> facets = variety.map(v -> {
			switch (v) {
			case UNION:
				return Deques.emptyDeque();
			case LIST:
				return restriction != null // standalone xs:list vs xs:restriction of xs:list
						? ConstrainingFacet.combineLikeFacets(restriction.base().get(), restriction.base().get().facets.get(), restriction.facets())
						: Deques.asDeque(new ConstrainingFacet.WhiteSpace(result.node(), result.annotations(), true, WhiteSpace.Value.COLLAPSE), Length.class, MaxLength.class, MinLength.class, ConstrainingFacet.Enumeration.class, Pattern.class, Assertions.class);
			case ATOMIC:
				return ConstrainingFacet.combineLikeFacets(baseType.get(), baseType.get().facets.get(), restriction.facets());
			default:
				throw new AssertionError();
			}
		});
		return new SimpleType(result.node(), result.annotations(), name, targetNamespace, finals, context, baseType, variety, facets, restriction, list, union);
	}

	static SimpleType wrap(final Node node, final Deque<Annotation> annotations, final String name, final String targetNamespace, final Deque<Final> finals, final Node context, final SimpleType baseSimpleType, final Deque<ConstrainingFacet> declaredFacets) {
		final Deferred<Deque<Object>> facets = baseSimpleType.facets.map(f -> ConstrainingFacet.combineLikeFacets(baseSimpleType, f, declaredFacets));
		return new SimpleType(node, annotations, name, targetNamespace, finals, context, () -> baseSimpleType, baseSimpleType.variety, facets, null, null, null) {

			@Override
			public SimpleType primitiveType() {
				return baseSimpleType.primitiveType();
			}

			@Override
			public SimpleType itemType() {
				return baseSimpleType.itemType();
			}

			@Override
			public Deque<SimpleType> memberTypes() {
				return baseSimpleType.memberTypes();
			}

		};
	}

	public static SimpleType xsAnySimpleType() {
		return xsAnySimpleType;
	}

	public static SimpleType xsAnyAtomicType() {
		return xsAnyAtomicType;
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#string">http://www.w3.org/TR/xmlschema11-2/#string</a> */
	public static SimpleType xsString() {
		return Objects.requireNonNull(PRIMITIVE_TYPES.get(STRING_NAME));
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#boolean">http://www.w3.org/TR/xmlschema11-2/#boolean</a> */
	public static SimpleType xsBoolean() {
		return Objects.requireNonNull(PRIMITIVE_TYPES.get(BOOLEAN_NAME));
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#float">http://www.w3.org/TR/xmlschema11-2/#float</a> */
	public static SimpleType xsFloat() {
		return Objects.requireNonNull(PRIMITIVE_TYPES.get(FLOAT_NAME));
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#double">http://www.w3.org/TR/xmlschema11-2/#double</a> */
	public static SimpleType xsDouble() {
		return Objects.requireNonNull(PRIMITIVE_TYPES.get(DOUBLE_NAME));
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#decimal">http://www.w3.org/TR/xmlschema11-2/#decimal</a> */
	public static SimpleType xsDecimal() {
		return Objects.requireNonNull(PRIMITIVE_TYPES.get(DECIMAL_NAME));
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#duration">http://www.w3.org/TR/xmlschema11-2/#duration</a> */
	public static SimpleType xsDuration() {
		return Objects.requireNonNull(PRIMITIVE_TYPES.get(DURATION_NAME));
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#dateTime">http://www.w3.org/TR/xmlschema11-2/#dateTime</a> */
	public static SimpleType xsDateTime() {
		return Objects.requireNonNull(PRIMITIVE_TYPES.get(DATETIME_NAME));
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#time">http://www.w3.org/TR/xmlschema11-2/#time</a> */
	public static SimpleType xsTime() {
		return Objects.requireNonNull(PRIMITIVE_TYPES.get(TIME_NAME));
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#date">http://www.w3.org/TR/xmlschema11-2/#date</a> */
	public static SimpleType xsDate() {
		return Objects.requireNonNull(PRIMITIVE_TYPES.get(DATE_NAME));
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#gYearMonth">http://www.w3.org/TR/xmlschema11-2/#gYearMonth</a> */
	public static SimpleType xsGYearMonth() {
		return Objects.requireNonNull(PRIMITIVE_TYPES.get(GYEARMONTH_NAME));
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#gYear">http://www.w3.org/TR/xmlschema11-2/#gYear</a> */
	public static SimpleType xsGYear() {
		return Objects.requireNonNull(PRIMITIVE_TYPES.get(GYEAR_NAME));
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#gMonthDay">http://www.w3.org/TR/xmlschema11-2/#gMonthDay</a> */
	public static SimpleType xsGMonthDay() {
		return Objects.requireNonNull(PRIMITIVE_TYPES.get(GMONTHDAY_NAME));
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#gDay">http://www.w3.org/TR/xmlschema11-2/#gDay</a> */
	public static SimpleType xsGDay() {
		return Objects.requireNonNull(PRIMITIVE_TYPES.get(GDAY_NAME));
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#gMonth">http://www.w3.org/TR/xmlschema11-2/#gMonth</a> */
	public static SimpleType xsGMonth() {
		return Objects.requireNonNull(PRIMITIVE_TYPES.get(GMONTH_NAME));
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#hexBinary">http://www.w3.org/TR/xmlschema11-2/#hexBinary</a> */
	public static SimpleType xsHexBinary() {
		return Objects.requireNonNull(PRIMITIVE_TYPES.get(HEXBINARY_NAME));
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#base64Binary">http://www.w3.org/TR/xmlschema11-2/#base64Binary</a> */
	public static SimpleType xsBase64Binary() {
		return Objects.requireNonNull(PRIMITIVE_TYPES.get(BASE64BINARY_NAME));
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#anyURI">http://www.w3.org/TR/xmlschema11-2/#anyURI</a> */
	public static SimpleType xsAnyURI() {
		return Objects.requireNonNull(PRIMITIVE_TYPES.get(ANYURI_NAME));
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#QName">http://www.w3.org/TR/xmlschema11-2/#QName</a> */
	public static SimpleType xsQName() {
		return Objects.requireNonNull(PRIMITIVE_TYPES.get(QNAME_NAME));
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#NOTATION">http://www.w3.org/TR/xmlschema11-2/#NOTATION</a> */
	public static SimpleType xsNOTATION() {
		return Objects.requireNonNull(PRIMITIVE_TYPES.get(NOTATION_NAME));
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#normalizedString">http://www.w3.org/TR/xmlschema11-2/#normalizedString</a> */
	public static SimpleType xsNormalizedString() {
		return Objects.requireNonNull(BUILTIN_TYPES.get(NORMALIZEDSTRING_NAME));
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#token">http://www.w3.org/TR/xmlschema11-2/#token</a> */
	public static SimpleType xsToken() {
		return Objects.requireNonNull(BUILTIN_TYPES.get(TOKEN_NAME));
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#language">http://www.w3.org/TR/xmlschema11-2/#language</a> */
	public static SimpleType xsLanguage() {
		return Objects.requireNonNull(BUILTIN_TYPES.get(LANGUAGE_NAME));
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#IDREFS">http://www.w3.org/TR/xmlschema11-2/#IDREFS</a> */
	public static SimpleType xsIDREFS() {
		return Objects.requireNonNull(BUILTIN_TYPES.get(IDREFS_NAME));
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#ENTITIES">http://www.w3.org/TR/xmlschema11-2/#ENTITIES</a> */
	public static SimpleType xsENTITIES() {
		return Objects.requireNonNull(BUILTIN_TYPES.get(ENTITIES_NAME));
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#NMTOKEN">http://www.w3.org/TR/xmlschema11-2/#NMTOKEN</a> */
	public static SimpleType xsNMTOKEN() {
		return Objects.requireNonNull(BUILTIN_TYPES.get(NMTOKEN_NAME));
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#NMTOKENS">http://www.w3.org/TR/xmlschema11-2/#NMTOKENS</a> */
	public static SimpleType xsNMTOKENS() {
		return Objects.requireNonNull(BUILTIN_TYPES.get(NMTOKENS_NAME));
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#Name">http://www.w3.org/TR/xmlschema11-2/#Name</a> */
	public static SimpleType xsName() {
		return Objects.requireNonNull(BUILTIN_TYPES.get(NAME_NAME));
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#NCName">http://www.w3.org/TR/xmlschema11-2/#NCName</a> */
	public static SimpleType xsNCName() {
		return Objects.requireNonNull(BUILTIN_TYPES.get(NCNAME_NAME));
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#ID">http://www.w3.org/TR/xmlschema11-2/#ID</a> */
	public static SimpleType xsID() {
		return Objects.requireNonNull(BUILTIN_TYPES.get(ID_NAME));
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#IDREF">http://www.w3.org/TR/xmlschema11-2/#IDREF</a> */
	public static SimpleType xsIDREF() {
		return Objects.requireNonNull(BUILTIN_TYPES.get(IDREF_NAME));
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#ENTITY">http://www.w3.org/TR/xmlschema11-2/#ENTITY</a> */
	public static SimpleType xsENTITY() {
		return Objects.requireNonNull(BUILTIN_TYPES.get(ENTITY_NAME));
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#integer">http://www.w3.org/TR/xmlschema11-2/#integer</a> */
	public static SimpleType xsInteger() {
		return Objects.requireNonNull(BUILTIN_TYPES.get(INTEGER_NAME));
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#nonPositiveInteger">http://www.w3.org/TR/xmlschema11-2/#nonPositiveInteger</a> */
	public static SimpleType xsNonPositiveInteger() {
		return Objects.requireNonNull(BUILTIN_TYPES.get(NONPOSITIVEINTEGER_NAME));
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#negativeInteger">http://www.w3.org/TR/xmlschema11-2/#negativeInteger</a> */
	public static SimpleType xsNegativeInteger() {
		return Objects.requireNonNull(BUILTIN_TYPES.get(NEGATIVEINTEGER_NAME));
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#long">http://www.w3.org/TR/xmlschema11-2/#long</a> */
	public static SimpleType xsLong() {
		return Objects.requireNonNull(BUILTIN_TYPES.get(LONG_NAME));
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#int">http://www.w3.org/TR/xmlschema11-2/#int</a> */
	public static SimpleType xsInt() {
		return Objects.requireNonNull(BUILTIN_TYPES.get(INT_NAME));
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#short">http://www.w3.org/TR/xmlschema11-2/#short</a> */
	public static SimpleType xsShort() {
		return Objects.requireNonNull(BUILTIN_TYPES.get(SHORT_NAME));
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#byte">http://www.w3.org/TR/xmlschema11-2/#byte</a> */
	public static SimpleType xsByte() {
		return Objects.requireNonNull(BUILTIN_TYPES.get(BYTE_NAME));
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#nonNegativeInteger">http://www.w3.org/TR/xmlschema11-2/#nonNegativeInteger</a> */
	public static SimpleType xsNonNegativeInteger() {
		return Objects.requireNonNull(BUILTIN_TYPES.get(NONNEGATIVEINTEGER_NAME));
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#unsignedLong">http://www.w3.org/TR/xmlschema11-2/#unsignedLong</a> */
	public static SimpleType xsUnsignedLong() {
		return Objects.requireNonNull(BUILTIN_TYPES.get(UNSIGNEDLONG_NAME));
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#unsignedInt">http://www.w3.org/TR/xmlschema11-2/#unsignedInt</a> */
	public static SimpleType xsUnsignedInt() {
		return Objects.requireNonNull(BUILTIN_TYPES.get(UNSIGNEDINT_NAME));
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#unsignedShort">http://www.w3.org/TR/xmlschema11-2/#unsignedShort</a> */
	public static SimpleType xsUnsignedShort() {
		return Objects.requireNonNull(BUILTIN_TYPES.get(UNSIGNEDSHORT_NAME));
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#unsignedByte">http://www.w3.org/TR/xmlschema11-2/#unsignedByte</a> */
	public static SimpleType xsUnsignedByte() {
		return Objects.requireNonNull(BUILTIN_TYPES.get(UNSIGNEDBYTE_NAME));
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#positiveInteger">http://www.w3.org/TR/xmlschema11-2/#positiveInteger</a> */
	public static SimpleType xsPositiveInteger() {
		return Objects.requireNonNull(BUILTIN_TYPES.get(POSITIVEINTEGER_NAME));
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#yearMonthDuration">http://www.w3.org/TR/xmlschema11-2/#yearMonthDuration</a> */
	public static SimpleType xsYearMonthDuration() {
		return Objects.requireNonNull(BUILTIN_TYPES.get(YEARMONTHDURATION_NAME));
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#dayTimeDuration">http://www.w3.org/TR/xmlschema11-2/#dayTimeDuration</a> */
	public static SimpleType xsDayTimeDuration() {
		return Objects.requireNonNull(BUILTIN_TYPES.get(DAYTIMEDURATION_NAME));
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#dateTimeStamp">http://www.w3.org/TR/xmlschema11-2/#dateTimeStamp</a> */
	public static SimpleType xsDateTimeStamp() {
		return Objects.requireNonNull(BUILTIN_TYPES.get(DATETIMESTAMP_NAME));
	}

	String lexicalMapping(final String value) {
		if (this == xsAnySimpleType) {
			return value;
		}
		final WhiteSpace.Value whiteSpaceValue;
		switch (variety()) {
		case ATOMIC:
			if (this == xsString()) {
				whiteSpaceValue = WhiteSpace.Value.PRESERVE;
			} else if (primitiveType() == xsString()) {
				whiteSpaceValue = facets().stream().filter(WhiteSpace.class::isInstance).map(WhiteSpace.class::cast).map(WhiteSpace::value).findAny().orElse(WhiteSpace.Value.PRESERVE);
			} else {
				whiteSpaceValue = WhiteSpace.Value.COLLAPSE;
			}
			break;
		case LIST:
			whiteSpaceValue = WhiteSpace.Value.COLLAPSE;
			break;
		case UNION:
			return value; // doesn't directly apply
		default:
			throw new AssertionError(variety().toString());
		}
		switch (whiteSpaceValue) {
		case PRESERVE:
			return value;
		case REPLACE:
			return value.replaceAll("[\r\n\t]", " ");
		case COLLAPSE:
			return NodeHelper.collapseWhitespace(value);
		default:
			throw new AssertionError(whiteSpaceValue);
		}
	}

	/** @return If the &lt;list&gt; alternative is chosen, then list, otherwise if the &lt;union&gt; alternative is chosen, then union, otherwise (the &lt;restriction&gt; alternative is chosen), then the {variety} of the {base type definition}. */
	public Variety variety() {
		return variety.get();
	}

	/** @return The appropriate case among the following:
	 * <br>1 If the &lt;restriction&gt; alternative is chosen and the children of the &lt;restriction&gt; element are all either &lt;simpleType&gt; elements, &lt;annotation&gt; elements, or elements which specify constraining facets supported by the processor, then the set of Constraining Facet components obtained by ·overlaying· the {facets} of the {base type definition} with the set of Constraining Facet components corresponding to those [children] of &lt;restriction&gt; which specify facets, as defined in Simple Type Restriction (Facets) (§3.16.6.4).
	 * <br>2 If the &lt;restriction&gt; alternative is chosen and the children of the &lt;restriction&gt; element include at least one element of which the processor has no prior knowledge (i.e. not a &lt;simpleType&gt; element, an &lt;annotation&gt; element, or an element denoting a constraining facet known to and supported by the processor), then the &lt;simpleType&gt; element maps to no component at all (but is not in error solely on account of the presence of the unknown element).
	 * <br>3 If the &lt;list&gt; alternative is chosen, then a set with one member, a whiteSpace facet with {value} = collapse and {fixed} = true.
	 * <br>4 otherwise the empty set */
	public Deque<ConstrainingFacet> facets() {
		return Deques.unmodifiableDeque(facetValues.get());
	}

	/** @return Based on {variety}, {facets}, {base type definition} and {member type definitions}, a set of Fundamental Facet components, one each as specified in The ordered Schema Component, The bounded Schema Component, The cardinality Schema Component and The numeric Schema Component . */
	public Deque<FundamentalFacet> fundamentalFacets() {
		return Deques.unmodifiableDeque(fundamentalFacets.get());
	}

	/** @return From among the ·ancestors· of this Simple Type Definition, that Simple Type Definition which corresponds to a primitive datatype. */
	public SimpleType primitiveType() {
		return primitiveType.get();
	}

	/** @return The appropriate case among the following:
	 * <br>1 If the {base type definition} is ·xs:anySimpleType·, then the Simple Type Definition (a) ·resolved· to by the ·actual value· of the itemType [attribute] of &lt;list&gt;, or (b), corresponding to the &lt;simpleType&gt; among the [children] of &lt;list&gt;, whichever is present.
	 * <br>Note: In this case, a &lt;list&gt; element will invariably be present; it will invariably have either an itemType [attribute] or a &lt;simpleType&gt; [child], but not both.
	 * <br>2 otherwise (that is, the {base type definition} is not ·xs:anySimpleType·), the {item type definition} of the {base type definition}.
	 * <br>Note: In this case, a &lt;restriction&gt; element will invariably be present. */
	public SimpleType itemType() {
		return itemType.get();
	}

	/** @return The appropriate case among the following:
	 * <br>1 If the {base type definition} is ·xs:anySimpleType·, then the sequence of Simple Type Definitions (a) ·resolved· to by the items in the ·actual value· of the memberTypes [attribute] of &lt;union&gt;, if any, and (b) corresponding to the &lt;simpleType&gt;s among the [children] of &lt;union&gt;, if any, in order.
	 * <br>Note: In this case, a &lt;union&gt; element will invariably be present; it will invariably have either a memberTypes [attribute] or one or more &lt;simpleType&gt; [children], or both.
	 * <br>2 otherwise (that is, the {base type definition} is not ·xs:anySimpleType·), the {member type definitions} of the {base type definition}.
	 * <br>Note: In this case, a &lt;restriction&gt; element will invariably be present. */
	public Deque<SimpleType> memberTypes() {
		return Deques.unmodifiableDeque(memberTypes.get());
	}

	@Override
	public Node node() {
		return node;
	}

	/** @return The ·annotation mapping· of the set of elements containing the &lt;simpleType&gt;, and one of the &lt;restriction&gt;, &lt;list&gt; or &lt;union&gt; [children], whichever is present, as defined in XML Representation of Annotation Schema Components (§3.15.2). */
	@Override
	public Deque<Annotation> annotations() {
		return Deques.unmodifiableDeque(annotations);
	}

	/** @return The ·actual value· of the name [attribute] if present on the &lt;simpleType&gt; element, otherwise ·absent·. */
	@Override
	public String name() {
		return name;
	}

	/** @return The ·actual value· of the targetNamespace [attribute] of the ancestor &lt;schema&gt; element information item if present, otherwise ·absent·. */
	@Override
	public String targetNamespace() {
		return targetNamespace;
	}

	/** @return The appropriate case among the following:
	 * <br>1 If the &lt;restriction&gt; alternative is chosen, then the type definition ·resolved· to by the ·actual value· of the base [attribute] of &lt;restriction&gt;, if present, otherwise the type definition corresponding to the &lt;simpleType&gt; among the [children] of &lt;restriction&gt;.
	 * <br>2 If the &lt;list&gt; or &lt;union&gt; alternative is chosen, then ·xs:anySimpleType·. */
	@Override
	public TypeDefinition baseType() {
		return baseType.get();
	}

	/** @return A subset of {restriction, extension, list, union}, determined as follows. [Definition:]  Let FS be the ·actual value· of the final [attribute], if present, otherwise the ·actual value· of the finalDefault [attribute] of the ancestor schema element, if present, otherwise the empty string. Then the property value is the appropriate case among the following:
	 * <br>1 If ·FS· is the empty string, then the empty set;
	 * <br>2 If ·FS· is "#all", then {restriction, extension, list, union};
	 * <br>3 otherwise Consider ·FS· as a space-separated list, and include restriction if "restriction" is in that list, and similarly for extension, list and union. */
	@Override
	public Deque<Final> finals() {
		return Deques.unmodifiableDeque(finals);
	}

	/** @return The appropriate case among the following:
	 * <br>1 If the name [attribute] is present, then ·absent·
	 * <br>2 otherwise the appropriate case among the following:
	 * <br>2.1 If the parent element information item is &lt;attribute&gt;, then the corresponding Attribute Declaration
	 * <br>2.2 If the parent element information item is &lt;element&gt;, then the corresponding Element Declaration
	 * <br>2.3 If the parent element information item is &lt;list&gt; or &lt;union&gt;, then the Simple Type Definition corresponding to the grandparent &lt;simpleType&gt; element information item
	 * <br>2.4 If the parent element information item is &lt;alternative&gt;, then the Element Declaration corresponding to the nearest enclosing &lt;element&gt; element information item
	 * <br>2.5 otherwise (the parent element information item is &lt;restriction&gt;), the appropriate case among the following:
	 * <br>2.5.1 If the grandparent element information item is &lt;simpleType&gt;, then the Simple Type Definition corresponding to the grandparent
	 * <br>2.5.2 otherwise (the grandparent element information item is &lt;simpleContent&gt;), the Simple Type Definition which is the {content type}.{simple type definition} of the Complex Type Definition corresponding to the great-grandparent &lt;complexType&gt; element information item. */
	@Override
	public Node context() {
		return context;
	}

}