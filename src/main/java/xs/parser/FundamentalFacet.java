package xs.parser;

import java.util.*;
import org.w3c.dom.*;
import xs.parser.internal.util.*;

public abstract class FundamentalFacet implements SchemaComponent {

	private static final Document fundamentalFacetsDocument;
	/** Derived from the table https://www.w3.org/TR/2012/REC-xmlschema11-2-20120405/datatypes.html#app-fundamental-facets */
	private static final Map<SimpleType, Deque<FundamentalFacet>> fundamentalFacets;

	private final Node node;

	static {
		fundamentalFacetsDocument = NodeHelper.newDocument();
		final Map<SimpleType, Deque<FundamentalFacet>> f = new HashMap<>();
		// Primitive
		f.put(SimpleType.xsString(), fundamentalFacets(Ordered.Value.FALSE, false, Cardinality.Value.COUNTABLY_INFINITE, false));
		f.put(SimpleType.xsBoolean(), fundamentalFacets(Ordered.Value.FALSE, false, Cardinality.Value.FINITE, false));
		f.put(SimpleType.xsFloat(), fundamentalFacets(Ordered.Value.PARTIAL, true, Cardinality.Value.FINITE, true));
		f.put(SimpleType.xsDouble(), fundamentalFacets(Ordered.Value.PARTIAL, true, Cardinality.Value.FINITE, true));
		f.put(SimpleType.xsDecimal(), fundamentalFacets(Ordered.Value.TOTAL, false, Cardinality.Value.COUNTABLY_INFINITE, true));
		f.put(SimpleType.xsDuration(), fundamentalFacets(Ordered.Value.PARTIAL, false, Cardinality.Value.COUNTABLY_INFINITE, false));
		f.put(SimpleType.xsDateTime(), fundamentalFacets(Ordered.Value.PARTIAL, false, Cardinality.Value.COUNTABLY_INFINITE, false));
		f.put(SimpleType.xsTime(), fundamentalFacets(Ordered.Value.PARTIAL, false, Cardinality.Value.COUNTABLY_INFINITE, false));
		f.put(SimpleType.xsDate(), fundamentalFacets(Ordered.Value.PARTIAL, false, Cardinality.Value.COUNTABLY_INFINITE, false));
		f.put(SimpleType.xsGYearMonth(), fundamentalFacets(Ordered.Value.PARTIAL, false, Cardinality.Value.COUNTABLY_INFINITE, false));
		f.put(SimpleType.xsGYear(), fundamentalFacets(Ordered.Value.PARTIAL, false, Cardinality.Value.COUNTABLY_INFINITE, false));
		f.put(SimpleType.xsGMonthDay(), fundamentalFacets(Ordered.Value.PARTIAL, false, Cardinality.Value.COUNTABLY_INFINITE, false));
		f.put(SimpleType.xsGDay(), fundamentalFacets(Ordered.Value.PARTIAL, false, Cardinality.Value.COUNTABLY_INFINITE, false));
		f.put(SimpleType.xsGMonth(), fundamentalFacets(Ordered.Value.PARTIAL, false, Cardinality.Value.COUNTABLY_INFINITE, false));
		f.put(SimpleType.xsHexBinary(), fundamentalFacets(Ordered.Value.FALSE, false, Cardinality.Value.COUNTABLY_INFINITE, false));
		f.put(SimpleType.xsBase64Binary(), fundamentalFacets(Ordered.Value.FALSE, false, Cardinality.Value.COUNTABLY_INFINITE, false));
		f.put(SimpleType.xsAnyURI(), fundamentalFacets(Ordered.Value.FALSE, false, Cardinality.Value.COUNTABLY_INFINITE, false));
		f.put(SimpleType.xsQName(), fundamentalFacets(Ordered.Value.FALSE, false, Cardinality.Value.COUNTABLY_INFINITE, false));
		f.put(SimpleType.xsNOTATION(), fundamentalFacets(Ordered.Value.FALSE, false, Cardinality.Value.COUNTABLY_INFINITE, false));
		// Built-in
		f.put(SimpleType.xsNormalizedString(), fundamentalFacets(Ordered.Value.FALSE, false, Cardinality.Value.COUNTABLY_INFINITE, false));
		f.put(SimpleType.xsToken(), fundamentalFacets(Ordered.Value.FALSE, false, Cardinality.Value.COUNTABLY_INFINITE, false));
		f.put(SimpleType.xsLanguage(), fundamentalFacets(Ordered.Value.FALSE, false, Cardinality.Value.COUNTABLY_INFINITE, false));
		f.put(SimpleType.xsIDREFS(), fundamentalFacets(Ordered.Value.FALSE, false, Cardinality.Value.COUNTABLY_INFINITE, false));
		f.put(SimpleType.xsENTITIES(), fundamentalFacets(Ordered.Value.FALSE, false, Cardinality.Value.COUNTABLY_INFINITE, false));
		f.put(SimpleType.xsNMTOKEN(), fundamentalFacets(Ordered.Value.FALSE, false, Cardinality.Value.COUNTABLY_INFINITE, false));
		f.put(SimpleType.xsNMTOKENS(), fundamentalFacets(Ordered.Value.FALSE, false, Cardinality.Value.COUNTABLY_INFINITE, false));
		f.put(SimpleType.xsName(), fundamentalFacets(Ordered.Value.FALSE, false, Cardinality.Value.COUNTABLY_INFINITE, false));
		f.put(SimpleType.xsNCName(), fundamentalFacets(Ordered.Value.FALSE, false, Cardinality.Value.COUNTABLY_INFINITE, false));
		f.put(SimpleType.xsID(), fundamentalFacets(Ordered.Value.FALSE, false, Cardinality.Value.COUNTABLY_INFINITE, false));
		f.put(SimpleType.xsIDREF(), fundamentalFacets(Ordered.Value.FALSE, false, Cardinality.Value.COUNTABLY_INFINITE, false));
		f.put(SimpleType.xsENTITY(), fundamentalFacets(Ordered.Value.FALSE, false, Cardinality.Value.COUNTABLY_INFINITE, false));
		f.put(SimpleType.xsInteger(), fundamentalFacets(Ordered.Value.TOTAL, false, Cardinality.Value.COUNTABLY_INFINITE, true));
		f.put(SimpleType.xsNonPositiveInteger(), fundamentalFacets(Ordered.Value.TOTAL, false, Cardinality.Value.COUNTABLY_INFINITE, true));
		f.put(SimpleType.xsNegativeInteger(), fundamentalFacets(Ordered.Value.TOTAL, false, Cardinality.Value.COUNTABLY_INFINITE, true));
		f.put(SimpleType.xsLong(), fundamentalFacets(Ordered.Value.TOTAL, true, Cardinality.Value.FINITE, true));
		f.put(SimpleType.xsInt(), fundamentalFacets(Ordered.Value.TOTAL, true, Cardinality.Value.FINITE, true));
		f.put(SimpleType.xsShort(), fundamentalFacets(Ordered.Value.TOTAL, true, Cardinality.Value.FINITE, true));
		f.put(SimpleType.xsByte(), fundamentalFacets(Ordered.Value.TOTAL, true, Cardinality.Value.FINITE, true));
		f.put(SimpleType.xsNonNegativeInteger(), fundamentalFacets(Ordered.Value.TOTAL, false, Cardinality.Value.COUNTABLY_INFINITE, true));
		f.put(SimpleType.xsUnsignedLong(), fundamentalFacets(Ordered.Value.TOTAL, true, Cardinality.Value.FINITE, true));
		f.put(SimpleType.xsUnsignedInt(), fundamentalFacets(Ordered.Value.TOTAL, true, Cardinality.Value.FINITE, true));
		f.put(SimpleType.xsUnsignedShort(), fundamentalFacets(Ordered.Value.TOTAL, true, Cardinality.Value.FINITE, true));
		f.put(SimpleType.xsUnsignedByte(), fundamentalFacets(Ordered.Value.TOTAL, true, Cardinality.Value.FINITE, true));
		f.put(SimpleType.xsPositiveInteger(), fundamentalFacets(Ordered.Value.TOTAL, false, Cardinality.Value.COUNTABLY_INFINITE, true));
		f.put(SimpleType.xsYearMonthDuration(), fundamentalFacets(Ordered.Value.PARTIAL, false, Cardinality.Value.COUNTABLY_INFINITE, false));
		f.put(SimpleType.xsDayTimeDuration(), fundamentalFacets(Ordered.Value.PARTIAL, false, Cardinality.Value.COUNTABLY_INFINITE, false));
		f.put(SimpleType.xsDateTimeStamp(), fundamentalFacets(Ordered.Value.PARTIAL, false, Cardinality.Value.COUNTABLY_INFINITE, false));
		fundamentalFacets = Collections.unmodifiableMap(f);
	}

	private FundamentalFacet(final Node node) {
		this.node = Objects.requireNonNull(node);
	}

	/**
	 * <table>
	 *   <caption style="font-size: large; text-align: left">Schema Component: ordered, a kind of Fundamental Facet</caption>
	 *   <thead>
	 *     <tr>
	 *       <th style="text-align: left">Method</th>
	 *       <th style="text-align: left">Property</th>
	 *       <th style="text-align: left">Representation</th>
	 *     </tr>
	 *   </thead>
	 *   <tbody>
	 *     <tr>
	 *       <td>{@link Ordered#value()}</td>
	 *       <td>{value}</td>
	 *       <td>One of {false, partial, total}. Required.</td>
	 *     </tr>
	 *   </tbody>
	 * </table>
	 */
	public static class Ordered extends FundamentalFacet {

		public enum Value {

			FALSE("false"),
			PARTIAL("partial"),
			TOTAL("total");

			private final String name;

			Value(final String name) {
				this.name = name;
			}

			public static Value getByName(final String name) {
				for (final Value e : values()) {
					if (e.getName().equals(name)) {
						return e;
					}
				}
				throw new IllegalArgumentException(name);
			}

			public String getName() {
				return name;
			}

			@Override
			public String toString() {
				return getName();
			}

		}

		private static final String NAME = "ordered";

		private final Value value;

		private Ordered(final Node node, final Value value) {
			super(node);
			this.value = Objects.requireNonNull(value);
		}

		@Override
		public Value value() {
			return value;
		}

	}

	/**
	 * <table>
	 *   <caption style="font-size: large; text-align: left">Schema Component: bounded, a kind of Fundamental Facet</caption>
	 *   <thead>
	 *     <tr>
	 *       <th style="text-align: left">Method</th>
	 *       <th style="text-align: left">Property</th>
	 *       <th style="text-align: left">Representation</th>
	 *     </tr>
	 *   </thead>
	 *   <tbody>
	 *     <tr>
	 *       <td>{@link Bounded#value()}</td>
	 *       <td>{value}</td>
	 *       <td>An xs:boolean value. Required.</td>
	 *     </tr>
	 *   </tbody>
	 * </table>
	 */
	public static class Bounded extends FundamentalFacet {

		private static final String NAME = "bounded";

		private final boolean value;

		private Bounded(final Node node, final boolean value) {
			super(node);
			this.value = value;
		}

		@Override
		public Boolean value() {
			return value;
		}

	}

	/**
	 * <table>
	 *   <caption style="font-size: large; text-align: left">Schema Component: cardinality, a kind of Fundamental Facet</caption>
	 *   <thead>
	 *     <tr>
	 *       <th style="text-align: left">Method</th>
	 *       <th style="text-align: left">Property</th>
	 *       <th style="text-align: left">Representation</th>
	 *     </tr>
	 *   </thead>
	 *   <tbody>
	 *     <tr>
	 *       <td>{@link Cardinality#value()}</td>
	 *       <td>{value}</td>
	 *       <td>One of {finite, countably infinite}. Required.</td>
	 *     </tr>
	 *   </tbody>
	 * </table>
	 */
	public static class Cardinality extends FundamentalFacet {

		public enum Value {

			FINITE("finite"),
			COUNTABLY_INFINITE("countably infinite");

			private final String name;

			Value(final String name) {
				this.name = name;
			}

			public static Value getByName(final String name) {
				for (final Value e : values()) {
					if (e.getName().equals(name)) {
						return e;
					}
				}
				throw new IllegalArgumentException(name);
			}

			public String getName() {
				return name;
			}

			@Override
			public String toString() {
				return getName();
			}

		}

		private static final String NAME = "cardinality";

		private final Value value;

		private Cardinality(final Node node, final Value value) {
			super(node);
			this.value = Objects.requireNonNull(value);
		}

		@Override
		public Value value() {
			return value;
		}

	}

	/**
	 * <table>
	 *   <caption style="font-size: large; text-align: left">Schema Component: numeric, a kind of Fundamental Facet</caption>
	 *   <thead>
	 *     <tr>
	 *       <th style="text-align: left">Method</th>
	 *       <th style="text-align: left">Property</th>
	 *       <th style="text-align: left">Representation</th>
	 *     </tr>
	 *   </thead>
	 *   <tbody>
	 *     <tr>
	 *       <td>{@link Numeric#value()}</td>
	 *       <td>{value}</td>
	 *       <td>An xs:boolean value. Required.</td>
	 *     </tr>
	 *   </tbody>
	 * </table>
	 */
	public static class Numeric extends FundamentalFacet {

		private static final String NAME = "numeric";

		private final boolean value;

		private Numeric(final Node node, final boolean value) {
			super(node);
			this.value = value;
		}

		@Override
		public Boolean value() {
			return value;
		}

	}

	private static Node createElementNS(final String name, final String value) {
		final Node elem = fundamentalFacetsDocument.createElementNS(null, name);
		final Attr valueAttr = fundamentalFacetsDocument.createAttributeNS(null, "value");
		valueAttr.setNodeValue(value);
		elem.getAttributes().setNamedItem(valueAttr);
		return elem;
	}

	private static Deque<FundamentalFacet> fundamentalFacets(final Ordered.Value ordered, final boolean bounded, final Cardinality.Value cardinality, final boolean numeric) {
		final Node orderedNode = createElementNS(Ordered.NAME, ordered.getName());
		final Node boundedNode = createElementNS(Bounded.NAME, Boolean.toString(bounded));
		final Node cardinalityNode = createElementNS(Cardinality.NAME, cardinality.getName());
		final Node numericNode = createElementNS(Numeric.NAME, Boolean.toString(numeric));
		return Deques.asDeque(new Ordered(orderedNode, ordered), new Bounded(boundedNode, bounded), new Cardinality(cardinalityNode, cardinality), new Numeric(numericNode, numeric));
	}

	static Deque<FundamentalFacet> find(final SimpleType simpleType) {
		return fundamentalFacets.get(simpleType);
	}

	public abstract Object value();

	@Override
	public Node node() {
		return node;
	}

}
