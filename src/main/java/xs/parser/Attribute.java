package xs.parser;

import java.util.*;
import javax.xml.*;
import javax.xml.namespace.*;
import org.w3c.dom.*;
import xs.parser.Annotation.*;
import xs.parser.Schema.*;
import xs.parser.internal.*;
import xs.parser.internal.util.*;
import xs.parser.internal.util.SequenceParser.*;

/**
 * <pre>
 * &lt;attribute
 *   default = string
 *   fixed = string
 *   form = (qualified | unqualified)
 *   id = ID
 *   name = NCName
 *   ref = QName
 *   targetNamespace = anyURI
 *   type = QName
 *   inheritable = boolean
 *   {any attributes with non-schema namespace . . .}&gt;
 *   Content: (annotation?, simpleType?)
 * &lt;/attribute&gt;
 * </pre>
 *
 * <table>
 *   <caption style="font-size: large; text-align: left">Schema Component: Attribute Declaration, a kind of Annotated Component</caption>
 *   <thead>
 *     <tr>
 *       <th style="text-align: left">Method</th>
 *       <th style="text-align: left">Property</th>
 *       <th style="text-align: left">Representation</th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <td>{@link Attribute#annotations()}</td>
 *       <td>{annotations}</td>
 *       <td>A sequence of Annotation components.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link Attribute#name()}</td>
 *       <td>{name}</td>
 *       <td>An xs:NCName value. Required.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link Attribute#targetNamespace()}</td>
 *       <td>{target namespace}</td>
 *       <td>An xs:anyURI value. Optional.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link Attribute#typeDefinition()}</td>
 *       <td>{type definition}</td>
 *       <td>A Simple Type Definition component. Required.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link Attribute#scope()}</td>
 *       <td>{scope}</td>
 *       <td>A Scope property record. Required.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link Attribute#valueConstraint()}</td>
 *       <td>{value constraint}</td>
 *       <td>A Value Constraint property record. Optional.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link Attribute#inheritable()}</td>
 *       <td>{inheritable}</td>
 *       <td>An xs:boolean value. Required.</td>
 *     </tr>
 *   </tbody>
 * </table>
 */
public class Attribute implements AnnotatedComponent {

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
	 *       <td>Either a Complex Type Definition or a Attribute Group Definition. Required if {variety} is local, otherwise must be ·absent·</td>
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
		private final Node parent;

		Scope(final Variety variety, final Node parent) {
			if ((Variety.LOCAL.equals(variety) && parent == null) || (Variety.GLOBAL.equals(variety) && parent != null)) {
				throw new IllegalArgumentException(variety.toString());
			}
			this.variety = Objects.requireNonNull(variety);
			this.parent = parent;
		}

		/** @return either local or global, as appropriate */
		public Variety variety() {
			return variety;
		}

		/** @return If the &lt;attribute&gt; element information item has &lt;complexType&gt; as an ancestor, the Complex Type Definition corresponding to that item, otherwise (the &lt;attribute&gt; element information item is within an &lt;attributeGroup&gt; element information item), the Attribute Group Definition corresponding to that item or ·absent·. */
		public Node parent() {
			return parent;
		}

	}

	public enum Use {

		REQUIRED("required"),
		OPTIONAL("optional"),
		PROHIBITED("prohibited");

		private final String name;

		Use(final String name) {
			this.name = name;
		}

		private static Use getAttrValueAsUse(final Attr attr) {
			final String value = NodeHelper.collapseWhitespace(attr.getValue());
			for (final Use u : values()) {
				if (u.getName().equals(value)) {
					return u;
				}
			}
			throw new IllegalArgumentException(value);
		}

		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return getName();
		}

	}

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
	 *       <td>{@link ValueConstraint#variety()}</td>
	 *       <td>{variety}</td>
	 *       <td>One of {default, fixed}. Required.</td>
	 *     </tr>
	 *     <tr>
	 *       <td>{@link ValueConstraint#value()}</td>
	 *       <td>{value}</td>
	 *       <td>An ·actual value·. Required.</td>
	 *     </tr>
	 *     <tr>
	 *       <td>{@link ValueConstraint#lexicalForm()}</td>
	 *       <td>{lexical form}</td>
	 *       <td>A character string. Required.</td>
	 *     </tr>
	 *   </tbody>
	 * </table>
	 */
	public static class ValueConstraint {

		public enum Variety {

			DEFAULT,
			FIXED;

		}

		private final Variety variety;
		private final Deferred<Object> value;
		private final Deferred<String> lexicalForm;

		ValueConstraint(final Schema schema, final Deferred<SimpleType> simpleType, final Variety variety, final String value) {
			Objects.requireNonNull(simpleType);
			this.variety = variety;
			this.value = simpleType.map(s -> {
				if (SimpleType.Variety.ATOMIC.equals(s.variety())) {
					switch (s.primitiveTypeDefinition().name()) {
					case SimpleType.STRING_NAME:
						return lexicalForm();
					case SimpleType.BOOLEAN_NAME:
						return NodeHelper.getNodeValueAsBoolean(s.node(), lexicalForm());
					case SimpleType.DECIMAL_NAME:
						final OptionalInt fractionDigits = s.facets().stream().filter(ConstrainingFacet.FractionDigits.class::isInstance).mapToInt(f -> ((ConstrainingFacet.FractionDigits) f).value().intValue()).findAny();
						if (fractionDigits.isPresent() && fractionDigits.getAsInt() == 0) {
							return NodeHelper.getNodeValueAsInteger(s.node(), lexicalForm());
						}
						// fallthrough
					case SimpleType.FLOAT_NAME:
					case SimpleType.DOUBLE_NAME:
						return NodeHelper.getNodeValueAsDecimal(s.node(), lexicalForm());
					case SimpleType.DURATION_NAME:
						return NodeHelper.getNodeValueAsDuration(s.node(), lexicalForm());
					case SimpleType.DATETIME_NAME:
						return NodeHelper.getNodeValueAsDateTime(s.node(), lexicalForm());
					case SimpleType.TIME_NAME:
						return NodeHelper.getNodeValueAsTime(s.node(), lexicalForm());
					case SimpleType.DATE_NAME:
						return NodeHelper.getNodeValueAsDate(s.node(), lexicalForm());
					case SimpleType.GYEARMONTH_NAME:
						return NodeHelper.getNodeValueAsGYearMonth(s.node(), lexicalForm());
					case SimpleType.GYEAR_NAME:
						return NodeHelper.getNodeValueAsGYear(s.node(), lexicalForm());
					case SimpleType.GMONTHDAY_NAME:
						return NodeHelper.getNodeValueAsGMonthDay(s.node(), lexicalForm());
					case SimpleType.GDAY_NAME:
						return NodeHelper.getNodeValueAsGDay(s.node(), lexicalForm());
					case SimpleType.GMONTH_NAME:
						return NodeHelper.getNodeValueAsGMonth(s.node(), lexicalForm());
					case SimpleType.HEXBINARY_NAME:
						return NodeHelper.getNodeValueAsHexBinary(s.node(), lexicalForm());
					case SimpleType.BASE64BINARY_NAME:
						return NodeHelper.getNodeValueAsBase64Binary(s.node(), lexicalForm());
					case SimpleType.ANYURI_NAME:
						return NodeHelper.getNodeValueAsAnyUri(s.node(), lexicalForm());
					case SimpleType.QNAME_NAME:
						return NodeHelper.getNodeValueAsQName(s.node(), lexicalForm());
					case SimpleType.NOTATION_NAME:
						final QName notation = NodeHelper.getNodeValueAsQName(s.node(), lexicalForm());
						if (schema.notationDeclarations().stream().noneMatch(n -> NodeHelper.equalsName(notation, n))) {
							throw NodeHelper.newFacetException(s.node(), value, (s.targetNamespace() != null ? '{' + s.targetNamespace() + '}' : "") + s.name());
						}
						return notation;
					default:
						throw new AssertionError();
					}
				}
				return lexicalForm();
			});
			this.lexicalForm = simpleType.map(s -> s.lexicalMapping(value));
		}

		/** @return either default or fixed, as appropriate */
		public Variety variety() {
			return variety;
		}

		/** @return the ·actual value· of the [attribute] (with respect to {attribute declaration}.{type definition}) */
		public Object value() {
			return value.get();
		}

		/** @return the ·normalized value· of the [attribute] (with respect to {attribute declaration}.{type definition}) */
		public String lexicalForm() {
			return lexicalForm.get();
		}

	}

	private static final Attribute xsiType;
	private static final Attribute xsiNil;
	private static final Attribute xsiSchemaLocation;
	private static final Attribute xsiNoNamespaceSchemaLocation;
	private static final SequenceParser parser = new SequenceParser()
			.optionalAttributes(AttrParser.ID, AttrParser.DEFAULT, AttrParser.FIXED, AttrParser.FORM, AttrParser.NAME, AttrParser.TARGET_NAMESPACE, AttrParser.TYPE, AttrParser.INHERITABLE)
			.elements(0, 1, TagParser.ANNOTATION)
			.elements(0, 1, TagParser.SIMPLE_TYPE);

	static {
		final Document xsiSchemaDocument = NodeHelper.newSchemaDocument(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
		final Node xsiTypeNode = NodeHelper.newSchemaNode(xsiSchemaDocument, TagParser.Names.ATTRIBUTE, "type");
		xsiType = new Attribute(xsiTypeNode, Deques.emptyDeque(), "type", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, SimpleType::xsQName, new Scope(Scope.Variety.GLOBAL, null), null, null);
		final Node xsiNilNode = NodeHelper.newSchemaNode(xsiSchemaDocument, TagParser.Names.ATTRIBUTE, "nil");
		xsiNil = new Attribute(xsiNilNode, Deques.emptyDeque(), "nil", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, SimpleType::xsBoolean, new Scope(Scope.Variety.GLOBAL, null), null, null);
		final Node xsiSchemaLocationNode = NodeHelper.newSchemaNode(xsiSchemaDocument, TagParser.Names.ATTRIBUTE, "schemaLocation");
		xsiSchemaLocation = new Attribute(xsiSchemaLocationNode, Deques.emptyDeque(), "schemaLocation", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, Deferred.of(() -> new SimpleType(SimpleType.xsAnySimpleType().node(), Deques.emptyDeque(), null, XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, Deques.emptyDeque(), null, SimpleType::xsAnySimpleType, () -> SimpleType.Variety.LIST, Deques.emptyDeque(), null, Deferred.of(() -> new SimpleType.List(AnnotationSet.EMPTY, SimpleType::xsAnyURI)), null)), new Scope(Scope.Variety.GLOBAL, null), null, null);
		final Node xsiNoNamespaceSchemaLocationNode = NodeHelper.newSchemaNode(xsiSchemaDocument, TagParser.Names.ATTRIBUTE, "noNamespaceSchemaLocation");
		xsiNoNamespaceSchemaLocation = new Attribute(xsiNoNamespaceSchemaLocationNode, Deques.emptyDeque(), "noNamespaceSchemaLocation", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, SimpleType::xsAnyURI, new Scope(Scope.Variety.GLOBAL, null), null, null);
	}

	private final Node node;
	private final Deque<Annotation> annotations;
	private final String name;
	private final String targetNamespace;
	private final Deferred<SimpleType> typeDefinition;
	private final Scope scope;
	private final ValueConstraint valueConstraint;
	private final boolean inheritable;

	Attribute(final Node node, final Deque<Annotation> annotations, final String name, final String targetNamespace, final Deferred<SimpleType> typeDefinition, final Scope scope, final ValueConstraint valueConstraint, final Boolean inheritable) {
		this.node = Objects.requireNonNull(node);
		this.annotations = Objects.requireNonNull(annotations);
		this.name = name;
		this.targetNamespace = NodeHelper.requireNonEmpty(node, targetNamespace);
		this.typeDefinition = Objects.requireNonNull(typeDefinition);
		this.scope = scope;
		this.valueConstraint = valueConstraint;
		this.inheritable = inheritable != null && inheritable.booleanValue();
	}

	private static Attribute parse(final Result result) {
		final Node node = result.node();
		final Deque<Annotation> annotations = Annotation.of(result).resolve(node);
		final String defaultValue = result.value(AttrParser.DEFAULT);
		final String fixedValue = result.value(AttrParser.FIXED);
		final boolean isGlobal = NodeHelper.isParentSchemaElement(result);
		final Form form = result.value(AttrParser.FORM);
		String targetNamespace = result.value(AttrParser.TARGET_NAMESPACE);
		final Scope scope;
		if (isGlobal) {
			if (form != null) {
				throw new Schema.ParseException(node, "'form' attribute is only allowed for local attribute declarations");
			} else if (targetNamespace != null) {
				throw new Schema.ParseException(node, "'targetNamespace' attribute is only allowed for local attribute declarations");
			}
			targetNamespace = result.schema().targetNamespace();
			scope = new Scope(Scope.Variety.GLOBAL, null);
		} else {
			if (targetNamespace == null && (Form.QUALIFIED.equals(form) || (form == null && Form.QUALIFIED.equals(result.schema().attributeFormDefault())))) { // 3.2.2.2
				targetNamespace = result.schema().targetNamespace();
			}
			Node n = result.parent().node();
			while (!TagParser.ATTRIBUTE_GROUP.equalsName(n) && !TagParser.COMPLEX_TYPE.equalsName(n)) {
				n = n.getParentNode();
				if (n == null) {
					throw new ParseException(result.node(), TagParser.ATTRIBUTE.getName() + " must be a descendent of " + TagParser.COMPLEX_TYPE.getName() + " or " + TagParser.ATTRIBUTE_GROUP.getName());
				}
			}
			scope = new Scope(Scope.Variety.LOCAL, n);
		}
		final QName typeName = result.value(AttrParser.TYPE);
		final Deferred<SimpleType> simpleTypeChild = result.parse(TagParser.SIMPLE_TYPE);
		final Deferred<SimpleType> simpleType = typeName != null
				? result.schema().find(typeName, SimpleType.class)
				: simpleTypeChild != null
						? simpleTypeChild : SimpleType::xsAnySimpleType;
		final ValueConstraint valueConstraint = defaultValue != null ? new ValueConstraint(result.schema(), simpleType, ValueConstraint.Variety.DEFAULT, defaultValue)
				: fixedValue != null ? new ValueConstraint(result.schema(), simpleType, ValueConstraint.Variety.FIXED, fixedValue)
				: null;
		final String name = result.value(AttrParser.NAME);
		final Boolean inheritable = result.value(AttrParser.INHERITABLE);
		return new Attribute(node, annotations, name, targetNamespace, simpleType, scope, valueConstraint, inheritable);
	}

	static void register() {
		AttrParser.register(AttrParser.Names.DEFAULT, NodeHelper::getAttrValueAsString);
		AttrParser.register(AttrParser.Names.FIXED, NodeHelper::getAttrValueAsString);
		AttrParser.register(AttrParser.Names.INHERITABLE, (Boolean) null);
		AttrParser.register(AttrParser.Names.USE, Use.class, Use.OPTIONAL, Use::getAttrValueAsUse);
		TagParser.register(TagParser.Names.ATTRIBUTE, parser, Attribute.class, Attribute::parse);
	}

	/**
	 * The xsi:type attribute is used to signal use of a type other than the declared type of an element. See xsi:type (§2.7.1).
	 * @return xsi:type
	 */
	public static Attribute xsiType() {
		return xsiType;
	}

	/**
	 * The xsi:nil attribute is used to signal that an element's content is "nil" (or "null"). See xsi:nil (§2.7.2).
	 * @return xsi:nil
	 */
	public static Attribute xsiNil() {
		return xsiNil;
	}

	/**
	 * The xsi:schemaLocation attribute is used to signal possible locations of relevant schema documents. See xsi:schemaLocation, xsi:noNamespaceSchemaLocation (§2.7.3).
	 * @return xsi:schemaLocation
	 */
	public static Attribute xsiSchemaLocation() {
		return xsiSchemaLocation;
	}

	/**
	 * The xsi:noNamespaceSchemaLocation attribute is used to signal possible locations of relevant schema documents. See xsi:schemaLocation, xsi:noNamespaceSchemaLocation (§2.7.3).
	 * @return xsi:noNamespaceSchemaLocation
	 */
	public static Attribute xsiNoNamespaceSchemaLocation() {
		return xsiNoNamespaceSchemaLocation;
	}

	/** @return The ·actual value· of the name [attribute] */
	public String name() {
		return name;
	}

	/** @return The ·actual value· of the targetNamespace [attribute] of the parent &lt;schema&gt; element information item, or ·absent· if there is none. */
	public String targetNamespace() {
		return targetNamespace;
	}

	/** @return A Scope as follows: */
	public Scope scope() {
		return scope;
	}

	/**
	 * @return
	 * <table>
	 *   <caption style="text-align: left">If there is a default or a fixed [attribute], then a Value Constraint as follows, otherwise ·absent·.</caption>
	 *   <thead>
	 *     <tr>
	 *       <th style="text-align: left">Property</th>
	 *       <th style="text-align: left">Value</th>
	 *     </tr>
	 *   </thead>
	 *   <tbody>
	 *     <tr>
	 *       <td>{@link ValueConstraint#variety()}</td>
	 *       <td>{variety}</td>
	 *       <td>either default or fixed, as appropriate</td>
	 *     </tr>
	 *     <tr>
	 *       <td>{@link ValueConstraint#value()}</td>
	 *       <td>{value}</td>
	 *       <td>the ·actual value· (with respect to the {type definition}) of the [attribute]</td>
	 *     </tr>
	 *     <tr>
	 *       <td>{@link ValueConstraint#lexicalForm()}</td>
	 *       <td>{lexical form}</td>
	 *       <td>the ·normalized value· (with respect to the {type definition}) of the [attribute]</td>
	 *     </tr>
	 *   </tbody>
	 * </table>
	 */
	public ValueConstraint valueConstraint() {
		return valueConstraint;
	}

	/** @return The ·actual value· of the inheritable [attribute], if present, otherwise false. */
	public boolean inheritable() {
		return inheritable;
	}

	/** @return The simple type definition corresponding to the &lt;simpleType&gt; element information item in the [children], if present, otherwise the simple type definition ·resolved· to by the ·actual value· of the type [attribute], if present, otherwise ·xs:anySimpleType·. */
	public SimpleType typeDefinition() {
		return typeDefinition.get();
	}

	@Override
	public Node node() {
		return node;
	}

	/** @return The ·annotation mapping· of the &lt;attribute&gt; element, as defined in XML Representation of Annotation Schema Components (§3.15.2). */
	@Override
	public Deque<Annotation> annotations() {
		return Deques.unmodifiableDeque(annotations);
	}

}