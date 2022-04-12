package xs.parser;

import java.util.*;
import javax.xml.namespace.*;
import xs.parser.Annotation.*;
import xs.parser.internal.*;
import xs.parser.internal.SequenceParser.*;

/**
 * <pre>
 * &lt;restriction
 *   base = QName
 *   id = ID
 *   {any attributes with non-schema namespace . . .}&gt;
 *   Content: (annotation?, openContent?, (group | all | choice | sequence)?, ((attribute | attributeGroup)*, anyAttribute?), assert*)
 * &lt;/restriction&gt;
 *
 * &lt;extension
 *   base = QName
 *   id = ID
 *   {any attributes with non-schema namespace . . .}&gt;
 *   Content: (annotation?, openContent?, ((group | all | choice | sequence)?, ((attribute | attributeGroup)*, anyAttribute?), assert*))
 * &lt;/extension&gt;
 * </pre>
 */
public class ComplexDerivation {

	protected static final SequenceParser parser = new SequenceParser()
			.requiredAttributes(AttributeValue.BASE)
			.optionalAttributes(AttributeValue.ID)
			.elements(0, 1, ElementValue.ANNOTATION)
			.elements(0, 1, ElementValue.OPENCONTENT)
			.elements(0, 1, ElementValue.GROUP, ElementValue.ALL, ElementValue.CHOICE, ElementValue.SEQUENCE)
			.elements(0, Integer.MAX_VALUE, ElementValue.ATTRIBUTE, ElementValue.ATTRIBUTEGROUP)
			.elements(0, 1, ElementValue.ANYATTRIBUTE)
			.elements(0, Integer.MAX_VALUE, ElementValue.ASSERT);

	private final Deque<Annotation> annotations;
	private final Deferred<ComplexType> base;
	private final OpenContent openContent;
	private final Particle<ModelGroup> group;
	private final Particle<ModelGroup> all;
	private final Particle<ModelGroup> choice;
	private final Particle<ModelGroup> sequence;
	private final Deferred<Deque<AttributeUse>> attributeUses;
	private final Wildcard attributeWildcard;
	private final Deque<Assertion> asserts;

	ComplexDerivation(final Deque<Annotation> annotations, final Deferred<ComplexType> base, final Particle<ModelGroup> group, final Particle<ModelGroup> all, final Particle<ModelGroup> choice, final Particle<ModelGroup> sequence, final OpenContent openContent, final Deferred<Deque<AttributeUse>> attributeUses, final Wildcard attributeWildcard, final Deque<Assertion> asserts) {
		this.annotations = Objects.requireNonNull(annotations);
		this.base = Objects.requireNonNull(base);
		this.group = group;
		this.all = all;
		this.choice = choice;
		this.sequence = sequence;
		this.openContent = openContent;
		this.attributeUses = Objects.requireNonNull(attributeUses);
		this.attributeWildcard = attributeWildcard;
		this.asserts = Objects.requireNonNull(asserts);
	}

	protected static ComplexDerivation parse(final Result result) {
		final QName baseType = result.value(AttributeValue.BASE);
		final Deferred<ComplexType> base = result.schema().find(baseType, ComplexType.class);
		final Particle<ModelGroup> group = result.parse(ElementValue.GROUP);
		final Particle<ModelGroup> all = result.parse(ElementValue.ALL);
		final Particle<ModelGroup> choice = result.parse(ElementValue.CHOICE);
		final Particle<ModelGroup> sequence = result.parse(ElementValue.SEQUENCE);
		final OpenContent openContent = result.parse(ElementValue.OPENCONTENT);
		final Deque<AttributeGroup> attributeGroups = result.parseAll(ElementValue.ATTRIBUTEGROUP);
		final Deferred<Deque<AttributeUse>> attributeUses = AttributeGroup.findAttributeUses(result.parseAll(ElementValue.ATTRIBUTE), attributeGroups);
		final AnnotationsBuilder annotations = new AnnotationsBuilder(result).add(attributeGroups);
		if (openContent != null) {
			annotations.add(openContent::annotations);
		}
		final Wildcard attributeWildcard = result.parse(ElementValue.ANYATTRIBUTE);
		final Deque<Assertion> asserts = result.parseAll(ElementValue.ASSERT);
		return new ComplexDerivation(annotations.build(), base, group, all, choice, sequence, openContent, attributeUses, attributeWildcard, asserts);
	}

	protected Deque<Annotation> annotations() {
		return annotations;
	}

	protected ComplexType base() {
		return base.get();
	}

	protected OpenContent openContent() {
		return openContent;
	}

	protected Particle<ModelGroup> group() {
		return group;
	}

	protected Particle<ModelGroup> all() {
		return all;
	}

	protected Particle<ModelGroup> choice() {
		return choice;
	}

	protected Particle<ModelGroup> sequence() {
		return sequence;
	}

	protected Deferred<Deque<AttributeUse>> attributeUses() {
		return attributeUses;
	}

	protected Wildcard attributeWildcard() {
		return attributeWildcard;
	}

	protected Deque<Assertion> asserts() {
		return asserts;
	}

}
