package xs.parser;

import java.util.*;
import java.util.stream.*;
import org.w3c.dom.*;
import xs.parser.internal.*;
import xs.parser.internal.util.*;
import xs.parser.internal.util.SequenceParser.*;

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

	private static final String NAMESPACE_ANY = "##any";
	private static final String NAMESPACE_LOCAL = "##local";
	private static final String NAMESPACE_OTHER = "##other";
	private static final String NAMESPACE_TARGET_NAMESPACE = "##targetNamespace";
	private static final String QNAME_DEFINED = "##defined";
	private static final String QNAME_DEFINED_SIBLING = "##definedSibling";
	private static final SequenceParser anyParser = new SequenceParser()
			.optionalAttributes(AttrParser.ID, AttrParser.MAX_OCCURS, AttrParser.MIN_OCCURS, AttrParser.ANY_NAMESPACE, AttrParser.NOT_NAMESPACE, AttrParser.NOT_QNAME, AttrParser.PROCESS_CONTENTS)
			.elements(0, 1, TagParser.ANNOTATION);
	private static final SequenceParser anyAttributeParser = new SequenceParser()
			.optionalAttributes(AttrParser.ID, AttrParser.ANY_NAMESPACE, AttrParser.NOT_NAMESPACE, AttrParser.NOT_QNAME, AttrParser.PROCESS_CONTENTS)
			.elements(0, 1, TagParser.ANNOTATION);

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
			case NAMESPACE_ANY:
				variety = Variety.ANY;
				namespaces = Collections.emptySet();
				break;
			case NAMESPACE_OTHER:
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
					.filter(s -> !NAMESPACE_LOCAL.equals(s))
					.map(s -> NAMESPACE_TARGET_NAMESPACE.equals(s) ? schemaTargetNamespace : s)
					.collect(Collectors.toCollection(LinkedHashSet::new));
		}
		final Set<String> disallowedNames = notQName.isEmpty()
				? Collections.emptySet()
				: notQName.stream()
						.map(s -> {
							switch (s) {
							case QNAME_DEFINED:
								return "defined";
							case QNAME_DEFINED_SIBLING:
								return "sibling";
							default:
								return s;
							}
						})
						.collect(Collectors.toCollection(LinkedHashSet::new));
		this.namespaceConstraint = new NamespaceConstraint(variety, namespaces, disallowedNames);
		this.processContents = processContents;
	}

	private static Particle parseAny(final Result result) {
		final Node node = result.node();
		final Deque<Annotation> annotations = Annotation.of(result).resolve(node);
		final String schemaTargetNamespace = result.schema().targetNamespace();
		final Deque<String> namespace = result.value(AttrParser.ANY_NAMESPACE);
		final Deque<String> notNamespace = result.value(AttrParser.NOT_NAMESPACE);
		final Deque<String> notQName = result.value(AttrParser.NOT_QNAME);
		final ProcessContents processContents = result.value(AttrParser.PROCESS_CONTENTS);
		final Number maxOccurs = result.value(AttrParser.MAX_OCCURS);
		final Number minOccurs = result.value(AttrParser.MIN_OCCURS);
		final Wildcard wildcard = new Wildcard(node, annotations, schemaTargetNamespace, namespace, notNamespace, notQName, processContents);
		return new Particle(node, annotations, maxOccurs, minOccurs, wildcard);
	}

	private static Wildcard parseAnyAttribute(final Result result) {
		final Node node = result.node();
		final Deque<Annotation> annotations = Annotation.of(result).resolve(node);
		final String schemaTargetNamespace = result.schema().targetNamespace();
		final Deque<String> namespace = result.value(AttrParser.ANY_NAMESPACE);
		final Deque<String> notNamespace = result.value(AttrParser.NOT_NAMESPACE);
		final Deque<String> notQName = result.value(AttrParser.NOT_QNAME);
		final ProcessContents processContents = result.value(AttrParser.PROCESS_CONTENTS);
		return new Wildcard(node, annotations, schemaTargetNamespace, namespace, notNamespace, notQName, processContents);
	}

	private static Deque<String> getAttrValueAsNamespace(final Attr attr) {
		final String value = NodeHelper.collapseWhitespace(attr.getValue());
		switch (value) {
		case NAMESPACE_ANY:
		case NAMESPACE_OTHER:
			return Deques.singletonDeque(value);
		default:
			final String[] values = value.split(NodeHelper.LIST_SEP);
			final Deque<String> ls = new ArrayDeque<>();
			for (final String v : values) {
				switch (v) {
				case NAMESPACE_LOCAL:
				case NAMESPACE_TARGET_NAMESPACE:
					ls.add(v);
					break;
				default:
					ls.add(NodeHelper.getNodeValueAsAnyUri(attr, v));
					break;
				}
			}
			return Deques.unmodifiableDeque(ls);
		}
	}

	private static Deque<String> getAttrValueAsNotNamespace(final Attr attr) {
		final String value = NodeHelper.collapseWhitespace(attr.getValue());
		final String[] values = value.split(NodeHelper.LIST_SEP);
		final Deque<String> notNamespace = new ArrayDeque<>(values.length);
		for (final String v : values) {
			switch (v) {
			case NAMESPACE_LOCAL:
			case NAMESPACE_TARGET_NAMESPACE:
				notNamespace.add(v);
				break;
			default:
				notNamespace.add(NodeHelper.getNodeValueAsAnyUri(attr, v));
				break;
			}
		}
		return notNamespace;
	}

	private static Deque<String> getAttrValueAsNotQName(final Attr attr) {
		final String value = NodeHelper.collapseWhitespace(attr.getValue());
		final String[] values = value.split(NodeHelper.LIST_SEP);
		final Deque<String> notQName = new ArrayDeque<>(values.length);
		for (final String v : values) {
			switch (v) {
			case QNAME_DEFINED:
				notQName.add(v);
				break;
			case QNAME_DEFINED_SIBLING:
				if (TagParser.Names.ANY_ATTRIBUTE.equals(attr.getLocalName())) {
					notQName.add(v);
					break;
				}
				// fallthrough
			default:
				NodeHelper.getNodeValueAsQName(attr, v);
				notQName.add(v);
				break;
			}
		}
		return notQName;
	}

	static void register() {
		AttrParser.register(AttrParser.Names.NAMESPACE, Deque.class, String.class, Wildcard::getAttrValueAsNamespace);
		AttrParser.register(AttrParser.Names.NOT_NAMESPACE, Deque.class, String.class, Wildcard::getAttrValueAsNotNamespace);
		AttrParser.register(AttrParser.Names.NOT_QNAME, Deque.class, String.class, Wildcard::getAttrValueAsNotQName);
		AttrParser.register(AttrParser.Names.PROCESS_CONTENTS, ProcessContents.class, ProcessContents.STRICT, ProcessContents::getAttrValueAsProcessContents);
		TagParser.register(TagParser.Names.ANY, anyParser, Particle.class, Wildcard::parseAny);
		TagParser.register(TagParser.Names.ANY_ATTRIBUTE, anyAttributeParser, Wildcard.class, Wildcard::parseAnyAttribute);
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
