package xs.parser;

import java.util.*;
import java.util.stream.*;
import org.w3c.dom.*;
import xs.parser.internal.*;
import xs.parser.internal.SequenceParser.*;

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
public class Wildcard implements Term {

	public enum ProcessContents {

		LAX("lax"),
		SKIP("skip"),
		STRICT("strict");

		private final String name;

		private ProcessContents(final String value) {
			this.name = value;
		}

		public static ProcessContents getByName(final Node node) {
			final String name = node.getNodeValue();
			for (final ProcessContents p : values()) {
				if (p.getName().equals(name)) {
					return p;
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

		private final Variety variety;
		private final Set<String> namespaces;
		private final Set<String> disallowedNames;

		private NamespaceConstraint(final Variety variety, final Set<String> namespaces, final Set<String> disallowedNames) {
			this.variety = variety;
			this.namespaces = namespaces;
			this.disallowedNames = disallowedNames;
		}

		public Variety variety() {
			return variety;
		}

		public Set<String> namespaces() {
			return Collections.unmodifiableSet(namespaces);
		}

		public Set<String> disallowedNames() {
			return Collections.unmodifiableSet(disallowedNames);
		}

	}

	protected static final SequenceParser anyParser = new SequenceParser()
			.optionalAttributes(AttributeValue.ID, AttributeValue.MAXOCCURS, AttributeValue.MINOCCURS, AttributeValue.ANY_NAMESPACE, AttributeValue.NOTNAMESPACE, AttributeValue.NOTQNAME, AttributeValue.PROCESSCONTENTS)
			.elements(0, 1, ElementValue.ANNOTATION);
	protected static final SequenceParser anyAttributeParser = new SequenceParser()
			.optionalAttributes(AttributeValue.ID, AttributeValue.ANY_NAMESPACE, AttributeValue.NOTNAMESPACE, AttributeValue.NOTQNAME, AttributeValue.PROCESSCONTENTS)
			.elements(0, 1, ElementValue.ANNOTATION);

	private final Node node;
	private final Deque<Annotation> annotations;
	private final NamespaceConstraint namespaceConstraint;
	private final ProcessContents processContents;

	private Wildcard(final Node node, final Deque<Annotation> annotations, final String schemaTargetNamespace, final Deque<String> namespace, final Deque<String> notNamespace, final Deque<String> notQName, final ProcessContents processContents) {
		this.node = Objects.requireNonNull(node);
		this.annotations = Objects.requireNonNull(annotations);
		final Variety variety;
		Set<String> namespaces = null;
		if (namespace.size() == 1) {
			switch (namespace.getFirst()) {
			case "##any":
				variety = Variety.ANY;
				namespaces = Collections.emptySet();
				break;
			case "##other":
				variety = Variety.NOT;
				namespaces = schemaTargetNamespace != null ? Collections.singleton(schemaTargetNamespace) : Collections.emptySet();
				break;
			default:
				variety = Variety.ENUMERATION;
			}
		} else if (!namespace.isEmpty()) {
			variety = Variety.ENUMERATION;
		} else if (notNamespace.isEmpty()) {
			variety = Variety.ANY;
			namespaces = Collections.emptySet();
		} else {
			variety = Variety.NOT;
		}
		if (namespaces == null) {
			namespaces = namespace.stream()
					.filter(s -> !"##local".equals(s))
					.map(s -> "##targetNamespace".equals(s) ? schemaTargetNamespace : s)
					.collect(Collectors.toCollection(LinkedHashSet::new));
		}
		final Set<String> disallowedNames = notQName.isEmpty()
				? Collections.emptySet()
				: notQName.stream()
						.map(s -> {
							switch (s) {
							case "##defined":
								return "defined";
							case "##definedSibling":
								return "sibling";
							default:
								return s;
							}
						})
						.collect(Collectors.toCollection(LinkedHashSet::new));
		this.namespaceConstraint = new NamespaceConstraint(variety, namespaces, disallowedNames);
		this.processContents = processContents;
	}

	protected static Particle<Wildcard> parseAny(final Result result) {
		final String schemaTargetNamespace = result.schema().targetNamespace();
		final Deque<String> namespace = result.value(AttributeValue.ANY_NAMESPACE);
		final Deque<String> notNamespace = result.value(AttributeValue.NOTNAMESPACE);
		final Deque<String> notQName = result.value(AttributeValue.NOTQNAME);
		final ProcessContents processContents = result.value(AttributeValue.PROCESSCONTENTS);
		final String maxOccurs = result.value(AttributeValue.MAXOCCURS);
		final String minOccurs = result.value(AttributeValue.MINOCCURS);
		final Wildcard wildcard = new Wildcard(result.node(), result.annotations(), schemaTargetNamespace, namespace, notNamespace, notQName, processContents);
		return new Particle<>(result.node(), result.annotations(), maxOccurs, minOccurs, wildcard);
	}

	protected static Wildcard parseAnyAttribute(final Result result) {
		final String schemaTargetNamespace = result.schema().targetNamespace();
		final Deque<String> namespace = result.value(AttributeValue.ANY_NAMESPACE);
		final Deque<String> notNamespace = result.value(AttributeValue.NOTNAMESPACE);
		final Deque<String> notQName = result.value(AttributeValue.NOTQNAME);
		final ProcessContents processContents = result.value(AttributeValue.PROCESSCONTENTS);
		return new Wildcard(result.node(), result.annotations(), schemaTargetNamespace, namespace, notNamespace, notQName, processContents);
	}

	public NamespaceConstraint namespaceConstraint() {
		return namespaceConstraint;
	}

	/** @return The ·actual value· of the processContents [attribute], if present, otherwise strict. */
	public ProcessContents processContents() {
		return processContents;
	}

	@Override
	public Node node() {
		return node;
	}

	/** @return The ·annotation mapping· of the &lt;any&gt; element, as defined in XML Representation of Annotation Schema Components (§3.15.2). Note: When this rule is used for an attribute wildcard (see XML Representation of Complex Type Definition Schema Components (§3.4.2)), the {annotations} is the ·annotation mapping· of the &lt;anyAttribute&gt; element. */
	@Override
	public Deque<Annotation> annotations() {
		return Deques.unmodifiableDeque(annotations);
	}

}