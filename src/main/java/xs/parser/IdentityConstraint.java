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
 * An identity-constraint definition is an association between a name and one of several varieties of identity-constraint related to uniqueness and reference. All the varieties use [XPath 2.0] expressions to pick out sets of information items relative to particular target element information items which are unique, or a key, or a ·valid· reference, within a specified scope. An element information item is only ·valid· with respect to an element declaration with identity-constraint definitions if those definitions are all satisfied for all the descendants of that element information item which they pick out.
 *
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
 *
 * <table>
 *   <caption style="font-size: large; text-align: left">Schema Component: Identity-Constraint Definition, a kind of Annotated Component</caption>
 *   <thead>
 *     <tr>
 *       <th style="text-align: left">Method</th>
 *       <th style="text-align: left">Property</th>
 *       <th style="text-align: left">Representation</th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <td>{@link IdentityConstraint#annotations()}</td>
 *       <td>{annotations}</td>
 *       <td>A sequence of Annotation components.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link IdentityConstraint#name()}</td>
 *       <td>{name}</td>
 *       <td>An xs:NCName value. Required.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link IdentityConstraint#targetNamespace()}</td>
 *       <td>{target namespace}</td>
 *       <td>An xs:anyURI value. Optional.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link IdentityConstraint#category()}</td>
 *       <td>{identity-constraint category}</td>
 *       <td>One of {key, keyref, unique}. Required.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link IdentityConstraint#selector()}</td>
 *       <td>{selector}</td>
 *       <td>An XPath Expression property record. Required.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link IdentityConstraint#fields()}</td>
 *       <td>{fields}</td>
 *       <td>A sequence of XPath Expression property records.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link IdentityConstraint#referencedKey()}</td>
 *       <td>{referenced key}</td>
 *       <td>An Identity-Constraint Definition component. Required if {identity-constraint category} is keyref, otherwise ({identity-constraint category} is key or unique) must be ·absent·.<br>If a value is present, its {identity-constraint category} must be key or unique.</td>
 *     </tr>
 *   </tbody>
 * </table>
 */
public class IdentityConstraint implements AnnotatedComponent {

	/** Identity-constraint category */
	public enum Category {

		/** Identity-constraint category key */
		KEY,
		/** Identity-constraint category keyref */
		KEYREF,
		/** Identity-constraint category unique */
		UNIQUE;

		private static Category fromNode(final Node node) {
			if (TagParser.KEY.equalsName(node)) {
				return KEY;
			} else if (TagParser.KEYREF.equalsName(node)) {
				return KEYREF;
			} else if (TagParser.UNIQUE.equalsName(node)) {
				return UNIQUE;
			}
			throw new IllegalArgumentException(NodeHelper.toString(node));
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
			visitor.onIdentityConstraint(context.get(), (org.w3c.dom.Element) node.cloneNode(true), this);
			annotations.forEach(a -> a.visit(visitor));
			if (referencedKey != null) {
				referencedKey.visit(visitor);
			}
		}
	}

	/** @return The ·actual value· of the name [attribute]. */
	public String name() {
		return name;
	}

	/** @return The ·actual value· of the targetNamespace [attribute] of the &lt;schema&gt; ancestor element information item if present, otherwise ·absent·. */
	public String targetNamespace() {
		return targetNamespace;
	}

	/** @return One of key, keyref or unique, depending on the item. */
	public Category category() {
		return category;
	}

	/** @return An XPath Expression property record, as described in section XML Representation of Assertion Schema Components (§3.13.2), with &lt;selector&gt; as the "host element" and xpath as the designated expression [attribute]. */
	public XPathExpression selector() {
		return selector.get();
	}

	/** @return A sequence of XPath Expression property records, corresponding to the &lt;field&gt; element information item [children], in order, following the rules given in XML Representation of Assertion Schema Components (§3.13.2), with &lt;field&gt; as the "host element" and xpath as the designated expression [attribute]. */
	public Deque<XPathExpression> fields() {
		return Deques.unmodifiableDeque(fields);
	}

	/** @return If the item is a &lt;keyref&gt;, the identity-constraint definition ·resolved· to by the ·actual value· of the refer [attribute], otherwise ·absent·. */
	public IdentityConstraint referencedKey() {
		return referencedKey;
	}

	@Override
	public Deque<Annotation> annotations() {
		return Deques.unmodifiableDeque(annotations);
	}

}
