package xs.parser;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import javax.xml.*;
import org.w3c.dom.*;
import xs.parser.internal.*;
import xs.parser.internal.util.*;
import xs.parser.internal.util.SequenceParser.*;
import xs.parser.v.*;

/**
 * An annotation is information for human and/or mechanical consumers. The interpretation of such information is not defined in this specification.
 *
 * <pre>
 * &lt;annotation
 *   id = ID
 *   {any attributes with non-schema namespace . . .}&gt;
 *   Content: (appinfo | documentation)*
 * &lt;/annotation&gt;
 *
 * &lt;appinfo
 *   source = anyURI
 *   {any attributes with non-schema namespace . . .}&gt;
 *   Content: ({any})*
 * &lt;/appinfo&gt;
 *
 * &lt;documentation
 *   source = anyURI
 *   xml:lang = language
 *   {any attributes with non-schema namespace . . .}&gt;
 *   Content: ({any})*
 * &lt;/documentation&gt;
 * </pre>
 *
 * <table>
 *   <caption style="font-size: large; text-align: left">Schema Component: Annotation, a kind of Component</caption>
 *   <thead>
 *     <tr>
 *       <th style="text-align: left">Method</th>
 *       <th style="text-align: left">Property</th>
 *       <th style="text-align: left">Representation</th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <td>{@link Annotation#applicationInformation()}</td>
 *       <td>{application information}</td>
 *       <td>A sequence of Element information items.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link Annotation#userInformation()}</td>
 *       <td>{user information}</td>
 *       <td>A sequence of Element information items.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link Annotation#attributes()}</td>
 *       <td>{attributes}</td>
 *       <td>A set of Attribute information items.</td>
 *     </tr>
 *   </tbody>
 * </table>
 */
public class Annotation implements SchemaComponent {

	static class AnnotationSet {

		public static final AnnotationSet EMPTY = new AnnotationSet();

		private final DeferredArrayDeque<Annotation> annotations;

		private AnnotationSet() {
			this.annotations = new DeferredArrayDeque<>();
		}

		private AnnotationSet(final Result result) {
			this(result.parseAll(TagParser.ANNOTATION));
		}

		private AnnotationSet(final Deque<Annotation> annotations) {
			this.annotations = new DeferredArrayDeque<>(annotations);
		}

		AnnotationSet add(final AnnotationSet a) {
			this.annotations.addAll(a.annotations);
			return this;
		}

		<T> AnnotationSet add(final Deferred<T> a, final Function<T, AnnotationSet> fn) {
			this.annotations.addAll(a.map(t -> fn.apply(t).annotations));
			return this;
		}

		AnnotationSet addAll(final Deque<? extends AnnotatedComponent> annotatedComponents) {
			return addAll(annotatedComponents, a -> new AnnotationSet(a.annotations()));
		}

		<T> AnnotationSet addAll(final Deque<T> annotated, final Function<T, AnnotationSet> fn) {
			this.annotations.addAll(Deferred.of(() -> {
				final Deque<Annotation> x = new ArrayDeque<>();
				for (final T a : annotated) {
					x.addAll(fn.apply(a).annotations);
				}
				return x;
			}));
			return this;
		}

		Deque<Annotation> resolve(final Node component) {
			return new DeferredArrayDeque<>(() -> {
				final ArrayDeque<Annotation> mapped = new ArrayDeque<>();
				for (final Annotation a : annotations) {
					mapped.add(new Annotation(a.context, a.node, a.applicationInformation, a.userInformation, Deferred.of(() -> {
						Node n = a.node;
						final Set<Attr> attrs = new LinkedHashSet<>();
						do {
							final NamedNodeMap nm = n.getAttributes();
							final int len = nm.getLength();
							for (int i = 0; i < len; ++i) {
								final Attr attr = (Attr) nm.item(i);
								if (attr.getNamespaceURI() != null && !XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(attr.getNamespaceURI())) {
									attrs.add(attr);
								}
							}
							n = n.getParentNode();
						} while (!component.isSameNode(n));
						return attrs;
					})));
				}
				return mapped;
			});
		}

	}

	/**
	 * <pre>
	 * &lt;appinfo
	 *   source = anyURI
	 *   {any attributes with non-schema namespace . . .}&gt;
	 *   Content: ({any})*
	 * &lt;/appinfo&gt;
	 * </pre>
	 */
	public static class Appinfo {

		private static final SequenceParser parser = new SequenceParser()
				.optionalAttributes(AttrParser.SOURCE)
				.allowsAnyContent(true);

		private final Node node;

		private Appinfo(final Node node) {
			this.node = Objects.requireNonNull(node);
		}

		private static Appinfo parse(final Result result) {
			return new Appinfo(result.node());
		}

	}

	/**
	 * <pre>
	 * &lt;documentation
	 *   source = anyURI
	 *   xml:lang = language
	 *   {any attributes with non-schema namespace . . .}&gt;
	 *   Content: ({any})*
	 * &lt;/documentation&gt;
	 * </pre>
	 */
	public static class Documentation {

		private static final SequenceParser parser = new SequenceParser()
				.optionalAttributes(AttrParser.SOURCE, AttrParser.XML_LANG)
				.allowsAnyContent(true);

		private final Node node;

		private Documentation(final Node node) {
			this.node = Objects.requireNonNull(node);
		}

		private static Documentation parse(final Result result) {
			return new Documentation(result.node());
		}

	}

	private static final SequenceParser parser = new SequenceParser()
			.optionalAttributes(AttrParser.ID)
			.elements(0, Integer.MAX_VALUE, TagParser.ANNOTATION.documentation(), TagParser.ANNOTATION.appinfo());

	private final Deferred<? extends AnnotatedComponent> context;
	private final Node node;
	private final Deque<Node> applicationInformation;
	private final Deque<Node> userInformation;
	private final Deferred<Set<Attr>> attributes;

	private Annotation(final Deferred<? extends AnnotatedComponent> context, final Node node, final Deque<Node> applicationInformation, final Deque<Node> userInformation, final Deferred<Set<Attr>> attributes) {
		this.context = Objects.requireNonNull(context);
		this.node = Objects.requireNonNull(node);
		this.applicationInformation = Objects.requireNonNull(applicationInformation);
		this.userInformation = Objects.requireNonNull(userInformation);
		this.attributes = Objects.requireNonNull(attributes);
	}

	private static Annotation parse(final Result result) {
		final Deferred<? extends AnnotatedComponent> context = result.context();
		final Node node = result.node();
		final Deque<Appinfo> appinfo = result.parseAll(TagParser.ANNOTATION.appinfo());
		final Deque<Node> applicationInformation = new DeferredArrayDeque<>(() -> appinfo.stream().map(a -> a.node).collect(Collectors.toCollection(ArrayDeque::new)));
		final Deque<Documentation> documentation = result.parseAll(TagParser.ANNOTATION.documentation());
		final Deque<Node> userInformation = new DeferredArrayDeque<>(() -> documentation.stream().map(d -> d.node).collect(Collectors.toCollection(ArrayDeque::new)));
		return new Annotation(context, node, applicationInformation, userInformation, Collections::emptySet);
	}

	static void register() {
		AttrParser.register(AttrParser.Names.SOURCE, NodeHelper::getAttrValueAsAnyUri);
		AttrParser.register(AttrParser.Names.XML_LANG, NodeHelper::getAttrValueAsLanguage);
		TagParser.register(TagParser.Names.APPINFO, Appinfo.parser, Appinfo.class, Appinfo::parse);
		TagParser.register(TagParser.Names.DOCUMENTATION, Documentation.parser, Documentation.class, Documentation::parse);
		TagParser.register(TagParser.Names.ANNOTATION, Annotation.parser, Annotation.class, Annotation::parse);
		VisitorHelper.register(Annotation.class, Annotation::visit);
	}

	static AnnotationSet of(final Result result) {
		return new AnnotationSet(result);
	}

	void visit(final Visitor visitor) {
		if (visitor.visit(context.get(), node, this)) {
			visitor.onAnnotation(context.get(), node, this);
		}
	}

	/** @return A sequence of the &lt;appinfo&gt; element information items from among the [children], in order, if any, otherwise the empty sequence. */
	public Deque<Node> applicationInformation() {
		return Deques.unmodifiableDeque(applicationInformation);
	}

	/** @return A sequence of the &lt;documentation&gt; element information items from among the [children], in order, if any, otherwise the empty sequence. */
	public Deque<Node> userInformation() {
		return Deques.unmodifiableDeque(userInformation);
	}

	/** @return A set of attribute information items, namely those allowed by the attribute wildcard in the type definition for the &lt;annotation&gt; item itself or for the enclosing items which correspond to the component within which the annotation component is located. */
	public Set<Attr> attributes() {
		return Collections.unmodifiableSet(attributes.get());
	}

}
