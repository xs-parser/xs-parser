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

	public class Result {

		private final Schema schema;
		private final Node node;
		private final Result parent;
		private final Map<AttributeValue<?>, Object> attributes = new LinkedHashMap<>();
		private final Map<QName, String> nonSchemaAttributes;
		private final Map<ElementValue<?>, Deque<Result>> parserResults = new LinkedHashMap<>();
		private final Deque<Node> anyContent;

		Result(final Schema schema, final Node node, final Result parent, final boolean allowsNonSchemaAttributes, final boolean allowsAnyContent) {
			this.schema = schema;
			this.node = node;
			this.parent = parent;
			nonSchemaAttributes = allowsNonSchemaAttributes ? new HashMap<>() : Collections.emptyMap();
			anyContent = allowsAnyContent ? new ArrayDeque<>() : Deques.emptyDeque();
		}

		@SuppressWarnings("unchecked")
		public <T> T value(final AttributeValue<T> a) {
			if (!attributes.containsKey(a)) {
				throw new IllegalArgumentException("Unregisted attribute " + a);
			}
			return (T) attributes.get(a);
		}

		public Map<QName, String> nonSchemaAttributes() {
			return nonSchemaAttributes;
		}

		public Deque<Annotation> annotations() {
			return parseAll(ElementValue.ANNOTATION);
		}

		@SafeVarargs public final <T> T parse(final ElementValue<? extends T> e, final ElementValue<? extends T>... more) {
			T t = parse(e);
			if (t != null) {
				return t;
			}
			for (final ElementValue<? extends T> v : more) {
				t = parse(v);
				if (t != null) {
					return t;
				}
			}
			return null;
		}

		public <T> T parse(final ElementValue<T> e) {
			if (!parserResults.containsKey(e)) {
				throw new IllegalArgumentException(e.toString());
			}
			final Deque<Result> results = parserResults.get(e);
			if (results == null) {
				return null;
			}
			final Result r = results.getFirst();
			checkIfCanParse(r, e);
			return e.getParser().parse(r);
		}

		@SuppressWarnings("unchecked")
		@SafeVarargs public final <T> Deque<T> parseAll(final ElementValue<? extends T> e, final ElementValue<? extends T>... more) {
			final Deque<? extends T> ls = parseAll(e);
			final Deque<Deque<? extends T>> moreDeques = new ArrayDeque<>();
			for (final ElementValue<? extends T> v : more) {
				moreDeques.add(parseAll(v));
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

		public <T> Deque<T> parseAll(final ElementValue<T> e) {
			if (!parserResults.containsKey(e)) {
				throw new IllegalArgumentException("Unregisted element " + e.getName());
			}
			final Deque<Result> results = parserResults.get(e);
			if (results == null) {
				return Deques.emptyDeque();
			}
			for (final Result r : results) {
				checkIfCanParse(r, e);
			}
			final DeferredArrayDeque<T> values = new DeferredArrayDeque<>(results.size());
			for (final Result r : results) {
				values.add(Deferred.of(() -> e.getParser().parse(r)));
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

	}

	private class Occur {

		private final int minInclusive;
		private final int maxInclusive;
		private final ElementValue<?>[] values;

		Occur(final int minInclusive, final int maxInclusive, final ElementValue<?>[] values) {
			if (minInclusive < 0 || maxInclusive < 0) {
				throw new IllegalArgumentException("minInclusive and maxInclusive must be greater than or equal to 0");
			}
			this.minInclusive = minInclusive;
			this.maxInclusive = maxInclusive;
			this.values = values;
		}

		ElementValue<?> elementFor(final Node node) {
			final String namespaceUri = NodeHelper.namespaceUri(node);
			final String localName = node.getLocalName();
			for (final ElementValue<?> v : values) {
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

	private final Map<QName, Map.Entry<AttributeValue<?>, Boolean>> attributeValues = new LinkedHashMap<>();
	private final List<Occur> subParsers = new ArrayList<>();
	private boolean allowsNonSchemaAttributes = true;
	private boolean allowsAnyContent = false;

	private static void checkIfCanParse(final Result r, final ElementValue<?> e) {
		if (!e.equalsName(r.node)) {
			throw new IllegalArgumentException(r.node.toString() + " does not match expected element name " + e.getName());
		}
	}

	public SequenceParser requiredAttributes(final AttributeValue<?>... attributeValues) {
		for (final AttributeValue<?> a : attributeValues) {
			this.attributeValues.put(a.getName(), new SimpleImmutableEntry<>(a, true));
		}
		return this;
	}

	public SequenceParser optionalAttributes(final AttributeValue<?>... attributeValues) {
		for (final AttributeValue<?> a : attributeValues) {
			this.attributeValues.put(a.getName(), new SimpleImmutableEntry<>(a, false));
		}
		return this;
	}

	@SafeVarargs public final SequenceParser elements(final int minInclusive, final int maxInclusive, final ElementValue<?>... e) {
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
			final Node n = attributes.item(i);
			if (n.getNodeName().startsWith("xmlns")) { // Ignore xmlns= and xmlns:*= attributes
				continue;
			}
			final QName attrName = n.getNamespaceURI() != null
					? new QName(n.getNamespaceURI(), n.getLocalName())
					: new QName(n.getLocalName());
			final Map.Entry<AttributeValue<?>, Boolean> a = attributeValues.get(attrName);
			if (Objects.equals(n.getNamespaceURI(), XMLConstants.W3C_XML_SCHEMA_NS_URI) || Objects.equals(n.getNamespaceURI(), XMLConstants.NULL_NS_URI)) {
				throw new SchemaParseException(node, "Found disallowed attribute present on element " + node.getLocalName() + ": " + n.getLocalName());
			}
			if (a == null) {
				if (allowsNonSchemaAttributes && n.getPrefix() != null && !XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(n.getPrefix())) {
					final QName name = new QName(n.getNamespaceURI(), n.getLocalName(), n.getPrefix());
					result.nonSchemaAttributes.put(name, n.getNodeValue());
				} else {
					throw new SchemaParseException(node, "Found disallowed non-schema attribute: " + n);
				}
			} else {
				final AttributeValue<?> v = a.getKey();
				result.attributes.put(v, v.parse(n));
			}
		}
		for (final Map.Entry<QName, Map.Entry<AttributeValue<?>, Boolean>> a : attributeValues.entrySet()) {
			final boolean isRequired = a.getValue().getValue();
			final AttributeValue<?> v = a.getValue().getKey();
			if (isRequired && !result.attributes.containsKey(v)) {
				throw new SchemaParseException(node, "Missing required attribute " + v.getName());
			}
			result.attributes.putIfAbsent(v, v.defaultValue());
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
					ElementValue<?> elementValue;
					while ((elementValue = iter.elementFor(n)) == null) {
						if (iter.minInclusive > amount || iter.maxInclusive < amount) {
							throw new SchemaParseException(node, "Invalid occurences for element " + node + ", " + iter.minInclusive + ", " + iter.maxInclusive + " " + amount);
						}
						if (parserIndex >= subParsers.size()) {
							throw new SchemaParseException(node, "Disallowed element: " + n + ", " + n.getClass() + ", expecting one of " + subParsers);
						}
						iter = subParsers.get(parserIndex++);
						result.parserResults.put(elementValue, null);
						amount = 0;
					}
					final Result r = elementValue.getParser().get().parse(schema, n, result);
					result.parserResults.computeIfAbsent(elementValue, e -> new ArrayDeque<>()).add(r);
					++amount;
				}
			}
			if (iter.minInclusive > amount || iter.maxInclusive < amount) {
				throw new SchemaParseException(node, "Invalid occurences for element " + node + ", " + iter.minInclusive + ", " + iter.maxInclusive + " " + amount);
			}
			// TODO: probably inefficient
			for (final Occur o : subParsers) {
				for (final ElementValue<?> e : o.values) {
					result.parserResults.putIfAbsent(e, null);
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
