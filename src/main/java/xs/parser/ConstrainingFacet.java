package xs.parser;

import static xs.parser.internal.util.Deques.*;

import java.util.*;
import java.util.stream.*;
import javax.xml.*;
import org.w3c.dom.*;
import xs.parser.internal.*;
import xs.parser.internal.util.*;
import xs.parser.internal.util.SequenceParser.*;

public abstract class ConstrainingFacet implements AnnotatedComponent {

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

		private static final String NAME = "length";

		private final String value;

		Length(final Node node, final Deque<Annotation> annotations, final Boolean fixed, final String value) {
			super(node, annotations, fixed);
			this.value = value;
		}

		@Override
		public String value() {
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

		private static final String NAME = "minLength";

		private final String value;

		MinLength(final Node node, final Deque<Annotation> annotations, final Boolean fixed, final String value) {
			super(node, annotations, fixed);
			this.value = value;
		}

		@Override
		public String value() {
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

		private static final String NAME = "maxLength";

		private final String value;

		MaxLength(final Node node, final Deque<Annotation> annotations, final Boolean fixed, final String value) {
			super(node, annotations, fixed);
			this.value = value;
		}

		@Override
		public String value() {
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

		private static final String NAME = "pattern";

		private final Set<String> value;

		Pattern(final Node node, final Deque<Annotation> annotations, final Set<String> value) {
			super(node, annotations, null);
			this.value = Objects.requireNonNull(value);
		}

		/**
		 * @return [Definition:]  Let R be a regular expression given by the appropriate case among the following:
		 * <br>1 If there is only one &lt;pattern&gt; among the [children] of a &lt;restriction&gt;, then the actual value of its value [attribute]
		 * <br>2 otherwise the concatenation of the actual values of all the &lt;pattern&gt; [children]'s value [attributes], in order, separated by '|', so forming a single regular expression with multiple ·branches·.
		 * <br>The value is then given by the appropriate case among the following:
		 * <br>1 If the {base type definition} of the ·owner· has a pattern facet among its {facets}, then the union of that pattern facet's {value} and {·R·}
		 * <br>2 otherwise just {·R·}
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

		private static final String NAME = "enumeration";

		private final Set<String> value;

		Enumeration(final Node node, final Deque<Annotation> annotations, final Set<String> value) {
			super(node, annotations, null);
			this.value = Objects.requireNonNull(value);
		}

		/**
		 * @return * The appropriate case among the following:
		 * <br>1 If there is only one &lt;enumeration&gt; among the [children] of a &lt;restriction&gt;, then a set with one member, the actual value of its value [attribute], interpreted as an instance of the {base type definition}.
		 * <br>2 otherwise a set of the actual values of all the &lt;enumeration&gt; [children]'s value [attributes], interpreted as instances of the {base type definition}.
		 * <br>Note: The value [attribute] is declared as having type ·anySimpleType·, but the {value} property of the enumeration facet must be a member of the {base type definition}. So in mapping from the XML representation to the enumeration component, the actual value is identified by using the ·lexical mapping· of the {base type definition}.
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

		public enum Value {

			PRESERVE("preserve"),
			REPLACE("replace"),
			COLLAPSE("collapse");

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

		private static final String NAME = "whiteSpace";

		private final Value value;

		WhiteSpace(final Node node, final Deque<Annotation> annotations, final Boolean fixed, final Value value) {
			super(node, annotations, fixed);
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

		private static final String NAME = "maxInclusive";

		private final String value;

		MaxInclusive(final Node node, final Deque<Annotation> annotations, final Boolean fixed, final String value) {
			super(node, annotations, fixed);
			this.value = value;
		}

		@Override
		public String value() {
			return value;
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

		private static final String NAME = "maxExclusive";

		private final String value;

		MaxExclusive(final Node node, final Deque<Annotation> annotations, final Boolean fixed, final String value) {
			super(node, annotations, fixed);
			this.value = value;
		}

		@Override
		public String value() {
			return value;
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

		private static final String NAME = "minExclusive";

		private final String value;

		MinExclusive(final Node node, final Deque<Annotation> annotations, final Boolean fixed, final String value) {
			super(node, annotations, fixed);
			this.value = value;
		}

		@Override
		public String value() {
			return value;
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

		private static final String NAME = "minInclusive";

		private final String value;

		MinInclusive(final Node node, final Deque<Annotation> annotations, final Boolean fixed, final String value) {
			super(node, annotations, fixed);
			this.value = value;
		}

		@Override
		public String value() {
			return value;
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

		private static final String NAME = "totalDigits";

		private final String value;

		TotalDigits(final Node node, final Deque<Annotation> annotations, final Boolean fixed, final String value) {
			super(node, annotations, fixed);
			this.value = value;
		}

		@Override
		public String value() {
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

		private static final String NAME = "fractionDigits";

		private final String value;

		FractionDigits(final Node node, final Deque<Annotation> annotations, final Boolean fixed, final String value) {
			super(node, annotations, fixed);
			this.value = value;
		}

		@Override
		public String value() {
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

		private static final String NAME = "assertion";

		private final Deque<Assertion> value;

		Assertions(final Node node, final Deque<Assertion> value) {
			super(node, Deques.emptyDeque(), null);
			this.value = Objects.requireNonNull(value);
		}

		/**
		 * @return A sequence whose members are Assertions drawn from the following sources, in order:
		 * <br>1 If the {base type definition} of the ·owner· has an assertions facet among its {facets}, then the Assertions which appear in the {value} of that assertions facet.
		 * <br>2 Assertions corresponding to the &lt;assertion&gt; element information items among the [children] of &lt;restriction&gt;, if any, in document order. For details of the construction of the Assertion components, see section 3.13.2 of [XSD 1.1 Part 1: Structures].
		 */
		@Override
		public Deque<Assertion> value() {
			return Deques.unmodifiableDeque(value);
		}

		/**
		 * @return The empty sequence.
		 * <br>Note: Annotations specified within an &lt;assertion&gt; element are captured by the individual Assertion component to which it maps.
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

		public enum Value {

			REQUIRED("required"),
			PROHIBITED("prohibited"),
			OPTIONAL("optional");

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

		private static final String NAME = "explicitTimezone";

		private final Value value;

		ExplicitTimezone(final Node node, final Deque<Annotation> annotations, final Boolean fixed, final Value value) {
			super(node, annotations, fixed);
			this.value = value;
		}

		@Override
		public Value value() {
			return value;
		}

	}

	private static final Map<String, Deque<Object>> defaultFacets;
	private static final Document defaultConstrainingFacetsDocument;
	static final SequenceParser parser = new SequenceParser()
			.requiredAttributes(AttributeValue.VALUE)
			.optionalAttributes(AttributeValue.ID, AttributeValue.FIXED)
			.elements(0, 1, ElementValue.ANNOTATION);

	static {
		defaultConstrainingFacetsDocument = NodeHelper.newDocument();
		final Map<String, Deque<Object>> df = new HashMap<>();
		final WhiteSpace preserve = new WhiteSpace(createSynthetic(WhiteSpace.NAME, WhiteSpace.Value.PRESERVE), Deques.emptyDeque(), null, WhiteSpace.Value.PRESERVE);
		final WhiteSpace replace = new WhiteSpace(createSynthetic(WhiteSpace.NAME, WhiteSpace.Value.REPLACE), Deques.emptyDeque(), null, WhiteSpace.Value.REPLACE);
		final WhiteSpace collapse = new WhiteSpace(createSynthetic(WhiteSpace.NAME, WhiteSpace.Value.COLLAPSE), Deques.emptyDeque(), null, WhiteSpace.Value.COLLAPSE);
		final WhiteSpace collapseFixed = new WhiteSpace(createSynthetic(WhiteSpace.NAME, WhiteSpace.Value.COLLAPSE), Deques.emptyDeque(), true, WhiteSpace.Value.COLLAPSE);
		final ExplicitTimezone optional = new ExplicitTimezone(createSynthetic(ExplicitTimezone.NAME, ExplicitTimezone.Value.OPTIONAL), Deques.emptyDeque(), null, ExplicitTimezone.Value.OPTIONAL);
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
		final Pattern languagePattern = new Pattern(createSynthetic(Pattern.NAME, languagePatternValue), Deques.emptyDeque(), Collections.singleton(languagePatternValue));
		df.put(SimpleType.LANGUAGE_NAME, asDeque(languagePattern, collapse, Length.class, MinLength.class, MaxLength.class, Enumeration.class, Assertions.class)); // 3.4.3
		final String nmTokenPatternValue = "\\c+";
		final Pattern nmTokenPattern = new Pattern(createSynthetic(Pattern.NAME, nmTokenPatternValue), Deques.emptyDeque(), Collections.singleton(nmTokenPatternValue));
		df.put(SimpleType.NMTOKEN_NAME, asDeque(nmTokenPattern, collapse, Length.class, MinLength.class, MaxLength.class, Enumeration.class, Assertions.class)); // 3.4.4
		final MinLength minLength1 = new MinLength(createSynthetic(MinLength.NAME, "1"), Deques.emptyDeque(), null, "1");
		df.put(SimpleType.NMTOKENS_NAME, asDeque(minLength1, collapse, Length.class, MaxLength.class, Enumeration.class, Pattern.class, Assertions.class)); // 3.4.5
		final String namePatternValue = "\\i\\c*";
		final Pattern namePattern = new Pattern(createSynthetic(Pattern.NAME, namePatternValue), Deques.emptyDeque(), Collections.singleton(namePatternValue));
		df.put(SimpleType.NAME_NAME, asDeque(namePattern, collapse, Length.class, MinLength.class, MaxLength.class, Enumeration.class, Assertions.class)); // 3.4.6
		final Set<String> ncNamePatternValue = new HashSet<>();
		ncNamePatternValue.add(namePatternValue);
		ncNamePatternValue.add("[\\i-[:]][\\c-[:]]*");
		final Pattern ncNamePattern = new Pattern(createSynthetic(Pattern.NAME, ncNamePatternValue), Deques.emptyDeque(), ncNamePatternValue);
		df.put(SimpleType.NCNAME_NAME, asDeque(ncNamePattern, collapse, Length.class, MinLength.class, MaxLength.class, Enumeration.class, Assertions.class)); // 3.4.7
		df.put(SimpleType.ID_NAME, asDeque(ncNamePattern, collapse, Length.class, MinLength.class, MaxLength.class, Enumeration.class, Assertions.class)); // 3.4.8
		df.put(SimpleType.IDREF_NAME, asDeque(ncNamePattern, collapse, Length.class, MinLength.class, MaxLength.class, Enumeration.class, Assertions.class)); // 3.4.9
		df.put(SimpleType.IDREFS_NAME, asDeque(minLength1, collapse, Length.class, MaxLength.class, Enumeration.class, Pattern.class, Assertions.class)); // 3.4.10
		df.put(SimpleType.ENTITY_NAME, asDeque(ncNamePattern, collapse, Length.class, MinLength.class, MaxLength.class, Enumeration.class, Assertions.class)); // 3.4.11
		df.put(SimpleType.ENTITIES_NAME, asDeque(minLength1, collapse, Length.class, MaxLength.class, Enumeration.class, Pattern.class, Assertions.class)); // 3.4.12
		final FractionDigits fractionDigits0Fixed = new FractionDigits(createSynthetic(FractionDigits.NAME, "0"), Deques.emptyDeque(), true, "0");
		final String integerPatternValue = "[\\-+]?[0-9]+";
		final Pattern integerPattern = new Pattern(createSynthetic(Pattern.NAME, integerPatternValue), Deques.emptyDeque(), Collections.singleton(integerPatternValue));
		df.put(SimpleType.INTEGER_NAME, asDeque(fractionDigits0Fixed, collapseFixed, integerPattern, TotalDigits.class, Enumeration.class, MaxInclusive.class, MaxExclusive.class, MinInclusive.class, MaxExclusive.class, Assertions.class)); // 3.4.13
		final MaxInclusive maxInclusive0 = new MaxInclusive(createSynthetic(MaxInclusive.NAME, "0"), Deques.emptyDeque(), null, "0");
		df.put(SimpleType.NONPOSITIVEINTEGER_NAME, asDeque(fractionDigits0Fixed, collapseFixed, integerPattern, maxInclusive0, TotalDigits.class, Enumeration.class, MaxExclusive.class, MinInclusive.class, MinExclusive.class, Assertions.class)); // 3.4.14
		final MaxInclusive maxInclusiveNeg1 = new MaxInclusive(createSynthetic(MaxInclusive.NAME, "-1"), Deques.emptyDeque(), null, "-1");
		df.put(SimpleType.NEGATIVEINTEGER_NAME, asDeque(fractionDigits0Fixed, collapseFixed, integerPattern, maxInclusiveNeg1, TotalDigits.class, Enumeration.class, MaxExclusive.class, MinInclusive.class, MinExclusive.class, Assertions.class)); // 3.4.15
		final MaxInclusive longMaxInclusive = new MaxInclusive(createSynthetic(MaxInclusive.NAME, Long.toString(Long.MAX_VALUE)), Deques.emptyDeque(), null, Long.toString(Long.MAX_VALUE));
		final MinInclusive longMinInclusive = new MinInclusive(createSynthetic(MinInclusive.NAME, Long.toString(Long.MIN_VALUE)), Deques.emptyDeque(), null, Long.toString(Long.MIN_VALUE));
		df.put(SimpleType.LONG_NAME, asDeque(fractionDigits0Fixed, collapseFixed, integerPattern, longMaxInclusive, longMinInclusive, TotalDigits.class, Enumeration.class, MaxExclusive.class, MinExclusive.class, Assertions.class)); // 3.4.16
		final MaxInclusive intMaxInclusive = new MaxInclusive(createSynthetic(MaxInclusive.NAME, Integer.toString(Integer.MAX_VALUE)), Deques.emptyDeque(), null, Integer.toString(Integer.MAX_VALUE));
		final MinInclusive intMinInclusive = new MinInclusive(createSynthetic(MinInclusive.NAME, Integer.toString(Integer.MIN_VALUE)), Deques.emptyDeque(), null, Integer.toString(Integer.MIN_VALUE));
		df.put(SimpleType.INT_NAME, asDeque(fractionDigits0Fixed, collapseFixed, integerPattern, intMaxInclusive, intMinInclusive, TotalDigits.class, Enumeration.class, MaxExclusive.class, MinExclusive.class, Assertions.class)); // 3.4.17
		final MaxInclusive shortMaxInclusive = new MaxInclusive(createSynthetic(MaxInclusive.NAME, Short.toString(Short.MAX_VALUE)), Deques.emptyDeque(), null, Short.toString(Short.MAX_VALUE));
		final MinInclusive shortMinInclusive = new MinInclusive(createSynthetic(MinInclusive.NAME, Short.toString(Short.MIN_VALUE)), Deques.emptyDeque(), null, Short.toString(Short.MIN_VALUE));
		df.put(SimpleType.SHORT_NAME, asDeque(fractionDigits0Fixed, collapseFixed, integerPattern, shortMaxInclusive, shortMinInclusive, TotalDigits.class, Enumeration.class, MaxExclusive.class, MinExclusive.class, Assertions.class)); // 3.4.18
		final MaxInclusive byteMaxInclusive = new MaxInclusive(createSynthetic(MaxInclusive.NAME, Byte.toString(Byte.MAX_VALUE)), Deques.emptyDeque(), null, Byte.toString(Byte.MAX_VALUE));
		final MinInclusive byteMinInclusive = new MinInclusive(createSynthetic(MinInclusive.NAME, Byte.toString(Byte.MIN_VALUE)), Deques.emptyDeque(), null, Byte.toString(Byte.MIN_VALUE));
		df.put(SimpleType.BYTE_NAME, asDeque(fractionDigits0Fixed, collapseFixed, integerPattern, byteMaxInclusive, byteMinInclusive, TotalDigits.class, Enumeration.class, MaxExclusive.class, MinExclusive.class, Assertions.class)); // 3.4.19
		final MinInclusive minInclusive0 = new MinInclusive(createSynthetic(MinInclusive.NAME, "0"), Deques.emptyDeque(), null, "0");
		df.put(SimpleType.NONNEGATIVEINTEGER_NAME, asDeque(fractionDigits0Fixed, collapseFixed, integerPattern, minInclusive0, TotalDigits.class, Enumeration.class, MaxInclusive.class, MaxExclusive.class, MinExclusive.class, Assertions.class)); // 3.4.20
		final MaxInclusive unsignedLongMaxInclusive = new MaxInclusive(createSynthetic(MaxInclusive.NAME, Long.toUnsignedString(-1L)), Deques.emptyDeque(), null, Long.toUnsignedString(-1L));
		df.put(SimpleType.UNSIGNEDLONG_NAME, asDeque(fractionDigits0Fixed, collapseFixed, integerPattern, unsignedLongMaxInclusive, minInclusive0, TotalDigits.class, Enumeration.class, MaxExclusive.class, MinExclusive.class, Assertions.class)); // 3.4.21
		final MaxInclusive unsignedIntMaxInclusive = new MaxInclusive(createSynthetic(MaxInclusive.NAME, Integer.toUnsignedString(-1)), Deques.emptyDeque(), null, Integer.toUnsignedString(-1));
		df.put(SimpleType.UNSIGNEDINT_NAME, asDeque(fractionDigits0Fixed, collapseFixed, integerPattern, unsignedIntMaxInclusive, minInclusive0, TotalDigits.class, Enumeration.class, MaxExclusive.class, MinExclusive.class, Assertions.class)); // 3.4.22
		final MaxInclusive unsignedShortMaxInclusive = new MaxInclusive(createSynthetic(MaxInclusive.NAME, "65535"), Deques.emptyDeque(), null, "65535");
		df.put(SimpleType.UNSIGNEDSHORT_NAME, asDeque(fractionDigits0Fixed, collapseFixed, integerPattern, unsignedShortMaxInclusive, minInclusive0, TotalDigits.class, Enumeration.class, MaxExclusive.class, MinExclusive.class, Assertions.class)); // 3.4.23
		final MaxInclusive unsignedByteMaxInclusive = new MaxInclusive(createSynthetic(MaxInclusive.NAME, "255"), Deques.emptyDeque(), null, "255");
		df.put(SimpleType.UNSIGNEDBYTE_NAME, asDeque(fractionDigits0Fixed, collapseFixed, integerPattern, unsignedByteMaxInclusive, minInclusive0, TotalDigits.class, Enumeration.class, MaxExclusive.class, MinExclusive.class, Assertions.class)); // 3.4.24
		final MinInclusive minInclusive1 = new MinInclusive(createSynthetic(MinInclusive.NAME, "1"), Deques.emptyDeque(), null, "1");
		df.put(SimpleType.POSITIVEINTEGER_NAME, asDeque(fractionDigits0Fixed, collapseFixed, integerPattern, minInclusive1, TotalDigits.class, Enumeration.class, MaxInclusive.class, MaxExclusive.class, MinExclusive.class, Assertions.class)); // 3.4.25
		final String yearMonthDurationPatternValue = "[^DT]*";
		final Pattern yearMonthDurationPattern = new Pattern(createSynthetic(Pattern.NAME, yearMonthDurationPatternValue), Deques.emptyDeque(), Collections.singleton(yearMonthDurationPatternValue));
		df.put(SimpleType.YEARMONTHDURATION_NAME, asDeque(collapseFixed, yearMonthDurationPattern, Enumeration.class, MaxInclusive.class, MaxExclusive.class, MinInclusive.class, MinExclusive.class, Assertions.class)); // 3.4.26
		final String dayTimeDurationPatternValue = "[^YM]*(T.*)?";
		final Pattern dayTimeDurationPattern = new Pattern(createSynthetic(Pattern.NAME, dayTimeDurationPatternValue), Deques.emptyDeque(), Collections.singleton(dayTimeDurationPatternValue));
		df.put(SimpleType.DAYTIMEDURATION_NAME, asDeque(collapseFixed, dayTimeDurationPattern, Enumeration.class, MaxInclusive.class, MaxExclusive.class, MinInclusive.class, MinExclusive.class, Assertions.class)); // 3.4.27
		final ExplicitTimezone dateTimeStampETZFixed = new ExplicitTimezone(createSynthetic(ExplicitTimezone.NAME, ExplicitTimezone.Value.REQUIRED), Deques.emptyDeque(), true, ExplicitTimezone.Value.REQUIRED);
		df.put(SimpleType.DATETIMESTAMP_NAME, asDeque(collapseFixed, dateTimeStampETZFixed, Pattern.class, Enumeration.class, MaxInclusive.class, MaxExclusive.class, MinInclusive.class, MinExclusive.class, Assertions.class)); // 3.4.28
		defaultFacets = Collections.unmodifiableMap(df);
	}

	private final Node node;
	private final Boolean fixed;
	private final Deque<Annotation> annotations;

	private ConstrainingFacet(final Node node, final Deque<Annotation> annotations, final Boolean fixed) {
		this.node = Objects.requireNonNull(node);
		this.annotations = Objects.requireNonNull(annotations);
		this.fixed = fixed;
	}

	private static Node createSynthetic(final String name, final Object value) {
		final Node elem = defaultConstrainingFacetsDocument.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, name);
		elem.setPrefix("xs");
		final Attr valueAttr = defaultConstrainingFacetsDocument.createAttributeNS(null, "value");
		valueAttr.setNodeValue(value.toString());
		elem.getAttributes().setNamedItem(valueAttr);
		return elem;
	}

	static Deque<Object> find(final String simpleTypeName) {
		return defaultFacets.get(simpleTypeName);
	}

	static Deque<Object> combineLikeFacets(final SimpleType baseType, final Deque<Object> baseFacets, final Deque<? extends ConstrainingFacet> declaredFacets) {
		final Deque<Object> newFacets = new DeferredArrayDeque<>(ConstrainingFacet.class.getClasses().length);
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
						first = new Pattern(facet.node(), firstAnnotations, new LinkedHashSet<>(basePattern.get().value()));
						firstAnnotations.addAll(basePattern.get().annotations());
						firstAnnotations.addAll(annotations);
						first.value.add(value.toString());
					} else {
						first = new Pattern(facet.node(), annotations, Collections.singleton(value.toString()));
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
					newFacets.addLast(new Enumeration(facet.node(), annotations, value));
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
					newFacets.addLast(new Assertions(facet.node(), value));
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
							throw new SchemaParseException(declaredFacet.node(), "Cannot modify constraining facet with fixed value = true");
						}
						found = true;
						foundBaseFacets.add(obj);
					}
				}
			}
			if (!found) {
				throw new SchemaParseException(declaredFacet.node().getParentNode(), "Constraining facet " + declaredFacet.getClass().getSimpleName() + " is not allowed for the datatype in this context");
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

	static ConstrainingFacet parse(final Result result) {
		final Boolean fixed = Boolean.valueOf(result.value(AttributeValue.FIXED));
		final String value = result.value(AttributeValue.VALUE);
		switch (result.node().getLocalName()) {
		case Length.NAME:
			return new Length(result.node(), result.annotations(), fixed, value);
		case MinLength.NAME:
			return new MinLength(result.node(), result.annotations(), fixed, value);
		case MaxLength.NAME:
			return new MaxLength(result.node(), result.annotations(), fixed, value);
		case Pattern.NAME:
			return new Pattern(result.node(), result.annotations(), Collections.singleton(value));
		case Enumeration.NAME:
			return new Enumeration(result.node(), result.annotations(), Collections.singleton(value));
		case WhiteSpace.NAME:
			return new WhiteSpace(result.node(), result.annotations(), fixed, WhiteSpace.Value.getByName(value));
		case MaxInclusive.NAME:
			return new MaxInclusive(result.node(), result.annotations(), fixed, value);
		case MaxExclusive.NAME:
			return new MaxExclusive(result.node(), result.annotations(), fixed, value);
		case MinExclusive.NAME:
			return new MinExclusive(result.node(), result.annotations(), fixed, value);
		case MinInclusive.NAME:
			return new MinInclusive(result.node(), result.annotations(), fixed, value);
		case TotalDigits.NAME:
			return new TotalDigits(result.node(), result.annotations(), fixed, value);
		case FractionDigits.NAME:
			return new FractionDigits(result.node(), result.annotations(), fixed, value);
		case Assertions.NAME:
			return new Assertions(result.node(), Deques.singletonDeque(Assertion.parse(result)));
		case ExplicitTimezone.NAME:
			return new ExplicitTimezone(result.node(), result.annotations(), fixed, ExplicitTimezone.Value.getByName(value));
		default:
			throw new SchemaParseException(result.node(), "Unknown constraining facet " + result.node().getLocalName());
		}
	}

	/** @return The actual value of the value [attribute] */
	public abstract Object value();

	/** @return The actual value of the fixed [attribute], if present, otherwise false or ·absent· */
	public Boolean fixed() {
		return fixed;
	}

	@Override
	public Node node() {
		return node;
	}

	/** @return The annotation mapping of the element, as defined in section XML Representation of Annotation Schema Components of [XSD 1.1 Part 1: Structures]. */
	@Override
	public Deque<Annotation> annotations() {
		return Deques.unmodifiableDeque(annotations);
	}

}
