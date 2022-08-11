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

	private final Deferred<? extends AnnotatedComponent> context;
	private final Node node;
	private final Deque<Annotation> annotations;
	private final String name;
	private final String targetNamespace;
	private final Deferred<ModelGroup> modelGroup;
	private final Deferred<Compositor> compositor;
	private final Deque<Particle> particles;

	private ModelGroup(final Deferred<? extends AnnotatedComponent> context, final Node node, final Deque<Annotation> annotations, final String name, final String targetNamespace, final Deferred<ModelGroup> modelGroup, final Deferred<Compositor> compositor, final Deque<Particle> particles) {
		this.context = Objects.requireNonNull(context);
		this.node = Objects.requireNonNull(node);
		this.annotations = Objects.requireNonNull(annotations);
		this.name = name;
		this.targetNamespace = NodeHelper.requireNonEmpty(node, targetNamespace);
		this.modelGroup = Objects.requireNonNull(modelGroup);
		this.compositor = Objects.requireNonNull(compositor);
		this.particles = Objects.requireNonNull(particles);
	}

	private static Particle parse(final Result result) {
		final Deferred<? extends AnnotatedComponent> context = result.context();
		final Node node = result.node();
		final Deque<Annotation> annotations = Annotation.of(result).resolve(node);
		final Number maxOccurs = result.value(AttrParser.MAX_OCCURS);
		final Number minOccurs = result.value(AttrParser.MIN_OCCURS);
		final QName refName = result.value(AttrParser.REF);
		final ModelGroup term;
		if (refName != null) {
			final Deferred<ModelGroup> ref = result.schema().find(refName, ModelGroup.class);
			term = new ModelGroup(context, node, annotations, refName.getLocalPart(), result.schema().targetNamespace(), ref.map(ModelGroup::modelGroup), ref.map(ModelGroup::compositor), ref.mapToDeque(ModelGroup::particles));
		} else {
			term = parseDecl(result);
		}
		return new Particle(context, node, annotations, maxOccurs, minOccurs, term);
	}

	private static ModelGroup parseDecl(final Result result) {
		final Deferred<? extends AnnotatedComponent> context = result.context();
		final Node node = result.node();
		final Deque<Annotation> annotations = Annotation.of(result).resolve(node);
		final Deferred<Particle> particle = result.parse(TagParser.ALL, TagParser.CHOICE, TagParser.SEQUENCE);
		final String name = result.value(AttrParser.NAME);
		final String targetNamespace = result.schema().targetNamespace();
		final Deferred<ModelGroup> modelGroup = particle.map(p -> (ModelGroup) p.term());
		final Deferred<Compositor> compositor = modelGroup.map(ModelGroup::compositor);
		final Deque<Particle> particles = modelGroup.mapToDeque(ModelGroup::particles);
		return new ModelGroup(context, node, annotations, name, targetNamespace, modelGroup, compositor, particles);
	}

	static void register() {
		TagParser.register(TagParser.Names.GROUP, parser, ModelGroup.class, ModelGroup::parseDecl);
		TagParser.register(TagParser.Names.GROUP, parser, Particle.class, ModelGroup::parse);
		VisitorHelper.register(ModelGroup.class, ModelGroup::visit);
	}

	static ModelGroup synthetic(final Deferred<? extends AnnotatedComponent> context, final Node node, final Deque<Annotation> annotations, final Compositor compositor, final Deque<Particle> particles) {
		final DeferredValue<ModelGroup> self = new DeferredValue<>();
		return self.set(new ModelGroup(context, node, annotations, null, null, self, () -> compositor, particles));
	}

	void visit(final Visitor visitor) {
		if (visitor.visit(context.get(), node, this)) {
			visitor.onModelGroup(context.get(), node, this);
			annotations.forEach(a -> a.visit(visitor));
			modelGroup().visit(visitor);
			particles.forEach(p -> p.visit(visitor));
		}
	}

	public String name() {
		return name;
	}

	public String targetNamespace() {
		return targetNamespace;
	}

	public ModelGroup modelGroup() {
		return modelGroup.get();
	}

	public Compositor compositor() {
		return compositor.get();
	}

	public Deque<Particle> particles() {
		return Deques.unmodifiableDeque(particles);
	}

	@Override
	public Deque<Annotation> annotations() {
		return Deques.unmodifiableDeque(annotations);
	}

}
