package xs.parser;

import java.util.*;
import javax.xml.namespace.*;
import org.w3c.dom.*;
import xs.parser.internal.*;
import xs.parser.internal.util.*;
import xs.parser.internal.util.SequenceParser.*;
import xs.parser.v.*;

/**
 * The model group, particle, and wildcard components contribute to the portion of a complex type definition that controls an element information item's content.
 * <p>
 * A model group is a constraint in the form of a grammar fragment that applies to lists of element information items. It consists of a list of particles, i.e. element declarations, wildcards and model groups. There are three varieties of model group:
 * <ul>
 *   <li>Sequence (the element information items match the particles in sequential order);</li>
 *   <li>Conjunction (the element information items match the particles, in any order);</li>
 *   <li>Disjunction (the element information items match one or more of the particles).</li>
 * </ul>
 *
 * Each model group denotes a set of sequences of element information items. Regarding that set of sequences as a language, the set of sequences recognized by a group G may be written L(G). A model group G is said to accept or recognize the members of L(G).
 * <p>
 * A model group definition associates a name and optional annotations with a Model Group. By reference to the name, the entire model group can be incorporated by reference into a {term}.
 * <p>
 * When the [children] of element information items are not constrained to be empty or by reference to a simple type definition (Simple Type Definitions (§3.16)), the sequence of element information item [children] content may be specified in more detail with a model group. Because the {term} property of a particle can be a model group, and model groups contain particles, model groups can indirectly contain other model groups; the grammar for model groups is therefore recursive. A model group directly contains the particles in the value of its {particles} property. A model group indirectly contains the particles, groups, wildcards, and element declarations which are ·contained· by the particles it ·directly contains·. A model group contains the components which it either ·directly contains· or ·indirectly contains·.
 *
 * <pre>
 * &lt;group
 *   id = ID
 *   maxOccurs = (nonNegativeInteger | unbounded) : 1
 *   minOccurs = nonNegativeInteger : 1
 *   name = NCName
 *   ref = QName
 *   {any attributes with non-schema namespace . . .}&gt;
 *   Content: (annotation?, (all | choice | sequence)?)
 * &lt;/group&gt;
 * </pre>
 *
 * <table>
 *   <caption style="font-size: large; text-align: left">Schema Component: Model Group Definition, a kind of Annotated Component</caption>
 *   <thead>
 *     <tr>
 *       <th style="text-align: left">Method</th>
 *       <th style="text-align: left">Property</th>
 *       <th style="text-align: left">Representation</th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <td>{@link ModelGroup#annotations()}</td>
 *       <td>{annotations}</td>
 *       <td>A sequence of Annotation components.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link ModelGroup#name()}</td>
 *       <td>{name}</td>
 *       <td>An xs:NCName value. Required.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link ModelGroup#targetNamespace()}</td>
 *       <td>{target namespace}</td>
 *       <td>An xs:anyURI value. Optional.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link ModelGroup#modelGroup()}</td>
 *       <td>{model group}</td>
 *       <td>A Model Group component. Required.</td>
 *     </tr>
 *   </tbody>
 * </table>
 *
 * <table>
 *   <caption style="font-size: large; text-align: left">Schema Component: Model Group, a kind of Term</caption>
 *   <thead>
 *     <tr>
 *       <th style="text-align: left">Method</th>
 *       <th style="text-align: left">Property</th>
 *       <th style="text-align: left">Representation</th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <td>{@link ModelGroup#annotations()}</td>
 *       <td>{annotations}</td>
 *       <td>A sequence of Annotation components.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link ModelGroup#compositor()}</td>
 *       <td>{compositor}</td>
 *       <td>One of {all, choice, sequence}. Required.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link ModelGroup#particles()}</td>
 *       <td>{particles}</td>
 *       <td>A sequence of Particle components.</td>
 *     </tr>
 *   </tbody>
 * </table>
 */
public class ModelGroup implements Term {

	/** Model group compositor */
	public enum Compositor {

		/** Model group compositor all */
		ALL,
		/** Model group compositor choice */
		CHOICE,
		/** Model group compositor sequence */
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

	/** @return The ·actual value· of the name [attribute] */
	public String name() {
		return name;
	}

	/** @return The ·actual value· of the targetNamespace [attribute] of the &lt;schema&gt; ancestor element information item if present, otherwise ·absent·. */
	public String targetNamespace() {
		return targetNamespace;
	}

	/** @return A model group which is the {term} of a particle corresponding to the &lt;all&gt;, &lt;choice&gt; or &lt;sequence&gt; among the [children] (there must be exactly one). */
	public ModelGroup modelGroup() {
		return modelGroup.get();
	}

	/** @return One of all, choice, sequence depending on the element information item. */
	public Compositor compositor() {
		return compositor.get();
	}

	/** @return A sequence of particles corresponding to all the &lt;all&gt;, &lt;choice&gt;, &lt;sequence&gt;, &lt;any&gt;, &lt;group&gt; or &lt;element&gt; items among the [children], in order. */
	public Deque<Particle> particles() {
		return Deques.unmodifiableDeque(particles);
	}

	/** @return The ·annotation mapping· of the &lt;group&gt;, &lt;all&gt;, &lt;choice&gt;, or &lt;sequence&gt; element, whichever is present, as defined in XML Representation of Annotation Schema Components (§3.15.2). */
	@Override
	public Deque<Annotation> annotations() {
		return Deques.unmodifiableDeque(annotations);
	}

}
