package xs.parser;

import java.util.*;
import org.w3c.dom.*;
import xs.parser.internal.*;
import xs.parser.internal.SequenceParser.*;

/**
 * <pre>
 * &lt;notation
 *   id = ID
 *   name = NCName
 *   public = token
 *   system = anyURI
 *   {any attributes with non-schema namespace . . .}&gt;
 *   Content: (annotation?)
 * &lt;/notation&gt;
 * </pre>
 */
public class Notation implements AnnotatedComponent {

	protected static final SequenceParser parser = new SequenceParser()
			.requiredAttributes(AttributeValue.NAME)
			.optionalAttributes(AttributeValue.ID, AttributeValue.PUBLIC, AttributeValue.SYSTEM)
			.elements(0, 1, ElementValue.ANNOTATION);

	private final Node node;
	private final Deque<Annotation> annotations;
	private final String name;
	private final String targetNamespace;
	private final String publicIdentiifer;
	private final String systemIdentifier;

	private Notation(final Node node, final Deque<Annotation> annotations, final String name, final String targetNamespace, final String publicId, final String systemId) {
		this.node = Objects.requireNonNull(node);
		this.annotations = Objects.requireNonNull(annotations);
		this.name = name;
		this.targetNamespace = NodeHelper.validateTargetNamespace(targetNamespace);
		this.publicIdentiifer = publicId;
		this.systemIdentifier = systemId;
	}

	protected static Notation parse(final Result result) {
		final String name = result.value(AttributeValue.NAME);
		final String targetNamespace = result.schema().targetNamespace();
		final String publicId = result.value(AttributeValue.PUBLIC);
		final String systemId = result.value(AttributeValue.SYSTEM);
		return new Notation(result.node(), result.annotations(), name, targetNamespace, publicId, systemId);
	}

	/** @return The ·actual value· of the name [attribute] */
	public String name() {
		return name;
	}

	/** @return The ·actual value· of the targetNamespace [attribute] of the &lt;schema&gt; ancestor element information item if present, otherwise ·absent·. */
	public String targetNamespace() {
		return targetNamespace;
	}

	/** @return The ·actual value· of the system [attribute], if present, otherwise ·absent·. */
	public String systemIdentifier() {
		return systemIdentifier;
	}

	/** @return The ·actual value· of the public [attribute], if present, otherwise ·absent·. */
	public String publicIdentiifer() {
		return publicIdentiifer;
	}

	@Override
	public Node node() {
		return node;
	}

	/** @return The ·annotation mapping· of the &lt;notation&gt; element, as defined in XML Representation of Annotation Schema Components (§3.15.2). */
	@Override
	public Deque<Annotation> annotations() {
		return Deques.unmodifiableDeque(annotations);
	}

}