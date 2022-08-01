package xs.parser;

import java.util.*;
import java.util.function.*;
import javax.xml.namespace.*;
import org.w3c.dom.*;
import xs.parser.Attribute.*;
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
 *   use = (optional | prohibited | required) : optional
 *   inheritable = boolean
 *   {any attributes with non-schema namespace . . .}&gt;
 *   Content: (annotation?, simpleType?)
 * &lt;/attribute&gt;
 * </pre>
 */
public class AttributeUse implements AnnotatedComponent {

	private static final SequenceParser parser = new SequenceParser()
			.optionalAttributes(AttrParser.ID, AttrParser.DEFAULT, AttrParser.FIXED, AttrParser.FORM, AttrParser.NAME, AttrParser.REF, AttrParser.TARGET_NAMESPACE, AttrParser.TYPE, AttrParser.USE, AttrParser.INHERITABLE)
			.elements(0, 1, TagParser.ANNOTATION)
			.elements(0, 1, TagParser.SIMPLE_TYPE);

	private final Node node;
	private final Use use;
	private final Deferred<Attribute> attributeDeclaration;
	private final Deferred<Boolean> inheritable;

	private AttributeUse(final Node node, final Use use, final Deferred<Attribute> attributeDeclaration, final Deferred<Boolean> inheritable) {
		this.node = Objects.requireNonNull(node);
		this.use = use;
		this.attributeDeclaration = Objects.requireNonNull(attributeDeclaration);
		this.inheritable = inheritable;
	}

	private static AttributeUse parse(final Result result) {
		final Node node = result.node();
		final Use use = result.value(AttrParser.USE);
		final String defaultValue = result.value(AttrParser.DEFAULT);
		final String fixedValue = result.value(AttrParser.FIXED);
		final Function<Deferred<SimpleType>, ValueConstraint> valueConstraint = s -> {
			if (defaultValue != null) {
				return new ValueConstraint(result.schema(), s, ValueConstraint.Variety.DEFAULT, defaultValue);
			} else if (fixedValue != null) {
				return new ValueConstraint(result.schema(), s, ValueConstraint.Variety.FIXED, fixedValue);
			}
			return null;
		};
		final Scope scope = NodeHelper.isParentSchemaElement(result)
				? new Scope(Scope.Variety.GLOBAL, null)
				: new Scope(Scope.Variety.LOCAL, result.parent().node());
		final Form form = result.value(AttrParser.FORM);
		String targetNamespace = result.value(AttrParser.TARGET_NAMESPACE);
		if (targetNamespace == null) {
			if (Form.QUALIFIED.equals(form) || (form == null && Form.QUALIFIED.equals(result.schema().attributeFormDefault()))) { // 3.2.2.2
				targetNamespace = result.schema().targetNamespace();
			}
		} else if (scope.variety() != Scope.Variety.LOCAL) {
			throw new ParseException(result.node(), "@targetNamespace may only appear on a local xs:attribute");
		}
		final String name = result.value(AttrParser.NAME);
		final Boolean inheritable = result.value(AttrParser.INHERITABLE);
		final QName refName = result.value(AttrParser.REF);
		final Deferred<Attribute> attributeDecl;
		if (refName != null) {
			attributeDecl = result.schema().find(refName, Attribute.class);
		} else {
			final Deque<Annotation> annotations = Annotation.of(result).resolve(node);
			final QName typeName = result.value(AttrParser.TYPE);
			final Deferred<SimpleType> simpleTypeChild = result.parse(TagParser.SIMPLE_TYPE);
			final Deferred<SimpleType> simpleType = typeName != null
					? result.schema().find(typeName, SimpleType.class)
					: simpleTypeChild != null
							? simpleTypeChild : SimpleType::xsAnySimpleType;
			final Attribute attr = new Attribute(node, annotations, name, targetNamespace, simpleType, scope, valueConstraint.apply(simpleType), inheritable);
			attributeDecl = () -> attr;
		}
		return new AttributeUse(node, use, attributeDecl, inheritable == null ? attributeDecl.map(Attribute::inheritable) : () -> inheritable);
	}

	static void register() {
		TagParser.register(TagParser.Names.ATTRIBUTE, parser, AttributeUse.class, AttributeUse::parse);
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
