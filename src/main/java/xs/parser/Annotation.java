package xs.parser;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import org.w3c.dom.*;
import xs.parser.internal.*;
import xs.parser.internal.util.*;
import xs.parser.internal.util.SequenceParser.*;

/**
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

	static class AnnotationsBuilder {

		private final Result result;
		private final Deque<Deque<Annotation>> annotations = new ArrayDeque<>();

		AnnotationsBuilder(final Result result) {
			this.result = result;
		}

		AnnotationsBuilder add(final Supplier<Deque<Annotation>> annotations) {
			this.annotations.add(annotations.get());
			return this;
		}

		AnnotationsBuilder add(final Deque<? extends AnnotatedComponent> annotations) {
			for (final AnnotatedComponent a : annotations) {
				this.annotations.add(a.annotations());
			}
			return this;
		}

		Deque<Annotation> build() {
			final Deque<Annotation> baseAnnotations = result.annotations();
			int size = baseAnnotations.size();
			for (final Deque<Annotation> a : annotations) {
				size += a.size();
			}
			final Deque<Annotation> build = new DeferredArrayDeque<>(size);
			build.addAll(baseAnnotations);
			for (final Deque<Annotation> a : annotations) {
				build.addAll(a);
			}
			return build;
		}

	}

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

	private final Node node;
	private final Deque<Node> applicationInformation;
	private final Deque<Node> userInformation;
	private final Deferred<Set<Node>> attributes;

	private Annotation(final Node node, final Deque<Node> applicationInformation, final Deque<Node> userInformation, final Deferred<Set<Node>> attributes) {
		this.node = Objects.requireNonNull(node);
		this.applicationInformation = Objects.requireNonNull(applicationInformation);
		this.userInformation = Objects.requireNonNull(userInformation);
		this.attributes = Objects.requireNonNull(attributes);
	}

	private static Annotation parse(final Result result) {
		final Deque<Appinfo> appinfo = result.parseAll(TagParser.ANNOTATION.appinfo());
		final Deque<Node> applicationInformation = appinfo.stream().map(a -> a.node).collect(Collectors.toCollection(ArrayDeque::new));
		final Deque<Documentation> documentation = result.parseAll(TagParser.ANNOTATION.documentation());
		final Deque<Node> userInformation = documentation.stream().map(d -> d.node).collect(Collectors.toCollection(ArrayDeque::new));
		return new Annotation(result.node(), applicationInformation, userInformation, Collections::emptySet /*TODO*/);
	}

	static void register() {
		AttrParser.register(AttrParser.Names.SOURCE, NodeHelper::getNodeValueAsAnyUri);
		AttrParser.register(AttrParser.Names.XML_LANG, NodeHelper::getNodeValueAsLanguage);
		TagParser.register(TagParser.Names.APPINFO, Appinfo.parser, Appinfo.class, Appinfo::parse);
		TagParser.register(TagParser.Names.DOCUMENTATION, Documentation.parser, Documentation.class, Documentation::parse);
		TagParser.register(TagParser.Names.ANNOTATION, Annotation.parser, Annotation.class, Annotation::parse);
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
	public Set<Node> attributes() {
		return Collections.unmodifiableSet(attributes.get());
	}

	@Override
	public Node node() {
		return node;
	}

}
