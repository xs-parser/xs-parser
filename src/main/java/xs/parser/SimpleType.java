package xs.parser;

import java.util.*;
import java.util.stream.*;
import javax.xml.*;
import javax.xml.namespace.*;
import org.w3c.dom.*;
import xs.parser.Annotation.*;
import xs.parser.ConstrainingFacet.*;
import xs.parser.internal.*;
import xs.parser.internal.util.*;
import xs.parser.internal.util.SequenceParser.*;
import xs.parser.v.*;

/**
 * A simple type definition is a set of constraints on strings and information about the values they encode, applicable to the ·normalized value· of an attribute information item or of an element information item with no element children. Informally, it applies to the values of attributes and the text-only content of elements.
 * <p>
 * Each simple type definition, whether built-in (that is, defined in [XML Schema: Datatypes]) or user-defined, is a ·restriction· of its ·base type definition·. A special ·restriction· of ·xs:anyType·, whose name is anySimpleType in the XSD namespace, is the root of the ·Type Definition Hierarchy· for all simple type definitions. ·xs:anySimpleType· has a lexical space containing all sequences of characters in the Universal Character Set (UCS) and a value space containing all atomic values and all finite-length lists of atomic values. As with ·xs:anyType·, this specification sometimes uses the qualified name xs:anySimpleType to designate this type definition. The built-in list datatypes all have ·xs:anySimpleType· as their ·base type definition·.
 * <p>
 * There is a further special datatype called anyAtomicType, a ·restriction· of ·xs:anySimpleType·, which is the ·base type definition· of all the primitive datatypes. This type definition is often referred to simply as "xs:anyAtomicType". It too is considered to have an unconstrained lexical space. Its value space consists of the union of the value spaces of all the primitive datatypes.
 * <p>
 * Datatypes can be constructed from other datatypes by restricting the value space or lexical space of a {base type definition} using zero or more Constraining Facets, by specifying the new datatype as a list of items of some {item type definition}, or by defining it as a union of some specified sequence of {member type definitions}.
 * <p>
 * The mapping from lexical space to value space is unspecified for items whose type definition is ·xs:anySimpleType· or ·xs:anyAtomicType·. Accordingly this specification does not constrain processors' behavior in areas where this mapping is implicated, for example checking such items against enumerations, constructing default attributes or elements whose declared type definition is ·xs:anySimpleType· or ·xs:anyAtomicType·, checking identity constraints involving such items.
 *
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
 *       <td>{@link SimpleType#baseTypeDefinition()}</td>
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
 *       <td>{@link SimpleType#primitiveTypeDefinition()}</td>
 *       <td>{primitive type definition}</td>
 *       <td>A Simple Type Definition component. With one exception, required if {variety} is atomic, otherwise must be ·absent·. The exception is ·xs:anyAtomicType·, whose {primitive type definition} is ·absent·.<br>If non-·absent·, must be a primitive definition.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link SimpleType#itemTypeDefinition()}</td>
 *       <td>{item type definition}</td>
 *       <td>A Simple Type Definition component. Required if {variety} is list, otherwise must be ·absent·.<br>The value of this property must be a primitive or ordinary simple type definition with {variety} = atomic, or an ordinary simple type definition with {variety} = union whose basic members are all atomic; the value must not itself be a list type (have {variety} = list) or have any basic members which are list types.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link SimpleType#memberTypeDefinitions()}</td>
 *       <td>{member type definitions}</td>
 *       <td>A sequence of primitive or ordinary Simple Type Definition components.<br>Must be present (but may be empty) if {variety} is union, otherwise must be ·absent·.<br>The sequence may contain any primitive or ordinary simple type definition, but must not contain any special type definitions.</td>
 *     </tr>
 *   </tbody>
 * </table>
 */
public class SimpleType implements TypeDefinition {

	/** Simple type variety */
	public enum Variety {

		/** Simple type variety atomic */
		ATOMIC,
		/** Simple type variety list */
		LIST,
		/** Simple type variety union */
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

		private final AnnotationSet annotations;
		private final Deferred<SimpleType> itemTypeDefinition;

		List(final AnnotationSet annotations, final Deferred<SimpleType> itemTypeDefinition) {
			this.annotations = Objects.requireNonNull(annotations);
			this.itemTypeDefinition = Objects.requireNonNull(itemTypeDefinition);
		}

		private static List parse(final Result result) {
			final AnnotationSet annotations = Annotation.of(result);
			final QName itemTypeName = result.value(AttrParser.ITEM_TYPE);
			if (itemTypeName != null) {
				return new List(annotations, result.schema().find(itemTypeName, SimpleType.class));
			}
			final Deferred<SimpleType> itemSimpleType = result.parse(TagParser.SIMPLE_TYPE);
			return new List(annotations, itemSimpleType);
		}

		private AnnotationSet annotations() {
			return annotations;
		}

		private Deferred<SimpleType> itemTypeDefinition() {
			return itemTypeDefinition;
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
				.elements(0, Integer.MAX_VALUE, TagParser.FACETS.length(), TagParser.FACETS.maxLength(), TagParser.FACETS.minLength(), TagParser.FACETS.pattern(), TagParser.FACETS.enumeration(), TagParser.FACETS.whiteSpace(), TagParser.FACETS.maxInclusive(), TagParser.FACETS.maxExclusive(), TagParser.FACETS.minExclusive(), TagParser.FACETS.minInclusive(), TagParser.FACETS.totalDigits(), TagParser.FACETS.fractionDigits(), TagParser.FACETS.assertion(), TagParser.FACETS.explicitTimezone());

		private final AnnotationSet annotations;
		private final Deferred<SimpleType> baseTypeDefinition;
		private final Deque<ConstrainingFacet> facets;

		private Restriction(final AnnotationSet annotations, final Deferred<SimpleType> baseTypeDefinition, final Deque<ConstrainingFacet> facets) {
			this.annotations = Objects.requireNonNull(annotations);
			this.baseTypeDefinition = Objects.requireNonNull(baseTypeDefinition);
			this.facets = Objects.requireNonNull(facets);
		}

		private static Restriction parse(final Result result) {
			final AnnotationSet annotations = Annotation.of(result);
			final QName baseType = result.value(AttrParser.BASE);
			final Deferred<SimpleType> baseTypeDefinition = baseType == null
					? result.parse(TagParser.SIMPLE_TYPE)
					: result.schema().find(baseType, SimpleType.class);
			final Deque<ConstrainingFacet> facets = result.parseAll(TagParser.FACETS.length(), TagParser.FACETS.maxLength(), TagParser.FACETS.minLength(), TagParser.FACETS.pattern(), TagParser.FACETS.enumeration(), TagParser.FACETS.whiteSpace(), TagParser.FACETS.maxInclusive(), TagParser.FACETS.maxExclusive(), TagParser.FACETS.minExclusive(), TagParser.FACETS.minInclusive(), TagParser.FACETS.totalDigits(), TagParser.FACETS.fractionDigits(), TagParser.FACETS.assertion(), TagParser.FACETS.explicitTimezone());
			return new Restriction(annotations, baseTypeDefinition, facets);
		}

		private AnnotationSet annotations() {
			return annotations;
		}

		private Deferred<SimpleType> baseTypeDefinition() {
			return baseTypeDefinition;
		}

		private Deque<ConstrainingFacet> facets() {
			return facets;
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

		private final AnnotationSet annotations;
		private final Deque<SimpleType> memberTypeDefinitions;

		private Union(final AnnotationSet annotations, final Deque<SimpleType> memberTypeDefinitions) {
			this.annotations = Objects.requireNonNull(annotations);
			this.memberTypeDefinitions = Objects.requireNonNull(memberTypeDefinitions);
		}

		private static Union parse(final Result result) {
			final AnnotationSet annotations = Annotation.of(result);
			final Deque<QName> memberTypes = result.value(AttrParser.MEMBER_TYPES);
			final Deque<SimpleType> memberTypeDefinitions;
			if (memberTypes != null) {
				memberTypeDefinitions = new DeferredArrayDeque<>(() -> memberTypes.stream().map(memberType -> result.schema().find(memberType, SimpleType.class).get()).collect(Collectors.toCollection(ArrayDeque::new)));
			} else {
				memberTypeDefinitions = result.parseAll(TagParser.SIMPLE_TYPE);
			}
			return new Union(annotations, memberTypeDefinitions);
		}

		private AnnotationSet annotations() {
			return annotations;
		}

		private Deque<SimpleType> memberTypeDefinitions() {
			return memberTypeDefinitions;
		}

	}

	private static final SequenceParser parser = new SequenceParser()
			.optionalAttributes(AttrParser.FINAL, AttrParser.ID, AttrParser.NAME)
			.elements(0, 1, TagParser.ANNOTATION)
			.elements(1, 1, TagParser.SIMPLE_TYPE.restriction(), TagParser.SIMPLE_TYPE.list(), TagParser.SIMPLE_TYPE.union());
	static final String ANYSIMPLETYPE_NAME = "anySimpleType";
	private static final Deferred<SimpleType> xsAnySimpleType;
	static final String ANYATOMICTYPE_NAME = "anyAtomicType";
	private static final Deferred<SimpleType> xsAnyAtomicType;
	private static final Map<String, Deferred<SimpleType>> PRIMITIVE_TYPE_DEFINITIONS;
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
	private static final Map<String, Deferred<SimpleType>> BUILTIN_TYPE_DEFINITIONS;
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
		xsAnySimpleType = Deferred.of(() -> {
			final Node xsAnySimpleTypeNode = NodeHelper.newGlobalNode(Schema.XSD, TagParser.Names.SIMPLE_TYPE, ANYSIMPLETYPE_NAME);
			return new SimpleType(Schema.XSD, () -> Schema.XSD, xsAnySimpleTypeNode, Deques.emptyDeque(), ANYSIMPLETYPE_NAME, XMLConstants.W3C_XML_SCHEMA_NS_URI, Deques.emptyDeque(), ComplexType::xsAnyType, () -> null, Deques.emptyDeque(), null, null, null);
		});
		xsAnyAtomicType = create(ANYATOMICTYPE_NAME, xsAnySimpleType);
		final Map<String, Deferred<SimpleType>> primitiveTypeDefinitions = new HashMap<>();
		primitiveTypeDefinitions.put(STRING_NAME, create(STRING_NAME, xsAnyAtomicType));
		primitiveTypeDefinitions.put(BOOLEAN_NAME, create(BOOLEAN_NAME, xsAnyAtomicType));
		primitiveTypeDefinitions.put(DECIMAL_NAME, create(DECIMAL_NAME, xsAnyAtomicType));
		primitiveTypeDefinitions.put(FLOAT_NAME, create(FLOAT_NAME, xsAnyAtomicType));
		primitiveTypeDefinitions.put(DOUBLE_NAME, create(DOUBLE_NAME, xsAnyAtomicType));
		primitiveTypeDefinitions.put(DURATION_NAME, create(DURATION_NAME, xsAnyAtomicType));
		primitiveTypeDefinitions.put(DATETIME_NAME, create(DATETIME_NAME, xsAnyAtomicType));
		primitiveTypeDefinitions.put(DATE_NAME, create(DATE_NAME, xsAnyAtomicType));
		primitiveTypeDefinitions.put(TIME_NAME, create(TIME_NAME, xsAnyAtomicType));
		primitiveTypeDefinitions.put(GYEARMONTH_NAME, create(GYEARMONTH_NAME, xsAnyAtomicType));
		primitiveTypeDefinitions.put(GYEAR_NAME, create(GYEAR_NAME, xsAnyAtomicType));
		primitiveTypeDefinitions.put(GMONTHDAY_NAME, create(GMONTHDAY_NAME, xsAnyAtomicType));
		primitiveTypeDefinitions.put(GDAY_NAME, create(GDAY_NAME, xsAnyAtomicType));
		primitiveTypeDefinitions.put(GMONTH_NAME, create(GMONTH_NAME, xsAnyAtomicType));
		primitiveTypeDefinitions.put(HEXBINARY_NAME, create(HEXBINARY_NAME, xsAnyAtomicType));
		primitiveTypeDefinitions.put(BASE64BINARY_NAME, create(BASE64BINARY_NAME, xsAnyAtomicType));
		primitiveTypeDefinitions.put(ANYURI_NAME, create(ANYURI_NAME, xsAnyAtomicType));
		primitiveTypeDefinitions.put(QNAME_NAME, create(QNAME_NAME, xsAnyAtomicType));
		primitiveTypeDefinitions.put(NOTATION_NAME, create(NOTATION_NAME, xsAnyAtomicType));
		if (primitiveTypeDefinitions.size() != 19) {
			throw new ExceptionInInitializerError("Wrong number of primitive type definitions: " + primitiveTypeDefinitions.size() + ", " + primitiveTypeDefinitions);
		}
		PRIMITIVE_TYPE_DEFINITIONS = Collections.unmodifiableMap(primitiveTypeDefinitions);
		final Map<String, Deferred<SimpleType>> builtinTypeDefinitions = new HashMap<>();
		builtinTypeDefinitions.put(NORMALIZEDSTRING_NAME, create(NORMALIZEDSTRING_NAME, PRIMITIVE_TYPE_DEFINITIONS.get(STRING_NAME)));
		builtinTypeDefinitions.put(TOKEN_NAME, create(TOKEN_NAME, builtinTypeDefinitions.get(NORMALIZEDSTRING_NAME)));
		builtinTypeDefinitions.put(LANGUAGE_NAME, create(LANGUAGE_NAME, builtinTypeDefinitions.get(TOKEN_NAME)));
		builtinTypeDefinitions.put(NMTOKEN_NAME, create(NMTOKEN_NAME, builtinTypeDefinitions.get(TOKEN_NAME)));
		builtinTypeDefinitions.put(NMTOKENS_NAME, create(NMTOKENS_NAME, xsAnySimpleType));
		builtinTypeDefinitions.put(NAME_NAME, create(NAME_NAME, builtinTypeDefinitions.get(TOKEN_NAME)));
		builtinTypeDefinitions.put(NCNAME_NAME, create(NCNAME_NAME, builtinTypeDefinitions.get(NAME_NAME)));
		builtinTypeDefinitions.put(ID_NAME, create(ID_NAME, builtinTypeDefinitions.get(NCNAME_NAME)));
		builtinTypeDefinitions.put(IDREF_NAME, create(IDREF_NAME, builtinTypeDefinitions.get(NCNAME_NAME)));
		builtinTypeDefinitions.put(IDREFS_NAME, create(IDREFS_NAME, xsAnySimpleType));
		builtinTypeDefinitions.put(ENTITY_NAME, create(ENTITY_NAME, builtinTypeDefinitions.get(NCNAME_NAME)));
		builtinTypeDefinitions.put(ENTITIES_NAME, create(ENTITIES_NAME, xsAnySimpleType));
		builtinTypeDefinitions.put(INTEGER_NAME, create(INTEGER_NAME, PRIMITIVE_TYPE_DEFINITIONS.get(DECIMAL_NAME)));
		builtinTypeDefinitions.put(NONPOSITIVEINTEGER_NAME, create(NONPOSITIVEINTEGER_NAME, builtinTypeDefinitions.get(INTEGER_NAME)));
		builtinTypeDefinitions.put(NEGATIVEINTEGER_NAME, create(NEGATIVEINTEGER_NAME, builtinTypeDefinitions.get(NONPOSITIVEINTEGER_NAME)));
		builtinTypeDefinitions.put(LONG_NAME, create(LONG_NAME, builtinTypeDefinitions.get(INTEGER_NAME)));
		builtinTypeDefinitions.put(INT_NAME, create(INT_NAME, builtinTypeDefinitions.get(LONG_NAME)));
		builtinTypeDefinitions.put(SHORT_NAME, create(SHORT_NAME, builtinTypeDefinitions.get(INT_NAME)));
		builtinTypeDefinitions.put(BYTE_NAME, create(BYTE_NAME, builtinTypeDefinitions.get(SHORT_NAME)));
		builtinTypeDefinitions.put(NONNEGATIVEINTEGER_NAME, create(NONNEGATIVEINTEGER_NAME, builtinTypeDefinitions.get(INTEGER_NAME)));
		builtinTypeDefinitions.put(UNSIGNEDLONG_NAME, create(UNSIGNEDLONG_NAME, builtinTypeDefinitions.get(NONNEGATIVEINTEGER_NAME)));
		builtinTypeDefinitions.put(UNSIGNEDINT_NAME, create(UNSIGNEDINT_NAME, builtinTypeDefinitions.get(UNSIGNEDLONG_NAME)));
		builtinTypeDefinitions.put(UNSIGNEDSHORT_NAME, create(UNSIGNEDSHORT_NAME, builtinTypeDefinitions.get(UNSIGNEDINT_NAME)));
		builtinTypeDefinitions.put(UNSIGNEDBYTE_NAME, create(UNSIGNEDBYTE_NAME, builtinTypeDefinitions.get(UNSIGNEDINT_NAME)));
		builtinTypeDefinitions.put(POSITIVEINTEGER_NAME, create(POSITIVEINTEGER_NAME, builtinTypeDefinitions.get(NONNEGATIVEINTEGER_NAME)));
		builtinTypeDefinitions.put(YEARMONTHDURATION_NAME, create(YEARMONTHDURATION_NAME, PRIMITIVE_TYPE_DEFINITIONS.get(DURATION_NAME)));
		builtinTypeDefinitions.put(DAYTIMEDURATION_NAME, create(DAYTIMEDURATION_NAME, PRIMITIVE_TYPE_DEFINITIONS.get(DURATION_NAME)));
		builtinTypeDefinitions.put(DATETIMESTAMP_NAME, create(DATETIMESTAMP_NAME, PRIMITIVE_TYPE_DEFINITIONS.get(DATETIME_NAME)));
		if (builtinTypeDefinitions.size() != 28) {
			throw new ExceptionInInitializerError("Wrong number of built-in type definitions: " + builtinTypeDefinitions.size() + ", " + builtinTypeDefinitions);
		}
		BUILTIN_TYPE_DEFINITIONS = Collections.unmodifiableMap(builtinTypeDefinitions);
	}

	private final Schema schema;
	private final Deferred<? extends AnnotatedComponent> context;
	private final Node node;
	private final Deque<Annotation> annotations;
	private final String name;
	private final String targetNamespace;
	private final Deferred<? extends TypeDefinition> baseTypeDefinition;
	private final Deque<Final> finals;
	private final Deferred<Variety> variety;
	private final Deque<Object> facets;
	private final Deque<ConstrainingFacet> facetValues;
	private final Deque<FundamentalFacet> fundamentalFacets;
	private final Deferred<SimpleType> primitiveTypeDefinition;
	private final Deferred<SimpleType> itemTypeDefinition;
	private final Deque<SimpleType> memberTypeDefinitions;

	SimpleType(final Schema schema, final Deferred<? extends AnnotatedComponent> context, final Node node, final Deque<Annotation> annotations, final String name, final String targetNamespace, final Deque<Final> finals, final Deferred<? extends TypeDefinition> baseTypeDefinition, final Deferred<Variety> variety, final Deque<Object> facets, final Deferred<Restriction> restriction, final Deferred<List> list, final Deferred<Union> union) {
		this.schema = Objects.requireNonNull(schema);
		this.context = Objects.requireNonNull(context);
		this.node = Objects.requireNonNull(node);
		this.annotations = Objects.requireNonNull(annotations);
		this.name = name;
		this.targetNamespace = NodeHelper.requireNonEmpty(node, targetNamespace);
		this.baseTypeDefinition = Objects.requireNonNull(baseTypeDefinition);
		this.finals = Objects.requireNonNull(finals);
		this.variety = Objects.requireNonNull(variety);
		this.facets = Objects.requireNonNull(facets);
		this.facetValues = new DeferredArrayDeque<>(() -> facets.stream().filter(ConstrainingFacet.class::isInstance).map(ConstrainingFacet.class::cast).collect(Collectors.toCollection(ArrayDeque::new)));
		this.fundamentalFacets = new DeferredArrayDeque<>(() -> {
			final Deque<FundamentalFacet> f = FundamentalFacet.find(this);
			if (f != null) {
				return f;
			}
			final TypeDefinition baseType = baseTypeDefinition.get();
			if (baseType instanceof ComplexType) {
				return Deques.emptyDeque();
			}
			return ((SimpleType) baseType).fundamentalFacets();
		});
		this.primitiveTypeDefinition = variety.map(v -> {
			if (Variety.ATOMIC.equals(v) && this != xsAnyAtomicType()) {
				if (PRIMITIVE_TYPE_DEFINITIONS.values().stream().anyMatch(s -> s.get() == this)) {
					return this;
				}
				final TypeDefinition baseType = baseTypeDefinition();
				if (baseType instanceof SimpleType) {
					return ((SimpleType) baseTypeDefinition()).primitiveTypeDefinition();
				}
			}
			return null;
		});
		this.itemTypeDefinition = variety.map(v -> {
			if (Variety.LIST.equals(v)) {
				final SimpleType itemSimpleType = restriction != null
						? restriction.get().baseTypeDefinition().get().itemTypeDefinition()
						: list.get().itemTypeDefinition().get();
				if (Variety.ATOMIC.equals(itemSimpleType.variety()) || (Variety.UNION.equals(itemSimpleType.variety()) && itemSimpleType.memberTypeDefinitions().stream().allMatch(s -> Variety.ATOMIC.equals(s.variety())))) {
					return itemSimpleType;
				}
				throw new Schema.ParseException(node, "simpleType with variety List must be composed of either a simpleType with variety Atomic or one with variety Union where all memberTypeDefinitions have variety Atomic");
			}
			return null;
		});
		this.memberTypeDefinitions = variety.mapToDeque(v -> {
			if (Variety.UNION.equals(v)) {
				return restriction != null
						? restriction.get().baseTypeDefinition().get().memberTypeDefinitions()
						: union.get().memberTypeDefinitions();
			}
			return Deques.emptyDeque();
		});
	}

	private static SimpleType parse(final Result result) {
		final Schema schema = result.schema();
		final Deferred<? extends AnnotatedComponent> context = result.context();
		final Node node = result.node();
		final String name = result.value(AttrParser.NAME);
		final String targetNamespace = result.schema().targetNamespace();
		Deque<Final> finals = result.value(AttrParser.FINAL);
		if (finals.isEmpty()) {
			finals = Deques.singletonDeque(result.schema().finalDefault());
		}
		final Deferred<Restriction> restriction = result.parse(TagParser.SIMPLE_TYPE.restriction());
		final Deferred<List> list = result.parse(TagParser.SIMPLE_TYPE.list());
		final Deferred<Union> union = result.parse(TagParser.SIMPLE_TYPE.union());
		final Deferred<Variety> variety = list != null ? () -> Variety.LIST
				: union != null ? () -> Variety.UNION
				: restriction.map(r -> r.baseTypeDefinition().get().variety());
		final Deferred<SimpleType> baseType = variety.map(v -> Variety.LIST.equals(v) || Variety.UNION.equals(v) ? xsAnySimpleType() : restriction.get().baseTypeDefinition().get());
		final DeferredValue<SimpleType> self = new DeferredValue<>();
		final Deque<Object> facets = variety.mapToDeque(v -> {
			switch (v) {
			case UNION:
				return Deques.asDeque(Pattern.class, ConstrainingFacet.Enumeration.class, Assertions.class);
			case LIST:
				return restriction != null // standalone xs:list vs xs:restriction of xs:list
						? ConstrainingFacet.combineLikeFacets(restriction.get().baseTypeDefinition().get(), restriction.get().baseTypeDefinition().get().facets, restriction.get().facets())
						: Deques.asDeque(WhiteSpace.collapseFixed.apply(self), Length.class, MaxLength.class, MinLength.class, ConstrainingFacet.Enumeration.class, Pattern.class, Assertions.class);
			case ATOMIC:
				return ConstrainingFacet.combineLikeFacets(baseType.get(), baseType.get().facets, restriction.get().facets());
			default:
				throw new AssertionError();
			}
		});
		final AnnotationSet annotations = Annotation.of(result);
		if (list != null) {
			annotations.add(list, List::annotations);
		} else if (union != null) {
			annotations.add(union, Union::annotations);
		} else {
			annotations.add(restriction, Restriction::annotations);
		}
		return self.set(new SimpleType(schema, context, node, annotations.resolve(node), name, targetNamespace, finals, baseType, variety, facets, restriction, list, union));
	}

	private static Deferred<SimpleType> create(final String name, final Deferred<? extends TypeDefinition> baseTypeDefinition) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(baseTypeDefinition);
		return Deferred.of(() -> {
			final DeferredValue<SimpleType> self = new DeferredValue<>();
			final Deque<Object> facets = ANYATOMICTYPE_NAME.equals(name) ? Deques.emptyDeque() : ConstrainingFacet.find(self, name);
			Objects.requireNonNull(facets);
			final Variety variety;
			final List list;
			switch (name) {
			case ENTITIES_NAME:
				list = new List(AnnotationSet.EMPTY, SimpleType::xsENTITY);
				variety = Variety.LIST;
				break;
			case IDREFS_NAME:
				list = new List(AnnotationSet.EMPTY, SimpleType::xsIDREF);
				variety = Variety.LIST;
				break;
			case NMTOKENS_NAME:
				list = new List(AnnotationSet.EMPTY, SimpleType::xsNMTOKEN);
				variety = Variety.LIST;
				break;
			default:
				list = null;
				variety = Variety.ATOMIC;
			}
			final Node node = NodeHelper.newGlobalNode(Schema.XSD, TagParser.Names.SIMPLE_TYPE, name);
			return self.set(new SimpleType(Schema.XSD, () -> Schema.XSD, node, Deques.emptyDeque(), name, XMLConstants.W3C_XML_SCHEMA_NS_URI, Deques.emptyDeque(), baseTypeDefinition, () -> variety, facets, null, () -> list, null));
		});
	}

	static void register() {
		AttrParser.register(AttrParser.Names.ITEM_TYPE, QName.class, NodeHelper::getAttrValueAsQName);
		AttrParser.register(AttrParser.Names.MEMBER_TYPES, Deque.class, QName.class, null, NodeHelper::getAttrValueAsQNames);
		TagParser.register(TagParser.Names.RESTRICTION, Restriction.parser, Restriction.class, Restriction::parse);
		TagParser.register(TagParser.Names.LIST, List.parser, List.class, List::parse);
		TagParser.register(TagParser.Names.UNION, Union.parser, Union.class, Union::parse);
		TagParser.register(TagParser.Names.SIMPLE_TYPE, SimpleType.parser, SimpleType.class, SimpleType::parse);
		VisitorHelper.register(SimpleType.class, SimpleType::visit);
	}

	static SimpleType findPrimitiveOrBuiltinType(final String localName) {
		Deferred<SimpleType> s = PRIMITIVE_TYPE_DEFINITIONS.get(localName);
		if (s != null) {
			return s.get();
		}
		s = BUILTIN_TYPE_DEFINITIONS.get(localName);
		if (s != null) {
			return s.get();
		}
		throw new IllegalArgumentException("No primitive or built-in simpleType for name " + localName);
	}

	static SimpleType wrap(final Schema schema, final Deferred<? extends AnnotatedComponent> context, final Node node, final Deque<Annotation> annotations, final String name, final String targetNamespace, final Deque<Final> finals, final SimpleType baseSimpleType, final Deque<ConstrainingFacet> declaredFacets) {
		final Deque<Object> facets = ConstrainingFacet.combineLikeFacets(baseSimpleType, baseSimpleType.facets, declaredFacets);
		return new SimpleType(schema, context, node, annotations, name, targetNamespace, finals, () -> baseSimpleType, baseSimpleType.variety, facets, null, null, null) {

			@Override
			public SimpleType primitiveTypeDefinition() {
				return baseSimpleType.primitiveTypeDefinition();
			}

			@Override
			public SimpleType itemTypeDefinition() {
				return baseSimpleType.itemTypeDefinition();
			}

			@Override
			public Deque<SimpleType> memberTypeDefinitions() {
				return baseSimpleType.memberTypeDefinitions();
			}

		};
	}

	/**
	 * The definition of anySimpleType is a special ·restriction· of anyType. The ·lexical space· of anySimpleType is the set of all sequences of Unicode characters, and its ·value space· includes all ·atomic values· and all finite-length lists of zero or more ·atomic values·.
	 * @return <a href="https://www.w3.org/TR/xmlschema11-2/#anySimpleType">https://www.w3.org/TR/xmlschema11-2/#anySimpleType</a>
	 */
	public static SimpleType xsAnySimpleType() {
		return xsAnySimpleType.get();
	}

	/**
	 * The definition of anyAtomicType is a special ·restriction· of anySimpleType. The ·value· and ·lexical spaces· of anyAtomicType are the unions of the ·value· and ·lexical spaces· of all the ·primitive· datatypes, and anyAtomicType is their ·base type·.
	 * @return <a href="https://www.w3.org/TR/xmlschema11-2/#anyAtomicType">https://www.w3.org/TR/xmlschema11-2/#anyAtomicType</a>
	 */
	public static SimpleType xsAnyAtomicType() {
		return xsAnyAtomicType.get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#string">http://www.w3.org/TR/xmlschema11-2/#string</a> */
	public static SimpleType xsString() {
		return PRIMITIVE_TYPE_DEFINITIONS.get(STRING_NAME).get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#boolean">http://www.w3.org/TR/xmlschema11-2/#boolean</a> */
	public static SimpleType xsBoolean() {
		return PRIMITIVE_TYPE_DEFINITIONS.get(BOOLEAN_NAME).get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#float">http://www.w3.org/TR/xmlschema11-2/#float</a> */
	public static SimpleType xsFloat() {
		return PRIMITIVE_TYPE_DEFINITIONS.get(FLOAT_NAME).get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#double">http://www.w3.org/TR/xmlschema11-2/#double</a> */
	public static SimpleType xsDouble() {
		return PRIMITIVE_TYPE_DEFINITIONS.get(DOUBLE_NAME).get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#decimal">http://www.w3.org/TR/xmlschema11-2/#decimal</a> */
	public static SimpleType xsDecimal() {
		return PRIMITIVE_TYPE_DEFINITIONS.get(DECIMAL_NAME).get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#duration">http://www.w3.org/TR/xmlschema11-2/#duration</a> */
	public static SimpleType xsDuration() {
		return PRIMITIVE_TYPE_DEFINITIONS.get(DURATION_NAME).get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#dateTime">http://www.w3.org/TR/xmlschema11-2/#dateTime</a> */
	public static SimpleType xsDateTime() {
		return PRIMITIVE_TYPE_DEFINITIONS.get(DATETIME_NAME).get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#time">http://www.w3.org/TR/xmlschema11-2/#time</a> */
	public static SimpleType xsTime() {
		return PRIMITIVE_TYPE_DEFINITIONS.get(TIME_NAME).get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#date">http://www.w3.org/TR/xmlschema11-2/#date</a> */
	public static SimpleType xsDate() {
		return PRIMITIVE_TYPE_DEFINITIONS.get(DATE_NAME).get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#gYearMonth">http://www.w3.org/TR/xmlschema11-2/#gYearMonth</a> */
	public static SimpleType xsGYearMonth() {
		return PRIMITIVE_TYPE_DEFINITIONS.get(GYEARMONTH_NAME).get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#gYear">http://www.w3.org/TR/xmlschema11-2/#gYear</a> */
	public static SimpleType xsGYear() {
		return PRIMITIVE_TYPE_DEFINITIONS.get(GYEAR_NAME).get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#gMonthDay">http://www.w3.org/TR/xmlschema11-2/#gMonthDay</a> */
	public static SimpleType xsGMonthDay() {
		return PRIMITIVE_TYPE_DEFINITIONS.get(GMONTHDAY_NAME).get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#gDay">http://www.w3.org/TR/xmlschema11-2/#gDay</a> */
	public static SimpleType xsGDay() {
		return PRIMITIVE_TYPE_DEFINITIONS.get(GDAY_NAME).get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#gMonth">http://www.w3.org/TR/xmlschema11-2/#gMonth</a> */
	public static SimpleType xsGMonth() {
		return PRIMITIVE_TYPE_DEFINITIONS.get(GMONTH_NAME).get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#hexBinary">http://www.w3.org/TR/xmlschema11-2/#hexBinary</a> */
	public static SimpleType xsHexBinary() {
		return PRIMITIVE_TYPE_DEFINITIONS.get(HEXBINARY_NAME).get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#base64Binary">http://www.w3.org/TR/xmlschema11-2/#base64Binary</a> */
	public static SimpleType xsBase64Binary() {
		return PRIMITIVE_TYPE_DEFINITIONS.get(BASE64BINARY_NAME).get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#anyURI">http://www.w3.org/TR/xmlschema11-2/#anyURI</a> */
	public static SimpleType xsAnyURI() {
		return PRIMITIVE_TYPE_DEFINITIONS.get(ANYURI_NAME).get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#QName">http://www.w3.org/TR/xmlschema11-2/#QName</a> */
	public static SimpleType xsQName() {
		return PRIMITIVE_TYPE_DEFINITIONS.get(QNAME_NAME).get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#NOTATION">http://www.w3.org/TR/xmlschema11-2/#NOTATION</a> */
	public static SimpleType xsNOTATION() {
		return PRIMITIVE_TYPE_DEFINITIONS.get(NOTATION_NAME).get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#normalizedString">http://www.w3.org/TR/xmlschema11-2/#normalizedString</a> */
	public static SimpleType xsNormalizedString() {
		return BUILTIN_TYPE_DEFINITIONS.get(NORMALIZEDSTRING_NAME).get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#token">http://www.w3.org/TR/xmlschema11-2/#token</a> */
	public static SimpleType xsToken() {
		return BUILTIN_TYPE_DEFINITIONS.get(TOKEN_NAME).get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#language">http://www.w3.org/TR/xmlschema11-2/#language</a> */
	public static SimpleType xsLanguage() {
		return BUILTIN_TYPE_DEFINITIONS.get(LANGUAGE_NAME).get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#IDREFS">http://www.w3.org/TR/xmlschema11-2/#IDREFS</a> */
	public static SimpleType xsIDREFS() {
		return BUILTIN_TYPE_DEFINITIONS.get(IDREFS_NAME).get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#ENTITIES">http://www.w3.org/TR/xmlschema11-2/#ENTITIES</a> */
	public static SimpleType xsENTITIES() {
		return BUILTIN_TYPE_DEFINITIONS.get(ENTITIES_NAME).get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#NMTOKEN">http://www.w3.org/TR/xmlschema11-2/#NMTOKEN</a> */
	public static SimpleType xsNMTOKEN() {
		return BUILTIN_TYPE_DEFINITIONS.get(NMTOKEN_NAME).get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#NMTOKENS">http://www.w3.org/TR/xmlschema11-2/#NMTOKENS</a> */
	public static SimpleType xsNMTOKENS() {
		return BUILTIN_TYPE_DEFINITIONS.get(NMTOKENS_NAME).get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#Name">http://www.w3.org/TR/xmlschema11-2/#Name</a> */
	public static SimpleType xsName() {
		return BUILTIN_TYPE_DEFINITIONS.get(NAME_NAME).get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#NCName">http://www.w3.org/TR/xmlschema11-2/#NCName</a> */
	public static SimpleType xsNCName() {
		return BUILTIN_TYPE_DEFINITIONS.get(NCNAME_NAME).get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#ID">http://www.w3.org/TR/xmlschema11-2/#ID</a> */
	public static SimpleType xsID() {
		return BUILTIN_TYPE_DEFINITIONS.get(ID_NAME).get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#IDREF">http://www.w3.org/TR/xmlschema11-2/#IDREF</a> */
	public static SimpleType xsIDREF() {
		return BUILTIN_TYPE_DEFINITIONS.get(IDREF_NAME).get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#ENTITY">http://www.w3.org/TR/xmlschema11-2/#ENTITY</a> */
	public static SimpleType xsENTITY() {
		return BUILTIN_TYPE_DEFINITIONS.get(ENTITY_NAME).get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#integer">http://www.w3.org/TR/xmlschema11-2/#integer</a> */
	public static SimpleType xsInteger() {
		return BUILTIN_TYPE_DEFINITIONS.get(INTEGER_NAME).get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#nonPositiveInteger">http://www.w3.org/TR/xmlschema11-2/#nonPositiveInteger</a> */
	public static SimpleType xsNonPositiveInteger() {
		return BUILTIN_TYPE_DEFINITIONS.get(NONPOSITIVEINTEGER_NAME).get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#negativeInteger">http://www.w3.org/TR/xmlschema11-2/#negativeInteger</a> */
	public static SimpleType xsNegativeInteger() {
		return BUILTIN_TYPE_DEFINITIONS.get(NEGATIVEINTEGER_NAME).get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#long">http://www.w3.org/TR/xmlschema11-2/#long</a> */
	public static SimpleType xsLong() {
		return BUILTIN_TYPE_DEFINITIONS.get(LONG_NAME).get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#int">http://www.w3.org/TR/xmlschema11-2/#int</a> */
	public static SimpleType xsInt() {
		return BUILTIN_TYPE_DEFINITIONS.get(INT_NAME).get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#short">http://www.w3.org/TR/xmlschema11-2/#short</a> */
	public static SimpleType xsShort() {
		return BUILTIN_TYPE_DEFINITIONS.get(SHORT_NAME).get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#byte">http://www.w3.org/TR/xmlschema11-2/#byte</a> */
	public static SimpleType xsByte() {
		return BUILTIN_TYPE_DEFINITIONS.get(BYTE_NAME).get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#nonNegativeInteger">http://www.w3.org/TR/xmlschema11-2/#nonNegativeInteger</a> */
	public static SimpleType xsNonNegativeInteger() {
		return BUILTIN_TYPE_DEFINITIONS.get(NONNEGATIVEINTEGER_NAME).get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#unsignedLong">http://www.w3.org/TR/xmlschema11-2/#unsignedLong</a> */
	public static SimpleType xsUnsignedLong() {
		return BUILTIN_TYPE_DEFINITIONS.get(UNSIGNEDLONG_NAME).get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#unsignedInt">http://www.w3.org/TR/xmlschema11-2/#unsignedInt</a> */
	public static SimpleType xsUnsignedInt() {
		return BUILTIN_TYPE_DEFINITIONS.get(UNSIGNEDINT_NAME).get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#unsignedShort">http://www.w3.org/TR/xmlschema11-2/#unsignedShort</a> */
	public static SimpleType xsUnsignedShort() {
		return BUILTIN_TYPE_DEFINITIONS.get(UNSIGNEDSHORT_NAME).get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#unsignedByte">http://www.w3.org/TR/xmlschema11-2/#unsignedByte</a> */
	public static SimpleType xsUnsignedByte() {
		return BUILTIN_TYPE_DEFINITIONS.get(UNSIGNEDBYTE_NAME).get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#positiveInteger">http://www.w3.org/TR/xmlschema11-2/#positiveInteger</a> */
	public static SimpleType xsPositiveInteger() {
		return BUILTIN_TYPE_DEFINITIONS.get(POSITIVEINTEGER_NAME).get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#yearMonthDuration">http://www.w3.org/TR/xmlschema11-2/#yearMonthDuration</a> */
	public static SimpleType xsYearMonthDuration() {
		return BUILTIN_TYPE_DEFINITIONS.get(YEARMONTHDURATION_NAME).get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#dayTimeDuration">http://www.w3.org/TR/xmlschema11-2/#dayTimeDuration</a> */
	public static SimpleType xsDayTimeDuration() {
		return BUILTIN_TYPE_DEFINITIONS.get(DAYTIMEDURATION_NAME).get();
	}

	/** @return <a href="http://www.w3.org/TR/xmlschema11-2/#dateTimeStamp">http://www.w3.org/TR/xmlschema11-2/#dateTimeStamp</a> */
	public static SimpleType xsDateTimeStamp() {
		return BUILTIN_TYPE_DEFINITIONS.get(DATETIMESTAMP_NAME).get();
	}

	Object valueSpace(final String value, final String lexicalForm) {
		if (Variety.ATOMIC.equals(variety())) {
			switch (primitiveTypeDefinition().name()) {
			case STRING_NAME:
				return lexicalForm;
			case BOOLEAN_NAME:
				return NodeHelper.getNodeValueAsBoolean(node, lexicalForm);
			case DECIMAL_NAME:
				final OptionalInt fractionDigits = facets().stream().filter(ConstrainingFacet.FractionDigits.class::isInstance).mapToInt(f -> ((ConstrainingFacet.FractionDigits) f).value().intValue()).findAny();
				if (fractionDigits.isPresent() && fractionDigits.getAsInt() == 0) {
					return NodeHelper.getNodeValueAsInteger(node, lexicalForm);
				}
				// fallthrough
			case FLOAT_NAME:
			case DOUBLE_NAME:
				return NodeHelper.getNodeValueAsDecimal(node, lexicalForm);
			case DURATION_NAME:
				return NodeHelper.getNodeValueAsDuration(node, lexicalForm);
			case DATETIME_NAME:
				return NodeHelper.getNodeValueAsDateTime(node, lexicalForm);
			case TIME_NAME:
				return NodeHelper.getNodeValueAsTime(node, lexicalForm);
			case DATE_NAME:
				return NodeHelper.getNodeValueAsDate(node, lexicalForm);
			case GYEARMONTH_NAME:
				return NodeHelper.getNodeValueAsGYearMonth(node, lexicalForm);
			case GYEAR_NAME:
				return NodeHelper.getNodeValueAsGYear(node, lexicalForm);
			case GMONTHDAY_NAME:
				return NodeHelper.getNodeValueAsGMonthDay(node, lexicalForm);
			case GDAY_NAME:
				return NodeHelper.getNodeValueAsGDay(node, lexicalForm);
			case GMONTH_NAME:
				return NodeHelper.getNodeValueAsGMonth(node, lexicalForm);
			case HEXBINARY_NAME:
				return NodeHelper.getNodeValueAsHexBinary(node, lexicalForm);
			case BASE64BINARY_NAME:
				return NodeHelper.getNodeValueAsBase64Binary(node, lexicalForm);
			case ANYURI_NAME:
				return NodeHelper.getNodeValueAsAnyUri(node, lexicalForm);
			case QNAME_NAME:
				return NodeHelper.getNodeValueAsQName(node, lexicalForm);
			case NOTATION_NAME:
				final QName notation = NodeHelper.getNodeValueAsQName(node, lexicalForm);
				if (schema.notationDeclarations().stream().noneMatch(n -> NodeHelper.equalsName(notation, n))) {
					throw NodeHelper.newFacetException(node, value, (targetNamespace != null ? '{' + targetNamespace + '}' : "") + name);
				}
				return notation;
			default:
				throw new AssertionError();
			}
		}
		return lexicalForm;
	}

	String lexicalMapping(final String value) {
		if (this == xsAnySimpleType()) {
			return value;
		}
		final WhiteSpace.Value whiteSpaceValue;
		switch (variety()) {
		case ATOMIC:
			if (this == xsString()) {
				whiteSpaceValue = WhiteSpace.Value.PRESERVE;
			} else if (primitiveTypeDefinition() == xsString()) {
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

	void visit(final Visitor visitor) {
		if (visitor.visit(context.get(), node, this)) {
			visitor.onSimpleType(context.get(), node, this);
			annotations.forEach(a -> a.visit(visitor));
			ComplexType.visitTypeDefinition(baseTypeDefinition(), visitor);
			facets().forEach(f -> f.visit(visitor));
			fundamentalFacets().forEach(f -> f.visit(visitor));
			if (primitiveTypeDefinition() != null) {
				primitiveTypeDefinition().visit(visitor);
			}
			if (itemTypeDefinition() != null) {
				itemTypeDefinition().visit(visitor);
			}
			memberTypeDefinitions().forEach(m -> m.visit(visitor));
		}
	}

	/** @return If the &lt;list&gt; alternative is chosen, then list, otherwise if the &lt;union&gt; alternative is chosen, then union, otherwise (the &lt;restriction&gt; alternative is chosen), then the {variety} of the {base type definition}. */
	public Variety variety() {
		return variety.get();
	}

	/**
	 * @return The appropriate case among the following:
	 * <ol>
	 *   <li>If the &lt;restriction&gt; alternative is chosen and the children of the &lt;restriction&gt; element are all either &lt;simpleType&gt; elements, &lt;annotation&gt; elements, or elements which specify constraining facets supported by the processor, then the set of Constraining Facet components obtained by ·overlaying· the {facets} of the {base type definition} with the set of Constraining Facet components corresponding to those [children] of &lt;restriction&gt; which specify facets, as defined in Simple Type Restriction (Facets) (§3.16.6.4).</li>
	 *   <li>If the &lt;restriction&gt; alternative is chosen and the children of the &lt;restriction&gt; element include at least one element of which the processor has no prior knowledge (i.e. not a &lt;simpleType&gt; element, an &lt;annotation&gt; element, or an element denoting a constraining facet known to and supported by the processor), then the &lt;simpleType&gt; element maps to no component at all (but is not in error solely on account of the presence of the unknown element).</li>
	 *   <li>If the &lt;list&gt; alternative is chosen, then a set with one member, a whiteSpace facet with {value} = collapse and {fixed} = true.</li>
	 *   <li>otherwise the empty set</li>
	 * </ol>
	 */
	public Deque<ConstrainingFacet> facets() {
		return Deques.unmodifiableDeque(facetValues);
	}

	/** @return Based on {variety}, {facets}, {base type definition} and {member type definitions}, a set of Fundamental Facet components, one each as specified in The ordered Schema Component, The bounded Schema Component, The cardinality Schema Component and The numeric Schema Component . */
	public Deque<FundamentalFacet> fundamentalFacets() {
		return Deques.unmodifiableDeque(fundamentalFacets);
	}

	/** @return From among the ·ancestors· of this Simple Type Definition, that Simple Type Definition which corresponds to a primitive datatype. */
	public SimpleType primitiveTypeDefinition() {
		return primitiveTypeDefinition != null ? primitiveTypeDefinition.get() : null;
	}

	/**
	 * @return The appropriate case among the following:
	 * <ol>
	 *   <li>If the {base type definition} is ·xs:anySimpleType·, then the Simple Type Definition (a) ·resolved· to by the ·actual value· of the itemType [attribute] of &lt;list&gt;, or (b), corresponding to the &lt;simpleType&gt; among the [children] of &lt;list&gt;, whichever is present.<br><i>Note: In this case, a &lt;list&gt; element will invariably be present; it will invariably have either an itemType [attribute] or a &lt;simpleType&gt; [child], but not both.</i></li>
	 *   <li>otherwise (that is, the {base type definition} is not ·xs:anySimpleType·), the {item type definition} of the {base type definition}.<br><i>Note: In this case, a &lt;restriction&gt; element will invariably be present.</i></li>
	 * </ol>
	 */
	public SimpleType itemTypeDefinition() {
		return itemTypeDefinition != null ? itemTypeDefinition.get() : null;
	}

	/**
	 * @return The appropriate case among the following:
	 * <ol>
	 *   <li>If the {base type definition} is ·xs:anySimpleType·, then the sequence of Simple Type Definitions (a) ·resolved· to by the items in the ·actual value· of the memberTypes [attribute] of &lt;union&gt;, if any, and (b) corresponding to the &lt;simpleType&gt;s among the [children] of &lt;union&gt;, if any, in order.<br><i>Note: In this case, a &lt;union&gt; element will invariably be present; it will invariably have either a memberTypes [attribute] or one or more &lt;simpleType&gt; [children], or both.</i></li>
	 *   <li>otherwise (that is, the {base type definition} is not ·xs:anySimpleType·), the {member type definitions} of the {base type definition}.<br><i>Note: In this case, a &lt;restriction&gt; element will invariably be present.</i></li>
	 * </ol>
	 */
	public Deque<SimpleType> memberTypeDefinitions() {
		return Deques.unmodifiableDeque(memberTypeDefinitions);
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

	/**
	 * @return The appropriate case among the following:
	 * <ol>
	 *   <li>If the &lt;restriction&gt; alternative is chosen, then the type definition ·resolved· to by the ·actual value· of the base [attribute] of &lt;restriction&gt;, if present, otherwise the type definition corresponding to the &lt;simpleType&gt; among the [children] of &lt;restriction&gt;.</li>
	 *   <li>If the &lt;list&gt; or &lt;union&gt; alternative is chosen, then ·xs:anySimpleType·.</li>
	 * </ol>
	 */
	@Override
	public TypeDefinition baseTypeDefinition() {
		return baseTypeDefinition.get();
	}

	/**
	 * @return A subset of {restriction, extension, list, union}, determined as follows. Let FS be the ·actual value· of the final [attribute], if present, otherwise the ·actual value· of the finalDefault [attribute] of the ancestor schema element, if present, otherwise the empty string. Then the property value is the appropriate case among the following:
	 * <ol>
	 *   <li>If ·FS· is the empty string, then the empty set;</li>
	 *   <li>If ·FS· is "#all", then {restriction, extension, list, union};</li>
	 *   <li>otherwise Consider ·FS· as a space-separated list, and include restriction if "restriction" is in that list, and similarly for extension, list and union.</li>
	 * </ol>
	 */
	@Override
	public Deque<Final> finals() {
		return Deques.unmodifiableDeque(finals);
	}

	/**
	 * @return The appropriate case among the following:
	 * <ol>
	 *   <li>If the name [attribute] is present, then ·absent·</li>
	 *   <li>otherwise the appropriate case among the following:
	 *     <ol>
	 *       <li>If the parent element information item is &lt;attribute&gt;, then the corresponding Attribute Declaration</li>
	 *       <li>If the parent element information item is &lt;element&gt;, then the corresponding Element Declaration</li>
	 *       <li>If the parent element information item is &lt;list&gt; or &lt;union&gt;, then the Simple Type Definition corresponding to the grandparent &lt;simpleType&gt; element information item</li>
	 *       <li>If the parent element information item is &lt;alternative&gt;, then the Element Declaration corresponding to the nearest enclosing &lt;element&gt; element information item</li>
	 *       <li>otherwise (the parent element information item is &lt;restriction&gt;), the appropriate case among the following:
	 *         <ol>
	 *           <li>If the grandparent element information item is &lt;simpleType&gt;, then the Simple Type Definition corresponding to the grandparent</li>
	 *           <li>otherwise (the grandparent element information item is &lt;simpleContent&gt;), the Simple Type Definition which is the {content type}.{simple type definition} of the Complex Type Definition corresponding to the great-grandparent &lt;complexType&gt; element information item.</li>
	 *         </ol>
	 *       </li>
	 *     </ol>
	 *   </li>
	 * </ol>
	 */
	@Override
	public AnnotatedComponent context() {
		return name == null ? context.get() : null;
	}

}
