package xs.parser;

import java.util.*;
import javax.xml.namespace.*;
import org.w3c.dom.*;
import xs.parser.internal.*;
import xs.parser.internal.util.*;
import xs.parser.internal.util.SequenceParser.*;

/**
 * <pre>
 * &lt;group
 *   id = ID
 *   maxOccurs = (nonNegativeInteger | unbounded)  : 1
 *   minOccurs = nonNegativeInteger : 1
 *   name = NCName
 *   ref = QName
 *   {any attributes with non-schema namespace . . .}&gt;
 *   Content: (annotation?, (all | choice | sequence)?)
 * &lt;/group&gt;
 * </pre>
 */
public class ModelGroup implements Term {

	public enum Compositor {

		ALL,
		CHOICE,
		SEQUENCE

	}

	private static final SequenceParser parser = new SequenceParser()
			.optionalAttributes(AttrParser.ID, AttrParser.MAX_OCCURS, AttrParser.MIN_OCCURS, AttrParser.NAME, AttrParser.REF)
			.elements(0, 1, TagParser.ANNOTATION)
			.elements(0, 1, TagParser.ALL, TagParser.CHOICE, TagParser.SEQUENCE);

	private final Node node;
	private final Deque<Annotation> annotations;
	private final String name;
	private final String targetNamespace;
	private final ModelGroup modelGroup;
	private final Compositor compositor;
	private final Deque<Particle> particles;

	private ModelGroup(final Node node, final Deque<Annotation> annotations, final String name, final String targetNamespace, final ModelGroup modelGroup, final Compositor compositor, final Deque<Particle> particles) {
		this.node = Objects.requireNonNull(node);
		this.annotations = Objects.requireNonNull(annotations);
		this.name = name;
		this.targetNamespace = NodeHelper.validateTargetNamespace(node, targetNamespace);
		this.modelGroup = modelGroup;
		this.compositor = compositor;
		this.particles = Objects.requireNonNull(particles);
	}

	private static Particle parse(final Result result) {
		final String maxOccurs = result.value(AttrParser.MAX_OCCURS);
		final String minOccurs = result.value(AttrParser.MIN_OCCURS);
		final QName refName = result.value(AttrParser.REF);
		final ModelGroup term;
		if (refName != null) {
			term = new ModelGroup(result.node(), result.annotations(), refName.getLocalPart(), result.schema().targetNamespace(), null, null, Deques.emptyDeque()) {

				final Deferred<ModelGroup> ref = result.schema().find(refName, ModelGroup.class);

				@Override
				public ModelGroup modelGroup() {
					return ref.get().modelGroup();
				}

				@Override
				public Compositor compositor() {
					return ref.get().compositor();
				}

				@Override
				public Deque<Particle> particles() {
					return ref.get().particles();
				}

			};
		} else {
			term = parseDecl(result);
		}
		return new Particle(result.node(), result.annotations(), maxOccurs, minOccurs, term);
	}

	private static ModelGroup parseDecl(final Result result) {
		final Particle particle = result.parse(TagParser.ALL, TagParser.CHOICE, TagParser.SEQUENCE);
		final String name = result.value(AttrParser.NAME);
		final String targetNamespace = result.schema().targetNamespace();
		final ModelGroup term = (ModelGroup) particle.term();
		return new ModelGroup(result.node(), result.annotations(), name, targetNamespace, term, term.compositor(), term.particles());
	}

	static void register() {
		TagParser.register(TagParser.Names.GROUP, parser, ModelGroup.class, ModelGroup::parseDecl);
		TagParser.register(TagParser.Names.GROUP, parser, Particle.class, ModelGroup::parse);
	}

	static ModelGroup synthetic(final Node node, final Deque<Annotation> annotations, final Compositor compositor, final Deque<Particle> particles) {
		return new ModelGroup(node, annotations, null, null, null, compositor, particles) {

			@Override
			public ModelGroup modelGroup() {
				return this;
			}

		};
	}

	public String name() {
		return name;
	}

	public String targetNamespace() {
		return targetNamespace;
	}

	public ModelGroup modelGroup() {
		return modelGroup;
	}

	public Compositor compositor() {
		return compositor;
	}

	public Deque<Particle> particles() {
		return Deques.unmodifiableDeque(particles);
	}

	@Override
	public Node node() {
		return node;
	}

	@Override
	public Deque<Annotation> annotations() {
		return Deques.unmodifiableDeque(annotations);
	}

}
