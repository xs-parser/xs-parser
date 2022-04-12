package xs.parser;

import java.util.*;
import javax.xml.namespace.*;
import xs.parser.Annotation.*;
import xs.parser.internal.*;
import xs.parser.internal.SequenceParser.*;

/**
 * <pre>
 * &lt;extension
 *   base = QName
 *   id = ID
 *   {any attributes with non-schema namespace . . .}&gt;
 *   Content: (annotation?, ((attribute | attributeGroup)*, anyAttribute?), assert*)
 * &lt;/extension&gt;
 * </pre>
 */
public class SimpleExtension {

	protected static final SequenceParser parser = new SequenceParser()
			.requiredAttributes(AttributeValue.BASE)
			.optionalAttributes(AttributeValue.ID)
			.elements(0, 1, ElementValue.ANNOTATION)
			.elements(0, Integer.MAX_VALUE, ElementValue.ATTRIBUTE, ElementValue.ATTRIBUTEGROUP)
			.elements(0, 1, ElementValue.ANYATTRIBUTE)
			.elements(0, Integer.MAX_VALUE, ElementValue.ASSERT);

	private final Deque<Annotation> annotations;
	private final Deferred<? extends TypeDefinition> base;
	private final Deferred<Deque<AttributeUse>> attributeUses;
	private final Wildcard attributeWildcard;
	private final Deque<Assertion> asserts;

	SimpleExtension(final Deque<Annotation> annotations, final Deferred<? extends TypeDefinition> base, final Deferred<Deque<AttributeUse>> attributeUses, final Wildcard attributeWildcard, final Deque<Assertion> asserts) {
		this.annotations = Objects.requireNonNull(annotations);
		this.base = Objects.requireNonNull(base);
		this.attributeUses = Objects.requireNonNull(attributeUses);
		this.attributeWildcard = attributeWildcard;
		this.asserts = Objects.requireNonNull(asserts);
	}

	protected static SimpleExtension parse(final Result result) {
		final QName baseType = result.value(AttributeValue.BASE);
		final Deferred<? extends TypeDefinition> base = result.schema().find(baseType, TypeDefinition.class);
		final Deque<AttributeGroup> attributeGroups = result.parseAll(ElementValue.ATTRIBUTEGROUP);
		final Deferred<Deque<AttributeUse>> attributeUses = AttributeGroup.findAttributeUses(result.parseAll(ElementValue.ATTRIBUTE), attributeGroups);
		final Deque<Annotation> annotations = new AnnotationsBuilder(result).add(attributeGroups).build();
		final Wildcard attributeWildcard = result.parse(ElementValue.ANYATTRIBUTE);
		final Deque<Assertion> asserts = result.parseAll(ElementValue.ASSERT);
		return new SimpleExtension(annotations, base, attributeUses, attributeWildcard, asserts);
	}

	protected Deque<Annotation> annotations() {
		return annotations;
	}

	protected TypeDefinition base() {
		return base.get();
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