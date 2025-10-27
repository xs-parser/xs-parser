package xs.parser;

import java.util.*;
import java.util.concurrent.*;
import javax.xml.*;
import org.w3c.dom.*;
import xs.parser.internal.*;
import xs.parser.internal.util.*;
import xs.parser.internal.util.SequenceParser.*;
import xs.parser.v.*;

/**
 * An assertion is a predicate associated with a type, which is checked for each instance of the type. If an element or attribute information item fails to satisfy an assertion associated with a given type, then that information item is not locally ·valid· with respect to that type.
 *
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
			this.prefix = Objects.requireNonNull(prefix);
			this.namespace = Objects.requireNonNull(namespace);
		}

		/** @return An xs:NCName value. Required. */
		public String prefix() {
			return prefix;
		}

		/** @return An xs:anyURI value. Required. */
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
			final Deque<Node> xmlnsAttrs = new ConcurrentLinkedDeque<>();
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
			this.namespaceBindings = new LinkedHashSet<>();
			while (!xmlnsAttrs.isEmpty()) {
				final Node attr = xmlnsAttrs.removeLast();
				final String prefix = XMLConstants.XMLNS_ATTRIBUTE.equals(attr.getNodeName())
						? XMLConstants.DEFAULT_NS_PREFIX
						: attr.getNodeName().substring(XMLConstants.XMLNS_ATTRIBUTE.length() + 1);
				final String namespace = ((Attr) attr).getValue();
				this.namespaceBindings.add(new NamespaceBinding(prefix, namespace));
			}
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
			this.expression = Objects.requireNonNull(expression);
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
		 * @return Let D be the ·actual value· of the xpathDefaultNamespace [attribute], if present on the host element, otherwise that of the xpathDefaultNamespace [attribute] of the &lt;schema&gt; ancestor. Then the value is the appropriate case among the following:
		 * <ol>
		 *   <li>If D is ##defaultNamespace, then the appropriate case among the following:
		 *     <ol>
		 *       <li>If there is an entry in the [in-scope namespaces] of the host element whose [prefix] is ·absent·, then the corresponding [namespace name];</li>
		 *       <li>otherwise ·absent·;</li>
		 *     </ol>
		 *   </li>
		 *   <li>If D is ##targetNamespace, then the appropriate case among the following:
		 *     <ol>
		 *       <li>If the targetNamespace [attribute] is present on the &lt;schema&gt; ancestor, then its ·actual value·;</li>
		 *       <li>otherwise ·absent·;</li>
		 *     </ol>
		 *   </li>
		 *   <li>If D is ##local, then ·absent·;</li>
		 *   <li>otherwise (D is an xs:anyURI value) D.</li>
		 * </ol>
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

	private final Deferred<? extends AnnotatedComponent> context;
	private final Node node;
	private final Deque<Annotation> annotations;
	private final XPathExpression test;

	Assertion(final Deferred<? extends AnnotatedComponent> context, final Node node, final Deque<Annotation> annotations, final XPathExpression test) {
		this.context = Objects.requireNonNull(context);
		this.node = Objects.requireNonNull(node);
		this.annotations = Objects.requireNonNull(annotations);
		this.test = test;
	}

	private static Assertion parse(final Result result) {
		final Deferred<? extends AnnotatedComponent> context = result.context();
		final Node node = result.node();
		final Deque<Annotation> annotations = Annotation.of(result).resolve(node);
		final String expression = result.value(AttrParser.TEST);
		final String xpathDefaultNamespace = result.value(AttrParser.XPATH_DEFAULT_NAMESPACE);
		final XPathExpression test = new XPathExpression(result, xpathDefaultNamespace, expression);
		return new Assertion(context, node, annotations, test);
	}

	static void register() {
		AttrParser.register(AttrParser.Names.TEST, XPathExpression::getAttrValueAsXPath);
		AttrParser.register(AttrParser.Names.XPATH, XPathExpression::getAttrValueAsXPath);
		TagParser.register(TagParser.Names.FIELD, XPathExpression.parser, XPathExpression.class, XPathExpression::parse);
		TagParser.register(TagParser.Names.SELECTOR, XPathExpression.parser, XPathExpression.class, XPathExpression::parse);
		TagParser.register(TagParser.Names.ASSERTION, Assertion.parser, Assertion.class, Assertion::parse);
		VisitorHelper.register(Assertion.class, Assertion::visit);
	}

	void visit(final Visitor visitor) {
		if (visitor.visit(context.get(), node, this)) {
			visitor.onAssertion(context.get(), node, this);
			annotations.forEach(a -> a.visit(visitor));
		}
	}

	/** @return An XPath Expression property record, as described below, with &lt;assert&gt; as the "host element" and test as the designated expression [attribute]. */
	public XPathExpression test() {
		return test;
	}

	/** @return The ·annotation mapping· of the &lt;assert&gt; element, as defined in XML Representation of Annotation Schema Components (§3.15.2). */
	@Override
	public Deque<Annotation> annotations() {
		return Deques.unmodifiableDeque(annotations);
	}

}