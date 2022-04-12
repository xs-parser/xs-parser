package xs.parser;

import java.util.*;
import javax.xml.namespace.*;
import org.w3c.dom.*;
import xs.parser.internal.*;
import xs.parser.internal.SequenceParser.*;

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

		ALL, CHOICE, SEQUENCE;

	}

	protected static final SequenceParser parser = new SequenceParser()
			.optionalAttributes(AttributeValue.ID, AttributeValue.MAXOCCURS, AttributeValue.MINOCCURS, AttributeValue.NAME, AttributeValue.REF)
			.elements(0, 1, ElementValue.ANNOTATION)
			.elements(0, 1, ElementValue.ALL, ElementValue.CHOICE, ElementValue.SEQUENCE);

	private final Node node;
	private final Deque<Annotation> annotations;
	private final String name;
	private final String targetNamespace;
	private final ModelGroup modelGroup;
	private final Compositor compositor;
	private final Deque<Particle<?>> particles;

	ModelGroup(final Node node, final Deque<Annotation> annotations, final String name, final String targetNamespace, final ModelGroup modelGroup, final Compositor compositor, final Deque<Particle<?>> particles) {
		this.node = Objects.requireNonNull(node);
		this.annotations = Objects.requireNonNull(annotations);
		this.name = name;
		this.targetNamespace = NodeHelper.validateTargetNamespace(targetNamespace);
		this.modelGroup = modelGroup;
		this.compositor = compositor;
		this.particles = Objects.requireNonNull(particles);
	}

	static ModelGroup synthetic(final Node node, final Deque<Annotation> annotations, final Compositor compositor, final Deque<Particle<?>> particles) {
		return new ModelGroup(node, annotations, null, null, null, compositor, particles) {
			@Override
			public ModelGroup modelGroup() {
				return this;
			}
		};
	}

	protected static ModelGroup parseDecl(final Result result) {
		final Particle<ModelGroup> particle = result.parse(ElementValue.ALL, ElementValue.CHOICE, ElementValue.SEQUENCE);
		final String name = result.value(AttributeValue.NAME);
		final String targetNamespace = result.schema().targetNamespace();
		final ModelGroup term = particle.term();
		return new ModelGroup(result.node(), result.annotations(), name, targetNamespace, term, term.compositor(), term.particles());
	}

	protected static Particle<ModelGroup> parse(final Result result) {
		final String maxOccurs = result.value(AttributeValue.MAXOCCURS);
		final String minOccurs = result.value(AttributeValue.MINOCCURS);
		final QName refName = result.value(AttributeValue.REF);
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
				public Deque<Particle<?>> particles() {
					return ref.get().particles();
				}

			};
		} else {
			term = parseDecl(result);
		}
		return new Particle<ModelGroup>(result.node(), result.annotations(), maxOccurs, minOccurs, term);
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

	public Deque<Particle<?>> particles() {
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