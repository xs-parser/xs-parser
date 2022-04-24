package xs.parser.x;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import javax.xml.namespace.*;
import net.sf.saxon.dom.*;
import net.sf.saxon.expr.*;
import net.sf.saxon.lib.*;
import net.sf.saxon.om.*;
import net.sf.saxon.s9api.*;
import net.sf.saxon.trans.*;
import org.w3c.dom.*;
import xs.parser.*;
import xs.parser.internal.*;

final class SaxonNodeSet extends NodeSet {

	private static final class DefaultCollection implements ResourceCollection {

		private final XdmValue xdmValue;
		private final String uri;

		DefaultCollection(final XdmValue xdmValue, final String uri) {
			this.xdmValue = xdmValue;
			this.uri = uri;
		}

		@Override
		public String getCollectionURI() {
			return uri;
		}

		@Override
		public Iterator<String> getResourceURIs(final XPathContext context) throws XPathException {
			return xdmValue.stream().filter(XdmItem::isNode).map(i -> ((NodeInfo) i.getUnderlyingValue()).getSystemId()).iterator();
		}

		@Override
		public Iterator<? extends Resource> getResources(final XPathContext context) throws XPathException {
			return xdmValue.stream().map(x -> new Resource() {

				@Override
				public String getResourceURI() {
					return x.isNode() ? ((NodeInfo) x.getUnderlyingValue()).getSystemId() : null;
				}

				@Override
				public Item getItem(final XPathContext context) throws XPathException {
					return x.getUnderlyingValue();
				}

				@Override
				public String getContentType() {
					return null;
				}

			}).iterator();
		}

		@Override
		public boolean isStable(final XPathContext context) {
			return true;
		}

		@Override
		public boolean stripWhitespace(final SpaceStrippingRule rules) {
			return false;
		}

	}

	private final class SaxonSpliterator extends BaseSpliterator<SaxonSpliterator> {

		SaxonSpliterator(final int position, final int countRemaining) {
			this.position = position;
			this.countRemaining = countRemaining;
		}

		@Override
		protected SaxonSpliterator newSpliterator(final int position, final int countRemaining) {
			return new SaxonSpliterator(position, countRemaining);
		}

		@Override
		public boolean tryAdvance(final Consumer<? super NodeSet> action) {
			if (countRemaining > 0) {
				final XdmItem i = ((XdmValue) underlyingValue).itemAt(position++);
				--countRemaining;
				action.accept(new SaxonNodeSet(namespaceContext, queryResultCache, expr, uri, i, defaultCollection));
				return true;
			}
			return false;
		}

	}

	private static final Processor processor = (Processor) SaxonProcessor.processor();
	private static final DocumentBuilder documentBuilder = processor.newDocumentBuilder();

	private final String uri;
	private final DefaultCollection defaultCollection;

	SaxonNodeSet(final NamespaceContext namespaceContext,
			final Map<Query, NodeSet> queryResultCache,
			final String expr,
			final String uri,
			final Object underlyingValue,
			final DefaultCollection defaultCollection) {
		super(namespaceContext, queryResultCache, expr, underlyingValue);
		this.uri = uri;
		this.defaultCollection = defaultCollection;
	}

	SaxonNodeSet(final NamespaceContext namespaceContext, final Node node) {
		this(namespaceContext, new LinkedHashMap<>(), "", node.getOwnerDocument().getDocumentURI(), wrap(node), new DefaultCollection(wrap(node), NodeHelper.ownerDocument(node).getDocumentURI()));
	}

	SaxonNodeSet(final NamespaceContext namespaceContext, final Schema schema) {
		this(namespaceContext, new LinkedHashMap<>(), "", schema.node().getOwnerDocument().getDocumentURI(), wrap(schema.node().getOwnerDocument()), new DefaultCollection(wrap(schema), schema.node().getOwnerDocument().getDocumentURI()));
	}

	private static String getUri(final XdmValue xdmValue, final String fallbackUri) {
		Objects.requireNonNull(fallbackUri);
		if (fallbackUri.isEmpty()) {
			throw new IllegalArgumentException(fallbackUri);
		}
		if (xdmValue instanceof XdmNode) {
			return ((XdmNode) xdmValue).getUnderlyingNode().getSystemId();
		}
		final String uri = xdmValue.stream().filter(XdmItem::isNode).map(x -> ((NodeInfo) x.getUnderlyingValue()).getSystemId()).findFirst().orElse(null);
		return uri == null || uri.isEmpty() ? fallbackUri : uri;
	}

	private static XdmNode wrap(final Node node) {
		final XdmNode xdmNode = documentBuilder.wrap(node);
		xdmNode.getUnderlyingValue().getRoot().setSystemId(NodeHelper.ownerDocument(node).getDocumentURI());
		return xdmNode;
	}

	private static XdmValue wrap(final Schema schema) {
		try {
			return new XdmValue(constituentSchemas(schema).stream().map(s -> wrap(s.node().getOwnerDocument())));
		} catch (final SaxonApiException e) {
			throw new SaxonApiUncheckedException(e);
		}
	}

	private XdmAtomicValue getAtomicValue() {
		if (underlyingValue instanceof XdmAtomicValue) {
			return (XdmAtomicValue) underlyingValue;
		}
		final XdmValue x = (XdmValue) underlyingValue;
		return x.size() == 1 && x.itemAt(0) instanceof XdmAtomicValue ? (XdmAtomicValue) x.itemAt(0) : null;
	}

	private void registerNamespaces(final BiConsumer<String, String> declareNamespace) {
		if (namespaceContext instanceof Iterable) {
			final Iterable<?> namespaces = (Iterable<?>) namespaceContext;
			for (final Object nsPair : namespaces) {
				if (nsPair instanceof Map.Entry) {
					final Map.Entry<?, ?> e = (Map.Entry<?, ?>) nsPair;
					declareNamespace.accept(e.getKey().toString(), e.getValue().toString());
				} else {
					throw new IllegalStateException("Unsupported " + (nsPair != null ? nsPair.getClass() : "null"));
				}
			}
		}
	}

	@Override
	public NodeSet xpath(final String expression) {
		Objects.requireNonNull(expression);
		final Query query = new Query(QueryType.XPATH, expression, underlyingValue);
		return queryResultCache.computeIfAbsent(query, q -> {
			final XdmValue xdmValue = (XdmValue) underlyingValue;
			final Processor xpathProcessor = new Processor(processor.getUnderlyingConfiguration());
			xpathProcessor.getUnderlyingConfiguration().registerCollection(defaultCollection.getCollectionURI(), defaultCollection);
			xpathProcessor.getUnderlyingConfiguration().setDefaultCollection(defaultCollection.getCollectionURI());
			final XPathCompiler xpathCompiler = processor.newXPathCompiler();
			registerNamespaces(xpathCompiler::declareNamespace);
			try {
				final XdmValue result;
				if (xdmValue.size() == 1) {
					result = xpathCompiler.evaluate(expression, xdmValue.itemAt(0));
				} else if (xdmValue instanceof XdmItem) {
					result = xpathCompiler.evaluate(expression, (XdmItem) xdmValue);
				} else {
					final XPathExecutable xpathExpr = xpathCompiler.compile(expression);
					result = new XdmValue(xdmValue.stream()
							.flatMap(x -> {
								final XPathSelector xpathSelector = xpathExpr.load();
								try {
									xpathSelector.setContextItem(x);
									return xpathSelector.evaluate().stream().map(a -> a); // coerce to Stream
								} catch (final SaxonApiException e) {
									throw new SaxonApiUncheckedException(e);
								}
							}));
				}
				final DefaultCollection newDefaultCollection = new DefaultCollection(result, getUri(result, uri));
				return new SaxonNodeSet(namespaceContext, queryResultCache, expression, newDefaultCollection.getCollectionURI(), result, newDefaultCollection);
			} catch (final SaxonApiException e) {
				throw new SaxonApiUncheckedException(e);
			}
		});
	}

	@Override
	public NodeSet xquery(final String expression) {
		Objects.requireNonNull(expression);
		final Query query = new Query(QueryType.XQUERY, expression, underlyingValue);
		return queryResultCache.computeIfAbsent(query, q -> {
			final Processor xqueryProcessor = new Processor(processor.getUnderlyingConfiguration());
			xqueryProcessor.getUnderlyingConfiguration().registerCollection(defaultCollection.getCollectionURI(), defaultCollection);
			xqueryProcessor.getUnderlyingConfiguration().setDefaultCollection(defaultCollection.getCollectionURI());
			final XdmValue xdmValue = (XdmValue) underlyingValue;
			final XQueryCompiler xqueryCompiler = xqueryProcessor.newXQueryCompiler();
			registerNamespaces(xqueryCompiler::declareNamespace);
			try {
				final XQueryExecutable xqueryExec = xqueryCompiler.compile(expression);
				final XdmValue result;
				if (xdmValue instanceof XdmItem) {
					final XQueryEvaluator xqueryEval = xqueryExec.load();
					xqueryEval.setContextItem((XdmItem) xdmValue);
					result = xqueryEval.evaluate();
				} else if (xdmValue.size() == 1) {
					final XQueryEvaluator xqueryEval = xqueryExec.load();
					xqueryEval.setContextItem(xdmValue.itemAt(0));
					result = xqueryEval.evaluate();
				} else {
					result = new XdmValue(xdmValue.stream()
							.flatMap(x -> {
								final XQueryEvaluator xquery = xqueryExec.load();
								try {
									xquery.setContextItem(x);
									return xquery.evaluate().stream().map(a -> a); // coerce to Stream
								} catch (final SaxonApiException e) {
									throw new SaxonApiUncheckedException(e);
								}
							}));
				}
				final DefaultCollection newDefaultCollection = new DefaultCollection(result, getUri(result, uri));
				return new SaxonNodeSet(namespaceContext, queryResultCache, expr, newDefaultCollection.getCollectionURI(), result, newDefaultCollection);
			} catch (final net.sf.saxon.s9api.SaxonApiException e) {
				throw new SaxonApiUncheckedException(e);
			}
		});
	}

	@Override
	public double getDoubleValue() {
		final XdmAtomicValue atomicValue = getAtomicValue();
		if (atomicValue == null) {
			throw IS_NOT_ATOMIC_EXCEPTION.get();
		}
		try {
			return atomicValue.getDoubleValue();
		} catch (final SaxonApiException e) {
			throw new SaxonApiUncheckedException(e);
		}
	}

	@Override
	public long getLongValue() {
		final XdmAtomicValue atomicValue = getAtomicValue();
		if (atomicValue == null) {
			throw IS_NOT_ATOMIC_EXCEPTION.get();
		}
		try {
			return atomicValue.getLongValue();
		} catch (final SaxonApiException e) {
			throw new SaxonApiUncheckedException(e);
		}
	}

	@Override
	public String getStringValue() {
		final XdmAtomicValue atomicValue = getAtomicValue();
		if (atomicValue == null) {
			throw IS_NOT_ATOMIC_EXCEPTION.get();
		}
		return atomicValue.getStringValue();
	}

	@Override
	public boolean getBooleanValue() {
		final XdmAtomicValue atomicValue = getAtomicValue();
		if (atomicValue == null) {
			throw IS_NOT_ATOMIC_EXCEPTION.get();
		}
		try {
			return atomicValue.getBooleanValue();
		} catch (final SaxonApiException e) {
			throw new SaxonApiUncheckedException(e);
		}
	}

	@Override
	public int size() {
		return ((XdmValue) underlyingValue).size();
	}

	@Override
	public boolean isAtomic() {
		return getAtomicValue() != null;
	}

	@Override
	public Stream<NodeSet> split() {
		return StreamSupport.stream(new SaxonSpliterator(0, size()), false);
	}

	@Override
	public Stream<Node> stream() {
		if (isAtomic()) {
			throw IS_ATOMIC_EXCEPTION.get();
		}
		return ((XdmValue) underlyingValue).stream()
				.filter(XdmItem::isNode)
				.map(x -> NodeOverNodeInfo.wrap((NodeInfo) x.getUnderlyingValue()));
	}

}