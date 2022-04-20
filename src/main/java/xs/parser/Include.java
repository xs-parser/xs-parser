package xs.parser;

import java.net.*;
import java.util.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;
import xs.parser.Schema.*;
import xs.parser.internal.*;
import xs.parser.internal.SequenceParser.*;

/**
 * <pre>
 * &lt;include
 *   id = ID
 *   schemaLocation = anyURI
 *   {any attributes with non-schema namespace . . .}&gt;
 *   Content: (annotation?)
 * &lt;/include&gt;
 * </pre>
 */
public class Include {

	static final String RESOURCE_PATH = Import.RESOURCE_PATH;
	protected static final SequenceParser parser = new SequenceParser()
			.requiredAttributes(AttributeValue.SCHEMALOCATION)
			.optionalAttributes(AttributeValue.ID)
			.elements(0, 1, ElementValue.ANNOTATION);
	// Stylesheet for Chameleon Inclusion (F.1)
	protected static final Object f1Xslt = SaxonProcessor.compileTemplate(new StreamSource(Include.class.getClassLoader().getResourceAsStream(RESOURCE_PATH + "F-1.xsl")));

	private final Schema schema;
	private final Node node;
	private final Deque<Annotation> annotations;
	private final String schemaLocation;
	private final Deferred<Schema> includedSchema = Deferred.of(this::includeSchema);
	private final boolean shouldCache;

	Include(final Schema schema, final Node node, final Deque<Annotation> annotations, final String schemaLocation, final boolean shouldCache) {
		this.schema = schema;
		this.node = Objects.requireNonNull(node);
		this.annotations = Objects.requireNonNull(annotations);
		this.schemaLocation = schemaLocation;
		this.shouldCache = shouldCache;
	}

	protected static Include parse(final Result result) {
		final String schemaLocation = result.value(AttributeValue.SCHEMALOCATION);
		return new Include(result.schema(), result.node(), result.annotations(), schemaLocation, true);
	}

	private Schema includeSchema() {
		try {
			final String expectedTargetNamespace = schema.targetNamespace();
			final Schema resultSchema = schema.findSchema(new DocumentResolver() {

				@Override
				public URI resolveUri(final String baseUri, final String namespace, final String schemaLocation) {
					return schema.documentResolver().resolveUri(baseUri, namespace, schemaLocation);
				}

				@Override
				public Document resolve(final URI resourceUri) throws Exception {
					final Document resolvedDocument = schema.documentResolver().resolve(resourceUri);
					if (resolvedDocument != null) {
						final Document transformed = transformDocument(resolveDocument(resolvedDocument));
						if (transformed.getDocumentURI() == null) {
							transformed.setDocumentURI(resolvedDocument.getDocumentURI());
						}
						return transformed;
					}
					return null;
				}

			}, shouldCache, expectedTargetNamespace, schemaLocation);
			if (Objects.equals(expectedTargetNamespace, resultSchema.targetNamespace())) {
				return resultSchema;
			} else {
				throw new SchemaParseException(node, "targetNamespace " + NodeHelper.toStringNamespace(resultSchema.targetNamespace()) + " of the included schema must match the targetNamespace " + NodeHelper.toStringNamespace(expectedTargetNamespace) + " of the current schema");
			}
		} catch (final SchemaParseException e) {
			throw e;
		} catch (final Exception e) {
			Reporting.report("Could not resolve " + node.getNodeName() + ", caused by " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
		}
		return Schema.EMPTY;
	}

	protected Document resolveDocument(final Document resolvedDocument) throws Exception {
		final Node childTargetNamespace = resolvedDocument.getDocumentElement().getAttributes().getNamedItemNS(null, AttributeValue.TARGETNAMESPACE.getName().getLocalPart());
		final String parentTargetNamespace = schema.targetNamespace();
		if (childTargetNamespace == null && parentTargetNamespace != null) {
			final Map<String, Object> params = new HashMap<>();
			final String prefixForTargetNamespace = node().lookupPrefix(parentTargetNamespace);
			if (prefixForTargetNamespace != null) {
				params.put("prefixForTargetNamespace", prefixForTargetNamespace);
			}
			try {
				params.put("newTargetNamespace", new URI(parentTargetNamespace));
			} catch (final URISyntaxException e) {
				throw new SchemaParseException(node, e);
			}
			return SaxonProcessor.transform(f1Xslt, resolvedDocument, params, null);
		}
		return resolvedDocument;
	}

	protected Document transformDocument(final Document doc) {
		// Overridden by xs:override & xs:redefine
		return doc;
	}

	protected Node node() {
		return node;
	}

	protected Deque<Annotation> annotations() {
		return annotations;
	}

	protected String schemaLocation() {
		return schemaLocation;
	}

	protected Schema includedSchema() {
		return includedSchema.get();
	}

}