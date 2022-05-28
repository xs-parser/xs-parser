package xs.parser.x;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import javax.xml.*;
import javax.xml.namespace.*;
import org.w3c.dom.*;
import xs.parser.*;
import xs.parser.internal.*;
import xs.parser.internal.util.*;

public abstract class NodeSet implements Iterable<NodeSet> {

	private static final class Namespaces implements Iterable<Map.Entry<String, String>>, NamespaceContext {

		private final Map<String, String> prefixToNamespaceUri = new HashMap<>();
		private final Map<String, String> namespaceUriToPrefix = new HashMap<>();

		private Namespaces() {
			put(XMLConstants.DEFAULT_NS_PREFIX, XMLConstants.NULL_NS_URI);
			put(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI);
			put(XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
			put("xs", XMLConstants.W3C_XML_SCHEMA_NS_URI);
			put("fn", "http://www.w3.org/2005/xpath-functions");
			put("math", "http://www.w3.org/2005/xpath-functions/math");
			put("map", "http://www.w3.org/2005/xpath-functions/map");
			put("array", "http://www.w3.org/2005/xpath-functions/array");
		}

		private void put(final String prefix, final String namespaceUri) {
			prefixToNamespaceUri.put(prefix, namespaceUri);
			namespaceUriToPrefix.put(namespaceUri, prefix);
		}

		@Override
		public String getNamespaceURI(final String prefix) {
			if (prefix == null) {
				throw new IllegalArgumentException(); // per Javadoc
			}
			return prefixToNamespaceUri.get(prefix);
		}

		@Override
		public String getPrefix(final String namespaceURI) {
			if (namespaceURI == null) {
				throw new IllegalArgumentException(); // per Javadoc
			}
			return namespaceUriToPrefix.get(namespaceURI);
		}

		@Override
		public Iterator<String> getPrefixes(final String namespaceURI) {
			final String prefix = getPrefix(namespaceURI);
			return prefix == null
					? Collections.emptyIterator()
					: Collections.singleton(prefix).iterator();
		}

		@Override
		public Iterator<Map.Entry<String, String>> iterator() {
			return prefixToNamespaceUri.entrySet().stream()
					.filter(e -> !XMLConstants.XMLNS_ATTRIBUTE.equals(e.getKey()))
					.iterator();
		}

	}

	protected abstract class BaseSpliterator<T extends Spliterator<NodeSet>> implements Spliterator<NodeSet> {

		protected int position;
		protected int countRemaining;

		protected abstract T newSpliterator(final int position, final int countRemaining);

		@Override
		public Spliterator<NodeSet> trySplit() {
			if (countRemaining <= 1) {
				return null;
			}
			final int newCountRemaining = countRemaining % 2 == 0 ? countRemaining / 2 : countRemaining / 2 + 1;
			countRemaining /= 2;
			return newSpliterator(countRemaining + 1, newCountRemaining);
		}

		@Override
		public long estimateSize() {
			return countRemaining;
		}

		@Override
		public int characteristics() {
			return Spliterator.SIZED | Spliterator.ORDERED;
		}

	}

	protected static final class Query {

		private final QueryType type;
		private final String expr;
		private final Object underlyingValue;

		Query(final QueryType type, final String expr, final Object underlyingValue) {
			this.type = type;
			this.expr = expr;
			this.underlyingValue = underlyingValue;
		}

		@Override
		public boolean equals(final Object other) {
			if (other == this) {
				return true;
			} else if (other instanceof Query) {
				final Query x = (Query) other;
				return Objects.equals(x.type, type) && Objects.equals(x.expr, expr) && Objects.equals(x.underlyingValue, underlyingValue);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return Objects.hash(type, expr, underlyingValue);
		}

	}

	protected enum QueryType {

		XPATH,
		XQUERY;

	}

	protected static final Supplier<IllegalStateException> IS_ATOMIC_EXCEPTION = () -> new IllegalStateException("isAtomic() must be " + false + " to invoke this method");
	protected static final Supplier<IllegalStateException> IS_NOT_ATOMIC_EXCEPTION = () -> new IllegalStateException("isAtomic() must be " + true + " to invoke this method");
	/**
	 * The default namespace context for evaluation of XPath and XQuery expressions.
	 * The following entries are defined:
	 * <table>
	 *   <caption>Default namespace context entries</caption>
	 *   <thead>
	 *     <tr><td><b>Prefix</b></td><td><b>Namespace URI</b></td></tr>
	 *   </thead>
	 *   <tbody>
	 *     <tr>
	 *       <td><code>""</code></td><td><code>""</code></td>
	 *     </tr>
	 *     <tr>
	 *       <td><code>"xml"</code></td><td><code>"http://www.w3.org/XML/1998/namespace"</code></td>
	 *     </tr>
	 *     <tr>
	 *       <td><code>"xmlns"</code></td><td><code>"http://www.w3.org/2000/xmlns/"</code></td>
	 *     </tr>
	 *     <tr>
	 *       <td><code>"xs"</code></td><td><code>"http://www.w3.org/2001/XMLSchema"</code></td>
	 *     </tr>
	 *     <tr>
	 *       <td><code>"fn"</code></td><td><code>"http://www.w3.org/2005/xpath-functions"</code></td>
	 *     </tr>
	 *     <tr>
	 *       <td><code>"math"</code></td><td><code>"http://www.w3.org/2005/xpath-functions/math"</code></td>
	 *     </tr>
	 *     <tr>
	 *       <td><code>"map"</code></td><td><code>"http://www.w3.org/2005/xpath-functions/map"</code></td>
	 *     </tr>
	 *     <tr>
	 *       <td><code>"array"</code></td><td><code>"http://www.w3.org/2005/xpath-functions/array"</code></td>
	 *     </tr>
	 *   </tbody>
	 * </table>
	 */
	public static final NamespaceContext DEFAULT_NAMESPACE_CONTEXT = new Namespaces();

	protected final Map<Query, NodeSet> queryResultCache;
	protected final NamespaceContext namespaceContext;
	protected final String expr;
	protected final Object underlyingValue;

	protected NodeSet(
			final NamespaceContext namespaceContext,
			final Map<Query, NodeSet> queryResultCache,
			final String expr,
			final Object underlyingValue) {
		this.namespaceContext = Objects.requireNonNull(namespaceContext);
		this.queryResultCache = Objects.requireNonNull(queryResultCache);
		this.expr = Objects.requireNonNull(expr);
		this.underlyingValue = Objects.requireNonNull(underlyingValue);
	}

	protected static Set<Schema> constituentSchemas(final Schema schema) {
		final Set<Schema> schemas = new LinkedHashSet<>();
		NodeHelper.findAllConstituentSchemas(schema, schemas);
		return schemas;
	}

	/**
	 * Returns a new {@code NodeSet} with the given namespace context and node.
	 * @param namespaceContext the namespace context
	 * @param node the node
	 * @return a new {@code NodeSet} with the given namespace context and node
	 */
	public static NodeSet of(final NamespaceContext namespaceContext, final Node node) {
		return SaxonProcessor.IS_SAXON_LOADED
				? new SaxonNodeSet(namespaceContext, node)
				: new JaxpNodeSet(namespaceContext, node);
	}

	/**
	 * Returns a new {@code NodeSet} with the {@link #DEFAULT_NAMESPACE_CONTEXT} and node.
	 * @param node the node
	 * @return a new {@code NodeSet} with the {@link #DEFAULT_NAMESPACE_CONTEXT} and node
	 */
	public static NodeSet of(final Node node) {
		return of(DEFAULT_NAMESPACE_CONTEXT, node);
	}

	/**
	 * Returns a new {@code NodeSet} with the given namespace context and schema.
	 * @param namespaceContext the namespace context
	 * @param schema the schema
	 * @return a new {@code NodeSet} with the given namespace context and schema
	 */
	public static NodeSet of(final NamespaceContext namespaceContext, final Schema schema) {
		return SaxonProcessor.IS_SAXON_LOADED
				? new SaxonNodeSet(namespaceContext, schema)
				: new JaxpNodeSet(namespaceContext, schema);
	}

	/**
	 * Returns a new {@code NodeSet} with the {@link #DEFAULT_NAMESPACE_CONTEXT} and schema.
	 * @param schema the schema
	 * @return a new {@code NodeSet} with the {@link #DEFAULT_NAMESPACE_CONTEXT} and schema
	 */
	public static NodeSet of(final Schema schema) {
		return of(DEFAULT_NAMESPACE_CONTEXT, schema);
	}

	/**
	 * Evaluates the given XPath expression for every node in this {@code NodeSet}.
	 * @param expression the XPath expression
	 * @return a new {@code NodeSet} with the result of the XPath evaluation
	 */
	public abstract NodeSet xpath(final String expression);

	/**
	 * Evaluates the given expression for every node in this {@code NodeSet}.
	 * @param expression the XQuery expression
	 * @return a new {@code NodeSet} with the result of the XQuery evaluation
	 */
	public abstract NodeSet xquery(final String expression);

	/**
	 * Returns the double value of the atomic value.
	 * @return the double value of the atomic value
	 * @throws IllegalStateException if {@link #isAtomic()} is {@code false}
	 */
	public abstract double getDoubleValue();

	/**
	 * Returns the long value of the atomic value.
	 * @return the long value of the atomic value
	 * @throws IllegalStateException if {@link #isAtomic()} is {@code false}
	 */
	public abstract long getLongValue();

	/**
	 * Returns the string value of the atomic value.
	 * @return the string value of the atomic value
	 * @throws IllegalStateException if {@link #isAtomic()} is {@code false}
	 */
	public abstract String getStringValue();

	/**
	 * Returns the boolean value of the atomic value.
	 * @return the boolean value of the atomic value
	 * @throws IllegalStateException if {@link #isAtomic()} is {@code false}
	 */
	public abstract boolean getBooleanValue();

	/**
	 * Returns the number of items contained within this {@code NodeSet}.
	 * @return the number of items contained within this {@code NodeSet}
	 */
	public abstract int size();

	/**
	 * Returns {@code true} if this {@code NodeSet} is a single atomic value.
	 * <br>This method will return {@code false} in the case of multiple atomic values; in that case, use {@link #split()} to handle each atomic value individually.
	 * This method must return {@code true} in order to invoke atomic getter methods such as {@link #getBooleanValue()}, {@link #getDoubleValue()}, {@link #getLongValue()}, and {@link #getStringValue()}.
	 * @return {@code true} if this {@code NodeSet} is a single atomic value
	 */
	public abstract boolean isAtomic();

	/**
	 * Returns a stream of {@code NodeSet}s. The possible values are:
	 * <table>
	 *   <caption>Return values</caption>
	 *   <thead>
	 *     <tr><td>size()</td><td>Returns</td></tr>
	 *   </thead>
	 *   <tbody>
	 *     <tr><td>0</td><td><code>Stream.empty()</code></td></tr>
	 *     <tr><td>1</td><td><code>Stream.of(this)</code></td></tr>
	 *     <tr><td>&gt; 1</td><td>a <code>Stream</code> of <code>NodeSet</code>s with <code>size() == 1</code></td></tr>
	 *   </tbody>
	 * </table>
	 * @return a stream of {@code NodeSet}s with {@code size() == 1}
	 */
	public abstract Stream<NodeSet> split();

	/**
	 * Returns a stream of nodes contained by this {@code NodeSet}.
	 * @return a stream of nodes contained by this {@code NodeSet}
	 * @throws IllegalStateException if {@link #isAtomic()} is {@code true}
	 */
	public abstract Stream<Node> stream();

	/**
	 * Returns the expression used to evaluate or construct this {@code NodeSet}, defaults to {@code ""} if no expression has been evaluated yet.
	 * @return the expression used to evaluate or construct this {@code NodeSet}, defaults to {@code ""} if no expression has been evaluated yet
	 */
	public String getExpression() {
		return expr;
	}

	/**
	 * Returns {@code true} if {@link #size()} is 0.
	 * @return {@code true} if {@link #size()} is 0
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * Returns {@code true} if {@link #size()} is greater than 0.
	 * @return {@code true} if {@link #size()} is greater than 0
	 */
	public boolean isPresent() {
		return !isEmpty();
	}

	/**
	 * Returns the first node present, or {@code null} if {@link #size()} is 0.
	 * @return the first node present, or {@code null} if {@link #size()} is 0
	 * @throws IllegalStateException if {@link #isAtomic()} is {@code true}
	 */
	public Node getSingleNodeValue() {
		return stream().findAny().orElse(null);
	}

	/**
	 * Returns an iterator over this {@code NodeSet}, equivalent to {@code split().iterator()}.
	 * @return an iterator over this {@code NodeSet}
	 */
	@Override
	public Iterator<NodeSet> iterator() {
		return split().iterator();
	}

}
