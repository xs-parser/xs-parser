package xs.parser.x;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import javax.xml.namespace.*;
import javax.xml.xpath.*;
import org.w3c.dom.*;
import xs.parser.*;
import xs.parser.internal.util.*;

final class JaxpNodeSet extends NodeSet {

	private final class JaxpSpliterator extends BaseSpliterator<JaxpSpliterator> {

		JaxpSpliterator(final int position, final int countRemaining) {
			this.position = position;
			this.countRemaining = countRemaining;
		}

		@Override
		protected JaxpSpliterator newSpliterator(final int position, final int countRemaining) {
			return new JaxpSpliterator(position, countRemaining);
		}

		@Override
		public boolean tryAdvance(final Consumer<? super NodeSet> action) {
			if (countRemaining > 0) {
				final NodeList n = (NodeList) underlyingValue;
				final Node node = n.item(position++);
				--countRemaining;
				action.accept(new JaxpNodeSet(namespaceContext, queryResultCache, expr, node, defaultCollection, false));
				return true;
			}
			return false;
		}

	}

	private final class CollectionResolver implements XPathFunctionResolver {

		@Override
		public XPathFunction resolveFunction(final QName functionName, final int arity) {
			if (arity == 0 && "http://www.w3.org/2005/xpath-functions".equals(functionName.getNamespaceURI())) {
				switch (functionName.getLocalPart()) {
				case "collection":
					return new XPathFunction() {

						@Override
						public Object evaluate(final @SuppressWarnings("rawtypes") List args) throws XPathFunctionException {
							return defaultCollection;
						}

					};
				case "uri-collection":
					return new XPathFunction() {

						@Override
						public Object evaluate(final @SuppressWarnings("rawtypes") List args) throws XPathFunctionException {
							if (defaultCollection != null) {
								return new NodeList() {

									final Document doc = NodeHelper.newDocument();

									@Override
									public Node item(final int index) {
										final String docUri = NodeHelper.ownerDocument(defaultCollection.item(index)).getDocumentURI();
										return doc.createTextNode(docUri);
									}

									@Override
									public int getLength() {
										return defaultCollection.getLength();
									}

								};
							}
							return null;
						}

					};
				default:
					break;
				}
			}
			return null;
		}

	}

	private static final class Nodes implements NodeList {

		private final Node[] n;

		Nodes(final Stream<Node> stream) {
			this.n = stream.toArray(Node[]::new);
		}

		@Override
		public Node item(final int index) {
			return n[index];
		}

		@Override
		public int getLength() {
			return n.length;
		}

	}

	private final NodeList defaultCollection;
	private final boolean isAtomic;

	JaxpNodeSet(final NamespaceContext namespaceContext,
			final Map<Query, NodeSet> queryResultCache,
			final String expr,
			final Object underlyingValue,
			final NodeList defaultCollection,
			final boolean isAtomic) {
		super(namespaceContext, queryResultCache, expr, underlyingValue);
		this.defaultCollection = defaultCollection;
		this.isAtomic = isAtomic;
	}

	JaxpNodeSet(final NamespaceContext namespaceContext, final Node node) {
		this(namespaceContext, new LinkedHashMap<>(), node.getOwnerDocument().getDocumentURI(), node, new Nodes(Stream.of(node)), false);
	}

	JaxpNodeSet(final NamespaceContext namespaceContext, final Schema schema) {
		this(namespaceContext, new LinkedHashMap<>(), schema.node().getOwnerDocument().getDocumentURI(), schema.node().getOwnerDocument(), new Nodes(constituentSchemas(schema).stream().map(s -> s.node().getOwnerDocument())), false);
	}

	@Override
	public NodeSet xpath(final String expression) {
		Objects.requireNonNull(expression);
		final Query query = new Query(QueryType.XPATH, expression, underlyingValue);
		return queryResultCache.computeIfAbsent(query, q -> {
			try {
				final XPathFactory xpathFactory = XPathFactory.newInstance();
				final XPath xpath = xpathFactory.newXPath();
				xpath.setNamespaceContext(namespaceContext);
				xpath.setXPathFunctionResolver(new CollectionResolver());
				if (namespaceContext != null) {
					xpath.setNamespaceContext(namespaceContext);
				}
				final XPathExpression xpathExpr = xpath.compile(expression);
				try {
					final NodeList collection;
					if (underlyingValue instanceof Node) {
						collection = (NodeList) xpathExpr.evaluate(underlyingValue, XPathConstants.NODESET);
					} else if (underlyingValue instanceof NodeList) {
						final NodeList nodeList = (NodeList) underlyingValue;
						final Deque<NodeList> nodeLists = new ArrayDeque<>(nodeList.getLength());
						for (int i = 0; i < nodeList.getLength(); ++i) {
							nodeLists.add((NodeList) xpathExpr.evaluate(nodeList.item(i), XPathConstants.NODESET));
						}
						collection = new NodeList() {

							final int length = nodeLists.stream().map(NodeList::getLength).reduce(0, Integer::sum);

							@Override
							public Node item(final int index) {
								if (index < 0 || index >= length) {
									throw new IndexOutOfBoundsException(Integer.toString(index));
								}
								int i = index;
								for (final NodeList n : nodeLists) {
									if (i < n.getLength()) {
										return n.item(i);
									}
									i -= n.getLength();
								}
								throw new AssertionError(index); // This should be impossible due to the above invariant check
							}

							@Override
							public int getLength() {
								return length;
							}

						};
					} else {
						throw new IllegalStateException(underlyingValue != null ? underlyingValue.getClass().toString() : "null");
					}
					return new JaxpNodeSet(namespaceContext, queryResultCache, expr, collection, collection, false);
				} catch (final XPathExpressionException e) {
					final String result = xpathExpr.evaluate(underlyingValue);
					return new JaxpNodeSet(namespaceContext, queryResultCache, expr, result, null, true);
				}
			} catch (final XPathExpressionException e) {
				throw new RuntimeException(e);
			}
		});
	}

	@Override
	public NodeSet xquery(final String expression) {
		throw new UnsupportedOperationException(getClass() + " does not support XQuery expressions, use " + SaxonNodeSet.class + " instead");
	}

	@Override
	public double getDoubleValue() {
		if (underlyingValue instanceof Double) {
			return (Double) underlyingValue;
		}
		return Double.parseDouble(getStringValue());
	}

	@Override
	public long getLongValue() {
		if (underlyingValue instanceof Long) {
			return (Long) underlyingValue;
		}
		return Long.parseLong(getStringValue());
	}

	@Override
	public String getStringValue() {
		if (isAtomic()) {
			return String.valueOf(underlyingValue);
		}
		throw IS_NOT_ATOMIC_EXCEPTION.get();
	}

	@Override
	public boolean getBooleanValue() {
		if (underlyingValue instanceof Boolean) {
			return (Boolean) underlyingValue;
		}
		return Boolean.parseBoolean(getStringValue());
	}

	@Override
	public int size() {
		if (underlyingValue == null) {
			return 0;
		} else if (isAtomic || underlyingValue instanceof Node) {
			return 1;
		}
		return ((NodeList) underlyingValue).getLength();
	}

	@Override
	public boolean isAtomic() {
		return isAtomic;
	}

	@Override
	public Stream<NodeSet> split() {
		if (isEmpty()) {
			return Stream.empty();
		} else if (size() == 1) {
			return Stream.of(this);
		}
		return StreamSupport.stream(new JaxpSpliterator(0, size()), false);
	}

	@Override
	public Stream<Node> stream() {
		if (isAtomic()) {
			throw IS_ATOMIC_EXCEPTION.get();
		} else if (underlyingValue instanceof Node) {
			return Stream.of((Node) underlyingValue);
		} else if (underlyingValue instanceof NodeList) {
			final NodeList n = (NodeList) underlyingValue;
			return IntStream.range(0, n.getLength()).mapToObj(n::item);
		}
		throw new AssertionError("Unexpected type: " + underlyingValue.getClass());
	}

}
