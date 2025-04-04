package xs.parser;

import static xs.parser.internal.util.Deques.*;

import java.math.*;
import java.util.*;
import java.util.stream.*;
import javax.xml.*;
import org.w3c.dom.*;
import xs.parser.internal.*;
import xs.parser.internal.TagParser.*;
import xs.parser.internal.util.*;
import xs.parser.internal.util.SequenceParser.*;
import xs.parser.v.*;

/**
 * Constraining facets are schema components whose values may be set or changed during ·derivation· (subject to facet-specific controls) to control various aspects of the derived datatype. All ·constraining facet· components defined by this specification are defined in this section. For example, whiteSpace is a ·constraining facet·. ·Constraining Facets· are given a value as part of the ·derivation· when an ·ordinary· datatype is defined by ·restricting· a ·primitive· or ·ordinary· datatype; a few ·constraining facets· have default values that are also provided for ·primitive· datatypes.
 * <p>
 * <i>Note: Schema components are identified by kind. "Constraining" is not a kind of component. Each kind of ·constraining facet· ("whiteSpace", "length", etc.) is a separate kind of schema component.</i>
 * <p>
 * This specification distinguishes three kinds of constraining facets:
 * <ul>
 *   <li>A constraining facet which is used to normalize an initial ·literal· before checking to see whether the resulting character sequence is a member of a datatype's ·lexical space· is a pre-lexical facet.<br>This specification defines just one ·pre-lexical· facet: whiteSpace.</li>
 *   <li>A constraining facet which directly restricts the ·lexical space· of a datatype is a lexical facet.<br>This specification defines just one ·lexical· facet: pattern.<br><i>Note: As specified normatively elsewhere, ·lexical· facets can have an indirect effect on the ·value space·: if every lexical representation of a value is removed from the ·lexical space·, the value itself is removed from the ·value space·.</i></li>
 *    <li>A constraining facet which directly restricts the ·value space· of a datatype is a value-based facet.<br>Most of the constraining facets defined by this specification are ·value-based· facets.<br><i>Note: As specified normatively elsewhere, ·value-based· facets can have an indirect effect on the ·lexical space·: if a value is removed from the ·value space·, its lexical representations are removed from the ·lexical space·.</i></li>
 * </ul>
 */
public abstract class ConstrainingFacet implements AnnotatedComponent {

	@FunctionalInterface
	interface Constructible {

		ConstrainingFacet apply(Deferred<SimpleType> simpleType);

	}

	/**
	 * <pre>
	 * &lt;length
	 *   fixed = boolean : false
	 *   id = ID
	 *   value = nonNegativeInteger
	 *   {any attributes with non-schema namespace . . .}&gt;
	 *   Content: (annotation?)
	 * &lt;/length&gt;
	 * </pre>
	 *
	 * <table>
	 *   <caption style="font-size: large; text-align: left">Schema Component: length, a kind of Constraining Facet</caption>
	 *   <thead>
	 *     <tr>
	 *       <th style="text-align: left">Method</th>
	 *       <th style="text-align: left">Property</th>
	 *       <th style="text-align: left">Representation</th>
	 *     </tr>
	 *   </thead>
	 *   <tbody>
	 *     <tr>
	 *       <td>{@link Length#annotations()}</td>
	 *       <td>{annotations}</td>
	 *       <td>A sequence of Annotation components.</td>
	 *     </tr>
	 *     <tr>
	 *       <td>{@link Length#value()}</td>
	 *       <td>{value}</td>
	 *       <td>An xs:nonNegativeInteger value. Required.</td>
	 *     </tr>
	 *     <tr>
	 *       <td>{@link Length#fixed()}</td>
	 *       <td>{fixed}</td>
	 *       <td>An xs:boolean value. Required.</td>
	 *     </tr>
	 *   </tbody>
	 * </table>
	 */
	public static class Length extends ConstrainingFacet {

		private final BigInteger value;

		Length(final Deferred<SimpleType> context, final Node node, final Deque<Annotation> annotations, final Boolean fixed, final BigInteger value) {
			super(context, node, annotations, fixed);
			this.value = value;
		}

		@Override
		public Number value() {
			return value;
		}

	}

	/**
	 * <pre>
	 * &lt;minLength
	 *   fixed = boolean : false
	 *   id = ID
	 *   value = nonNegativeInteger
	 *   {any attributes with non-schema namespace . . .}&gt;
	 *   Content: (annotation?)
	 * &lt;/minLength&gt;
	 * </pre>
	 *
	 * <table>
	 *   <caption style="font-size: large; text-align: left">Schema Component: minLength, a kind of Constraining Facet</caption>
	 *   <thead>
	 *     <tr>
	 *       <th style="text-align: left">Method</th>
	 *       <th style="text-align: left">Property</th>
	 *       <th style="text-align: left">Representation</th>
	 *     </tr>
	 *   </thead>
	 *   <tbody>
	 *     <tr>
	 *       <td>{@link MinLength#annotations()}</td>
	 *       <td>{annotations}</td>
	 *       <td>A sequence of Annotation components.</td>
	 *     </tr>
	 *     <tr>
	 *       <td>{@link MinLength#value()}</td>
	 *       <td>{value}</td>
	 *       <td>An xs:nonNegativeInteger value. Required.</td>
	 *     </tr>
	 *     <tr>
	 *       <td>{@link MinLength#fixed()}</td>
	 *       <td>{fixed}</td>
	 *       <td>An xs:boolean value. Required.</td>
	 *     </tr>
	 *   </tbody>
	 * </table>
	 */
	public static class MinLength extends ConstrainingFacet {

		private final BigInteger value;

		MinLength(final Deferred<SimpleType> context, final Node node, final Deque<Annotation> annotations, final Boolean fixed, final BigInteger value) {
			super(context, node, annotations, fixed);
			this.value = value;
		}

		@Override
		public Number value() {
			return value;
		}

	}

	/**
	 * <pre>
	 * &lt;maxLength
	 *   fixed = boolean : false
	 *   id = ID
	 *   value = nonNegativeInteger
	 *   {any attributes with non-schema namespace . . .}&gt;
	 *   Content: (annotation?)
	 * &lt;/maxLength&gt;
	 * </pre>
	 *
	 * <table>
	 *   <caption style="font-size: large; text-align: left">Schema Component: maxLength, a kind of Constraining Facet</caption>
	 *   <thead>
	 *     <tr>
	 *       <th style="text-align: left">Method</th>
	 *       <th style="text-align: left">Property</th>
	 *       <th style="text-align: left">Representation</th>
	 *     </tr>
	 *   </thead>
	 *   <tbody>
	 *     <tr>
	 *       <td>{@link MaxLength#annotations()}</td>
	 *       <td>{annotations}</td>
	 *       <td>A sequence of Annotation components.</td>
	 *     </tr>
	 *     <tr>
	 *       <td>{@link MaxLength#value()}</td>
	 *       <td>{value}</td>
	 *       <td>An xs:nonNegativeInteger value. Required.</td>
	 *     </tr>
	 *     <tr>
	 *       <td>{@link MaxLength#fixed()}</td>
	 *       <td>{fixed}</td>
	 *       <td>An xs:boolean value. Required.</td>
	 *     </tr>
	 *   </tbody>
	 * </table>
	 */
	public static class MaxLength extends ConstrainingFacet {

		private final BigInteger value;

		MaxLength(final Deferred<SimpleType> context, final Node node, final Deque<Annotation> annotations, final Boolean fixed, final BigInteger value) {
			super(context, node, annotations, fixed);
			this.value = value;
		}

		@Override
		public Number value() {
			return value;
		}

	}

	/**
	 * <pre>
	 * &lt;pattern
	 *   id = ID
	 *   value = string
	 *   {any attributes with non-schema namespace . . .}&gt;
	 *   Content: (annotation?)
	 * &lt;/pattern&gt;
	 * </pre>
	 *
	 * <table>
	 *   <caption style="font-size: large; text-align: left">Schema Component: pattern, a kind of Constraining Facet</caption>
	 *   <thead>
	 *     <tr>
	 *       <th style="text-align: left">Method</th>
	 *       <th style="text-align: left">Property</th>
	 *       <th style="text-align: left">Representation</th>
	 *     </tr>
	 *   </thead>
	 *   <tbody>
	 *     <tr>
	 *       <td>{@link Pattern#annotations()}</td>
	 *       <td>{annotations}</td>
	 *       <td>A sequence of Annotation components.</td>
	 *     </tr>
	 *     <tr>
	 *       <td>{@link Pattern#value()}</td>
	 *       <td>{value}</td>
	 *       <td>A non-empty set of ·regular expressions·.</td>
	 *     </tr>
	 *   </tbody>
	 * </table>
	 */
	public static class Pattern extends ConstrainingFacet {

		private final Set<String> value;

		Pattern(final Deferred<SimpleType> context, final Node node, final Deque<Annotation> annotations, final Set<String> value) {
			super(context, node, annotations, null);
			this.value = Objects.requireNonNull(value);
		}

		/**
		 * @return Let R be a regular expression given by the appropriate case among the following:
		 * <ol>
		 *   <li>If there is only one &lt;pattern&gt; among the [children] of a &lt;restriction&gt;, then the actual value of its value [attribute]</li>
		 *   <li>otherwise the concatenation of the actual values of all the &lt;pattern&gt; [children]'s value [attributes], in order, separated by '|', so forming a single regular expression with multiple ·branches·.</li>
		 * </ol>
		 *
		 * The value is then given by the appropriate case among the following:
		 * <ol>
		 *   <li>If the {base type definition} of the ·owner· has a pattern facet among its {facets}, then the union of that pattern facet's {value} and {·R·}</li>
		 *   <li>otherwise just {·R·}</li>
		 * </ol>
		 */
		@Override
		public Set<String> value() {
			return Collections.unmodifiableSet(value);
		}

	}

	/**
	 * <pre>
	 * &lt;enumeration
	 *   id = ID
	 *   value = anySimpleType
	 *   {any attributes with non-schema namespace . . .}&gt;
	 *   Content: (annotation?)
	 * &lt;/enumeration&gt;
	 * </pre>
	 *
	 * <table>
	 *   <caption style="font-size: large; text-align: left">Schema Component: pattern, a kind of Constraining Facet</caption>
	 *   <thead>
	 *     <tr>
	 *       <th style="text-align: left">Method</th>
	 *       <th style="text-align: left">Property</th>
	 *       <th style="text-align: left">Representation</th>
	 *     </tr>
	 *   </thead>
	 *   <tbody>
	 *     <tr>
	 *       <td>{@link ConstrainingFacet.Enumeration#annotations()}</td>
	 *       <td>{annotations}</td>
	 *       <td>A sequence of Annotation components.</td>
	 *     </tr>
	 *     <tr>
	 *       <td>{@link ConstrainingFacet.Enumeration#value()}</td>
	 *       <td>{value}</td>
	 *       <td>A set of values from the ·value space· of the {base type definition}.</td>
	 *     </tr>
	 *   </tbody>
	 * </table>
	 */
	public static class Enumeration extends ConstrainingFacet {

		private final Set<String> value;

		Enumeration(final Deferred<SimpleType> context, final Node node, final Deque<Annotation> annotations, final Set<String> value) {
			super(context, node, annotations, null);
			this.value = Objects.requireNonNull(value);
		}

		/**
		 * @return The appropriate case among the following:
		 * <ol>
		 *   <li>If there is only one &lt;enumeration&gt; among the [children] of a &lt;restriction&gt;, then a set with one member, the actual value of its value [attribute], interpreted as an instance of the {base type definition}.</li>
		 *   <li>otherwise a set of the actual values of all the &lt;enumeration&gt; [children]'s value [attributes], interpreted as instances of the {base type definition}.</li>
		 * </ol>
		 *
		 * <i>Note: The value [attribute] is declared as having type ·anySimpleType·, but the {value} property of the enumeration facet must be a member of the {base type definition}. So in mapping from the XML representation to the enumeration component, the actual value is identified by using the ·lexical mapping· of the {base type definition}.</i>
		 */
		@Override
		public Set<String> value() {
			return Collections.unmodifiableSet(value);
		}

	}

	/**
	 * <pre>
	 * &lt;whiteSpace
	 *   fixed = boolean : false
	 *   id = ID
	 *   value = (collapse | preserve | replace)
	 *   {any attributes with non-schema namespace . . .}&gt;
	 *   Content: (annotation?)
	 * &lt;/whiteSpace&gt;
	 * </pre>
	 *
	 * <table>
	 *   <caption style="font-size: large; text-align: left">Schema Component: whiteSpace, a kind of Constraining Facet</caption>
	 *   <thead>
	 *     <tr>
	 *       <th style="text-align: left">Method</th>
	 *       <th style="text-align: left">Property</th>
	 *       <th style="text-align: left">Representation</th>
	 *     </tr>
	 *   </thead>
	 *   <tbody>
	 *     <tr>
	 *       <td>{@link WhiteSpace#annotations()}</td>
	 *       <td>{annotations}</td>
	 *       <td>A sequence of Annotation components.</td>
	 *     </tr>
	 *     <tr>
	 *       <td>{@link WhiteSpace#value()}</td>
	 *       <td>{value}</td>
	 *       <td>One of {preserve, replace, collapse}. Required.</td>
	 *     </tr>
	 *     <tr>
	 *       <td>{@link WhiteSpace#fixed()}</td>
	 *       <td>{fixed}</td>
	 *       <td>An xs:boolean value. Required.</td>
	 *     </tr>
	 *   </tbody>
	 * </table>
	 */
	public static class WhiteSpace extends ConstrainingFacet {

		/** White space value */
		public enum Value {

			/** White space value preserve */
			PRESERVE("preserve"),
			/** White space value replace */
			REPLACE("replace"),
			/** White space value collapse */
			COLLAPSE("collapse");

			private final String name;

			Value(final String name) {
				this.name = name;
			}

			private static Value getAttrValueAsWhiteSpace(final Attr attr) {
				final String value = NodeHelper.collapseWhitespace(attr.getValue());
				for (final Value e : values()) {
					if (e.name.equals(value)) {
						return e;
					}
				}
				throw new IllegalArgumentException(value);
			}

			/**
			 * Returns the name of this constraining facet white space, i.e. {@code "preserve"}, {@code "replace"}, or {@code "collapse"} as appropriate.
			 * @return The name of this constraining facet white space
			 */
			@Override
			public String toString() {
				return name;
			}

		}

		static final Constructible collapseFixed = s -> new WhiteSpace(s, createSynthetic(Names.WHITE_SPACE, WhiteSpace.Value.COLLAPSE), Deques.emptyDeque(), true, WhiteSpace.Value.COLLAPSE);

		private final Value value;

		WhiteSpace(final Deferred<SimpleType> context, final Node node, final Deque<Annotation> annotations, final Boolean fixed, final Value value) {
			super(context, node, annotations, fixed);
			this.value = value;
		}

		@Override
		public Value value() {
			return value;
		}

	}

	/**
	 * <pre>
	 * &lt;maxInclusive
	 *   fixed = boolean : false
	 *   id = ID
	 *   value = anySimpleType
	 *   {any attributes with non-schema namespace . . .}&gt;
	 *   Content: (annotation?)
	 * &lt;/maxInclusive&gt;
	 * </pre>
	 *
	 * <table>
	 *   <caption style="font-size: large; text-align: left">Schema Component: maxInclusive, a kind of Constraining Facet</caption>
	 *   <thead>
	 *     <tr>
	 *       <th style="text-align: left">Method</th>
	 *       <th style="text-align: left">Property</th>
	 *       <th style="text-align: left">Representation</th>
	 *     </tr>
	 *   </thead>
	 *   <tbody>
	 *     <tr>
	 *       <td>{@link MaxInclusive#annotations()}</td>
	 *       <td>{annotations}</td>
	 *       <td>A sequence of Annotation components.</td>
	 *     </tr>
	 *     <tr>
	 *       <td>{@link MaxInclusive#value()}</td>
	 *       <td>{value}</td>
	 *       <td>Required.<br>A value from the ·value space· of the {base type definition}.</td>
	 *     </tr>
	 *     <tr>
	 *       <td>{@link MaxInclusive#fixed()}</td>
	 *       <td>{fixed}</td>
	 *       <td>An xs:boolean value. Required.</td>
	 *     </tr>
	 *   </tbody>
	 * </table>
	 */
	public static class MaxInclusive extends ConstrainingFacet {

		private final Deferred<Number> value;

		MaxInclusive(final Deferred<SimpleType> context, final Node node, final Deque<Annotation> annotations, final Boolean fixed, final Deferred<Number> value) {
			super(context, node, annotations, fixed);
			this.value = value;
		}

		@Override
		public Number value() {
			return value.get();
		}

	}

	/**
	 * <pre>
	 * &lt;maxExclusive
	 *   fixed = boolean : false
	 *   id = ID
	 *   value = anySimpleType
	 *   {any attributes with non-schema namespace . . .}&gt;
	 *   Content: (annotation?)
	 * &lt;/maxExclusive&gt;
	 * </pre>
	 *
	 * <table>
	 *   <caption style="font-size: large; text-align: left">Schema Component: maxExclusive, a kind of Constraining Facet</caption>
	 *   <thead>
	 *     <tr>
	 *       <th style="text-align: left">Method</th>
	 *       <th style="text-align: left">Property</th>
	 *       <th style="text-align: left">Representation</th>
	 *     </tr>
	 *   </thead>
	 *   <tbody>
	 *     <tr>
	 *       <td>{@link MaxExclusive#annotations()}</td>
	 *       <td>{annotations}</td>
	 *       <td>A sequence of Annotation components.</td>
	 *     </tr>
	 *     <tr>
	 *       <td>{@link MaxExclusive#value()}</td>
	 *       <td>{value}</td>
	 *       <td>Required.<br>A value from the ·value space· of the {base type definition}.</td>
	 *     </tr>
	 *     <tr>
	 *       <td>{@link MaxExclusive#fixed()}</td>
	 *       <td>{fixed}</td>
	 *       <td>An xs:boolean value. Required.</td>
	 *     </tr>
	 *   </tbody>
	 * </table>
	 */
	public static class MaxExclusive extends ConstrainingFacet {

		private final Deferred<Number> value;

		MaxExclusive(final Deferred<SimpleType> context, final Node node, final Deque<Annotation> annotations, final Boolean fixed, final Deferred<Number> value) {
			super(context, node, annotations, fixed);
			this.value = value;
		}

		@Override
		public Number value() {
			return value.get();
		}

	}

	/**
	 * <pre>
	 * &lt;minExclusive
	 *   fixed = boolean : false
	 *   id = ID
	 *   value = anySimpleType
	 *   {any attributes with non-schema namespace . . .}&gt;
	 *   Content: (annotation?)
	 * &lt;/minExclusive&gt;
	 * </pre>
	 *
	 * <table>
	 *   <caption style="font-size: large; text-align: left">Schema Component: minExclusive, a kind of Constraining Facet</caption>
	 *   <thead>
	 *     <tr>
	 *       <th style="text-align: left">Method</th>
	 *       <th style="text-align: left">Property</th>
	 *       <th style="text-align: left">Representation</th>
	 *     </tr>
	 *   </thead>
	 *   <tbody>
	 *     <tr>
	 *       <td>{@link MinExclusive#annotations()}</td>
	 *       <td>{annotations}</td>
	 *       <td>A sequence of Annotation components.</td>
	 *     </tr>
	 *     <tr>
	 *       <td>{@link MinExclusive#value()}</td>
	 *       <td>{value}</td>
	 *       <td>Required.<br>A value from the ·value space· of the {base type definition}.</td>
	 *     </tr>
	 *     <tr>
	 *       <td>{@link MinExclusive#fixed()}</td>
	 *       <td>{fixed}</td>
	 *       <td>An xs:boolean value. Required.</td>
	 *     </tr>
	 *   </tbody>
	 * </table>
	 */
	public static class MinExclusive extends ConstrainingFacet {

		private final Deferred<Number> value;

		MinExclusive(final Deferred<SimpleType> context, final Node node, final Deque<Annotation> annotations, final Boolean fixed, final Deferred<Number> value) {
			super(context, node, annotations, fixed);
			this.value = value;
		}

		@Override
		public Number value() {
			return value.get();
		}

	}

	/**
	 * <pre>
	 * &lt;minInclusive
	 *   fixed = boolean : false
	 *   id = ID
	 *   value = anySimpleType
	 *   {any attributes with non-schema namespace . . .}&gt;
	 *   Content: (annotation?)
	 * &lt;/minInclusive&gt;
	 * </pre>
	 *
	 * <table>
	 *   <caption style="font-size: large; text-align: left">Schema Component: minInclusive, a kind of Constraining Facet</caption>
	 *   <thead>
	 *     <tr>
	 *       <th style="text-align: left">Method</th>
	 *       <th style="text-align: left">Property</th>
	 *       <th style="text-align: left">Representation</th>
	 *     </tr>
	 *   </thead>
	 *   <tbody>
	 *     <tr>
	 *       <td>{@link MinInclusive#annotations()}</td>
	 *       <td>{annotations}</td>
	 *       <td>A sequence of Annotation components.</td>
	 *     </tr>
	 *     <tr>
	 *       <td>{@link MinInclusive#value()}</td>
	 *       <td>{value}</td>
	 *       <td>Required.<br>A value from the ·value space· of the {base type definition}.</td>
	 *     </tr>
	 *     <tr>
	 *       <td>{@link MinInclusive#fixed()}</td>
	 *       <td>{fixed}</td>
	 *       <td>An xs:boolean value. Required.</td>
	 *     </tr>
	 *   </tbody>
	 * </table>
	 */
	public static class MinInclusive extends ConstrainingFacet {

		private final Deferred<Number> value;

		MinInclusive(final Deferred<SimpleType> context, final Node node, final Deque<Annotation> annotations, final Boolean fixed, final Deferred<Number> value) {
			super(context, node, annotations, fixed);
			this.value = value;
		}

		@Override
		public Number value() {
			return value.get();
		}

	}

	/**
	 * <pre>
	 * &lt;totalDigits
	 *   fixed = boolean : false
	 *   id = ID
	 *   value = positiveInteger
	 *   {any attributes with non-schema namespace . . .}&gt;
	 *   Content: (annotation?)
	 * &lt;/totalDigits&gt;
	 * </pre>
	 *
	 * <table>
	 *   <caption style="font-size: large; text-align: left">Schema Component: totalDigits, a kind of Constraining Facet</caption>
	 *   <thead>
	 *     <tr>
	 *       <th style="text-align: left">Method</th>
	 *       <th style="text-align: left">Property</th>
	 *       <th style="text-align: left">Representation</th>
	 *     </tr>
	 *   </thead>
	 *   <tbody>
	 *     <tr>
	 *       <td>{@link TotalDigits#annotations()}</td>
	 *       <td>{annotations}</td>
	 *       <td>A sequence of Annotation components.</td>
	 *     </tr>
	 *     <tr>
	 *       <td>{@link TotalDigits#value()}</td>
	 *       <td>{value}</td>
	 *       <td>An xs:positiveInteger value. Required.</td>
	 *     </tr>
	 *     <tr>
	 *       <td>{@link TotalDigits#fixed()}</td>
	 *       <td>{fixed}</td>
	 *       <td>An xs:boolean value. Required.</td>
	 *     </tr>
	 *   </tbody>
	 * </table>
	 */
	public static class TotalDigits extends ConstrainingFacet {

		private final Number value;

		TotalDigits(final Deferred<SimpleType> context, final Node node, final Deque<Annotation> annotations, final Boolean fixed, final Number value) {
			super(context, node, annotations, fixed);
			this.value = value;
		}

		@Override
		public Number value() {
			return value;
		}

	}

	/**
	 * <pre>
	 * &lt;fractionDigits
	 *   fixed = boolean : false
	 *   id = ID
	 *   value = nonNegativeInteger
	 *   {any attributes with non-schema namespace . . .}&gt;
	 *   Content: (annotation?)
	 * &lt;/fractionDigits&gt;
	 * </pre>
	 *
	 * <table>
	 *   <caption style="font-size: large; text-align: left">Schema Component: fractionDigits, a kind of Constraining Facet</caption>
	 *   <thead>
	 *     <tr>
	 *       <th style="text-align: left">Method</th>
	 *       <th style="text-align: left">Property</th>
	 *       <th style="text-align: left">Representation</th>
	 *     </tr>
	 *   </thead>
	 *   <tbody>
	 *     <tr>
	 *       <td>{@link FractionDigits#annotations()}</td>
	 *       <td>{annotations}</td>
	 *       <td>A sequence of Annotation components.</td>
	 *     </tr>
	 *     <tr>
	 *       <td>{@link FractionDigits#value()}</td>
	 *       <td>{value}</td>
	 *       <td>An xs:nonNegativeInteger value. Required.</td>
	 *     </tr>
	 *     <tr>
	 *       <td>{@link FractionDigits#fixed()}</td>
	 *       <td>{fixed}</td>
	 *       <td>An xs:boolean value. Required.</td>
	 *     </tr>
	 *   </tbody>
	 * </table>
	 */
	public static class FractionDigits extends ConstrainingFacet {

		private final Number value;

		FractionDigits(final Deferred<SimpleType> context, final Node node, final Deque<Annotation> annotations, final Boolean fixed, final Number value) {
			super(context, node, annotations, fixed);
			this.value = value;
		}

		@Override
		public Number value() {
			return value;
		}

	}

	/**
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
	 *   <caption style="font-size: large; text-align: left">Schema Component: assertions, a kind of Constraining Facet</caption>
	 *   <thead>
	 *     <tr>
	 *       <th style="text-align: left">Method</th>
	 *       <th style="text-align: left">Property</th>
	 *       <th style="text-align: left">Representation</th>
	 *     </tr>
	 *   </thead>
	 *   <tbody>
	 *     <tr>
	 *       <td>{@link Assertions#annotations()}</td>
	 *       <td>{annotations}</td>
	 *       <td>A sequence of Annotation components.</td>
	 *     </tr>
	 *     <tr>
	 *       <td>{@link Assertions#value()}</td>
	 *       <td>{value}</td>
	 *       <td>A sequence of Assertion components.</td>
	 *     </tr>
	 *   </tbody>
	 * </table>
	 */
	public static class Assertions extends ConstrainingFacet {

		private final Deque<Assertion> value;

		Assertions(final Deferred<SimpleType> context, final Node node, final Deque<Assertion> value) {
			super(context, node, Deques.emptyDeque(), null);
			this.value = Objects.requireNonNull(value);
		}

		/**
		 * @return A sequence whose members are Assertions drawn from the following sources, in order:
		 * <ol>
		 *   <li>If the {base type definition} of the ·owner· has an assertions facet among its {facets}, then the Assertions which appear in the {value} of that assertions facet.</li>
		 *   <li>Assertions corresponding to the &lt;assertion&gt; element information items among the [children] of &lt;restriction&gt;, if any, in document order. For details of the construction of the Assertion components, see section 3.13.2 of [XSD 1.1 Part 1: Structures].</li>
		 * </ol>
		 */
		@Override
		public Deque<Assertion> value() {
			return Deques.unmodifiableDeque(value);
		}

		/**
		 * @return The empty sequence.
		 * <i>Note: Annotations specified within an &lt;assertion&gt; element are captured by the individual Assertion component to which it maps.</i>
		 */
		@Override
		public final Deque<Annotation> annotations() {
			return super.annotations();
		}

	}

	/**
	 * <pre>
	 * &lt;explicitTimezone
	 *   fixed = boolean : false
	 *   id = ID
	 *   value = NCName
	 *   {any attributes with non-schema namespace . . .}&gt;
	 *   Content: (annotation?)
	 * &lt;/explicitTimezone&gt;
	 * </pre>
	 *
	 * <table>
	 *   <caption style="font-size: large; text-align: left">Schema Component: explicitTimezone, a kind of Constraining Facet</caption>
	 *   <thead>
	 *     <tr>
	 *       <th style="text-align: left">Method</th>
	 *       <th style="text-align: left">Property</th>
	 *       <th style="text-align: left">Representation</th>
	 *     </tr>
	 *   </thead>
	 *   <tbody>
	 *     <tr>
	 *       <td>{@link ExplicitTimezone#annotations()}</td>
	 *       <td>{annotations}</td>
	 *       <td>A sequence of Annotation components.</td>
	 *     </tr>
	 *     <tr>
	 *       <td>{@link ExplicitTimezone#value()}</td>
	 *       <td>{value}</td>
	 *       <td>One of {required, prohibited, optional}. Required.</td>
	 *     </tr>
	 *     <tr>
	 *       <td>{@link ExplicitTimezone#fixed()}</td>
	 *       <td>{fixed}</td>
	 *       <td>An xs:boolean value. Required.</td>
	 *     </tr>
	 *   </tbody>
	 * </table>
	 */
	public static class ExplicitTimezone extends ConstrainingFacet {

		/** Explicit timezone value */
		public enum Value {

			/** Explicit timezone value required */
			REQUIRED("required"),
			/** Explicit timezone value prohibited */
			PROHIBITED("prohibited"),
			/** Explicit timezone value optional */
			OPTIONAL("optional");

			private final String name;

			Value(final String name) {
				this.name = name;
			}

			private static Value getAttrValueAsExplicitTimezone(final Attr attr) {
				final String value = NodeHelper.collapseWhitespace(attr.getValue());
				for (final Value e : values()) {
					if (e.name.equals(value)) {
						return e;
					}
				}
				throw new IllegalArgumentException(value);
			}

			/**
			 * Returns the name of this constraining facet explicit timezone, i.e. {@code "required"}, {@code "prohibited"}, or {@code "optional"} as appropriate.
			 * @return The name of this constraining facet explicit timezone
			 */
			@Override
			public String toString() {
				return name;
			}

		}

		private final Value value;

		ExplicitTimezone(final Deferred<SimpleType> context, final Node node, final Deque<Annotation> annotations, final Boolean fixed, final Value value) {
			super(context, node, annotations, fixed);
			this.value = value;
		}

		@Override
		public Value value() {
			return value;
		}

	}

	private static final Map<String, Deque<Object>> defaultFacets;
	private static final Document defaultConstrainingFacetsDocument;
	private static final SequenceParser parser = new SequenceParser()
			.requiredAttributes(AttrParser.VALUE)
			.optionalAttributes(AttrParser.ID, AttrParser.FIXED)
			.elements(0, 1, TagParser.ANNOTATION);

	static {
		defaultConstrainingFacetsDocument = NodeHelper.newDocument();
		final Map<String, Deque<Object>> df = new HashMap<>();
		final Constructible preserve = s -> new WhiteSpace(s, createSynthetic(Names.WHITE_SPACE, WhiteSpace.Value.PRESERVE), Deques.emptyDeque(), null, WhiteSpace.Value.PRESERVE);
		final Constructible replace = s -> new WhiteSpace(s, createSynthetic(Names.WHITE_SPACE, WhiteSpace.Value.REPLACE), Deques.emptyDeque(), null, WhiteSpace.Value.REPLACE);
		final Constructible collapse = s -> new WhiteSpace(s, createSynthetic(Names.WHITE_SPACE, WhiteSpace.Value.COLLAPSE), Deques.emptyDeque(), null, WhiteSpace.Value.COLLAPSE);
		final Constructible collapseFixed = WhiteSpace.collapseFixed;
		final Constructible optional = s -> new ExplicitTimezone(s, createSynthetic(Names.EXPLICIT_TIMEZONE, ExplicitTimezone.Value.OPTIONAL), Deques.emptyDeque(), null, ExplicitTimezone.Value.OPTIONAL);
		// Primitive
		df.put(SimpleType.STRING_NAME, asDeque(preserve, Length.class, MinLength.class, MaxLength.class, Pattern.class, Enumeration.class, Assertions.class)); // 3.3.1
		df.put(SimpleType.BOOLEAN_NAME, asDeque(collapseFixed, Pattern.class, Assertions.class)); // 3.3.2
		df.put(SimpleType.DECIMAL_NAME, asDeque(collapseFixed, TotalDigits.class, FractionDigits.class, Pattern.class, Enumeration.class, MaxInclusive.class, MaxExclusive.class, MinInclusive.class, MinExclusive.class, Assertions.class)); // 3.3.3
		df.put(SimpleType.FLOAT_NAME, asDeque(collapseFixed, Pattern.class, Enumeration.class, MaxInclusive.class, MaxExclusive.class, MinInclusive.class, MinExclusive.class, Assertions.class)); // 3.3.4
		df.put(SimpleType.DOUBLE_NAME, asDeque(collapseFixed, Pattern.class, Enumeration.class, MaxInclusive.class, MaxExclusive.class, MinInclusive.class, MinExclusive.class, Assertions.class)); // 3.3.5
		df.put(SimpleType.DURATION_NAME, asDeque(collapseFixed, Pattern.class, Enumeration.class, MaxInclusive.class, MaxExclusive.class, MinInclusive.class, MinExclusive.class, Assertions.class)); // 3.3.6
		df.put(SimpleType.DATETIME_NAME, asDeque(collapseFixed, optional, Pattern.class, Enumeration.class, MaxInclusive.class, MaxExclusive.class, MinInclusive.class, MinExclusive.class, Assertions.class)); // 3.3.7
		df.put(SimpleType.TIME_NAME, asDeque(collapseFixed, optional, Pattern.class, Enumeration.class, MaxInclusive.class, MaxExclusive.class, MinInclusive.class, MinExclusive.class, Assertions.class)); // 3.3.8
		df.put(SimpleType.DATE_NAME, asDeque(collapseFixed, optional, Pattern.class, Enumeration.class, MaxInclusive.class, MaxExclusive.class, MinInclusive.class, MinExclusive.class, Assertions.class)); // 3.3.9
		df.put(SimpleType.GYEARMONTH_NAME, asDeque(collapseFixed, optional, Pattern.class, Enumeration.class, MaxInclusive.class, MaxExclusive.class, MinInclusive.class, MinExclusive.class, Assertions.class)); // 3.3.10
		df.put(SimpleType.GYEAR_NAME, asDeque(collapseFixed, optional, Pattern.class, Enumeration.class, MaxInclusive.class, MaxExclusive.class, MinInclusive.class, MinExclusive.class, Assertions.class)); // 3.3.11
		df.put(SimpleType.GMONTHDAY_NAME, asDeque(collapseFixed, optional, Pattern.class, Enumeration.class, MaxInclusive.class, MaxExclusive.class, MinInclusive.class, MinExclusive.class, Assertions.class)); // 3.3.12
		df.put(SimpleType.GDAY_NAME, asDeque(collapseFixed, optional, Pattern.class, Enumeration.class, MaxInclusive.class, MaxExclusive.class, MinInclusive.class, MinExclusive.class, Assertions.class)); // 3.3.13
		df.put(SimpleType.GMONTH_NAME, asDeque(collapseFixed, optional, Pattern.class, Enumeration.class, MaxInclusive.class, MaxExclusive.class, MinInclusive.class, MinExclusive.class, Assertions.class)); // 3.3.14
		df.put(SimpleType.HEXBINARY_NAME, asDeque(collapseFixed, Length.class, MinLength.class, MaxLength.class, Pattern.class, Enumeration.class, Assertions.class)); // 3.3.15
		df.put(SimpleType.BASE64BINARY_NAME, asDeque(collapseFixed, Length.class, MinLength.class, MaxLength.class, Pattern.class, Enumeration.class, Assertions.class)); // 3.3.16
		df.put(SimpleType.ANYURI_NAME, asDeque(collapseFixed, Length.class, MinLength.class, MaxLength.class, Pattern.class, Enumeration.class, Assertions.class)); // 3.3.17
		df.put(SimpleType.QNAME_NAME, asDeque(collapseFixed, Length.class, MinLength.class, MaxLength.class, Pattern.class, Enumeration.class, Assertions.class)); // 3.3.18
		df.put(SimpleType.NOTATION_NAME, asDeque(collapseFixed, Length.class, MinLength.class, MaxLength.class, Pattern.class, Enumeration.class, Assertions.class)); // 3.3.19
		// Built-in
		df.put(SimpleType.NORMALIZEDSTRING_NAME, asDeque(replace, Length.class, MinLength.class, MaxLength.class, Pattern.class, Enumeration.class, Assertions.class)); // 3.4.1
		df.put(SimpleType.TOKEN_NAME, asDeque(collapse, Length.class, MinLength.class, MaxLength.class, Pattern.class, Enumeration.class, Assertions.class)); // 3.4.2
		final String languagePatternValue = "[a-zA-Z]{1,8}(-[a-zA-Z0-9]{1,8})*";
		final Constructible languagePattern = s -> new Pattern(s, createSynthetic(Names.PATTERN, languagePatternValue), Deques.emptyDeque(), Collections.singleton(languagePatternValue));
		df.put(SimpleType.LANGUAGE_NAME, asDeque(languagePattern, collapse, Length.class, MinLength.class, MaxLength.class, Enumeration.class, Assertions.class)); // 3.4.3
		final String nmTokenPatternValue = "\\c+";
		final Constructible nmTokenPattern = s -> new Pattern(s, createSynthetic(Names.PATTERN, nmTokenPatternValue), Deques.emptyDeque(), Collections.singleton(nmTokenPatternValue));
		df.put(SimpleType.NMTOKEN_NAME, asDeque(nmTokenPattern, collapse, Length.class, MinLength.class, MaxLength.class, Enumeration.class, Assertions.class)); // 3.4.4
		final Constructible minLength1 = s -> new MinLength(s, createSynthetic(Names.MIN_LENGTH, 1), Deques.emptyDeque(), null, BigInteger.ONE);
		df.put(SimpleType.NMTOKENS_NAME, asDeque(minLength1, collapse, Length.class, MaxLength.class, Enumeration.class, Pattern.class, Assertions.class)); // 3.4.5
		final String namePatternValue = "\\i\\c*";
		final Constructible namePattern = s -> new Pattern(s, createSynthetic(Names.PATTERN, namePatternValue), Deques.emptyDeque(), Collections.singleton(namePatternValue));
		df.put(SimpleType.NAME_NAME, asDeque(namePattern, collapse, Length.class, MinLength.class, MaxLength.class, Enumeration.class, Assertions.class)); // 3.4.6
		final Set<String> ncNamePatternValue = new HashSet<>();
		ncNamePatternValue.add(namePatternValue);
		ncNamePatternValue.add("[\\i-[:]][\\c-[:]]*");
		final Constructible ncNamePattern = s -> new Pattern(s, createSynthetic(Names.PATTERN, ncNamePatternValue), Deques.emptyDeque(), ncNamePatternValue);
		df.put(SimpleType.NCNAME_NAME, asDeque(ncNamePattern, collapse, Length.class, MinLength.class, MaxLength.class, Enumeration.class, Assertions.class)); // 3.4.7
		df.put(SimpleType.ID_NAME, asDeque(ncNamePattern, collapse, Length.class, MinLength.class, MaxLength.class, Enumeration.class, Assertions.class)); // 3.4.8
		df.put(SimpleType.IDREF_NAME, asDeque(ncNamePattern, collapse, Length.class, MinLength.class, MaxLength.class, Enumeration.class, Assertions.class)); // 3.4.9
		df.put(SimpleType.IDREFS_NAME, asDeque(minLength1, collapse, Length.class, MaxLength.class, Enumeration.class, Pattern.class, Assertions.class)); // 3.4.10
		df.put(SimpleType.ENTITY_NAME, asDeque(ncNamePattern, collapse, Length.class, MinLength.class, MaxLength.class, Enumeration.class, Assertions.class)); // 3.4.11
		df.put(SimpleType.ENTITIES_NAME, asDeque(minLength1, collapse, Length.class, MaxLength.class, Enumeration.class, Pattern.class, Assertions.class)); // 3.4.12
		final Constructible fractionDigits0Fixed = s -> new FractionDigits(s, createSynthetic(Names.FRACTION_DIGITS, BigInteger.ZERO), Deques.emptyDeque(), true, BigInteger.ZERO);
		final String integerPatternValue = "[\\-+]?[0-9]+";
		final Constructible integerPattern = s -> new Pattern(s, createSynthetic(Names.PATTERN, integerPatternValue), Deques.emptyDeque(), Collections.singleton(integerPatternValue));
		df.put(SimpleType.INTEGER_NAME, asDeque(fractionDigits0Fixed, collapseFixed, integerPattern, TotalDigits.class, Enumeration.class, MaxInclusive.class, MaxExclusive.class, MinInclusive.class, MinExclusive.class, Assertions.class)); // 3.4.13
		final Constructible maxInclusive0 = s -> new MaxInclusive(s, createSynthetic(Names.MAX_INCLUSIVE, BigInteger.ZERO), Deques.emptyDeque(), null, () -> BigInteger.ZERO);
		df.put(SimpleType.NONPOSITIVEINTEGER_NAME, asDeque(fractionDigits0Fixed, collapseFixed, integerPattern, maxInclusive0, TotalDigits.class, Enumeration.class, MaxExclusive.class, MinInclusive.class, MinExclusive.class, Assertions.class)); // 3.4.14
		final BigInteger neg1 = BigInteger.ONE.negate();
		final Constructible maxInclusiveNeg1 = s -> new MaxInclusive(s, createSynthetic(Names.MAX_INCLUSIVE, neg1), Deques.emptyDeque(), null, () -> neg1);
		df.put(SimpleType.NEGATIVEINTEGER_NAME, asDeque(fractionDigits0Fixed, collapseFixed, integerPattern, maxInclusiveNeg1, TotalDigits.class, Enumeration.class, MaxExclusive.class, MinInclusive.class, MinExclusive.class, Assertions.class)); // 3.4.15
		final BigInteger longMaxInclusiveValue = BigInteger.valueOf(Long.MAX_VALUE);
		final Constructible longMaxInclusive = s -> new MaxInclusive(s, createSynthetic(Names.MAX_INCLUSIVE, longMaxInclusiveValue), Deques.emptyDeque(), null, () -> longMaxInclusiveValue);
		final BigInteger longMinInclusiveValue = BigInteger.valueOf(Long.MIN_VALUE);
		final Constructible longMinInclusive = s -> new MinInclusive(s, createSynthetic(Names.MIN_INCLUSIVE, longMinInclusiveValue), Deques.emptyDeque(), null, () -> longMinInclusiveValue);
		df.put(SimpleType.LONG_NAME, asDeque(fractionDigits0Fixed, collapseFixed, integerPattern, longMaxInclusive, longMinInclusive, TotalDigits.class, Enumeration.class, MaxExclusive.class, MinExclusive.class, Assertions.class)); // 3.4.16
		final BigInteger intMaxInclusiveValue = BigInteger.valueOf(Integer.MAX_VALUE);
		final Constructible intMaxInclusive = s -> new MaxInclusive(s, createSynthetic(Names.MAX_INCLUSIVE, intMaxInclusiveValue), Deques.emptyDeque(), null, () -> intMaxInclusiveValue);
		final BigInteger intMinInclusiveValue = BigInteger.valueOf(Integer.MIN_VALUE);
		final Constructible intMinInclusive = s -> new MinInclusive(s, createSynthetic(Names.MIN_INCLUSIVE, intMinInclusiveValue), Deques.emptyDeque(), null, () -> intMinInclusiveValue);
		df.put(SimpleType.INT_NAME, asDeque(fractionDigits0Fixed, collapseFixed, integerPattern, intMaxInclusive, intMinInclusive, TotalDigits.class, Enumeration.class, MaxExclusive.class, MinExclusive.class, Assertions.class)); // 3.4.17
		final BigInteger shortMaxInclusiveValue = BigInteger.valueOf(Short.MAX_VALUE);
		final Constructible shortMaxInclusive = s -> new MaxInclusive(s, createSynthetic(Names.MAX_INCLUSIVE, shortMaxInclusiveValue), Deques.emptyDeque(), null, () -> shortMaxInclusiveValue);
		final BigInteger shortMinInclusiveValue = BigInteger.valueOf(Short.MIN_VALUE);
		final Constructible shortMinInclusive = s -> new MinInclusive(s, createSynthetic(Names.MIN_INCLUSIVE, shortMinInclusiveValue), Deques.emptyDeque(), null, () -> shortMinInclusiveValue);
		df.put(SimpleType.SHORT_NAME, asDeque(fractionDigits0Fixed, collapseFixed, integerPattern, shortMaxInclusive, shortMinInclusive, TotalDigits.class, Enumeration.class, MaxExclusive.class, MinExclusive.class, Assertions.class)); // 3.4.18
		final BigInteger byteMaxInclusiveValue = BigInteger.valueOf(Byte.MAX_VALUE);
		final Constructible byteMaxInclusive = s -> new MaxInclusive(s, createSynthetic(Names.MAX_INCLUSIVE, byteMaxInclusiveValue), Deques.emptyDeque(), null, () -> byteMaxInclusiveValue);
		final BigInteger byteMinInclusiveValue = BigInteger.valueOf(Byte.MIN_VALUE);
		final Constructible byteMinInclusive = s -> new MinInclusive(s, createSynthetic(Names.MIN_INCLUSIVE, byteMinInclusiveValue), Deques.emptyDeque(), null, () -> byteMinInclusiveValue);
		df.put(SimpleType.BYTE_NAME, asDeque(fractionDigits0Fixed, collapseFixed, integerPattern, byteMaxInclusive, byteMinInclusive, TotalDigits.class, Enumeration.class, MaxExclusive.class, MinExclusive.class, Assertions.class)); // 3.4.19
		final Constructible minInclusive0 = s -> new MinInclusive(s, createSynthetic(Names.MIN_INCLUSIVE, BigInteger.ZERO), Deques.emptyDeque(), null, () -> BigInteger.ZERO);
		df.put(SimpleType.NONNEGATIVEINTEGER_NAME, asDeque(fractionDigits0Fixed, collapseFixed, integerPattern, minInclusive0, TotalDigits.class, Enumeration.class, MaxInclusive.class, MaxExclusive.class, MinExclusive.class, Assertions.class)); // 3.4.20
		final BigInteger unsignedLongMaxInclusiveValue = new BigInteger(Long.toUnsignedString(-1));
		final Constructible unsignedLongMaxInclusive = s -> new MaxInclusive(s, createSynthetic(Names.MAX_INCLUSIVE, unsignedLongMaxInclusiveValue), Deques.emptyDeque(), null, () -> unsignedLongMaxInclusiveValue);
		df.put(SimpleType.UNSIGNEDLONG_NAME, asDeque(fractionDigits0Fixed, collapseFixed, integerPattern, unsignedLongMaxInclusive, minInclusive0, TotalDigits.class, Enumeration.class, MaxExclusive.class, MinExclusive.class, Assertions.class)); // 3.4.21
		final BigInteger unsignedIntMaxInclusiveValue = new BigInteger(Integer.toUnsignedString(-1));
		final Constructible unsignedIntMaxInclusive = s -> new MaxInclusive(s, createSynthetic(Names.MAX_INCLUSIVE, unsignedIntMaxInclusiveValue), Deques.emptyDeque(), null, () -> unsignedIntMaxInclusiveValue);
		df.put(SimpleType.UNSIGNEDINT_NAME, asDeque(fractionDigits0Fixed, collapseFixed, integerPattern, unsignedIntMaxInclusive, minInclusive0, TotalDigits.class, Enumeration.class, MaxExclusive.class, MinExclusive.class, Assertions.class)); // 3.4.22
		final BigInteger unsignedShortMaxInclusiveValue = BigInteger.valueOf(65535);
		final Constructible unsignedShortMaxInclusive = s -> new MaxInclusive(s, createSynthetic(Names.MAX_INCLUSIVE, unsignedShortMaxInclusiveValue), Deques.emptyDeque(), null, () -> unsignedShortMaxInclusiveValue);
		df.put(SimpleType.UNSIGNEDSHORT_NAME, asDeque(fractionDigits0Fixed, collapseFixed, integerPattern, unsignedShortMaxInclusive, minInclusive0, TotalDigits.class, Enumeration.class, MaxExclusive.class, MinExclusive.class, Assertions.class)); // 3.4.23
		final BigInteger unsignedByteMaxInclusiveValue = BigInteger.valueOf(255);
		final Constructible unsignedByteMaxInclusive = s -> new MaxInclusive(s, createSynthetic(Names.MAX_INCLUSIVE, unsignedByteMaxInclusiveValue), Deques.emptyDeque(), null, () -> unsignedByteMaxInclusiveValue);
		df.put(SimpleType.UNSIGNEDBYTE_NAME, asDeque(fractionDigits0Fixed, collapseFixed, integerPattern, unsignedByteMaxInclusive, minInclusive0, TotalDigits.class, Enumeration.class, MaxExclusive.class, MinExclusive.class, Assertions.class)); // 3.4.24
		final Constructible minInclusive1 = s -> new MinInclusive(s, createSynthetic(Names.MIN_INCLUSIVE, BigInteger.ONE), Deques.emptyDeque(), null, () -> BigInteger.ONE);
		df.put(SimpleType.POSITIVEINTEGER_NAME, asDeque(fractionDigits0Fixed, collapseFixed, integerPattern, minInclusive1, TotalDigits.class, Enumeration.class, MaxInclusive.class, MaxExclusive.class, MinExclusive.class, Assertions.class)); // 3.4.25
		final String yearMonthDurationPatternValue = "[^DT]*";
		final Constructible yearMonthDurationPattern = s -> new Pattern(s, createSynthetic(Names.PATTERN, yearMonthDurationPatternValue), Deques.emptyDeque(), Collections.singleton(yearMonthDurationPatternValue));
		df.put(SimpleType.YEARMONTHDURATION_NAME, asDeque(collapseFixed, yearMonthDurationPattern, Enumeration.class, MaxInclusive.class, MaxExclusive.class, MinInclusive.class, MinExclusive.class, Assertions.class)); // 3.4.26
		final String dayTimeDurationPatternValue = "[^YM]*(T.*)?";
		final Constructible dayTimeDurationPattern = s -> new Pattern(s, createSynthetic(Names.PATTERN, dayTimeDurationPatternValue), Deques.emptyDeque(), Collections.singleton(dayTimeDurationPatternValue));
		df.put(SimpleType.DAYTIMEDURATION_NAME, asDeque(collapseFixed, dayTimeDurationPattern, Enumeration.class, MaxInclusive.class, MaxExclusive.class, MinInclusive.class, MinExclusive.class, Assertions.class)); // 3.4.27
		final Constructible dateTimeStampETZFixed = s -> new ExplicitTimezone(s, createSynthetic(Names.EXPLICIT_TIMEZONE, ExplicitTimezone.Value.REQUIRED), Deques.emptyDeque(), true, ExplicitTimezone.Value.REQUIRED);
		df.put(SimpleType.DATETIMESTAMP_NAME, asDeque(collapseFixed, dateTimeStampETZFixed, Pattern.class, Enumeration.class, MaxInclusive.class, MaxExclusive.class, MinInclusive.class, MinExclusive.class, Assertions.class)); // 3.4.28
		df.forEach((k, v) -> {
			if (v.stream().distinct().count() != v.size()) {
				throw new AssertionError(v.toString());
			}
		});
		defaultFacets = Collections.unmodifiableMap(df);
	}

	private final Deferred<SimpleType> context;
	private final Node node;
	private final Boolean fixed;
	private final Deque<Annotation> annotations;

	private ConstrainingFacet(final Deferred<SimpleType> context, final Node node, final Deque<Annotation> annotations, final Boolean fixed) {
		this.context = Objects.requireNonNull(context);
		this.node = Objects.requireNonNull(node);
		this.annotations = Objects.requireNonNull(annotations);
		this.fixed = fixed;
	}

	private static Deferred<Number> getAttrValueAsNumber(final Deferred<SimpleType> context, final Attr attr) {
		final String value = NodeHelper.collapseWhitespace(attr.getValue());
		return context.map(s -> {
			if (SimpleType.xsDouble() == s.primitiveTypeDefinition() || SimpleType.xsFloat() == s.primitiveTypeDefinition()) {
				return new BigDecimal(value);
			} else if (SimpleType.xsDecimal() == s.primitiveTypeDefinition()) {
				TypeDefinition base = s.baseTypeDefinition();
				while (ComplexType.xsAnyType() != base) {
					if (SimpleType.xsInteger() == base) {
						return new BigInteger(value);
					} else if (SimpleType.xsDecimal() == base) {
						return new BigDecimal(value);
					}
					base = base.baseTypeDefinition();
				}
			}
			throw new AssertionError();
		});
	}

	private static ConstrainingFacet parse(final Result result) {
		final Node node = result.node();
		final Deferred<SimpleType> context = result.context();
		final Deque<Annotation> annotations = Annotation.of(result).resolve(node);
		final Boolean fixed = Boolean.valueOf(result.value(AttrParser.FIXED));
		final Attr value = result.attr(AttrParser.VALUE);
		switch (node.getLocalName()) {
		case Names.LENGTH:
			return new Length(context, node, annotations, fixed, NodeHelper.getAttrValueAsNonNegativeInteger(value));
		case Names.MIN_LENGTH:
			return new MinLength(context, node, annotations, fixed, NodeHelper.getAttrValueAsNonNegativeInteger(value));
		case Names.MAX_LENGTH:
			return new MaxLength(context, node, annotations, fixed, NodeHelper.getAttrValueAsNonNegativeInteger(value));
		case Names.PATTERN:
			return new Pattern(context, node, annotations, Collections.singleton(NodeHelper.getAttrValueAsString(value)));
		case Names.ENUMERATION:
			return new Enumeration(context, node, annotations, Collections.singleton(NodeHelper.getAttrValueAsString(value)));
		case Names.WHITE_SPACE:
			return new WhiteSpace(context, node, annotations, fixed, WhiteSpace.Value.getAttrValueAsWhiteSpace(value));
		case Names.MAX_INCLUSIVE:
			return new MaxInclusive(context, node, annotations, fixed, getAttrValueAsNumber(context, value));
		case Names.MAX_EXCLUSIVE:
			return new MaxExclusive(context, node, annotations, fixed, getAttrValueAsNumber(context, value));
		case Names.MIN_EXCLUSIVE:
			return new MinExclusive(context, node, annotations, fixed, getAttrValueAsNumber(context, value));
		case Names.MIN_INCLUSIVE:
			return new MinInclusive(context, node, annotations, fixed, getAttrValueAsNumber(context, value));
		case Names.TOTAL_DIGITS:
			return new TotalDigits(context, node, annotations, fixed, NodeHelper.getAttrValueAsPositiveInteger(value));
		case Names.FRACTION_DIGITS:
			return new FractionDigits(context, node, annotations, fixed, NodeHelper.getAttrValueAsNonNegativeInteger(value));
		case Names.ASSERTION:
			return new Assertions(context, node, Deques.singletonDeque(TagParser.ASSERTION.parse(result)));
		case Names.EXPLICIT_TIMEZONE:
			return new ExplicitTimezone(context, node, annotations, fixed, ExplicitTimezone.Value.getAttrValueAsExplicitTimezone(value));
		default:
			throw new Schema.ParseException(node, "Unknown constraining facet " + node.getLocalName());
		}
	}

	private static Node createSynthetic(final String name, final Object value) {
		final Node elem = defaultConstrainingFacetsDocument.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, name);
		elem.setPrefix("xs");
		final Attr valueAttr = defaultConstrainingFacetsDocument.createAttributeNS(null, "value");
		valueAttr.setNodeValue(value.toString());
		elem.getAttributes().setNamedItem(valueAttr);
		return elem;
	}

	static void register() {
		AttrParser.register(AttrParser.Names.VALUE, NodeHelper::getAttrValueAsString);
		TagParser.register(Names.LENGTH, parser, Length.class, ConstrainingFacet::parse);
		TagParser.register(Names.MAX_LENGTH, parser, MaxLength.class, ConstrainingFacet::parse);
		TagParser.register(Names.MIN_LENGTH, parser, MinLength.class, ConstrainingFacet::parse);
		TagParser.register(Names.PATTERN, parser, Pattern.class, ConstrainingFacet::parse);
		TagParser.register(Names.ENUMERATION, parser, Enumeration.class, ConstrainingFacet::parse);
		TagParser.register(Names.WHITE_SPACE, parser, WhiteSpace.class, ConstrainingFacet::parse);
		TagParser.register(Names.MAX_INCLUSIVE, parser, MaxInclusive.class, ConstrainingFacet::parse);
		TagParser.register(Names.MAX_EXCLUSIVE, parser, MaxExclusive.class, ConstrainingFacet::parse);
		TagParser.register(Names.MIN_EXCLUSIVE, parser, MinExclusive.class, ConstrainingFacet::parse);
		TagParser.register(Names.MIN_INCLUSIVE, parser, MinInclusive.class, ConstrainingFacet::parse);
		TagParser.register(Names.TOTAL_DIGITS, parser, TotalDigits.class, ConstrainingFacet::parse);
		TagParser.register(Names.FRACTION_DIGITS, parser, FractionDigits.class, ConstrainingFacet::parse);
		TagParser.register(Names.ASSERTION, parser, Assertions.class, ConstrainingFacet::parse);
		TagParser.register(Names.EXPLICIT_TIMEZONE, parser, ExplicitTimezone.class, ConstrainingFacet::parse);
		VisitorHelper.register(ConstrainingFacet.class, ConstrainingFacet::visit);
	}

	static Deque<Object> find(final Deferred<SimpleType> simpleType, final String simpleTypeName) {
		return simpleType.mapToDeque(s -> defaultFacets.get(simpleTypeName).stream().map(o -> o instanceof Constructible ? ((Constructible) o).apply(simpleType) : o).collect(Collectors.toCollection(ArrayDeque::new)));
	}

	static Deque<Object> combineLikeFacets(final SimpleType baseType, final Deque<Object> baseFacets, final Deque<? extends ConstrainingFacet> declaredFacets) {
		final Deque<Object> newFacets = new DeferredArrayDeque<>();
		boolean patternFound = false;
		boolean enumerationFound = false;
		boolean assertionFound = false;
		for (final ConstrainingFacet facet : declaredFacets) {
			if (facet instanceof Pattern) {
				if (!patternFound) {
					patternFound = true;
					final Deque<Annotation> annotations = new ArrayDeque<>();
					final StringBuilder value = new StringBuilder();
					for (final ConstrainingFacet c : declaredFacets) {
						if (c instanceof Pattern) {
							for (final Annotation a : c.annotations()) {
								annotations.addFirst(a);
							}
							for (final String s : ((Pattern) c).value()) {
								if (value.length() > 0) {
									value.append("|");
								}
								value.append(s);
							}
						}
					}
					final Optional<Pattern> basePattern = baseType.facets().stream().filter(Pattern.class::isInstance).map(Pattern.class::cast).findAny();
					final Pattern first;
					if (basePattern.isPresent()) {
						final Deque<Annotation> firstAnnotations = new ArrayDeque<>(basePattern.get().annotations().size() + annotations.size());
						first = new Pattern(facet.context, facet.node, firstAnnotations, new LinkedHashSet<>(basePattern.get().value()));
						firstAnnotations.addAll(basePattern.get().annotations());
						firstAnnotations.addAll(annotations);
						first.value.add(value.toString());
					} else {
						first = new Pattern(facet.context, facet.node, annotations, Collections.singleton(value.toString()));
					}
					newFacets.addLast(first);
				}
			} else if (facet instanceof Enumeration) {
				if (!enumerationFound) {
					enumerationFound = true;
					final Deque<Annotation> annotations = new ArrayDeque<>();
					final Set<String> value = new LinkedHashSet<>();
					for (final ConstrainingFacet c : declaredFacets) {
						if (c instanceof Enumeration) {
							annotations.addAll(c.annotations());
							for (final String v : ((Enumeration) c).value()) {
								value.add(baseType.lexicalMapping(v));
							}
						}
					}
					newFacets.addLast(new Enumeration(facet.context, facet.node, annotations, value));
				}
			} else if (facet instanceof Assertions) {
				if (!assertionFound) {
					assertionFound = true;
					final Deque<Assertion> value = new ArrayDeque<>();
					for (final Deque<? extends ConstrainingFacet> x : Deques.asDeque(baseType.facets(), declaredFacets)) {
						for (final ConstrainingFacet c : x) {
							if (c instanceof Assertions) {
								value.addAll(((Assertions) c).value());
							}
						}
					}
					newFacets.addLast(new Assertions(facet.context, facet.node, value));
				}
			} else {
				newFacets.addLast(facet);
			}
		}
		final Set<Object> foundBaseFacets = new HashSet<>();
		// Add the declared facets that override any of the base facets
		for (final ConstrainingFacet declaredFacet : declaredFacets) {
			if (declaredFacet instanceof Pattern || declaredFacet instanceof Enumeration || declaredFacet instanceof Assertions) {
				// Do nothing; handled above
				continue;
			}
			boolean found = false;
			final Iterator<Object> iter = baseFacets.iterator();
			while (iter.hasNext() && !found) {
				final Object obj = iter.next();
				if (obj instanceof Class) {
					final Class<?> facetClass = (Class<?>) obj;
					if (facetClass.isInstance(declaredFacet)) {
						found = true;
						foundBaseFacets.add(obj);
					}
				} else {
					final ConstrainingFacet sibling = (ConstrainingFacet) obj;
					if (sibling.getClass().equals(declaredFacet.getClass())) {
						if (Boolean.TRUE == sibling.fixed() && !Objects.equals(sibling.value(), declaredFacet.value())) {
							throw new Schema.ParseException(declaredFacet.node, "Cannot modify fixed constraining facet");
						}
						found = true;
						foundBaseFacets.add(obj);
					}
				}
			}
			if (!found) {
				throw new Schema.ParseException(declaredFacet.node.getParentNode(), "Constraining facet " + declaredFacet.getClass().getSimpleName() + " is not allowed for the datatype in this context");
			} else if (!newFacets.contains(declaredFacet)) {
				newFacets.addLast(declaredFacet);
			}
		}
		final Iterator<Object> baseFacetsDescIter = baseFacets.descendingIterator();
		// Add the remaining base facets that were not overridden by the declared facets
		while (baseFacetsDescIter.hasNext()) {
			final Object baseFacet = baseFacetsDescIter.next();
			if (Stream.concat(foundBaseFacets.stream(), newFacets.stream())
					.noneMatch(f -> baseFacet.getClass().equals(f instanceof Class ? (Class<?>) f : f.getClass()))) {
				newFacets.addFirst(baseFacet);
			}
		}
		return newFacets;
	}

	void visit(final Visitor visitor) {
		if (visitor.visit(context.get(), node, this)) {
			visitor.onConstrainingFacet(context.get(), (org.w3c.dom.Element) node.cloneNode(true), this);
			annotations.forEach(a -> a.visit(visitor));
		}
	}

	/** @return The actual value of the value [attribute] */
	public abstract Object value();

	/** @return The actual value of the fixed [attribute], if present, otherwise false or ·absent· */
	public Boolean fixed() {
		return fixed;
	}

	/** @return The annotation mapping of the element, as defined in section XML Representation of Annotation Schema Components of [XSD 1.1 Part 1: Structures]. */
	@Override
	public Deque<Annotation> annotations() {
		return Deques.unmodifiableDeque(annotations);
	}

}
