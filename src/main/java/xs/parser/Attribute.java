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
import xs.parser.v.*;

/**
 * An attribute declaration is an association between a name and a simple type definition, together with occurrence information and (optionally) a default value. The association is either global, or local to its containing complex type definition. Attribute declarations contribute to ·validation· as part of complex type definition ·validation·, when their occurrence, defaults and type components are checked against an attribute information item with a matching name and namespace.
 *
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

		/** Attribute scope variety */
		public enum Variety {

			/** Attribute scope variety global */
			GLOBAL,
			/** Attribute scope variety local */
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
			} else if (parent instanceof ComplexType || parent instanceof AttributeGroup) {
				this.parent = parent;
			} else {
				throw new ParseException(node, TagParser.ATTRIBUTE.getName() + " must be a descendent of " + TagParser.COMPLEX_TYPE.getName() + " or " + TagParser.ATTRIBUTE_GROUP.getName());
			}
		}

		/** @return Either local or global, as appropriate */
		public Variety variety() {
			return variety;
		}

		/** @return If the &lt;attribute&gt; element information item has &lt;complexType&gt; as an ancestor, the Complex Type Definition corresponding to that item, otherwise (the &lt;attribute&gt; element information item is within an &lt;attributeGroup&gt; element information item), the Attribute Group Definition corresponding to that item or ·absent·. */
		public AnnotatedComponent parent() {
			return parent;
		}

	}

	/**
	 * Attribute uses correspond to all uses of &lt;attribute&gt; which allow a use attribute. These in turn correspond to two components in each case, an attribute use and its {attribute declaration} (although note the latter is not new when the attribute use is a reference to a top-level attribute declaration).
	 */
	public enum Use {

		/** Attribute use required */
		REQUIRED("required"),
		/** Attribute use optional */
		OPTIONAL("optional"),
		/** Attribute use prohibited */
		PROHIBITED("prohibited");

		private final String name;

		Use(final String name) {
			this.name = name;
		}

		private static Use getAttrValueAsUse(final Attr attr) {
			final String value = NodeHelper.collapseWhitespace(attr.getValue());
			for (final Use u : values()) {
				if (u.name.equals(value)) {
					return u;
				}
			}
			throw new IllegalArgumentException(value);
		}

		/**
		 * Returns the name of this attribute use, i.e. {@code "required"}, {@code "optional"}, or {@code "prohibited"} as appropriate.
		 * @return The name of this attribute use
		 */
		@Override
		public String toString() {
			return name;
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

		/** Attribute value constraint variety */
		public enum Variety {

			/** Attribute value constraint variety default */
			DEFAULT,
			/** Attribute value constraint variety fixed */
			FIXED;

		}

		private final Variety variety;
		private final Deferred<Object> value;
		private final Deferred<String> lexicalForm;

		ValueConstraint(final Deferred<SimpleType> simpleType, final Variety variety, final String value) {
			Objects.requireNonNull(simpleType);
			this.variety = variety;
			this.value = simpleType.map(s -> s.valueSpace(value, lexicalForm()));
			this.lexicalForm = simpleType.map(s -> s.lexicalMapping(value));
		}

		/** @return Either default or fixed, as appropriate */
		public Variety variety() {
			return variety;
		}

		/** @return The ·actual value· of the [attribute] (with respect to {attribute declaration}.{type definition}) */
		public Object value() {
			return value.get();
		}

		/** @return The ·normalized value· of the [attribute] (with respect to {attribute declaration}.{type definition}) */
		public String lexicalForm() {
			return lexicalForm.get();
		}

	}

	private static final Deferred<Attribute> xsiType;
	private static final Deferred<Attribute> xsiNil;
	private static final Deferred<Attribute> xsiSchemaLocation;
	private static final Deferred<Attribute> xsiNoNamespaceSchemaLocation;
	private static final SequenceParser parser = new SequenceParser()
			.optionalAttributes(AttrParser.ID, AttrParser.DEFAULT, AttrParser.FIXED, AttrParser.FORM, AttrParser.NAME, AttrParser.TARGET_NAMESPACE, AttrParser.TYPE, AttrParser.INHERITABLE)
			.elements(0, 1, TagParser.ANNOTATION)
			.elements(0, 1, TagParser.SIMPLE_TYPE);

	static {
		xsiType = Deferred.of(() -> {
			final Node xsiTypeNode = NodeHelper.newGlobalNode(Schema.XSI, TagParser.Names.ATTRIBUTE, "type");
			return new Attribute(() -> Schema.XSI, xsiTypeNode, Deques.emptyDeque(), "type", new DeferredValue<>(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI), SimpleType::xsQName, new DeferredValue<>(new Scope(Scope.Variety.GLOBAL, null, xsiTypeNode)), null, null);
		});
		xsiNil = Deferred.of(() -> {
			final Node xsiNilNode = NodeHelper.newGlobalNode(Schema.XSI, TagParser.Names.ATTRIBUTE, "nil");
			return new Attribute(() -> Schema.XSI, xsiNilNode, Deques.emptyDeque(), "nil", new DeferredValue<>(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI), SimpleType::xsBoolean, new DeferredValue<>(new Scope(Scope.Variety.GLOBAL, null, xsiNilNode)), null, null);
		});
		xsiSchemaLocation = Deferred.of(() -> {
			final Node xsiSchemaLocationNode = NodeHelper.newGlobalNode(Schema.XSI, TagParser.Names.ATTRIBUTE, "schemaLocation");
			final Node xsiSchemaLocationTypeNode = NodeHelper.newLocalNode(Schema.XSI, xsiSchemaLocationNode, TagParser.Names.SIMPLE_TYPE);
			return new Attribute(() -> Schema.XSI, xsiSchemaLocationNode, Deques.emptyDeque(), "schemaLocation", new DeferredValue<>(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI), Deferred.of(() -> new SimpleType(Schema.XSI, () -> Schema.XSI, xsiSchemaLocationTypeNode, Deques.emptyDeque(), null, XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, Deques.emptyDeque(), SimpleType::xsAnySimpleType, () -> SimpleType.Variety.LIST, Deques.emptyDeque(), null, Deferred.of(() -> new SimpleType.List(AnnotationSet.EMPTY, SimpleType::xsAnyURI)), null)), new DeferredValue<>(new Scope(Scope.Variety.GLOBAL, null, xsiSchemaLocationNode)), null, null);
		});
		xsiNoNamespaceSchemaLocation = Deferred.of(() -> {
			final Node xsiNoNamespaceSchemaLocationNode = NodeHelper.newGlobalNode(Schema.XSI, TagParser.Names.ATTRIBUTE, "noNamespaceSchemaLocation");
			return new Attribute(() -> Schema.XSI, xsiNoNamespaceSchemaLocationNode, Deques.emptyDeque(), "noNamespaceSchemaLocation", new DeferredValue<>(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI), SimpleType::xsAnyURI, new DeferredValue<>(new Scope(Scope.Variety.GLOBAL, null, xsiNoNamespaceSchemaLocationNode)), null, null);
		});
	}

	private final Deferred<? extends AnnotatedComponent> context;
	private final Node node;
	private final Deque<Annotation> annotations;
	private final String name;
	private final Deferred<String> targetNamespace;
	private final Deferred<SimpleType> typeDefinition;
	private final Deferred<Scope> scope;
	private final ValueConstraint valueConstraint;
	private final boolean inheritable;

	Attribute(final Deferred<? extends AnnotatedComponent> context, final Node node, final Deque<Annotation> annotations, final String name, final Deferred<String> targetNamespace, final Deferred<SimpleType> typeDefinition, final Deferred<Scope> scope, final ValueConstraint valueConstraint, final Boolean inheritable) {
		this.context = Objects.requireNonNull(context);
		this.node = Objects.requireNonNull(node);
		this.annotations = Objects.requireNonNull(annotations);
		this.name = name;
		this.targetNamespace = targetNamespace.map(tns -> NodeHelper.requireNonEmpty(node, tns));
		this.typeDefinition = Objects.requireNonNull(typeDefinition);
		this.scope = Objects.requireNonNull(scope);
		this.valueConstraint = valueConstraint;
		this.inheritable = inheritable != null && inheritable.booleanValue();
	}

	private static Attribute parse(final Result result) {
		final Deferred<? extends AnnotatedComponent> context = result.context();
		final Node node = result.node();
		final Deque<Annotation> annotations = Annotation.of(result).resolve(node);
		final String defaultValue = result.value(AttrParser.DEFAULT);
		final String fixedValue = result.value(AttrParser.FIXED);
		final Form form = result.value(AttrParser.FORM);
		final Deferred<Scope> scope = context.map(ctx -> {
			if (ctx instanceof Schema) {
				if (form != null) {
					throw new Schema.ParseException(node, "'form' attribute is only allowed for local attribute declarations");
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
					throw new Schema.ParseException(node, "'targetNamespace' attribute is only allowed for local attribute declarations");
				}
				return result.schema().targetNamespace();
			} else {
				if (targetNamespaceValue == null && (Form.QUALIFIED.equals(form) || (form == null && Form.QUALIFIED.equals(result.schema().attributeFormDefault())))) { // 3.2.2.2
					return result.schema().targetNamespace();
				}
				return targetNamespaceValue;
			}
		});
		final QName typeName = result.value(AttrParser.TYPE);
		final Deferred<SimpleType> simpleTypeChild = result.parse(TagParser.SIMPLE_TYPE);
		final Deferred<SimpleType> simpleType = typeName != null
				? result.schema().find(typeName, SimpleType.class)
				: simpleTypeChild != null
						? simpleTypeChild : SimpleType::xsAnySimpleType;
		final ValueConstraint valueConstraint = defaultValue != null ? new ValueConstraint(simpleType, ValueConstraint.Variety.DEFAULT, defaultValue)
				: fixedValue != null ? new ValueConstraint(simpleType, ValueConstraint.Variety.FIXED, fixedValue)
				: null;
		final String name = result.value(AttrParser.NAME);
		final Boolean inheritable = result.value(AttrParser.INHERITABLE);
		return new Attribute(context, node, annotations, name, targetNamespace, simpleType, scope, valueConstraint, inheritable);
	}

	static void register() {
		AttrParser.register(AttrParser.Names.DEFAULT, NodeHelper::getAttrValueAsString);
		AttrParser.register(AttrParser.Names.FIXED, NodeHelper::getAttrValueAsString);
		AttrParser.register(AttrParser.Names.INHERITABLE, (Boolean) null);
		AttrParser.register(AttrParser.Names.USE, Use.class, Use.OPTIONAL, Use::getAttrValueAsUse);
		TagParser.register(TagParser.Names.ATTRIBUTE, parser, Attribute.class, Attribute::parse);
		VisitorHelper.register(Attribute.class, Attribute::visit);
	}

	/**
	 * The xsi:type attribute is used to signal use of a type other than the declared type of an element. See xsi:type (§2.7.1).
	 * @return The xsi:type attribute
	 */
	public static Attribute xsiType() {
		return xsiType.get();
	}

	/**
	 * The xsi:nil attribute is used to signal that an element's content is "nil" (or "null"). See xsi:nil (§2.7.2).
	 * @return The xsi:nil attribute
	 */
	public static Attribute xsiNil() {
		return xsiNil.get();
	}

	/**
	 * The xsi:schemaLocation attribute is used to signal possible locations of relevant schema documents. See xsi:schemaLocation, xsi:noNamespaceSchemaLocation (§2.7.3).
	 * @return The xsi:schemaLocation attribute
	 */
	public static Attribute xsiSchemaLocation() {
		return xsiSchemaLocation.get();
	}

	/**
	 * The xsi:noNamespaceSchemaLocation attribute is used to signal possible locations of relevant schema documents. See xsi:schemaLocation, xsi:noNamespaceSchemaLocation (§2.7.3).
	 * @return The xsi:noNamespaceSchemaLocation attribute
	 */
	public static Attribute xsiNoNamespaceSchemaLocation() {
		return xsiNoNamespaceSchemaLocation.get();
	}

	void visit(final Visitor visitor) {
		if (visitor.visit(context.get(), node, this)) {
			visitor.onAttribute(context.get(), node, this);
			annotations.forEach(a -> a.visit(visitor));
			typeDefinition().visit(visitor);
		}
	}

	/** @return The ·actual value· of the name [attribute] */
	public String name() {
		return name;
	}

	/** @return The ·actual value· of the targetNamespace [attribute] of the parent &lt;schema&gt; element information item, or ·absent· if there is none. */
	public String targetNamespace() {
		return targetNamespace.get();
	}

	/** @return A Scope as follows: */
	public Scope scope() {
		return scope.get();
	}

	/**
	 * @return If there is a default or a fixed [attribute], then a Value Constraint as follows, otherwise ·absent·.
	 * <table>
	 *   <caption style="text-align: left">Value Constraint</caption>
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

	/** @return The ·annotation mapping· of the &lt;attribute&gt; element, as defined in XML Representation of Annotation Schema Components (§3.15.2). */
	@Override
	public Deque<Annotation> annotations() {
		return Deques.unmodifiableDeque(annotations);
	}

}