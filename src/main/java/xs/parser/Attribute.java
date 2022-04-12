package xs.parser;

import java.util.*;
import javax.xml.*;
import javax.xml.namespace.*;
import org.w3c.dom.*;
import xs.parser.Schema.*;
import xs.parser.internal.*;
import xs.parser.internal.SequenceParser.*;

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
 *       <td>{@link Attribute#type()}</td>
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

		public Scope(final Variety variety, final Node parent) {
			this.variety = Objects.requireNonNull(variety);
			this.parent = parent;
			assert (Variety.LOCAL.equals(variety) && parent != null) || (Variety.GLOBAL.equals(variety) && parent == null) : variety.toString();
		}

		/** @return either local or global, as appropriate */
		public Variety variety() {
			return variety;
		}

		/** @return If the lt;attribute&gt; element information item has lt;complexType&gt; as an ancestor, the Complex Type Definition corresponding to that item, otherwise (the &lt;attribute&gt; element information item is within an lt;attributeGroup&gt; element information item), the Attribute Group Definition corresponding to that item or ·absent·. */
		public Node parent() {
			return parent;
		}

	}

	public enum Use {

		REQUIRED("required"),
		OPTIONAL("optional"),
		PROHIBITED("prohibited");

		private final String name;

		private Use(final String value) {
			this.name = value;
		}

		public static Use getByName(final Node node) {
			final String name = node.getNodeValue();
			for (final Use u : values()) {
				if (u.getName().equals(name)) {
					return u;
				}
			}
			throw new IllegalArgumentException(name.toString());
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
		private final String value;
		private final Deferred<String> lexicalForm;

		public ValueConstraint(final Deferred<SimpleType> simpleType, final Variety variety, final String value) {
			Objects.requireNonNull(simpleType);
			this.variety = variety;
			this.value = value;
			this.lexicalForm = simpleType.map(s -> s.lexicalMapping(value));
		}

		/** @return either default or fixed, as appropriate */
		public Variety variety() {
			return variety;
		}

		/** @return the ·actual value· of the [attribute] (with respect to {attribute declaration}.{type definition}) */
		public String value() {
			return value;
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
	protected static final SequenceParser parser = new SequenceParser()
			.optionalAttributes(AttributeValue.ID, AttributeValue.DEFAULT, AttributeValue.FIXED, AttributeValue.FORM, AttributeValue.NAME, AttributeValue.TARGETNAMESPACE, AttributeValue.TYPE, AttributeValue.INHERITABLE)
			.elements(0, 1, ElementValue.ANNOTATION)
			.elements(0, 1, ElementValue.SIMPLETYPE);

	static {
		final Document doc = NodeHelper.newDocument();
		final Node xsiTypeNode = NodeHelper.newNode(doc, ElementValue.ATTRIBUTE, "xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "type");
		xsiType = new Attribute(xsiTypeNode, Deques.emptyDeque(), "type", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, Deferred.of(SimpleType::xsQName), new Scope(Scope.Variety.GLOBAL, null), null, null);
		final Node xsiNilNode = NodeHelper.newNode(doc, ElementValue.ATTRIBUTE, "xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "nil");
		xsiNil = new Attribute(xsiNilNode, Deques.emptyDeque(), "nil", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, Deferred.of(SimpleType::xsBoolean), new Scope(Scope.Variety.GLOBAL, null), null, null);
		final Node xsiSchemaLocationNode = NodeHelper.newNode(doc, ElementValue.ATTRIBUTE, "xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "schemaLocation");
		final Node xsiSchemaLocationTypeNode = NodeHelper.newNode(doc, ElementValue.SIMPLETYPE, "xs", XMLConstants.W3C_XML_SCHEMA_NS_URI, null);
		xsiSchemaLocation = new Attribute(xsiSchemaLocationNode, Deques.emptyDeque(), "schemaLocation", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, Deferred.of(() -> new SimpleType(xsiSchemaLocationTypeNode, Deques.emptyDeque(), null, XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, Deques.emptyDeque(), null, Deferred.of(SimpleType::xsAnySimpleType), Deferred.value(SimpleType.Variety.LIST), Deferred.value(Deques.emptyDeque()), null, new SimpleList(null, Deferred.of(SimpleType::xsAnyURI)), null)), new Scope(Scope.Variety.GLOBAL, null), null, null);
		final Node xsiNoNamespaceSchemaLocationNode = NodeHelper.newNode(doc, ElementValue.ATTRIBUTE, "xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "noNamespaceSchemaLocation");
		xsiNoNamespaceSchemaLocation = new Attribute(xsiNoNamespaceSchemaLocationNode, Deques.emptyDeque(), "noNamespaceSchemaLocation", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, Deferred.of(SimpleType::xsAnyURI), new Scope(Scope.Variety.GLOBAL, null), null, null);
	}

	private final Node node;
	private final Deque<Annotation> annotations;
	private final String name;
	private final String targetNamespace;
	private final Deferred<SimpleType> type;
	private final Scope scope;
	private final ValueConstraint valueConstraint;
	private final boolean inheritable;

	Attribute(final Node node, final Deque<Annotation> annotations, final String name, final String targetNamespace, final Deferred<SimpleType> type, final Scope scope, final ValueConstraint valueConstraint, final Boolean inheritable) {
		this.node = Objects.requireNonNull(node);
		this.annotations = Objects.requireNonNull(annotations);
		this.name = name;
		this.targetNamespace = NodeHelper.validateTargetNamespace(targetNamespace);
		this.type = Objects.requireNonNull(type);
		this.scope = scope;
		this.valueConstraint = valueConstraint;
		this.inheritable = inheritable != null && inheritable.booleanValue();
	}

	protected static Attribute parse(final Result result) {
		final String defaultValue = result.value(AttributeValue.DEFAULT);
		final String fixedValue = result.value(AttributeValue.FIXED);
		final boolean isGlobal = NodeHelper.isParentSchemaElement(result);
		final Form form = result.value(AttributeValue.FORM);
		String targetNamespace;
		final Scope scope;
		if (isGlobal) {
			targetNamespace = result.schema().targetNamespace();
			scope = new Scope(Scope.Variety.GLOBAL, null);
		} else {
			targetNamespace = result.value(AttributeValue.TARGETNAMESPACE);
			if (targetNamespace == null && (Form.QUALIFIED.equals(form) || (form == null && Form.QUALIFIED.equals(result.schema().attributeFormDefault())))) { // 3.2.2.2
				targetNamespace = result.schema().targetNamespace();
			}
			Node n = result.parent().node();
			while (!ElementValue.ATTRIBUTEGROUP.equalsName(n) && !ElementValue.COMPLEXTYPE.equalsName(n)) {
				if ((n = n.getParentNode()) == null) {
					throw new SchemaParseException(result.node(), ElementValue.ATTRIBUTE.getName() + " must be a descendent of " + ElementValue.COMPLEXTYPE.getName() + " or " + ElementValue.ATTRIBUTEGROUP.getName());
				}
			}
			scope = new Scope(Scope.Variety.LOCAL, n);
		}
		final QName typeName = result.value(AttributeValue.TYPE);
		final SimpleType simpleTypeChild = result.parse(ElementValue.SIMPLETYPE);
		final Deferred<SimpleType> simpleType = typeName != null
				? result.schema().find(typeName, SimpleType.class)
				: Deferred.value(simpleTypeChild != null ? simpleTypeChild : SimpleType.xsAnySimpleType());
		final ValueConstraint valueConstraint = defaultValue != null ? new ValueConstraint(simpleType, ValueConstraint.Variety.DEFAULT, defaultValue)
				: fixedValue != null ? new ValueConstraint(simpleType, ValueConstraint.Variety.FIXED, fixedValue)
				: null;
		final String name = result.value(AttributeValue.NAME);
		final Boolean inheritable = result.value(AttributeValue.INHERITABLE);
		return new Attribute(result.node(), result.annotations(), name, targetNamespace, simpleType, scope, valueConstraint, inheritable);
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
	public SimpleType type() {
		return type.get();
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