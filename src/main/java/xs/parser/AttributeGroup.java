package xs.parser;

import java.util.*;
import javax.xml.namespace.*;
import org.w3c.dom.*;
import xs.parser.internal.*;
import xs.parser.internal.SequenceParser.*;

/**
 * <pre>
 * &lt;attributeGroup
 *   id = ID
 *   name = NCName
 *   ref = QName
 *   {any attributes with non-schema namespace . . .}&gt;
 *   Content: (annotation?, ((attribute | attributeGroup)*, anyAttribute?))
 * &lt;/attributeGroup&gt;
 * </pre>
 *
 * <table>
 *   <caption style="font-size: large; text-align: left">Schema Component: Schema, a kind of Annotated Component</caption>
 *   <thead>
 *     <tr>
 *       <th style="text-align: left">Method</th>
 *       <th style="text-align: left">Property</th>
 *       <th style="text-align: left">Representation</th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <td>{@link AttributeGroup#annotations()}</td>
 *       <td>{annotations}</td>
 *       <td>A sequence of Annotation components.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link AttributeGroup#name()}</td>
 *       <td>{name}</td>
 *       <td>An xs:NCName value. Required.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link AttributeGroup#targetNamespace()}</td>
 *       <td>{target namespace}</td>
 *       <td>An xs:anyURI value. Optional.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link AttributeGroup#attributeUses()}</td>
 *       <td>{attribute uses}</td>
 *       <td>A set of Attribute Use components.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link AttributeGroup#attributeWildcard()}</td>
 *       <td>{attribute wildcard}</td>
 *       <td>A Wildcard component. Optional.</td>
 *     </tr>
 *   </tbody>
 * </table>
 */
public class AttributeGroup implements AnnotatedComponent {

	protected static final SequenceParser parser = new SequenceParser()
			.optionalAttributes(AttributeValue.ID, AttributeValue.REF, AttributeValue.NAME)
			.elements(0, 1, ElementValue.ANNOTATION)
			.elements(0, Integer.MAX_VALUE, ElementValue.ATTRIBUTE, ElementValue.ATTRIBUTEGROUP)
			.elements(0, 1, ElementValue.ANYATTRIBUTE);

	private final Node node;
	private final Deque<Annotation> annotations;
	private final String name;
	private final String targetNamespace;
	private final Deferred<Deque<AttributeUse>> attributeUses;
	private final Wildcard attributeWildcard;

	private AttributeGroup(final Node node, final Deque<Annotation> annotations, final String name, final String targetNamespace, final Deferred<Deque<AttributeUse>> attributeUses, final Wildcard attributeWildcard) {
		this.node = Objects.requireNonNull(node);
		this.annotations = Objects.requireNonNull(annotations);
		this.name = name;
		this.targetNamespace = NodeHelper.validateTargetNamespace(targetNamespace);
		this.attributeUses = Objects.requireNonNull(attributeUses);
		this.attributeWildcard = attributeWildcard;
	}

	static Deferred<Deque<AttributeUse>> findAttributeUses(final Deque<AttributeUse> attributes, final Deque<AttributeGroup> attributeGroups) {
		if (attributeGroups.isEmpty()) {
			return Deferred.value(attributes);
		}
		return Deferred.of(() -> {
			int size = attributes.size();
			for (final AttributeGroup a : attributeGroups) {
				size += a.attributeUses().size();
			}
			final DeferredArrayDeque<AttributeUse> attributeUses = new DeferredArrayDeque<>(size);
			attributeUses.addAll(attributes);
			for (final AttributeGroup a : attributeGroups) {
				attributeUses.addAll(a.attributeUses());
			}
			return attributeUses;
		});
	}

	protected static AttributeGroup parse(final Result result) {
		final String targetNamespace = result.schema().targetNamespace();
		final QName refAttr = result.value(AttributeValue.REF);
		if (refAttr != null) {
			final Deferred<AttributeGroup> ref = result.schema().find(refAttr, AttributeGroup.class);
			return new AttributeGroup(result.node(), result.annotations(), null, targetNamespace, Deferred.none(), null) {

				@Override
				public Deque<AttributeUse> attributeUses() {
					return ref.get().attributeUses();
				}

				@Override
				public Wildcard attributeWildcard() {
					return ref.get().attributeWildcard();
				}

				@Override
				public String name() {
					return ref.get().name();
				}

			};
		}
		final String name = result.value(AttributeValue.NAME);
		final Deque<AttributeUse> attributes = result.parseAll(ElementValue.ATTRIBUTE);
		final Deque<AttributeGroup> attributeGroups = result.parseAll(ElementValue.ATTRIBUTEGROUP);
		final Deferred<Deque<AttributeUse>> attributeUses = findAttributeUses(attributes, attributeGroups);
		final Wildcard attributeWildcard = result.parse(ElementValue.ANYATTRIBUTE);
		return new AttributeGroup(result.node(), result.annotations(), name, targetNamespace, attributeUses, attributeWildcard);
	}

	/** @return The ·actual value· of the name [attribute] */
	public String name() {
		return name;
	}

	/** @return The ·actual value· of the targetNamespace [attribute] of the &lt;schema&gt; ancestor element information item if present, otherwise ·absent·. */
	public String targetNamespace() {
		return targetNamespace;
	}

	/** @return The union of the set of attribute uses corresponding to the &lt;attribute&gt; [children], if any, with the {attribute uses} of the attribute groups ·resolved· to by the ·actual value·s of the ref [attribute] of the &lt;attributeGroup&gt; [children], if any. Note: As described below, circular references from &lt;attributeGroup&gt; to &lt;attributeGroup&gt; are not errors. */
	public Deque<AttributeUse> attributeUses() {
		return Deques.unmodifiableDeque(attributeUses.get());
	}

	/** @return The Wildcard determined by applying the attribute-wildcard mapping described in Common Rules for Attribute Wildcards (§3.6.2.2) to the &lt;attributeGroup&gt; element information item. */
	public Wildcard attributeWildcard() {
		return attributeWildcard;
	}

	@Override
	public Node node() {
		return node;
	}

	/** @return The ·annotation mapping· of the &lt;attributeGroup&gt; element and its &lt;attributeGroup&gt; [children], if present, as defined in XML Representation of Annotation Schema Components (§3.15.2). */
	@Override
	public Deque<Annotation> annotations() {
		return Deques.unmodifiableDeque(annotations);
	}

}