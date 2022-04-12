package xs.parser;

import java.util.*;
import javax.xml.*;
import org.w3c.dom.*;
import xs.parser.internal.*;
import xs.parser.internal.SequenceParser.*;

/**
 * <pre>
 * &lt;import
 *   id = ID
 *   namespace = anyURI
 *   schemaLocation = anyURI
 *   {any attributes with non-schema namespace . . .}&gt;
 *   Content: (annotation?)
 * &lt;/import&gt;
 * </pre>
 */
public class Import {

	static final String RESOURCE_PATH = "xs/parser/";
	protected static final SequenceParser parser = new SequenceParser()
			.optionalAttributes(AttributeValue.ID, AttributeValue.NAMESPACE, AttributeValue.SCHEMALOCATION)
			.elements(0, 1, ElementValue.ANNOTATION);

	private final Schema schema;
	private final Node node;
	private final Deque<Annotation> annotations;
	private final String namespace;
	private final String schemaLocation;
	private final Deferred<Schema> importedSchema = Deferred.of(this::importSchema);

	private Import(final Schema schema, final Node node, final Deque<Annotation> annotations, final String namespace, final String schemaLocation) {
		this.schema = schema;
		this.node = Objects.requireNonNull(node);
		this.annotations = Objects.requireNonNull(annotations);
		if (schema.targetNamespace() != null && schema.targetNamespace().equals(namespace)) {
			throw new SchemaParseException(node, "Failed to resolve import: @namespace=" + namespace + " may not match the xs:schema/@targetNamespace=" + schema.targetNamespace() + " it is imported by");
		}
		if (XMLConstants.NULL_NS_URI.equals(namespace)) {
			throw new SchemaParseException(node, "@namespace must be absent or non-empty");
		}
		this.namespace = namespace;
		this.schemaLocation = schemaLocation;
	}

	protected static Import parse(final Result result) {
		final String namespace = result.value(AttributeValue.NAMESPACE);
		final String schemaLocation = result.value(AttributeValue.SCHEMALOCATION);
		return new Import(result.schema(), result.node(), result.annotations(), namespace, schemaLocation);
	}

	private Schema importSchema() {
		try {
			final Schema resultSchema = schema.findSchema(schema.documentResolver(), true, namespace, schemaLocation);
			if (resultSchema.targetNamespace() == null && namespace == null || namespace.equals(resultSchema.targetNamespace())) {
				return resultSchema;
			}
			throw new SchemaParseException(node, "Failed to resolve xs:import, @namespace=" + namespace + " does not match expected xs:schema/@targetNamespace=" + resultSchema.targetNamespace());
		} catch (final SchemaParseException e) {
			throw e;
		} catch (final Exception e) {
			Reporting.report("Failed to resolve xs:import, " + e.getClass().getName() + " " + e.getMessage(), e);
		}
		return Schema.EMPTY;
	}

	protected Deque<Annotation> annotations() {
		return annotations;
	}

	protected String namespace() {
		return namespace;
	}

	protected String schemaLocation() {
		return schemaLocation;
	}

	protected Schema importedSchema() {
		return importedSchema.get();
	}

}