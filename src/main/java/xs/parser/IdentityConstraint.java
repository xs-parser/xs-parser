package xs.parser;

import java.util.*;
import javax.xml.namespace.*;
import org.w3c.dom.*;
import xs.parser.Assertion.*;
import xs.parser.internal.*;
import xs.parser.internal.util.*;
import xs.parser.internal.util.SequenceParser.*;

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
			return TagParser.KEY.equalsName(node) ? KEY
					: TagParser.KEYREF.equalsName(node) ? KEYREF
					: TagParser.UNIQUE.equalsName(node) ? UNIQUE
					: null;
		}

	}

	private static final SequenceParser parser = new SequenceParser()
			.optionalAttributes(AttrParser.ID, AttrParser.NAME, AttrParser.REF, AttrParser.REFER)
			.elements(0, 1, TagParser.ANNOTATION)
			.elements(0, 1, TagParser.SELECTOR)
			.elements(0, Integer.MAX_VALUE, TagParser.FIELD);

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
		this.targetNamespace = NodeHelper.requireNonEmpty(node, targetNamespace);
		this.category = category;
		this.selector = selector;
		this.fields = Objects.requireNonNull(fields);
		this.referencedKey = referencedKey;
	}

	private static IdentityConstraint parse(final Result result) {
		final String name = result.value(AttrParser.NAME);
		final String targetNamespace = result.schema().targetNamespace();
		final Category category = Category.fromNode(result.node());
		assert category != null : NodeHelper.toString(result.node());
		final QName refAttr = result.value(AttrParser.REF);
		if (refAttr != null) {
			IdentityConstraint ref = null;
			final Deque<IdentityConstraint> siblingKeys = result.parseAll(TagParser.KEY, TagParser.KEYREF, TagParser.UNIQUE);
			for (final IdentityConstraint k : siblingKeys) {
				if (category.equals(Category.fromNode(k.node())) && refAttr.getLocalPart().equals(k.name())) {
					ref = k;
					break;
				}
			}
			if (ref == null) {
				throw new Schema.ParseException(result.node(), "No sibling key for identity constraint @ref");
			}
			return new IdentityConstraint(result.node(), result.annotations(), ref.name(), ref.targetNamespace(), category, ref.selector(), ref.fields(), ref.referencedKey());
		}
		final XPathExpression selector = result.parse(TagParser.SELECTOR);
		final Deque<XPathExpression> fields = result.parseAll(TagParser.FIELD);
		final QName refer = result.value(AttrParser.REFER);
		IdentityConstraint referencedKey = null;
		if (refer != null && TagParser.KEYREF.equalsName(result.node())) {
			final Deque<IdentityConstraint> siblingKeys = result.parseAll(TagParser.KEY, TagParser.KEYREF, TagParser.UNIQUE);
			for (final IdentityConstraint k : siblingKeys) {
				if (TagParser.KEY.equalsName(k.node()) && refer.getLocalPart().equals(k.name())) {
					referencedKey = k;
					break;
				}
			}
			if (referencedKey == null) {
				throw new Schema.ParseException(result.node(), refer + " did not match any known identity constraints");
			}
		}
		return new IdentityConstraint(result.node(), result.annotations(), name, targetNamespace, category, selector, fields, referencedKey);
	}

	static void register() {
		AttrParser.register(AttrParser.Names.REFER, QName.class, NodeHelper::getAttrValueAsQName);
		TagParser.register(new String[] { TagParser.Names.KEY, TagParser.Names.KEYREF, TagParser.Names.UNIQUE }, parser, IdentityConstraint.class, IdentityConstraint::parse);
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
