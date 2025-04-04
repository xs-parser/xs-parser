package xs.parser;

import java.util.*;
import org.w3c.dom.*;
import xs.parser.internal.util.*;
import xs.parser.v.*;

/**
 * Each fundamental facet is a schema component that provides a limited piece of information about some aspect of each datatype. All ·fundamental facet· components are defined in this section. For example, cardinality is a ·fundamental facet·. Most ·fundamental facets· are given a value fixed with each primitive datatype's definition, and this value is not changed by subsequent ·derivations· (even when it would perhaps be reasonable to expect an application to give a more accurate value based on the constraining facets used to define the ·derivation·). The cardinality and bounded facets are exceptions to this rule; their values may change as a result of certain ·derivations·.
 * <p>
 * <i>Note: Schema components are identified by kind. "Fundamental" is not a kind of component. Each kind of ·fundamental facet· ("ordered", "bounded", etc.) is a separate kind of schema component.</i>
 * <p>
 * A ·fundamental facet· can occur only in the {fundamental facets} of a Simple Type Definition, and this is the only place where ·fundamental facet· components occur. Each kind of ·fundamental facet· component occurs (once) in each Simple Type Definition's {fundamental facets} set.
 * <p>
 * <i>Note: The value of any ·fundamental facet· component can always be calculated from other properties of its ·owner·. Fundamental facets are not required for schema processing, but some applications use them.</i>
 */
public abstract class FundamentalFacet implements SchemaComponent {

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

		/** Ordered values */
		public enum Value {

			/** Ordered value false */
			FALSE("false"),
			/** Ordered value partial */
			PARTIAL("partial"),
			/** Ordered value total */
			TOTAL("total");

			private final String name;

			Value(final String name) {
				this.name = name;
			}

			/**
			 * Returns the name of the fundamental facet ordered, i.e. {@code "false"}, {@code "partial"}, or {@code "total"} as appropriate.
			 * @return The name of this fundamental facet ordered
			 */
			@Override
			public String toString() {
				return name;
			}

		}

		private static final String NAME = "ordered";

		private final Value value;

		private Ordered(final SimpleType context, final Node node, final Value value) {
			super(context, node);
			this.value = Objects.requireNonNull(value);
		}

		/** @return One of {false, partial, total}. Required. */
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

		private Bounded(final SimpleType context, final Node node, final boolean value) {
			super(context, node);
			this.value = value;
		}

		/** @return An xs:boolean value. Required. */
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

		/** Cardinality values */
		public enum Value {

			/** Cardinality value finite */
			FINITE("finite"),
			/** Cardinality value countably infinite */
			COUNTABLY_INFINITE("countably infinite");

			private final String name;

			Value(final String name) {
				this.name = name;
			}

			/**
			 * Returns the name of this fundamental facet cardinality, i.e. {@code "finite"} or {@code "countably infinite"} as appropriate.
			 * @return The name of this fundamental facet cardinality
			 */
			@Override
			public String toString() {
				return name;
			}

		}

		private static final String NAME = "cardinality";

		private final Value value;

		private Cardinality(final SimpleType context, final Node node, final Value value) {
			super(context, node);
			this.value = Objects.requireNonNull(value);
		}

		/** @return One of {finite, countably infinite}. Required. */
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

		private Numeric(final SimpleType context, final Node node, final boolean value) {
			super(context, node);
			this.value = value;
		}

		/** @return An xs:boolean value. Required. */
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

	private static final Document fundamentalFacetsDocument;
	/** Derived from the table https://www.w3.org/TR/2012/REC-xmlschema11-2-20120405/datatypes.html#app-fundamental-facets */
	private static final Map<SimpleType, Deque<FundamentalFacet>> fundamentalFacets;

	static {
		fundamentalFacetsDocument = NodeHelper.newDocument();
		final Map<SimpleType, Deque<FundamentalFacet>> f = new HashMap<>();
		// Primitive
		putFundamentalFacets(f, SimpleType.xsString(), Ordered.Value.FALSE, false, Cardinality.Value.COUNTABLY_INFINITE, false);
		putFundamentalFacets(f, SimpleType.xsBoolean(), Ordered.Value.FALSE, false, Cardinality.Value.FINITE, false);
		putFundamentalFacets(f, SimpleType.xsFloat(), Ordered.Value.PARTIAL, true, Cardinality.Value.FINITE, true);
		putFundamentalFacets(f, SimpleType.xsDouble(), Ordered.Value.PARTIAL, true, Cardinality.Value.FINITE, true);
		putFundamentalFacets(f, SimpleType.xsDecimal(), Ordered.Value.TOTAL, false, Cardinality.Value.COUNTABLY_INFINITE, true);
		putFundamentalFacets(f, SimpleType.xsDuration(), Ordered.Value.PARTIAL, false, Cardinality.Value.COUNTABLY_INFINITE, false);
		putFundamentalFacets(f, SimpleType.xsDateTime(), Ordered.Value.PARTIAL, false, Cardinality.Value.COUNTABLY_INFINITE, false);
		putFundamentalFacets(f, SimpleType.xsTime(), Ordered.Value.PARTIAL, false, Cardinality.Value.COUNTABLY_INFINITE, false);
		putFundamentalFacets(f, SimpleType.xsDate(), Ordered.Value.PARTIAL, false, Cardinality.Value.COUNTABLY_INFINITE, false);
		putFundamentalFacets(f, SimpleType.xsGYearMonth(), Ordered.Value.PARTIAL, false, Cardinality.Value.COUNTABLY_INFINITE, false);
		putFundamentalFacets(f, SimpleType.xsGYear(), Ordered.Value.PARTIAL, false, Cardinality.Value.COUNTABLY_INFINITE, false);
		putFundamentalFacets(f, SimpleType.xsGMonthDay(), Ordered.Value.PARTIAL, false, Cardinality.Value.COUNTABLY_INFINITE, false);
		putFundamentalFacets(f, SimpleType.xsGDay(), Ordered.Value.PARTIAL, false, Cardinality.Value.COUNTABLY_INFINITE, false);
		putFundamentalFacets(f, SimpleType.xsGMonth(), Ordered.Value.PARTIAL, false, Cardinality.Value.COUNTABLY_INFINITE, false);
		putFundamentalFacets(f, SimpleType.xsHexBinary(), Ordered.Value.FALSE, false, Cardinality.Value.COUNTABLY_INFINITE, false);
		putFundamentalFacets(f, SimpleType.xsBase64Binary(), Ordered.Value.FALSE, false, Cardinality.Value.COUNTABLY_INFINITE, false);
		putFundamentalFacets(f, SimpleType.xsAnyURI(), Ordered.Value.FALSE, false, Cardinality.Value.COUNTABLY_INFINITE, false);
		putFundamentalFacets(f, SimpleType.xsQName(), Ordered.Value.FALSE, false, Cardinality.Value.COUNTABLY_INFINITE, false);
		putFundamentalFacets(f, SimpleType.xsNOTATION(), Ordered.Value.FALSE, false, Cardinality.Value.COUNTABLY_INFINITE, false);
		// Built-in
		putFundamentalFacets(f, SimpleType.xsNormalizedString(), Ordered.Value.FALSE, false, Cardinality.Value.COUNTABLY_INFINITE, false);
		putFundamentalFacets(f, SimpleType.xsToken(), Ordered.Value.FALSE, false, Cardinality.Value.COUNTABLY_INFINITE, false);
		putFundamentalFacets(f, SimpleType.xsLanguage(), Ordered.Value.FALSE, false, Cardinality.Value.COUNTABLY_INFINITE, false);
		putFundamentalFacets(f, SimpleType.xsIDREFS(), Ordered.Value.FALSE, false, Cardinality.Value.COUNTABLY_INFINITE, false);
		putFundamentalFacets(f, SimpleType.xsENTITIES(), Ordered.Value.FALSE, false, Cardinality.Value.COUNTABLY_INFINITE, false);
		putFundamentalFacets(f, SimpleType.xsNMTOKEN(), Ordered.Value.FALSE, false, Cardinality.Value.COUNTABLY_INFINITE, false);
		putFundamentalFacets(f, SimpleType.xsNMTOKENS(), Ordered.Value.FALSE, false, Cardinality.Value.COUNTABLY_INFINITE, false);
		putFundamentalFacets(f, SimpleType.xsName(), Ordered.Value.FALSE, false, Cardinality.Value.COUNTABLY_INFINITE, false);
		putFundamentalFacets(f, SimpleType.xsNCName(), Ordered.Value.FALSE, false, Cardinality.Value.COUNTABLY_INFINITE, false);
		putFundamentalFacets(f, SimpleType.xsID(), Ordered.Value.FALSE, false, Cardinality.Value.COUNTABLY_INFINITE, false);
		putFundamentalFacets(f, SimpleType.xsIDREF(), Ordered.Value.FALSE, false, Cardinality.Value.COUNTABLY_INFINITE, false);
		putFundamentalFacets(f, SimpleType.xsENTITY(), Ordered.Value.FALSE, false, Cardinality.Value.COUNTABLY_INFINITE, false);
		putFundamentalFacets(f, SimpleType.xsInteger(), Ordered.Value.TOTAL, false, Cardinality.Value.COUNTABLY_INFINITE, true);
		putFundamentalFacets(f, SimpleType.xsNonPositiveInteger(), Ordered.Value.TOTAL, false, Cardinality.Value.COUNTABLY_INFINITE, true);
		putFundamentalFacets(f, SimpleType.xsNegativeInteger(), Ordered.Value.TOTAL, false, Cardinality.Value.COUNTABLY_INFINITE, true);
		putFundamentalFacets(f, SimpleType.xsLong(), Ordered.Value.TOTAL, true, Cardinality.Value.FINITE, true);
		putFundamentalFacets(f, SimpleType.xsInt(), Ordered.Value.TOTAL, true, Cardinality.Value.FINITE, true);
		putFundamentalFacets(f, SimpleType.xsShort(), Ordered.Value.TOTAL, true, Cardinality.Value.FINITE, true);
		putFundamentalFacets(f, SimpleType.xsByte(), Ordered.Value.TOTAL, true, Cardinality.Value.FINITE, true);
		putFundamentalFacets(f, SimpleType.xsNonNegativeInteger(), Ordered.Value.TOTAL, false, Cardinality.Value.COUNTABLY_INFINITE, true);
		putFundamentalFacets(f, SimpleType.xsUnsignedLong(), Ordered.Value.TOTAL, true, Cardinality.Value.FINITE, true);
		putFundamentalFacets(f, SimpleType.xsUnsignedInt(), Ordered.Value.TOTAL, true, Cardinality.Value.FINITE, true);
		putFundamentalFacets(f, SimpleType.xsUnsignedShort(), Ordered.Value.TOTAL, true, Cardinality.Value.FINITE, true);
		putFundamentalFacets(f, SimpleType.xsUnsignedByte(), Ordered.Value.TOTAL, true, Cardinality.Value.FINITE, true);
		putFundamentalFacets(f, SimpleType.xsPositiveInteger(), Ordered.Value.TOTAL, false, Cardinality.Value.COUNTABLY_INFINITE, true);
		putFundamentalFacets(f, SimpleType.xsYearMonthDuration(), Ordered.Value.PARTIAL, false, Cardinality.Value.COUNTABLY_INFINITE, false);
		putFundamentalFacets(f, SimpleType.xsDayTimeDuration(), Ordered.Value.PARTIAL, false, Cardinality.Value.COUNTABLY_INFINITE, false);
		putFundamentalFacets(f, SimpleType.xsDateTimeStamp(), Ordered.Value.PARTIAL, false, Cardinality.Value.COUNTABLY_INFINITE, false);
		fundamentalFacets = Collections.unmodifiableMap(f);
	}

	private final Node node;
	private final SimpleType context;

	private FundamentalFacet(final SimpleType context, final Node node) {
		this.context = Objects.requireNonNull(context);
		this.node = Objects.requireNonNull(node);
	}

	private static Deque<FundamentalFacet> fundamentalFacets(final SimpleType simpleType, final Ordered.Value ordered, final boolean bounded, final Cardinality.Value cardinality, final boolean numeric) {
		final Node orderedNode = createElementNS(Ordered.NAME, ordered.name);
		final Node boundedNode = createElementNS(Bounded.NAME, Boolean.toString(bounded));
		final Node cardinalityNode = createElementNS(Cardinality.NAME, cardinality.name);
		final Node numericNode = createElementNS(Numeric.NAME, Boolean.toString(numeric));
		return Deques.asDeque(new Ordered(simpleType, orderedNode, ordered), new Bounded(simpleType, boundedNode, bounded), new Cardinality(simpleType, cardinalityNode, cardinality), new Numeric(simpleType, numericNode, numeric));
	}

	private static void putFundamentalFacets(final Map<SimpleType, Deque<FundamentalFacet>> f, final SimpleType simpleType, final Ordered.Value ordered, final boolean bounded, final Cardinality.Value cardinality, final boolean numeric) {
		f.put(simpleType, fundamentalFacets(simpleType, ordered, bounded, cardinality, numeric));
	}

	static Deque<FundamentalFacet> find(final SimpleType simpleType) {
		return fundamentalFacets.get(simpleType);
	}

	void visit(final Visitor visitor) {
		if (visitor.visit(context, node, this)) {
			visitor.onFundamentalFacet(context, (org.w3c.dom.Element) node.cloneNode(true), this);
		}
	}

	/** @return The value of this fundamental facet */
	public abstract Object value();

}
