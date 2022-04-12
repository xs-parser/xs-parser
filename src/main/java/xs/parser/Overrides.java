package xs.parser;

import java.util.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;
import xs.parser.internal.*;
import xs.parser.internal.SequenceParser.*;

/**
 * <pre>
 * &lt;override
 *   id = ID
 *   schemaLocation = anyURI
 *   {any attributes with non-schema namespace . . .}&gt;
 *   Content: (annotation | (simpleType | complexType | group | attributeGroup | element | attribute | notation))*
 * &lt;/override&gt;
 * </pre>
 */
public class Overrides extends Redefine {

	protected static final SequenceParser parser = new SequenceParser()
			.requiredAttributes(AttributeValue.SCHEMALOCATION)
			.optionalAttributes(AttributeValue.ID)
			.elements(0, Integer.MAX_VALUE, ElementValue.ANNOTATION, ElementValue.SIMPLETYPE, ElementValue.COMPLEXTYPE, ElementValue.GROUP_DECL, ElementValue.ATTRIBUTEGROUP, ElementValue.ELEMENT_DECL, ElementValue.ATTRIBUTE_DECL, ElementValue.NOTATION);
	// Stylesheet for xs:override (F.2)
	protected static final Object f2Xslt = SaxonProcessor.compileTemplate(new StreamSource(Include.class.getClassLoader().getResourceAsStream(RESOURCE_PATH + "F-2.xsl")));

	private final Deque<Element> elements;
	private final Deque<Attribute> attributes;
	private final Deque<Notation> notations;

	private Overrides(final Schema schema, final Node node, final Deque<Annotation> annotations, final String schemaLocation, final Deque<SimpleType> simpleTypes, final Deque<ComplexType> complexTypes, final Deque<ModelGroup> groups, final Deque<AttributeGroup> attributeGroups, final Deque<Element> elements, final Deque<Attribute> attributes, final Deque<Notation> notations) {
		super(schema, node, annotations, schemaLocation, simpleTypes, complexTypes, groups, attributeGroups);
		this.elements = Objects.requireNonNull(elements);
		this.attributes = Objects.requireNonNull(attributes);
		this.notations = Objects.requireNonNull(notations);
	}

	protected static Overrides parse(final Result result) {
		final String schemaLocation = result.value(AttributeValue.SCHEMALOCATION);
		final Deque<SimpleType> simpleTypes = result.parseAll(ElementValue.SIMPLETYPE);
		final Deque<ComplexType> complexTypes = result.parseAll(ElementValue.COMPLEXTYPE);
		final Deque<ModelGroup> groups = result.parseAll(ElementValue.GROUP_DECL);
		final Deque<AttributeGroup> attributeGroups = result.parseAll(ElementValue.ATTRIBUTEGROUP);
		final Deque<Element> elements = result.parseAll(ElementValue.ELEMENT_DECL);
		final Deque<Attribute> attributes = result.parseAll(ElementValue.ATTRIBUTE_DECL);
		final Deque<Notation> notations = result.parseAll(ElementValue.NOTATION);
		return new Overrides(result.schema(), result.node(), result.annotations(), schemaLocation, simpleTypes, complexTypes, groups, attributeGroups, elements, attributes, notations);
	}

	@Override
	protected Document transformDocument(final Document doc) {
		final Map<String, Object> params = new HashMap<>();
		params.put("overrideElement", node());
		params.put("overriddenSchema", doc.getDocumentElement());
		return SaxonProcessor.transform(f2Xslt, doc, params, "perform-override");
	}

	public Deque<Element> elements() {
		return Deques.unmodifiableDeque(elements);
	}

	public Deque<Attribute> attributes() {
		return Deques.unmodifiableDeque(attributes);
	}

	public Deque<Notation> notations() {
		return Deques.unmodifiableDeque(notations);
	}

}
