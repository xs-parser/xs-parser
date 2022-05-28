package xs.parser;

import java.util.*;
import javax.xml.*;
import org.w3c.dom.*;
import xs.parser.internal.*;
import xs.parser.internal.util.*;
import xs.parser.internal.util.SequenceParser.*;

/**
 * <pre>
 * &lt;assertion
 *   id = ID
 *   test = an XPath expression
 *   xpathDefaultNamespace = (anyURI | (##defaultNamespace | ##targetNamespace | ##local))
 *   {any attributes with non-schema namespace . . .}&gt;
 *   Content: (annotation?)
 * &lt;/assertion&gt;
 * </pre>
 *
 * <table>
 *   <caption style="font-size: large; text-align: left">Schema Component: Assertion, a kind of Annotated Component</caption>
 *   <thead>
 *     <tr>
 *       <th style="text-align: left">Method</th>
 *       <th style="text-align: left">Property</th>
 *       <th style="text-align: left">Representation</th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <td>{@link Assertion#annotations()}</td>
 *       <td>{annotations}</td>
 *       <td>A sequence of Annotation components.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link Assertion#test()}</td>
 *       <td>{test}</td>
 *       <td>An XPath Expression property record. Required.</td>
 *     </tr>
 *   </tbody>
 * </table>
 */
public class Assertion implements AnnotatedComponent {

	/**
	 * <table>
	 *   <caption style="font-size: large; text-align: left">Property Record: Namespace Binding</caption>
	 *   <thead>
	 *     <tr>
	 *       <th style="text-align: left">Method</th>
	 *       <th style="text-align: left">Property</th>
	 *       <th style="text-align: left">Representation</th>
	 *     </tr>
	 *   </thead>
	 *   <tbody>
	 *     <tr>
	 *       <td>{@link NamespaceBinding#prefix()}</td>
	 *       <td>{prefix}</td>
	 *       <td>An xs:NCName value. Required.</td>
	 *     </tr>
	 *     <tr>
	 *       <td>{@link NamespaceBinding#namespace()}</td>
	 *       <td>{namespace}</td>
	 *       <td>An xs:anyURI value. Required.</td>
	 *     </tr>
	 *   </tbody>
	 * </table>
	 */
	public static class NamespaceBinding {

		private final String prefix;
		private final String namespace;

		private NamespaceBinding(final String prefix, final String namespace) {
			this.prefix = prefix;
			this.namespace = namespace;
		}

		public String prefix() {
			return prefix;
		}

		public String namespace() {
			return namespace;
		}

	}

	/**
	 * <pre>
	 * &lt;selector
	 *   id = ID
	 *   xpath = a subset of XPath expression, see below
	 *   xpathDefaultNamespace = (anyURI | (##defaultNamespace | ##targetNamespace | ##local))
	 *   {any attributes with non-schema namespace . . .}&gt;
	 *   Content: (annotation?)
	 * &lt;/selector&gt;
	 *
	 * &lt;field
	 *   id = ID
	 *   xpath = a subset of XPath expression, see below
	 *   xpathDefaultNamespace = (anyURI | (##defaultNamespace | ##targetNamespace | ##local))
	 *   {any attributes with non-schema namespace . . .}&gt;
	 *   Content: (annotation?)
	 * &lt;/field&gt;
	 * </pre>
	 *
	 * <table>
	 *   <caption style="font-size: large; text-align: left">Property Record: XPath Expression</caption>
	 *   <thead>
	 *     <tr>
	 *       <th style="text-align: left">Method</th>
	 *       <th style="text-align: left">Property</th>
	 *       <th style="text-align: left">Representation</th>
	 *     </tr>
	 *   </thead>
	 *   <tbody>
	 *     <tr>
	 *       <td>{@link XPathExpression#namespaceBindings()}</td>
	 *       <td>{namespace bindings}</td>
	 *       <td>A set of Namespace Binding property records.</td>
	 *     </tr>
	 *     <tr>
	 *       <td>{@link XPathExpression#defaultNamespace()}</td>
	 *       <td>{default namespace}</td>
	 *       <td>An xs:anyURI value. Optional.</td>
	 *     </tr>
	 *     <tr>
	 *       <td>{@link XPathExpression#baseURI()}</td>
	 *       <td>{base URI}</td>
	 *       <td>An xs:anyURI value. Optional.</td>
	 *     </tr>
	 *     <tr>
	 *       <td>{@link XPathExpression#expression()}</td>
	 *       <td>{expression}</td>
	 *       <td>An [XPath 2.0] expression. Required.</td>
	 *     </tr>
	 *   </tbody>
	 * </table>
	 */
	public static class XPathExpression {

		private static final SequenceParser parser = new SequenceParser()
				.requiredAttributes(AttrParser.XPATH)
				.optionalAttributes(AttrParser.ID, AttrParser.XPATH_DEFAULT_NAMESPACE)
				.elements(0, 1, TagParser.ANNOTATION);

		private final Set<NamespaceBinding> namespaceBindings;
		private final String defaultNamespace;
		private final String baseURI;
		private final String expression;

		XPathExpression(final Result result, final String xpathDefaultNamespace, final String expression) {
			Node iter = result.node();
			final Deque<Node> xmlnsAttrs = new ArrayDeque<>();
			do {
				if (iter.hasAttributes()) {
					for (int i = 0; i < iter.getAttributes().getLength(); ++i) {
						final Node attr = iter.getAttributes().item(i);
						if (attr.getNodeName().startsWith(XMLConstants.XMLNS_ATTRIBUTE)) {
							xmlnsAttrs.addLast(attr);
						}
					}
				}
			} while ((iter = iter.getParentNode()) != null);
			final Map<String, String> xmlns = new LinkedHashMap<>();
			while (!xmlnsAttrs.isEmpty()) {
				final Node attr = xmlnsAttrs.removeLast();
				final String key = XMLConstants.XMLNS_ATTRIBUTE.equals(attr.getNodeName())
						? XMLConstants.DEFAULT_NS_PREFIX
						: attr.getNodeName().substring(XMLConstants.XMLNS_ATTRIBUTE.length() + 1);
				xmlns.put(key, ((Attr) attr).getValue());
			}
			this.namespaceBindings = new LinkedHashSet<>();
			xmlns.forEach((prefix, namespace) -> this.namespaceBindings.add(new NamespaceBinding(prefix, namespace)));
			final String defaultNs = xpathDefaultNamespace != null ? xpathDefaultNamespace : result.schema().xpathDefaultNamespace();
			switch (defaultNs) {
			case "##defaultNamespace":
				this.defaultNamespace = namespaceBindings.stream().filter(n -> XMLConstants.DEFAULT_NS_PREFIX.equals(n.prefix())).map(NamespaceBinding::namespace).findAny().orElse(null);
				break;
			case "##targetNamespace":
				this.defaultNamespace = result.schema().targetNamespace();
				break;
			case "##local":
				this.defaultNamespace = null;
				break;
			default:
				this.defaultNamespace = defaultNs;
			}
			this.baseURI = result.node().getBaseURI();
			this.expression = expression;
		}

		private static XPathExpression parse(final Result result) {
			final String expression = result.value(AttrParser.XPATH);
			final String xpathDefaultNamespace = result.value(AttrParser.XPATH_DEFAULT_NAMESPACE);
			return new XPathExpression(result, xpathDefaultNamespace, expression);
		}

		private static String getAttrValueAsXPath(final Attr attr) {
			// TODO: Parse and validate XPath
			return NodeHelper.collapseWhitespace(attr.getValue());
		}

		/** @return A set of Namespace Binding property records. Each member corresponds to an entry in the [in-scope namespaces] of the host element, with {prefix} being the [prefix] and {namespace} the [namespace name]. */
		public Set<NamespaceBinding> namespaceBindings() {
			return Collections.unmodifiableSet(namespaceBindings);
		}

		/**
		 *
		 * @return Let D be the ·actual value· of the xpathDefaultNamespace [attribute], if present on the host element, otherwise that of the xpathDefaultNamespace [attribute] of the &lt;schema&gt; ancestor. Then the value is the appropriate case among the following:
		 * <br>1 If D is ##defaultNamespace, then the appropriate case among the following:
		 * <br>  1.1 If there is an entry in the [in-scope namespaces] of the host element whose [prefix] is ·absent·, then the corresponding [namespace name];
		 * <br>  1.2 otherwise ·absent·;
		 * <br>2 If D is ##targetNamespace, then the appropriate case among the following:
		 * <br>  2.1 If the targetNamespace [attribute] is present on the &lt;schema&gt; ancestor, then its ·actual value·;
		 * <br>  2.2 otherwise ·absent·;
		 * <br>3 If D is ##local, then ·absent·;
		 * <br>4 otherwise (D is an xs:anyURI value) D.
		 */
		public String defaultNamespace() {
			return defaultNamespace;
		}

		/** @return The [base URI] of the host element. */
		public String baseURI() {
			return baseURI;
		}

		/** @return An XPath expression corresponding to the ·actual value· of the designated [attribute] of the host element. */
		public String expression() {
			return expression;
		}

	}

	private static final SequenceParser parser = new SequenceParser()
			.requiredAttributes(AttrParser.TEST)
			.optionalAttributes(AttrParser.ID, AttrParser.XPATH_DEFAULT_NAMESPACE)
			.elements(0, 1, TagParser.ANNOTATION);

	private final Node node;
	private final Deque<Annotation> annotations;
	private final XPathExpression test;

	Assertion(final Node node, final Deque<Annotation> annotations, final XPathExpression test) {
		this.node = Objects.requireNonNull(node);
		this.annotations = Objects.requireNonNull(annotations);
		this.test = test;
	}

	private static Assertion parse(final Result result) {
		final XPathExpression test = XPathExpression.parse(result);
		return new Assertion(result.node(), result.annotations(), test);
	}

	static void register() {
		AttrParser.register(AttrParser.Names.TEST, XPathExpression::getAttrValueAsXPath);
		AttrParser.register(AttrParser.Names.XPATH, XPathExpression::getAttrValueAsXPath);
		TagParser.register(new String[] { TagParser.Names.FIELD, TagParser.Names.SELECTOR }, XPathExpression.parser, XPathExpression.class, XPathExpression::parse);
		TagParser.register(TagParser.Names.ASSERTION, Assertion.parser, Assertion.class, Assertion::parse);
	}

	/** @return An XPath Expression property record, as described below, with &lt;assert&gt; as the "host element" and test as the designated expression [attribute]. */
	public XPathExpression test() {
		return test;
	}

	@Override
	public Node node() {
		return node;
	}

	/** @return The ·annotation mapping· of the &lt;assert&gt; element, as defined in XML Representation of Annotation Schema Components (§3.15.2). */
	@Override
	public Deque<Annotation> annotations() {
		return Deques.unmodifiableDeque(annotations);
	}

}