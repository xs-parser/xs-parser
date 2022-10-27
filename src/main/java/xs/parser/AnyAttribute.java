package xs.parser;

import java.util.*;
import java.util.stream.*;
import org.w3c.dom.*;
import xs.parser.internal.*;
import xs.parser.internal.util.*;
import xs.parser.internal.util.SequenceParser.*;
import xs.parser.v.*;

class AnyAttribute implements Wildcard {

	private static final String NAMESPACE_ANY = "##any";
	private static final String NAMESPACE_LOCAL = "##local";
	private static final String NAMESPACE_OTHER = "##other";
	private static final String NAMESPACE_TARGET_NAMESPACE = "##targetNamespace";
	private static final String QNAME_DEFINED = "##defined";
	private static final String QNAME_DEFINED_SIBLING = "##definedSibling";
	private static final SequenceParser parser = new SequenceParser()
			.optionalAttributes(AttrParser.ID, AttrParser.ANY_NAMESPACE, AttrParser.NOT_NAMESPACE, AttrParser.NOT_QNAME, AttrParser.PROCESS_CONTENTS)
			.elements(0, 1, TagParser.ANNOTATION);

	private final Deferred<? extends AnnotatedComponent> context;
	private final Node node;
	private final Deque<Annotation> annotations;
	private final NamespaceConstraint namespaceConstraint;
	private final ProcessContents processContents;

	AnyAttribute(final Deferred<? extends AnnotatedComponent> context, final Node node, final Deque<Annotation> annotations, final String schemaTargetNamespace, final Deque<String> namespace, final Deque<String> notNamespace, final Deque<String> notQName, final ProcessContents processContents) {
		this.context = Objects.requireNonNull(context);
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
		final Set<Object> disallowedNames = notQName.isEmpty()
				? Collections.emptySet()
				: notQName.stream()
						.map(s -> {
							switch (s) {
							case QNAME_DEFINED:
								return NamespaceConstraint.DEFINED;
							case QNAME_DEFINED_SIBLING:
								return NamespaceConstraint.SIBLING;
							default:
								return NodeHelper.getNodeValueAsQName(node, s);
							}
						})
						.collect(Collectors.toCollection(LinkedHashSet::new));
		this.namespaceConstraint = new Wildcard.NamespaceConstraint(variety, namespaces, disallowedNames);
		this.processContents = Objects.requireNonNull(processContents);
	}

	AnyAttribute(final Deferred<? extends AnnotatedComponent> context, final Node node, final Deque<Annotation> annotations, final NamespaceConstraint namespaceConstraint, final ProcessContents processContents) {
		this.context = Objects.requireNonNull(context);
		this.node = Objects.requireNonNull(node);
		this.annotations = Objects.requireNonNull(annotations);
		this.namespaceConstraint = Objects.requireNonNull(namespaceConstraint);
		this.processContents = Objects.requireNonNull(processContents);
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

	private static AnyAttribute parse(final Result result) {
		final Deferred<? extends AnnotatedComponent> context = result.context();
		final Node node = result.node();
		final Deque<Annotation> annotations = Annotation.of(result).resolve(node);
		final String schemaTargetNamespace = result.schema().targetNamespace();
		final Deque<String> namespace = result.value(AttrParser.ANY_NAMESPACE);
		final Deque<String> notNamespace = result.value(AttrParser.NOT_NAMESPACE);
		final Deque<String> notQName = result.value(AttrParser.NOT_QNAME);
		final ProcessContents processContents = result.value(AttrParser.PROCESS_CONTENTS);
		return new AnyAttribute(context, node, annotations, schemaTargetNamespace, namespace, notNamespace, notQName, processContents);
	}

	static void register() {
		AttrParser.register(AttrParser.Names.NAMESPACE, Deque.class, String.class, AnyAttribute::getAttrValueAsNamespace);
		AttrParser.register(AttrParser.Names.NOT_NAMESPACE, Deque.class, String.class, AnyAttribute::getAttrValueAsNotNamespace);
		AttrParser.register(AttrParser.Names.NOT_QNAME, Deque.class, String.class, AnyAttribute::getAttrValueAsNotQName);
		AttrParser.register(AttrParser.Names.PROCESS_CONTENTS, ProcessContents.class, ProcessContents.STRICT, ProcessContents::getAttrValueAsProcessContents);
		TagParser.register(TagParser.Names.ANY_ATTRIBUTE, parser, Wildcard.class, AnyAttribute::parse);
		VisitorHelper.register(AnyAttribute.class, AnyAttribute::visit);
	}

	void visit(final Visitor visitor) {
		if (visitor.visit(context.get(), node, this)) {
			visitor.onWildcard(context.get(), node, this);
			annotations.forEach(a -> a.visit(visitor));
		}
	}

	@Override
	public NamespaceConstraint namespaceConstraint() {
		return namespaceConstraint;
	}

	@Override
	public ProcessContents processContents() {
		return processContents;
	}

	@Override
	public Deque<Annotation> annotations() {
		return Deques.unmodifiableDeque(annotations);
	}

}
