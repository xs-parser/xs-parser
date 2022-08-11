package xs.parser;

import java.util.*;
import org.w3c.dom.*;
import xs.parser.internal.util.*;
import xs.parser.v.*;

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

		public enum Value {

			FALSE("false"),
			PARTIAL("partial"),
			TOTAL("total");

			private final String name;

			Value(final String name) {
				this.name = name;
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

		private Ordered(final SimpleType context, final Node node, final Value value) {
			super(context, node);
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

		private Bounded(final SimpleType context, final Node node, final boolean value) {
			super(context, node);
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

		private Cardinality(final SimpleType context, final Node node, final Value value) {
			super(context, node);
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

		private Numeric(final SimpleType context, final Node node, final boolean value) {
			super(context, node);
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
		final Node orderedNode = createElementNS(Ordered.NAME, ordered.getName());
		final Node boundedNode = createElementNS(Bounded.NAME, Boolean.toString(bounded));
		final Node cardinalityNode = createElementNS(Cardinality.NAME, cardinality.getName());
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
			visitor.onFundamentalFacet(context, node, this);
		}
	}

	public abstract Object value();

}
