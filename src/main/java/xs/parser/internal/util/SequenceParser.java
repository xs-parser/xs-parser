package xs.parser.internal.util;

import java.util.*;
import java.util.AbstractMap.*;
import java.util.stream.*;
import javax.xml.*;
import javax.xml.namespace.*;
import org.w3c.dom.*;
import xs.parser.*;
import xs.parser.internal.*;

public final class SequenceParser {

	public static class AttrValue<T> {

		private final Attr attr;
		private final T value;

		public AttrValue(final Attr attr, final T value) {
			this.attr = attr;
			this.value = value;
		}

		public T getValue() {
			return value;
		}

	}

	public class Result {

		private final Schema schema;
		private final Deferred<Result> context;
		private final Node node;
		private final Map<AttrParser<?>, AttrValue<?>> attributes = new LinkedHashMap<>();
		private final Map<TagParser<?>, Deque<Result>> parserResults = new LinkedHashMap<>();
		private final Deque<Node> anyContent;
		private final DeferredValue<Object> value = new DeferredValue<>();

		Result(final Schema schema, final Result parent, final Node node, final boolean allowsAnyContent) {
			this.schema = schema;
			this.context = Deferred.of(() -> {
				for (Result ctx = parent; ctx != null; ctx = ctx.context.get()) {
					if (ctx.value.get() instanceof SchemaComponent) {
						return ctx;
					}
				}
				return null;
			});
			this.node = node;
			anyContent = allowsAnyContent ? new ArrayDeque<>() : Deques.emptyDeque();
		}

		public Attr attr(final AttrParser<?> a) {
			final AttrValue<?> val = attributes.get(a);
			if (val == null) {
				throw new IllegalArgumentException("Unregistered attribute \"" + a + '"');
			}
			return val.attr;
		}

		@SuppressWarnings("unchecked")
		public <T> T value(final AttrParser<T> a) {
			final AttrValue<?> val = attributes.get(a);
			if (val == null) {
				throw new IllegalArgumentException("Unregistered attribute \"" + a + '"');
			}
			return (T) val.value;
		}

		@SuppressWarnings("unchecked")
		@SafeVarargs public final <T> Deferred<T> parse(final TagParser<? extends T> first, final TagParser<? extends T>... more) {
			Deferred<T> t = (Deferred<T>) parse(first);
			if (t != null) {
				return t;
			}
			for (final TagParser<? extends T> m : more) {
				t = (Deferred<T>) parse(m);
				if (t != null) {
					return t;
				}
			}
			return null;
		}

		public <T> Deferred<T> parse(final TagParser<T> t) {
			if (!parserResults.containsKey(t)) {
				throw new IllegalArgumentException(t.toString());
			}
			final Deque<Result> results = parserResults.get(t);
			if (results == null) {
				return null;
			}
			final Result r = results.getFirst();
			checkIfCanParse(r, t);
			return Deferred.of(() -> {
				final T v = Objects.requireNonNull(t.parse(r));
				r.value.set(v);
				return v;
			});
		}

		@SafeVarargs public final <T> Deque<T> parseAll(final TagParser<? extends T> first, final TagParser<? extends T>... more) {
			final Deque<T> x = new DeferredArrayDeque<>(parseAll(first));
			for (final TagParser<? extends T> t : more) {
				x.addAll(parseAll(t));
			}
			return x;
		}

		public <T> Deque<T> parseAll(final TagParser<T> t) {
			if (!parserResults.containsKey(t)) {
				throw new IllegalArgumentException("Unregistered element \"" + t.getName() + '"');
			}
			final Deque<Result> results = parserResults.get(t);
			if (results == null) {
				return Deques.emptyDeque();
			}
			for (final Result r : results) {
				checkIfCanParse(r, t);
			}
			return new DeferredArrayDeque<>(() -> {
				final ArrayDeque<T> x = new ArrayDeque<>();
				for (final Result r : results) {
					final T v = t.parse(r);
					r.value.set(v);
					x.addLast(v);
				}
				return x;
			});
		}

		public Deque<Node> anyContent() {
			return anyContent;
		}

		public Schema schema() {
			return schema;
		}

		public Node node() {
			return node;
		}

		@SuppressWarnings("unchecked")
		public <T extends SchemaComponent> Deferred<T> context() {
			return context.map(p -> (T) (p != null ? p.value.get() : schema));
		}

	}

	private class Occur {

		private final int minInclusive;
		private final int maxInclusive;
		private final TagParser<?>[] values;

		Occur(final int minInclusive, final int maxInclusive, final TagParser<?>[] values) {
			if (minInclusive < 0 || maxInclusive < 0) {
				throw new IllegalArgumentException("minInclusive and maxInclusive must be greater than or equal to 0");
			}
			this.minInclusive = minInclusive;
			this.maxInclusive = maxInclusive;
			this.values = values;
		}

		private TagParser<?> elementFor(final Node node) {
			final String namespaceUri = NodeHelper.namespaceUri(node);
			final String localName = node.getLocalName();
			for (final TagParser<?> t : values) {
				if (t.getName().getNamespaceURI().equals(namespaceUri) && t.getName().getLocalPart().equals(localName)) {
					return t;
				}
			}
			return null;
		}

		@Override
		public String toString() {
			return Arrays.stream(values).collect(Collectors.toCollection(ArrayDeque::new)).toString();
		}

	}

	private final Map<QName, Map.Entry<AttrParser<?>, Boolean>> attrParsers = new LinkedHashMap<>();
	private final List<Occur> subParsers = new ArrayList<>();
	private boolean allowsAnyContent = false;

	private static void checkIfCanParse(final Result r, final TagParser<?> t) {
		if (!t.equalsName(r.node)) {
			throw new IllegalArgumentException(r.node.toString() + " does not match expected element name " + t.getName());
		}
	}

	public SequenceParser requiredAttributes(final AttrParser<?>... attrParsers) {
		for (final AttrParser<?> a : attrParsers) {
			this.attrParsers.put(a.getName(), new SimpleImmutableEntry<>(a, true));
		}
		return this;
	}

	public SequenceParser optionalAttributes(final AttrParser<?>... attrParsers) {
		for (final AttrParser<?> a : attrParsers) {
			this.attrParsers.put(a.getName(), new SimpleImmutableEntry<>(a, false));
		}
		return this;
	}

	@SafeVarargs
	public final SequenceParser elements(final int minInclusive, final int maxInclusive, final TagParser<?>... e) {
		this.subParsers.add(new Occur(minInclusive, maxInclusive, e));
		return this;
	}

	public SequenceParser allowsAnyContent(final boolean value) {
		this.allowsAnyContent = value;
		return this;
	}

	public Result parse(final Schema schema, final Node node) {
		return parse(schema, null, node);
	}

	private Result parse(final Schema schema, final Result parent, final Node node) {
		final Result result = new Result(schema, parent, node, allowsAnyContent);
		final NamedNodeMap attributes = node.getAttributes();
		final int len = attributes.getLength();
		for (int i = 0; i < len; ++i) {
			final Attr a = (Attr) attributes.item(i);
			if (a.getNodeName().startsWith("xmlns")) {
				// Ignore 'xmlns' and 'xmlns:*' attributes
				continue;
			}
			final QName attrName = a.getNamespaceURI() != null
					? new QName(a.getNamespaceURI(), a.getLocalName())
					: new QName(a.getLocalName());
			final Map.Entry<AttrParser<?>, Boolean> e = attrParsers.get(attrName);
			if (e != null) {
				final AttrParser<?> v = e.getKey();
				result.attributes.put(v, new AttrValue<>(a, v.parse(a)));
			} else if (a.getNamespaceURI() == null || XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(a.getNamespaceURI())) {
				throw NodeHelper.newParseException(node, "Found disallowed attribute + '" + a.getName() + "'");
			}
		}
		for (final Map.Entry<QName, Map.Entry<AttrParser<?>, Boolean>> a : attrParsers.entrySet()) {
			final boolean isRequired = a.getValue().getValue();
			final AttrParser<?> v = a.getValue().getKey();
			if (isRequired && !result.attributes.containsKey(v)) {
				throw NodeHelper.newParseException(node, "Missing required attribute " + v.getName());
			}
			result.attributes.putIfAbsent(v, v.getDefault());
		}
		if (allowsAnyContent) {
			for (Node n = node.getFirstChild(); n != null; n = n.getNextSibling()) {
				result.anyContent.add(n);
			}
		} else if (!subParsers.isEmpty()) {
			int parserIndex = 0;
			int amount = 0;
			Occur iter = subParsers.get(parserIndex++);
			for (Node n = node.getFirstChild(); n != null; n = n.getNextSibling()) {
				if (n instanceof org.w3c.dom.Element) {
					TagParser<?> tagParser;
					while ((tagParser = iter.elementFor(n)) == null) {
						if (iter.minInclusive > amount || iter.maxInclusive < amount) {
							throw NodeHelper.newParseException(node, "Invalid occurences for element " + node + ", " + iter.minInclusive + ", " + iter.maxInclusive + " " + amount);
						} else if (parserIndex >= subParsers.size()) {
							throw NodeHelper.newParseException(node, "Disallowed element: " + n + ", " + n.getClass() + ", expecting one of " + subParsers);
						}
						iter = subParsers.get(parserIndex++);
						result.parserResults.put(tagParser, null);
						amount = 0;
					}
					final Result r = tagParser.getSequenceParser().parse(schema, result, n);
					result.parserResults.computeIfAbsent(tagParser, e -> new ArrayDeque<>()).add(r);
					++amount;
				}
			}
			if (iter.minInclusive > amount || iter.maxInclusive < amount) {
				throw NodeHelper.newParseException(node, "Invalid occurences for element " + node + ", " + iter.minInclusive + ", " + iter.maxInclusive + " " + amount);
			}
			// TODO: probably inefficient
			for (final Occur o : subParsers) {
				for (final TagParser<?> t : o.values) {
					result.parserResults.putIfAbsent(t, null);
				}
			}
		}
		return result;
	}

	@Override
	public String toString() {
		return subParsers.stream().map(o -> Arrays.stream(o.values).map(e -> e.getName().toString()).collect(Collectors.joining(", "))).collect(Collectors.joining(", "));
	}

}
