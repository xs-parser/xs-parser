package xs.parser;

import java.util.*;
import javax.xml.namespace.*;
import org.w3c.dom.*;
import xs.parser.Assertion.*;
import xs.parser.internal.*;
import xs.parser.internal.SequenceParser.*;

/**
 * <pre>
 * &lt;unique
 *   id = ID
 *   name = NCName
 *   ref = QName
 *   {any attributes with non-schema namespace . . .}&gt;
 *   Content: (annotation?, (selector, field+)?)
 * &lt;/unique&gt;
 *
 * &lt;key
 *   id = ID
 *   name = NCName
 *   ref = QName
 *   {any attributes with non-schema namespace . . .}&gt;
 *   Content: (annotation?, (selector, field+)?)
 * &lt;/key&gt;
 *
 * &lt;keyref
 *   id = ID
 *   name = NCName
 *   ref = QName
 *   refer = QName
 *   {any attributes with non-schema namespace . . .}&gt;
 *   Content: (annotation?, (selector, field+)?)
 * &lt;/keyref&gt;
 * </pre>
 */
public class IdentityConstraint implements AnnotatedComponent {

	public enum Category {

		KEY, KEYREF, UNIQUE;

		private static Category fromNode(final Node node) {
			return ElementValue.KEY.equalsName(node) ? KEY
					: ElementValue.KEYREF.equalsName(node) ? KEYREF
					: ElementValue.UNIQUE.equalsName(node) ? UNIQUE
					: null;
		}

	}

	protected static final SequenceParser parser = new SequenceParser()
			.optionalAttributes(AttributeValue.ID, AttributeValue.NAME, AttributeValue.REF, AttributeValue.REFER)
			.elements(0, 1, ElementValue.ANNOTATION)
			.elements(0, 1, ElementValue.SELECTOR)
			.elements(0, Integer.MAX_VALUE, ElementValue.FIELD);

	private final Node node;
	private final Deque<Annotation> annotations;
	private final String name;
	private final String targetNamespace;
	private final Category category;
	private final XPathExpression selector;
	private final Deque<XPathExpression> fields;
	private final IdentityConstraint referencedKey;

	private IdentityConstraint(final Node node, final Deque<Annotation> annotations, final String name, final String targetNamespace, final Category category, final XPathExpression selector, final Deque<XPathExpression> fields, final IdentityConstraint referencedKey) {
		this.node = Objects.requireNonNull(node);
		this.annotations = Objects.requireNonNull(annotations);
		this.name = name;
		this.targetNamespace = NodeHelper.validateTargetNamespace(targetNamespace);
		this.category = category;
		this.selector = selector;
		this.fields = Objects.requireNonNull(fields);
		this.referencedKey = referencedKey;
	}

	protected static IdentityConstraint parse(final Result result) {
		final String name = result.value(AttributeValue.NAME);
		final String targetNamespace = result.schema().targetNamespace();
		final Category category = Category.fromNode(result.node());
		assert category != null : NodeHelper.toString(result.node());
		final QName refAttr = result.value(AttributeValue.REF);
		if (refAttr != null) {
			IdentityConstraint ref = null;
			final Deque<IdentityConstraint> siblingKeys = result.parseAll(ElementValue.KEY, ElementValue.KEYREF, ElementValue.UNIQUE);
			for (final IdentityConstraint k : siblingKeys) {
				if (category.equals(Category.fromNode(k.node())) && refAttr.getLocalPart().equals(k.name())) {
					ref = k;
					break;
				}
			}
			if (ref == null) {
				throw new SchemaParseException(result.node(), "No sibling key for identity constraint @ref");
			}
			return new IdentityConstraint(result.node(), result.annotations(), ref.name(), ref.targetNamespace(), category, ref.selector(), ref.fields(), ref.referencedKey());
		}
		final XPathExpression selector = result.parse(ElementValue.SELECTOR);
		final Deque<XPathExpression> fields = result.parseAll(ElementValue.FIELD);
		final QName refer = result.value(AttributeValue.REFER);
		IdentityConstraint referencedKey = null;
		if (refer != null && ElementValue.KEYREF.equalsName(result.node())) {
			final Deque<IdentityConstraint> siblingKeys = result.parseAll(ElementValue.KEY, ElementValue.KEYREF, ElementValue.UNIQUE);
			for (final IdentityConstraint k : siblingKeys) {
				if (ElementValue.KEY.equalsName(k.node()) && refer.getLocalPart().equals(k.name())) {
					referencedKey = k;
					break;
				}
			}
			if (referencedKey == null) {
				throw new SchemaParseException(result.node(), refer + " did not match any known identity constraints");
			}
		}
		return new IdentityConstraint(result.node(), result.annotations(), name, targetNamespace, category, selector, fields, referencedKey);
	}

	public String name() {
		return name;
	}

	public String targetNamespace() {
		return targetNamespace;
	}

	public Category category() {
		return category;
	}

	public XPathExpression selector() {
		return selector;
	}

	public Deque<XPathExpression> fields() {
		return Deques.unmodifiableDeque(fields);
	}

	public IdentityConstraint referencedKey() {
		return referencedKey;
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
