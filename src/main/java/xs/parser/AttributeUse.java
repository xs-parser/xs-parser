package xs.parser;

import java.util.*;
import java.util.function.*;
import javax.xml.namespace.*;
import org.w3c.dom.*;
import xs.parser.Attribute.*;
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
 *   use = (optional | prohibited | required) : optional
 *   inheritable = boolean
 *   {any attributes with non-schema namespace . . .}&gt;
 *   Content: (annotation?, simpleType?)
 * &lt;/attribute&gt;
 * </pre>
 */
public class AttributeUse implements AnnotatedComponent {

	protected static final SequenceParser parser = new SequenceParser()
			.optionalAttributes(AttributeValue.ID, AttributeValue.DEFAULT, AttributeValue.FIXED, AttributeValue.FORM, AttributeValue.NAME, AttributeValue.REF, AttributeValue.TARGETNAMESPACE, AttributeValue.TYPE, AttributeValue.USE, AttributeValue.INHERITABLE)
			.elements(0, 1, ElementValue.ANNOTATION)
			.elements(0, 1, ElementValue.SIMPLETYPE);

	private final Node node;
	private final Use use;
	private final Deferred<Attribute> attributeDeclaration;
	private final Deferred<Boolean> inheritable;

	AttributeUse(final Node node, final Use use, final Deferred<Attribute> attributeDeclaration, final Deferred<Boolean> inheritable) {
		this.node = Objects.requireNonNull(node);
		this.use = use;
		this.attributeDeclaration = Objects.requireNonNull(attributeDeclaration);
		this.inheritable = inheritable;
	}

	protected static AttributeUse parse(final Result result) {
		final Use use = result.value(AttributeValue.USE);
		final String defaultValue = result.value(AttributeValue.DEFAULT);
		final String fixedValue = result.value(AttributeValue.FIXED);
		final Function<Deferred<SimpleType>, ValueConstraint> valueConstraint = s -> {
			if (defaultValue != null) {
				return new ValueConstraint(s, ValueConstraint.Variety.DEFAULT, defaultValue);
			} else if (fixedValue != null) {
				return new ValueConstraint(s, ValueConstraint.Variety.FIXED, fixedValue);
			}
			return null;
		};
		final Scope scope = NodeHelper.isParentSchemaElement(result)
				? new Scope(Scope.Variety.GLOBAL, null)
				: new Scope(Scope.Variety.LOCAL, result.parent().node());
		final Form form = result.value(AttributeValue.FORM);
		String targetNamespace = result.value(AttributeValue.TARGETNAMESPACE);
		if (targetNamespace == null) {
			if (Form.QUALIFIED.equals(form) || (form == null && Form.QUALIFIED.equals(result.schema().attributeFormDefault()))) { // 3.2.2.2
				targetNamespace = result.schema().targetNamespace();
			}
		} else if (scope.variety() != Scope.Variety.LOCAL) {
			throw new SchemaParseException(result.node(), "@targetNamespace may only appear on a local xs:attribute");
		}
		final String name = result.value(AttributeValue.NAME);
		final Boolean inheritable = result.value(AttributeValue.INHERITABLE);
		final QName refName = result.value(AttributeValue.REF);
		final Deferred<Attribute> attributeDecl;
		if (refName != null) {
			attributeDecl = result.schema().find(refName, Attribute.class);
		} else {
			final QName typeName = result.value(AttributeValue.TYPE);
			final SimpleType simpleTypeChild = result.parse(ElementValue.SIMPLETYPE);
			final Deferred<SimpleType> simpleType = typeName != null
					? result.schema().find(typeName, SimpleType.class)
					: Deferred.value(simpleTypeChild != null ? simpleTypeChild : SimpleType.xsAnySimpleType());
			attributeDecl = Deferred.value(new Attribute(result.node(), result.annotations(), name, targetNamespace, simpleType, scope, valueConstraint.apply(simpleType), inheritable));
		}
		return new AttributeUse(result.node(), use, attributeDecl, inheritable == null ? attributeDecl.map(Attribute::inheritable) : Deferred.value(inheritable));
	}

	/** @return true if the &lt;attribute&gt; element has use = required, otherwise false. */
	public boolean required() {
		return Use.REQUIRED.equals(use);
	}

	/** @return The (top-level) attribute declaration ·resolved· to by the ·actual value· of the ref [attribute] */
	public Attribute attributeDeclaration() {
		return attributeDeclaration.get();
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
	 *       <td>the ·actual value· of the [attribute] (with respect to {attribute declaration}.{type definition})</td>
	 *     </tr>
	 *     <tr>
	 *       <td>{@link ValueConstraint#lexicalForm()}</td>
	 *       <td>{lexical form}</td>
	 *       <td>the ·normalized value· of the [attribute] (with respect to {attribute declaration}.{type definition})</td>
	 *     </tr>
	 *   </tbody>
	 * </table>
	 */
	public Attribute.ValueConstraint valueConstraint() {
		return attributeDeclaration().valueConstraint();
	}

	/** @return The ·actual value· of the inheritable [attribute], if present, otherwise false. */
	public boolean inheritable() {
		return inheritable.get().booleanValue();
	}

	@Override
	public Node node() {
		return node;
	}

	/** @return The same annotations as the {annotations} of the Attribute Declaration. See below. */
	@Override
	public Deque<Annotation> annotations() {
		return attributeDeclaration().annotations();
	}

}
