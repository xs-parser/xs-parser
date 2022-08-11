package xs.parser;

import java.util.*;
import javax.xml.namespace.*;
import org.w3c.dom.*;
import xs.parser.Assertion.*;
import xs.parser.internal.*;
import xs.parser.internal.util.*;
import xs.parser.internal.util.SequenceParser.*;
import xs.parser.v.*;

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
			if (TagParser.KEY.equalsName(node)) {
				return KEY;
			} else if (TagParser.KEYREF.equalsName(node)) {
				return KEYREF;
			} else if (TagParser.UNIQUE.equalsName(node)) {
				return UNIQUE;
			} else {
				throw new IllegalArgumentException(NodeHelper.toString(node));
			}
		}

	}

	private static final SequenceParser parser = new SequenceParser()
			.optionalAttributes(AttrParser.ID, AttrParser.NAME, AttrParser.REF, AttrParser.REFER)
			.elements(0, 1, TagParser.ANNOTATION)
			.elements(0, 1, TagParser.SELECTOR)
			.elements(0, Integer.MAX_VALUE, TagParser.FIELD);

	private final Deferred<? extends AnnotatedComponent> context;
	private final Node node;
	private final Deque<Annotation> annotations;
	private final String name;
	private final String targetNamespace;
	private final Category category;
	private final Deferred<XPathExpression> selector;
	private final Deque<XPathExpression> fields;
	private final IdentityConstraint referencedKey;

	private IdentityConstraint(final Deferred<? extends AnnotatedComponent> context, final Node node, final Deque<Annotation> annotations, final String name, final String targetNamespace, final Category category, final Deferred<XPathExpression> selector, final Deque<XPathExpression> fields, final IdentityConstraint referencedKey) {
		this.context = Objects.requireNonNull(context);
		this.node = Objects.requireNonNull(node);
		this.annotations = Objects.requireNonNull(annotations);
		this.name = name;
		this.targetNamespace = NodeHelper.requireNonEmpty(node, targetNamespace);
		this.category = Objects.requireNonNull(category);
		this.selector = Objects.requireNonNull(selector);
		this.fields = Objects.requireNonNull(fields);
		this.referencedKey = referencedKey;
	}

	private static IdentityConstraint parse(final Result result) {
		final Deferred<? extends AnnotatedComponent> context = result.context();
		final Node node = result.node();
		final Deque<Annotation> annotations = Annotation.of(result).resolve(node);
		final String name = result.value(AttrParser.NAME);
		final String targetNamespace = result.schema().targetNamespace();
		final Category category = Category.fromNode(node);
		final QName refAttr = result.value(AttrParser.REF);
		if (refAttr != null) {
			IdentityConstraint ref = null;
			final Deque<IdentityConstraint> siblingKeys = result.parseAll(TagParser.KEY, TagParser.KEYREF, TagParser.UNIQUE);
			for (final IdentityConstraint k : siblingKeys) {
				if (category.equals(Category.fromNode(k.node)) && refAttr.getLocalPart().equals(k.name())) {
					ref = k;
					break;
				}
			}
			if (ref == null) {
				throw new Schema.ParseException(node, "No sibling key for identity constraint @ref");
			}
			return new IdentityConstraint(context, node, annotations, ref.name(), ref.targetNamespace(), category, ref.selector, ref.fields(), ref.referencedKey());
		}
		final Deferred<XPathExpression> selector = result.parse(TagParser.SELECTOR);
		final Deque<XPathExpression> fields = result.parseAll(TagParser.FIELD);
		final QName refer = result.value(AttrParser.REFER);
		IdentityConstraint referencedKey = null;
		if (refer != null && Category.KEYREF.equals(category)) {
			final Deque<IdentityConstraint> siblingKeys = result.parseAll(TagParser.KEY, TagParser.KEYREF, TagParser.UNIQUE);
			for (final IdentityConstraint k : siblingKeys) {
				if (Category.KEY.equals(k.category) && NodeHelper.equalsName(refer, k)) {
					referencedKey = k;
					break;
				}
			}
			if (referencedKey == null) {
				throw new Schema.ParseException(node, refer + " did not match any known identity constraints");
			}
		}
		return new IdentityConstraint(context, node, annotations, name, targetNamespace, category, selector, fields, referencedKey);
	}

	static void register() {
		AttrParser.register(AttrParser.Names.REFER, QName.class, NodeHelper::getAttrValueAsQName);
		TagParser.register(TagParser.Names.KEY, parser, IdentityConstraint.class, IdentityConstraint::parse);
		TagParser.register(TagParser.Names.KEYREF, parser, IdentityConstraint.class, IdentityConstraint::parse);
		TagParser.register(TagParser.Names.UNIQUE, parser, IdentityConstraint.class, IdentityConstraint::parse);
		VisitorHelper.register(IdentityConstraint.class, IdentityConstraint::visit);
	}

	void visit(final Visitor visitor) {
		if (visitor.visit(context.get(), node, this)) {
			visitor.onIdentityConstraint(context.get(), node, this);
			annotations.forEach(a -> a.visit(visitor));
			if (referencedKey != null) {
				referencedKey.visit(visitor);
			}
		}
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
		return selector.get();
	}

	public Deque<XPathExpression> fields() {
		return Deques.unmodifiableDeque(fields);
	}

	public IdentityConstraint referencedKey() {
		return referencedKey;
	}

	@Override
	public Deque<Annotation> annotations() {
		return Deques.unmodifiableDeque(annotations);
	}

}
