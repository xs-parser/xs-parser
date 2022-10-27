package xs.parser;

import java.util.*;
import javax.xml.namespace.*;
import org.w3c.dom.*;
import xs.parser.internal.*;
import xs.parser.internal.util.*;
import xs.parser.internal.util.SequenceParser.*;
import xs.parser.v.*;

/**
 * <pre>
 * &lt;any
 *   id = ID
 *   maxOccurs = (nonNegativeInteger | unbounded)  : 1
 *   minOccurs = nonNegativeInteger : 1
 *   namespace = ((##any | ##other) | List of (anyURI | (##targetNamespace | ##local)) )
 *   notNamespace = List of (anyURI | (##targetNamespace | ##local))
 *   notQName = List of (QName | (##defined | ##definedSibling))
 *   processContents = (lax | skip | strict) : strict
 *   {any attributes with non-schema namespace . . .}&gt;
 *   Content: (annotation?)
 * &lt;/any&gt;
 *
 * &lt;anyAttribute
 *   id = ID
 *   namespace = ((##any | ##other) | List of (anyURI | (##targetNamespace | ##local)) )
 *   notNamespace = List of (anyURI | (##targetNamespace | ##local))
 *   notQName = List of (QName | ##defined)
 *   processContents = (lax | skip | strict) : strict
 *   {any attributes with non-schema namespace . . .}&gt;
 *   Content: (annotation?)
 * &lt;/anyAttribute&gt;
 * </pre>
 *
 * <table>
 *   <caption style="font-size: large; text-align: left">Schema Component: Wildcard, a kind of Term</caption>
 *   <thead>
 *     <tr>
 *       <th style="text-align: left">Method</th>
 *       <th style="text-align: left">Property</th>
 *       <th style="text-align: left">Representation</th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <td>{@link Wildcard#annotations()}</td>
 *       <td>{annotations}</td>
 *       <td>A sequence of Annotation components.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link Wildcard#namespaceConstraint()}</td>
 *       <td>{namespace constraint}</td>
 *       <td>A Namespace Constraint property record. Required.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link Wildcard#processContents()}</td>
 *       <td>{process contents}</td>
 *       <td>One of {skip, strict, lax}. Required.</td>
 *     </tr>
 *   </tbody>
 * </table>
 */
public interface Wildcard extends Term {

	public static class Any extends Particle implements Wildcard {

		private static final SequenceParser parser = new SequenceParser()
				.optionalAttributes(AttrParser.ID, AttrParser.MAX_OCCURS, AttrParser.MIN_OCCURS, AttrParser.ANY_NAMESPACE, AttrParser.NOT_NAMESPACE, AttrParser.NOT_QNAME, AttrParser.PROCESS_CONTENTS)
				.elements(0, 1, TagParser.ANNOTATION);

		private final Deferred<? extends AnnotatedComponent> context;
		private final Node node;
		private final AnyAttribute wildcard;

		private Any(final Deferred<? extends AnnotatedComponent> context, final Node node, final Deque<Annotation> annotations, final Number maxOccurs, final Number minOccurs, final String schemaTargetNamespace, final Deque<String> namespace, final Deque<String> notNamespace, final Deque<String> notQName, final ProcessContents processContents, final DeferredValue<? extends Term> self) {
			super(context, node, annotations, maxOccurs, minOccurs, self);
			this.context = Objects.requireNonNull(context);
			this.node = Objects.requireNonNull(node);
			this.wildcard = new AnyAttribute(context, node, annotations, schemaTargetNamespace, namespace, notNamespace, notQName, processContents);
		}

		private Any(final Deferred<? extends AnnotatedComponent> context, final Node node, final Deque<Annotation> annotations, final Number maxOccurs, final Number minOccurs, final AnyAttribute wildcard, final DeferredValue<? extends Term> self) {
			super(context, node, annotations, maxOccurs, minOccurs, self);
			this.context = Objects.requireNonNull(context);
			this.node = Objects.requireNonNull(node);
			this.wildcard = Objects.requireNonNull(wildcard);
		}

		private static Any parse(final Result result) {
			final Deferred<? extends AnnotatedComponent> context = result.context();
			final Node node = result.node();
			final Deque<Annotation> annotations = Annotation.of(result).resolve(node);
			final String schemaTargetNamespace = result.schema().targetNamespace();
			final Deque<String> namespace = result.value(AttrParser.ANY_NAMESPACE);
			final Deque<String> notNamespace = result.value(AttrParser.NOT_NAMESPACE);
			final Deque<String> notQName = result.value(AttrParser.NOT_QNAME);
			final ProcessContents processContents = result.value(AttrParser.PROCESS_CONTENTS);
			final Number maxOccurs = result.value(AttrParser.MAX_OCCURS);
			final Number minOccurs = result.value(AttrParser.MIN_OCCURS);
			final DeferredValue<Any> self = new DeferredValue<>();
			return self.set(new Any(context, node, annotations, maxOccurs, minOccurs, schemaTargetNamespace, namespace, notNamespace, notQName, processContents, self));
		}

		static void register() {
			TagParser.register(TagParser.Names.ANY, parser, Any.class, Any::parse);
			VisitorHelper.register(Any.class, Any::visit);
		}

		Any union(final Any other) {
			final AnyAttribute wc = new AnyAttribute(context, node, wildcard.annotations(), namespaceConstraint().union(other.namespaceConstraint()), processContents());
			final DeferredValue<Any> self = new DeferredValue<>();
			return self.set(new Any(context, node, annotations(), maxOccurs(), minOccurs(), wc, self));
		}

		@Override
		void visit(final Visitor visitor) {
			if (visitor.visit(context.get(), node, this)) {
				visitor.onParticle(context.get(), node, this);
				visitor.onWildcard(context.get(), node, this);
				annotations().forEach(a -> a.visit(visitor));
			}
		}

		@Override
		public NamespaceConstraint namespaceConstraint() {
			return wildcard.namespaceConstraint();
		}

		@Override
		public ProcessContents processContents() {
			return wildcard.processContents();
		}

	}

	public enum ProcessContents {

		LAX("lax"),
		SKIP("skip"),
		STRICT("strict");

		private final String name;

		private ProcessContents(final String name) {
			this.name = name;
		}

		public static ProcessContents getAttrValueAsProcessContents(final Attr attr) {
			final String value = NodeHelper.collapseWhitespace(attr.getValue());
			for (final ProcessContents p : values()) {
				if (p.getName().equals(value)) {
					return p;
				}
			}
			throw new IllegalArgumentException(value);
		}

		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return getName();
		}

	}

	public enum Variety {

		ANY,
		NOT,
		ENUMERATION;

	}

	/**
	 * <table>
	 *   <caption style="font-size: large; text-align: left">Property Record: Namespace Constraint</caption>
	 *   <thead>
	 *     <tr>
	 *       <th style="text-align: left">Method</th>
	 *       <th style="text-align: left">Property</th>
	 *       <th style="text-align: left">Representation</th>
	 *     </tr>
	 *   </thead>
	 *   <tbody>
	 *     <tr>
	 *       <td>{@link NamespaceConstraint#variety()}</td>
	 *       <td>{variety}</td>
	 *       <td>One of {any, enumeration, not}. Required.</td>
	 *     </tr>
	 *     <tr>
	 *       <td>{@link NamespaceConstraint#namespaces()}</td>
	 *       <td>{namespaces}</td>
	 *       <td>A set each of whose members is either an xs:anyURI value or the distinguished value ·absent·. Required.</td>
	 *     </tr>
	 *     <tr>
	 *       <td>{@link NamespaceConstraint#disallowedNames()}</td>
	 *       <td>{disallowed names}</td>
	 *       <td>A set each of whose members is either an xs:QName value or the keyword defined or the keyword sibling. Required.</td>
	 *     </tr>
	 *   </tbody>
	 * </table>
	 */
	public static class NamespaceConstraint {

		static final String DEFINED = "defined";
		static final String SIBLING = "sibling";

		private final Variety variety;
		private final Set<String> namespaces;
		private final Set<Object> disallowedNames;

		NamespaceConstraint(final Variety variety, final Set<String> namespaces, final Set<Object> disallowedNames) {
			this.variety = Objects.requireNonNull(variety);
			this.namespaces = Objects.requireNonNull(namespaces);
			this.disallowedNames = Objects.requireNonNull(disallowedNames);
		}

		/**
		 * <b>Validation Rule: Wildcard allows Expanded Name</b>
		 * <p>For an expanded name E, i.e. a (namespace name, local name) pair, to be ·valid· with respect to a namespace constraint C all of the following must be true:
		 * <br>1 The namespace name is ·valid· with respect to C, as defined in Wildcard allows Namespace Name (§3.10.4.3);
		 * <br>2 C.{disallowed names} does not contain E.
		 * @param name the expanded name
		 * @return {@code true} if the expanded name is allowed
		 */
		private boolean allowsExpandedName(final QName name) {
			return !disallowedNames.contains(name) && allowsNamespaceName(name.getNamespaceURI());
		}

		/**
		 * <b>Validation Rule: Wildcard allows Namespace Name</b>
		 * <p>For a value V which is either a namespace name or ·absent· to be ·valid· with respect to a namespace constraint C (the value of a {namespace constraint}) one of the following must be true:
		 * <br>1 C.{variety} = any.
		 * <br>2 C.{variety} = not, and V is not identical to any of the members of C.{namespaces}.
		 * <br>3 C.{variety} = enumeration, and V is identical to one of the members of C.{namespaces}.
		 * @param namespace the namespace name
		 * @return {@code true} if the namespace name is allowed
		 */
		private boolean allowsNamespaceName(final String namespace) {
			switch (variety) {
			case ANY:
				return true;
			case ENUMERATION:
				return namespaces.contains(namespace);
			case NOT:
				return !namespaces.contains(namespace);
			default:
				throw new AssertionError(variety.toString());
			}
		}

		/**
		 * Returns the union of {@code this} and the {@code other} namespace constraint as described by https://www.w3.org/TR/xmlschema11-1/#sec-cos-aw-union.
		 * @param other the other namespace constraint
		 * @return the union of {@code this} and the {@code other} namespace constraint
		 */
		NamespaceConstraint union(final NamespaceConstraint other) {
			final Variety v;
			final Set<String> ns = new LinkedHashSet<>();
			if (variety.equals(other.variety) && namespaces.equals(other.namespaces)) {
				v = variety;
				ns.addAll(namespaces);
			} else if (Variety.ANY.equals(variety) || Variety.ANY.equals(other.variety)) {
				v = Variety.ANY;
			} else if (Variety.ENUMERATION.equals(variety) && Variety.ENUMERATION.equals(other.variety)) {
				v = Variety.ENUMERATION;
				ns.addAll(namespaces);
				ns.addAll(other.namespaces);
			} else if (Variety.NOT.equals(variety) && Variety.NOT.equals(other.variety)) {
				ns.addAll(namespaces);
				ns.retainAll(other.namespaces);
				v = ns.isEmpty() ? Variety.ANY : Variety.NOT;
			} else {
				final Set<String> s1;
				final Set<String> s2;
				if (Variety.NOT.equals(variety) && Variety.ENUMERATION.equals(other.variety)) {
					s1 = namespaces;
					s2 = other.namespaces;
				} else {
					s1 = other.namespaces;
					s2 = namespaces;
				}
				s1.removeAll(s2);
				v = s1.isEmpty() ? Variety.ANY : Variety.NOT;
				ns.addAll(s1);
			}
			final Set<Object> disallowedNs = new LinkedHashSet<>(disallowedNames);
			disallowedNs.removeIf(n -> !(n instanceof QName) || other.allowsExpandedName((QName) n));
			final Set<Object> otherDisallowedNs = new LinkedHashSet<>(other.disallowedNames);
			otherDisallowedNs.removeIf(n -> !(n instanceof QName) || allowsExpandedName((QName) n));
			disallowedNs.addAll(otherDisallowedNs);
			if (disallowedNames.contains(DEFINED) && other.disallowedNames.contains(DEFINED)) {
				disallowedNs.add(DEFINED);
			}
			return new NamespaceConstraint(v, ns, disallowedNs);
		}

		/** @return the appropriate case among the following:
		 * <br>1 If the namespace [attribute] is present, then the appropriate case among the following:
		 * <br>  1.1 If namespace = "##any", then any;
		 * <br>  1.2 If namespace = "##other", then not;
		 * <br>  1.3 otherwise enumeration;
		 * <br>2 If the notNamespace [attribute] is present, then not;
		 * <br>3 otherwise (neither namespace nor notNamespace is present) any. */
		public Variety variety() {
			return variety;
		}

		/** @return the appropriate case among the following:
		 * <br>1 If neither namespace nor notNamespace is present, then the empty set;
		 * <br>2 If namespace = "##any", then the empty set;
		 * <br>3 If namespace = "##other", then a set consisting of ·absent· and, if the targetNamespace [attribute] of the &lt;schema&gt; ancestor element information item is present, its ·actual value·;
		 * <br>4 otherwise a set whose members are namespace names corresponding to the space-delimited substrings of the ·actual value· of the namespace or notNamespace [attribute] (whichever is present), except
		 * <br>  4.1 if one such substring is ##targetNamespace, the corresponding member is the ·actual value· of the targetNamespace [attribute] of the &lt;schema&gt; ancestor element information item if present, otherwise ·absent·;
		 * <br>  4.2 if one such substring is ##local, the corresponding member is ·absent·. */
		public Set<String> namespaces() {
			return Collections.unmodifiableSet(namespaces);
		}

		/** @return If the notQName [attribute] is present, then a set whose members correspond to the items in the ·actual value· of the notQName [attribute], as follows.
		 * <ul>
		 *   <li>If the item is a QName value (i.e. an expanded name), then that QName value is a member of the set.</li>
		 *   <li>If the item is the token "##defined", then the keyword defined is a member of the set.</li>
		 *   <li>If the item is the token "##definedSibling", then the keyword sibling is a member of the set.</li>
		 * </ul>
		 * If the notQName [attribute] is not present, then the empty set. */
		public Set<Object> disallowedNames() {
			return Collections.unmodifiableSet(disallowedNames);
		}

	}

	/** @return A Namespace Constraint. */
	public NamespaceConstraint namespaceConstraint();

	/** @return The ·actual value· of the processContents [attribute], if present, otherwise strict. */
	public ProcessContents processContents();

	/** @return The ·annotation mapping· of the &lt;any&gt; element, as defined in XML Representation of Annotation Schema Components (§3.15.2). Note: When this rule is used for an attribute wildcard (see XML Representation of Complex Type Definition Schema Components (§3.4.2)), the {annotations} is the ·annotation mapping· of the &lt;anyAttribute&gt; element. */
	@Override
	public Deque<Annotation> annotations();

}
