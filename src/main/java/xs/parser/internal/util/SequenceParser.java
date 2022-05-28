package xs.parser.internal.util;

import java.util.*;
import java.util.AbstractMap.*;
import java.util.concurrent.atomic.*;
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
		private final Node node;
		private final Result parent;
		private final Map<AttrParser<?>, AttrValue<?>> attributes = new LinkedHashMap<>();
		private final Map<QName, String> nonSchemaAttributes;
		private final Map<TagParser<?>, Deque<Result>> parserResults = new LinkedHashMap<>();
		private final Deque<Node> anyContent;
		private final AtomicReference<Object> value = new AtomicReference<>();

		Result(final Schema schema, final Node node, final Result parent, final boolean allowsNonSchemaAttributes, final boolean allowsAnyContent) {
			this.schema = schema;
			this.node = node;
			this.parent = parent;
			nonSchemaAttributes = allowsNonSchemaAttributes ? new HashMap<>() : Collections.emptyMap();
			anyContent = allowsAnyContent ? new ArrayDeque<>() : Deques.emptyDeque();
		}

		public Attr attr(final AttrParser<?> a) {
			if (!attributes.containsKey(a)) {
				throw new IllegalArgumentException("Unregisted attribute " + a);
			}
			return attributes.get(a).attr;
		}

		@SuppressWarnings("unchecked")
		public <T> T value(final AttrParser<T> a) {
			if (!attributes.containsKey(a)) {
				throw new IllegalArgumentException("Unregisted attribute " + a);
			}
			return (T) attributes.get(a).value;
		}

		public Map<QName, String> nonSchemaAttributes() {
			return nonSchemaAttributes;
		}

		public Deque<Annotation> annotations() {
			return parseAll(TagParser.ANNOTATION);
		}

		@SafeVarargs public final <T> T parse(final TagParser<? extends T> first, final TagParser<? extends T>... more) {
			T t = parse(first);
			if (t != null) {
				return t;
			}
			for (final TagParser<? extends T> m : more) {
				t = parse(m);
				if (t != null) {
					return t;
				}
			}
			return null;
		}

		public <T> T parse(final TagParser<T> t) {
			if (!parserResults.containsKey(t)) {
				throw new IllegalArgumentException(t.toString());
			}
			final Deque<Result> results = parserResults.get(t);
			if (results == null) {
				return null;
			}
			final Result r = results.getFirst();
			checkIfCanParse(r, t);
			final T v = t.parse(r);
			r.setValue(v);
			return v;
		}

		@SuppressWarnings("unchecked")
		@SafeVarargs public final <T> Deque<T> parseAll(final TagParser<? extends T> first, final TagParser<? extends T>... more) {
			final Deque<? extends T> ls = parseAll(first);
			final Deque<Deque<? extends T>> moreDeques = new ArrayDeque<>();
			for (final TagParser<? extends T> t : more) {
				moreDeques.add(parseAll(t));
			}
			if (moreDeques.isEmpty()) {
				return (Deque<T>) ls;
			}
			int size = ls.size();
			for (final Deque<? extends T> x : moreDeques) {
				size += x.size();
			}
			final DeferredArrayDeque<T> d = new DeferredArrayDeque<>(size);
			d.addAll(ls);
			for (final Deque<? extends T> x : moreDeques) {
				d.addAll(x);
			}
			return d;
		}

		public <T> Deque<T> parseAll(final TagParser<T> t) {
			if (!parserResults.containsKey(t)) {
				throw new IllegalArgumentException("Unregisted element " + t.getName());
			}
			final Deque<Result> results = parserResults.get(t);
			if (results == null) {
				return Deques.emptyDeque();
			}
			for (final Result r : results) {
				checkIfCanParse(r, t);
			}
			final DeferredArrayDeque<T> values = new DeferredArrayDeque<>(results.size());
			for (final Result r : results) {
				values.add(Deferred.of(() -> {
					final T v = t.parse(r);
					r.setValue(v);
					return v;
				}));
			}
			return values;
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

		public Result parent() {
			return parent;
		}

		public Deferred<Object> defer() {
			return value::get;
		}

		public void setValue(final Object value) {
			this.value.set(value);
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
			for (final TagParser<?> v : values) {
				if (v.getName().getNamespaceURI().equals(namespaceUri) && v.getName().getLocalPart().equals(localName)) {
					return v;
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
	private boolean allowsNonSchemaAttributes = true;
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

	public SequenceParser allowsNonSchemaAttributes(final boolean value) {
		this.allowsNonSchemaAttributes = value;
		return this;
	}

	public SequenceParser allowsAnyContent(final boolean value) {
		this.allowsAnyContent = value;
		return this;
	}

	public Result parse(final Schema schema, final Node node) {
		return parse(schema, node, null);
	}

	private Result parse(final Schema schema, final Node node, final Result parent) {
		final Result result = new Result(schema, node, parent, allowsNonSchemaAttributes, allowsAnyContent);
		final NamedNodeMap attributes = node.getAttributes();
		for (int i = 0; i < attributes.getLength(); ++i) {
			final Attr a = (Attr) attributes.item(i);
			if (a.getNodeName().startsWith("xmlns")) { // Ignore xmlns= and xmlns:*= attributes
				continue;
			}
			final QName attrName = a.getNamespaceURI() != null
					? new QName(a.getNamespaceURI(), a.getLocalName())
					: new QName(a.getLocalName());
			final Map.Entry<AttrParser<?>, Boolean> e = attrParsers.get(attrName);
			if (Objects.equals(a.getNamespaceURI(), XMLConstants.W3C_XML_SCHEMA_NS_URI) || Objects.equals(a.getNamespaceURI(), XMLConstants.NULL_NS_URI)) {
				throw NodeHelper.newParseException(node, "Found disallowed attribute present on element " + node.getLocalName() + ": " + a.getLocalName());
			}
			if (e == null) {
				if (allowsNonSchemaAttributes && a.getPrefix() != null && !XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(a.getPrefix())) {
					final QName name = new QName(a.getNamespaceURI(), a.getLocalName(), a.getPrefix());
					result.nonSchemaAttributes.put(name, a.getValue());
				} else {
					throw NodeHelper.newParseException(node, "Found disallowed non-schema attribute: " + a);
				}
			} else {
				final AttrParser<?> v = e.getKey();
				result.attributes.put(v, new AttrValue<>(a, v.parse(a)));
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
						}
						if (parserIndex >= subParsers.size()) {
							throw NodeHelper.newParseException(node, "Disallowed element: " + n + ", " + n.getClass() + ", expecting one of " + subParsers);
						}
						iter = subParsers.get(parserIndex++);
						result.parserResults.put(tagParser, null);
						amount = 0;
					}
					final Result r = tagParser.getSequenceParser().parse(schema, n, result);
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
