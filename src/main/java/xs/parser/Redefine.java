package xs.parser;

import java.util.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;
import xs.parser.internal.*;
import xs.parser.internal.SequenceParser.*;

/**
 * <pre>
 * &lt;redefine
 *   id = ID
 *   schemaLocation = anyURI
 *   {any attributes with non-schema namespace . . .}&gt;
 *   Content: (annotation | (simpleType | complexType | group | attributeGroup))*
 * &lt;/redefine&gt;
 * </pre>
 */
public class Redefine extends Include {

	protected static final SequenceParser parser = new SequenceParser()
			.requiredAttributes(AttributeValue.SCHEMALOCATION)
			.optionalAttributes(AttributeValue.ID)
			.elements(0, Integer.MAX_VALUE, ElementValue.ANNOTATION, ElementValue.SIMPLETYPE, ElementValue.COMPLEXTYPE, ElementValue.GROUP_DECL, ElementValue.ATTRIBUTEGROUP);
	// Stylesheet for xs:redefine
	protected static final Object redefineXslt = SaxonProcessor.compileTemplate(new StreamSource(Include.class.getClassLoader().getResourceAsStream(RESOURCE_PATH + "xs-redefine.xsl")));

	private final Deque<SimpleType> simpleTypes;
	private final Deque<ComplexType> complexTypes;
	private final Deque<ModelGroup> groups;
	private final Deque<AttributeGroup> attributeGroups;

	Redefine(final Schema schema, final Node node, final Deque<Annotation> annotations, final String schemaLocation, final Deque<SimpleType> simpleTypes, final Deque<ComplexType> complexTypes, final Deque<ModelGroup> groups, final Deque<AttributeGroup> attributeGroups) {
		super(schema, node, annotations, schemaLocation, false);
		this.simpleTypes = Objects.requireNonNull(simpleTypes);
		this.complexTypes = Objects.requireNonNull(complexTypes);
		this.groups = Objects.requireNonNull(groups);
		this.attributeGroups = Objects.requireNonNull(attributeGroups);
	}

	protected static Redefine parse(final Result result) {
		final String schemaLocation = result.value(AttributeValue.SCHEMALOCATION);
		final Deque<SimpleType> simpleTypes = result.parseAll(ElementValue.SIMPLETYPE);
		final Deque<ComplexType> complexTypes = result.parseAll(ElementValue.COMPLEXTYPE);
		final Deque<ModelGroup> groups = result.parseAll(ElementValue.GROUP_DECL);
		final Deque<AttributeGroup> attributeGroups = result.parseAll(ElementValue.ATTRIBUTEGROUP);
		return new Redefine(result.schema(), result.node(), result.annotations(), schemaLocation, simpleTypes, complexTypes, groups, attributeGroups);
	}

	@Override
	protected Document transformDocument(final Document doc) {
		final Map<String, Object> params = new HashMap<>();
		params.put("redefineElement", node());
		params.put("redefinedSchema", doc.getDocumentElement());
		return SaxonProcessor.transform(redefineXslt, doc, params, "perform-redefine");
	}

	public Deque<SimpleType> simpleTypes() {
		return Deques.unmodifiableDeque(simpleTypes);
	}

	public Deque<ComplexType> complexTypes() {
		return Deques.unmodifiableDeque(complexTypes);
	}

	public Deque<ModelGroup> groups() {
		return Deques.unmodifiableDeque(groups);
	}

	public Deque<AttributeGroup> attributeGroups() {
		return Deques.unmodifiableDeque(attributeGroups);
	}

}
