package xs.parser;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.AbstractMap.*;
import java.util.function.*;
import javax.xml.*;
import javax.xml.namespace.*;
import javax.xml.parsers.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import xs.parser.Annotation.*;
import xs.parser.TypeDefinition.*;
import xs.parser.Wildcard.*;
import xs.parser.internal.*;
import xs.parser.internal.util.*;
import xs.parser.internal.util.SequenceParser.*;
import xs.parser.v.*;

/**
 * <pre>
 * &lt;schema
 *   attributeFormDefault = (qualified | unqualified) : unqualified
 *   blockDefault = (#all | List of (extension | restriction | substitution))  : ''
 *   defaultAttributes = QName
 *   xpathDefaultNamespace = (anyURI | (##defaultNamespace | ##targetNamespace | ##local))  : ##local
 *   elementFormDefault = (qualified | unqualified) : unqualified
 *   finalDefault = (#all | List of (extension | restriction | list | union))  : ''
 *   id = ID
 *   targetNamespace = anyURI
 *   version = token
 *   xml:lang = language
 *   {any attributes with non-schema namespace . . .}&gt;
 *   Content: ((include | import | redefine | override | annotation)*, (defaultOpenContent, annotation*)?, ((simpleType | complexType | group | attributeGroup | element | attribute | notation), annotation*)*)
 * &lt;/schema&gt;
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
 *       <td>{@link Schema#annotations()}</td>
 *       <td>{annotations}</td>
 *       <td>A sequence of Annotation components.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link Schema#typeDefinitions()}</td>
 *       <td>{type definitions}</td>
 *       <td>A set of Type Definition components.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link Schema#attributeDeclarations()}</td>
 *       <td>{attribute declarations}</td>
 *       <td>A set of Attribute Declaration components.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link Schema#elementDeclarations()}</td>
 *       <td>{element declarations}</td>
 *       <td>A set of Element Declaration components.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link Schema#attributeGroupDefinitions()}</td>
 *       <td>{attribute group definitions}</td>
 *       <td>A set of Attribute Group Definition components.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link Schema#modelGroupDefinitions()}</td>
 *       <td>{model group definitions}</td>
 *       <td>A set of Model Group Definition components.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link Schema#notationDeclarations()}</td>
 *       <td>{notation declarations}</td>
 *       <td>A set of Notation Declaration components.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link Schema#identityConstraintDefinitions()}</td>
 *       <td>{identity-constraint definitions}</td>
 *       <td>A set of Identity-Constraint Definition components.</td>
 *     </tr>
 *   </tbody>
 * </table>
 */
public class Schema implements AnnotatedComponent {

	public enum Block {

		DEFAULT(""),
		EXTENSION("extension"),
		RESTRICTION("restriction"),
		SUBSTITUTION("substitution"),
		ALL("#all");

		private final String name;

		private Block(final String name) {
			this.name = name;
		}

		private static Deque<Block> getAttrValueAsBlocks(final Attr attr) {
			final String value = NodeHelper.collapseWhitespace(attr.getValue());
			if ("#all".equals(value)) {
				return Deques.singletonDeque(Block.ALL);
			}
			final String[] values = value.split(NodeHelper.LIST_SEP);
			final Deque<Block> ls = new ArrayDeque<>();
			for (final String v : values) {
				final Block b = Block.getByName(v);
				if (Block.ALL.equals(b)) {
					throw NodeHelper.newParseException(attr, Block.ALL + " cannot be present in List");
				}
				ls.add(b);
			}
			return Deques.unmodifiableDeque(ls);
		}

		private static Block getAttrValueAsBlock(final Attr attr) {
			return getByName(NodeHelper.collapseWhitespace(attr.getValue()));
		}

		static Block getByName(final String name) {
			for (final Block b : values()) {
				if (b.getName().equals(name)) {
					return b;
				}
			}
			throw new IllegalArgumentException(name);
		}

		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return getName();
		}

	}

	public enum Form {

		UNQUALIFIED("unqualified"),
		QUALIFIED("qualified");

		private final String name;

		private Form(final String name) {
			this.name = name;
		}

		static Form getAttrValueAsForm(final Attr attr) {
			final String value = NodeHelper.collapseWhitespace(attr.getValue());
			for (final Form f : values()) {
				if (f.getName().equals(value)) {
					return f;
				}
			}
			throw new IllegalArgumentException(value);
		}

		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return getName();
		}

	}

	/**
	 * <pre>
	 * &lt;defaultOpenContent
	 *   appliesToEmpty = boolean : false
	 *   id = ID
	 *   mode = (interleave | suffix) : interleave
	 *   {any attributes with non-schema namespace . . .}&gt;
	 *   Content: (annotation?, any)
	 * &lt;/defaultOpenContent&gt;
	 * </pre>
	 */
	public static class DefaultOpenContent extends ComplexType.OpenContent {

		private static final SequenceParser parser = new SequenceParser()
				.optionalAttributes(AttrParser.APPLIES_TO_EMPTY, AttrParser.ID, AttrParser.MODE)
				.elements(0, 1, TagParser.ANNOTATION)
				.elements(1, 1, TagParser.ANY);

		private final boolean appliesToEmpty;

		private DefaultOpenContent(final AnnotationSet annotations, final boolean appliesToEmpty, final Mode mode, final Deferred<Any> wildcard) {
			super(annotations, mode, wildcard);
			this.appliesToEmpty = appliesToEmpty;
		}

		private static DefaultOpenContent parse(final Result result) {
			final AnnotationSet annotations = Annotation.of(result);
			final boolean appliesToEmpty = result.value(AttrParser.APPLIES_TO_EMPTY);
			final Mode mode = result.value(AttrParser.MODE);
			final Deferred<Any> wildcard = result.parse(TagParser.ANY);
			return new DefaultOpenContent(annotations, appliesToEmpty, mode, wildcard);
		}

		boolean appliesToEmpty() {
			return appliesToEmpty;
		}

	}

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
	public static class Import {

		private static final SequenceParser parser = new SequenceParser()
				.optionalAttributes(AttrParser.ID, AttrParser.NAMESPACE, AttrParser.SCHEMA_LOCATION)
				.elements(0, 1, TagParser.ANNOTATION);

		private final Schema schema;
		private final Node node;
		private final AnnotationSet annotations;
		private final String namespace;
		private final String schemaLocation;
		private final Deferred<Schema> importedSchema = Deferred.of(this::importSchema);

		private Import(final Schema schema, final Node node, final AnnotationSet annotations, final String namespace, final String schemaLocation) {
			this.schema = schema;
			this.node = Objects.requireNonNull(node);
			this.annotations = Objects.requireNonNull(annotations);
			if (schema.targetNamespace() != null && schema.targetNamespace().equals(namespace)) {
				throw new ParseException(node, "namespace " + NodeHelper.toStringNamespace(namespace) + " must not match the targetNamespace " + NodeHelper.toStringNamespace(schema.targetNamespace()) + " of the current schema");
			}
			if (XMLConstants.NULL_NS_URI.equals(namespace)) {
				throw new ParseException(node, "@namespace must be absent or non-empty");
			}
			this.namespace = namespace;
			this.schemaLocation = schemaLocation;
		}

		private static Import parse(final Result result) {
			final AnnotationSet annotations = Annotation.of(result);
			final String namespace = result.value(AttrParser.NAMESPACE);
			final String schemaLocation = result.value(AttrParser.SCHEMA_LOCATION);
			return new Import(result.schema(), result.node(), annotations, namespace, schemaLocation);
		}

		private Schema importSchema() {
			try {
				final Schema resultSchema = schema.findSchema(schema.documentResolver(), true, namespace, schemaLocation);
				if (Objects.equals(namespace, resultSchema.targetNamespace())) {
					return resultSchema;
				}
				throw new ParseException(node, "namespace " + NodeHelper.toStringNamespace(namespace) + " must match the targetNamespace " + NodeHelper.toStringNamespace(resultSchema.targetNamespace()) + " of the imported schema");
			} catch (final ParseException e) {
				throw e;
			} catch (final Exception e) {
				Reporting.report("Could not resolve xs:import, caused by " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
			}
			return Schema.EMPTY;
		}

		private Schema importedSchema() {
			return importedSchema.get();
		}

		AnnotationSet annotations() {
			return annotations;
		}

	}

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
	public static class Include {

		private static final SequenceParser parser = new SequenceParser()
				.requiredAttributes(AttrParser.SCHEMA_LOCATION)
				.optionalAttributes(AttrParser.ID)
				.elements(0, 1, TagParser.ANNOTATION);
		private static final String RESOURCE_PATH = "xs/parser/";
		// Stylesheet for Chameleon Inclusion (F.1)
		private static final Object f1Xslt = SaxonProcessor.compileTemplate(new StreamSource(Include.class.getClassLoader().getResourceAsStream(RESOURCE_PATH + "F-1.xsl")));

		private final Schema schema;
		private final Node node;
		private final AnnotationSet annotations;
		private final String schemaLocation;
		private final Deferred<Schema> includedSchema = Deferred.of(this::includeSchema);
		private final boolean shouldCache;

		private Include(final Schema schema, final Node node, final AnnotationSet annotations, final String schemaLocation, final boolean shouldCache) {
			this.schema = schema;
			this.node = Objects.requireNonNull(node);
			this.annotations = Objects.requireNonNull(annotations);
			this.schemaLocation = schemaLocation;
			this.shouldCache = shouldCache;
		}

		private static Include parse(final Result result) {
			final AnnotationSet annotations = Annotation.of(result);
			final String schemaLocation = result.value(AttrParser.SCHEMA_LOCATION);
			return new Include(result.schema(), result.node(), annotations, schemaLocation, true);
		}

		private Schema includeSchema() {
			try {
				final String expectedTargetNamespace = schema.targetNamespace();
				final Schema resultSchema = schema.findSchema(new DefaultDocumentResolver() {

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
					throw new ParseException(node, "targetNamespace " + NodeHelper.toStringNamespace(resultSchema.targetNamespace()) + " of the included schema must match the targetNamespace " + NodeHelper.toStringNamespace(expectedTargetNamespace) + " of the current schema");
				}
			} catch (final ParseException e) {
				throw e;
			} catch (final Exception e) {
				Reporting.report("Could not resolve " + node.getNodeName() + ", caused by " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
			}
			return Schema.EMPTY;
		}

		Document resolveDocument(final Document resolvedDocument) {
			final Node childTargetNamespace = resolvedDocument.getDocumentElement().getAttributes().getNamedItemNS(null, AttrParser.TARGET_NAMESPACE.getName().getLocalPart());
			final String parentTargetNamespace = schema.targetNamespace();
			if (childTargetNamespace == null && parentTargetNamespace != null) {
				final Map<String, Object> params = new HashMap<>();
				final String prefixForTargetNamespace = node.lookupPrefix(parentTargetNamespace);
				if (prefixForTargetNamespace != null) {
					params.put("prefixForTargetNamespace", prefixForTargetNamespace);
				}
				try {
					params.put("newTargetNamespace", new URI(parentTargetNamespace));
				} catch (final URISyntaxException e) {
					throw new ParseException(node, e);
				}
				return SaxonProcessor.transform(f1Xslt, resolvedDocument, params, null);
			}
			return resolvedDocument;
		}

		Document transformDocument(final Document doc) {
			// Overridden by xs:override & xs:redefine
			return doc;
		}

		AnnotationSet annotations() {
			return annotations;
		}

		Node node() {
			return node;
		}

		Schema includedSchema() {
			return includedSchema.get();
		}

	}

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
	public static class Overrides extends Redefine {

		private static final SequenceParser parser = new SequenceParser()
				.requiredAttributes(AttrParser.SCHEMA_LOCATION)
				.optionalAttributes(AttrParser.ID)
				.elements(0, Integer.MAX_VALUE, TagParser.ANNOTATION, TagParser.SIMPLE_TYPE, TagParser.COMPLEX_TYPE, TagParser.GROUP, TagParser.ATTRIBUTE_GROUP, TagParser.ELEMENT, TagParser.ATTRIBUTE, TagParser.NOTATION);
		// Stylesheet for xs:override (F.2)
		private static final Object f2Xslt = SaxonProcessor.compileTemplate(new StreamSource(Include.class.getClassLoader().getResourceAsStream(Include.RESOURCE_PATH + "F-2.xsl")));

		private Overrides(final Schema schema, final Node node, final AnnotationSet annotations, final String schemaLocation) {
			super(schema, node, annotations, schemaLocation);
		}

		private static Overrides parse(final Result result) {
			final AnnotationSet annotations = Annotation.of(result);
			final String schemaLocation = result.value(AttrParser.SCHEMA_LOCATION);
			return new Overrides(result.schema(), result.node(), annotations, schemaLocation);
		}

		@Override
		Document transformDocument(final Document doc) {
			final Map<String, Object> params = new HashMap<>();
			params.put("overrideElement", node());
			params.put("overriddenSchema", doc.getDocumentElement());
			return SaxonProcessor.transform(f2Xslt, doc, params, "perform-override");
		}

	}

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
	public static class Redefine extends Include {

		private static final SequenceParser parser = new SequenceParser()
				.requiredAttributes(AttrParser.SCHEMA_LOCATION)
				.optionalAttributes(AttrParser.ID)
				.elements(0, Integer.MAX_VALUE, TagParser.ANNOTATION, TagParser.SIMPLE_TYPE, TagParser.COMPLEX_TYPE, TagParser.GROUP, TagParser.ATTRIBUTE_GROUP);
		// Stylesheet for xs:redefine
		private static final Object redefineXslt = SaxonProcessor.compileTemplate(new StreamSource(Include.class.getClassLoader().getResourceAsStream(Include.RESOURCE_PATH + "xs-redefine.xsl")));

		private Redefine(final Schema schema, final Node node, final AnnotationSet annotations, final String schemaLocation) {
			super(schema, node, annotations, schemaLocation, false);
		}

		private static Redefine parse(final Result result) {
			final AnnotationSet annotations = Annotation.of(result);
			final String schemaLocation = result.value(AttrParser.SCHEMA_LOCATION);
			return new Redefine(result.schema(), result.node(), annotations, schemaLocation);
		}

		@Override
		Document transformDocument(final Document doc) {
			final Map<String, Object> params = new HashMap<>();
			params.put("redefineElement", node());
			params.put("redefinedSchema", doc.getDocumentElement());
			return SaxonProcessor.transform(redefineXslt, doc, params, "perform-redefine");
		}

	}

	private class Def<T extends SchemaComponent> {

		private final Deque<T> declared;
		private final Deque<T> constituents;
		private final Deque<T> all;

		Def(final Deque<T> declared, final Function<Schema, Def<T>> mapper) {
			this(declared, mapper, null);
		}

		Def(final Deque<T> declared, final Function<Schema, Def<T>> mapper, final Consumer<Deque<T>> after) {
			this.declared = declared;
			final Set<Schema> schemas = new LinkedHashSet<>();
			schemas.add(Schema.this);
			this.constituents = findAll(schemas, mapper);
			this.all = new DeferredArrayDeque<>(() -> {
				final Deque<T> x = new ArrayDeque<>(constituents);
				x.addAll(declared);
				if (after != null) {
					after.accept(x);
				}
				return x;
			});
		}

		Def() {
			this.declared = Deques.emptyDeque();
			this.constituents = Deques.emptyDeque();
			this.all = Deques.emptyDeque();
		}

		private <U extends SchemaComponent> Deque<U> findAll(final Set<Schema> schemas, final Function<Schema, Def<U>> mapper) {
			return new DeferredArrayDeque<>(constituentSchemas.map(c -> {
				if (c.isEmpty()) {
					return Deques.emptyDeque();
				}
				final Deque<U> x = new ArrayDeque<>();
				for (final Schema s : c) {
					if (schemas.add(s)) {
						final Def<U> def = mapper.apply(s);
						x.addAll(def.findAll(schemas, mapper));
						x.addAll(def.declared);
					}
				}
				return x;
			}));
		}

	}

	public interface DocumentResolver {

		public URI resolveUri(String baseUri, String namespace, String schemaLocation);

		public Document resolve(URI resourceUri) throws Exception;

		public Document newDocument(final InputSource source) throws Exception;

	}

	public static class DefaultDocumentResolver implements DocumentResolver {

		private final DocumentBuilder documentBuilder = NodeHelper.newDocumentBuilder();

		public URI resolveUri(final String baseUri, final String namespace, final String schemaLocation) {
			if (schemaLocation != null) {
				URI schemaLocationUri = null;
				try {
					schemaLocationUri = new URI(schemaLocation);
					return baseUri == null
							? schemaLocationUri
							: new URI(baseUri).resolve(schemaLocationUri);
				} catch (final URISyntaxException e) {
					return schemaLocationUri;
				}
			}
			return null;
		}

		public Document resolve(final URI resourceUri) throws Exception {
			if (resourceUri == null) {
				return null;
			} else if (resourceUri.getScheme() != null) {
				final InputSource source = new InputSource(resourceUri.toString());
				return newDocument(source);
			} else {
				final Path path = Paths.get(resourceUri.toString());
				if (Files.isRegularFile(path)) {
					try (final FileInputStream stream = new FileInputStream(path.toFile())) {
						final InputSource source = new InputSource(stream);
						source.setSystemId(resourceUri.toString());
						return newDocument(source);
					}
				} else {
					final InputStream stream = getClass().getResourceAsStream(resourceUri.toString());
					if (stream == null) {
						return null;
					}
					final InputSource source = new InputSource(stream);
					source.setSystemId(resourceUri.toString());
					return newDocument(source);
				}
			}
		}

		@Override
		public Document newDocument(final InputSource source) throws Exception {
			return documentBuilder.parse(source);
		}

	}

	public static class ParseException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		private final transient Node node;

		ParseException(final String message) {
			super(message);
			this.node = null;
		}

		ParseException(final Node node) {
			super(node != null ? NodeHelper.toString(node) : "");
			this.node = node;
		}

		ParseException(final Node node, final String message) {
			super(node != null ? formMessage(node, message) : message);
			this.node = node;
		}

		ParseException(final Node node, final Throwable cause) {
			super(node != null ? NodeHelper.toString(node) : null, cause);
			this.node = node;
		}

		private static String formMessage(final Node node, final String message) {
			final String nodeTypeName;
			final String nodeString;
			switch (node.getNodeType()) {
			case Node.ATTRIBUTE_NODE:
				nodeTypeName = "attribute";
				nodeString = node.getNodeName() + "=\"" + ((Attr) node).getValue() + '"';
				break;
			case Node.ELEMENT_NODE:
				nodeTypeName = "element";
				final StringBuilder builder = new StringBuilder();
				final NamedNodeMap attrs = node.getAttributes();
				for (int i = 0; i < attrs.getLength(); ++i) {
					final Attr attr = (Attr) attrs.item(i);
					builder.append(' ');
					builder.append(attr.getNodeName());
					builder.append("=\"");
					builder.append(attr.getValue());
					builder.append('"');
				}
				nodeString = '<' + node.getNodeName() + builder.toString() + '>';
				break;
			default:
				nodeTypeName = "node";
				nodeString = node.getNodeName();
				break;
			}
			return message + " of " + nodeTypeName + " '" + nodeString + "' in " + node.getOwnerDocument().getDocumentURI();
		}

		public Node node() {
			return node;
		}

	}

	static {
		NodeHelper.setNewParseException(ParseException::new);
		NodeHelper.setSchemaToOwnerDocument(s -> s.document);
		NodeHelper.setSchemaFindAllConstituentSchemas(Schema::findAllConstituentSchemas);
		Alternative.register();
		Annotation.register();
		Any.register();
		AnyAttribute.register();
		Assertion.register();
		Attribute.register();
		AttributeGroup.register();
		AttributeUse.register();
		ComplexType.register();
		ConstrainingFacet.register();
		Element.register();
		IdentityConstraint.register();
		ModelGroup.register();
		Notation.register();
		Particle.register();
		SimpleType.register();
		AttrParser.register(AttrParser.Names.APPLIES_TO_EMPTY, false);
		AttrParser.register(AttrParser.Names.ATTRIBUTE_FORM_DEFAULT, Form.class, Form.UNQUALIFIED, Form::getAttrValueAsForm);
		AttrParser.register(AttrParser.Names.BLOCK, Deque.class, Block.class, Block::getAttrValueAsBlocks);
		AttrParser.register(AttrParser.Names.BLOCK_DEFAULT, Block.class, Block.DEFAULT, Block::getAttrValueAsBlock);
		AttrParser.register(AttrParser.Names.DEFAULT_ATTRIBUTES, QName.class, NodeHelper::getAttrValueAsQName);
		AttrParser.register(AttrParser.Names.ELEMENT_FORM_DEFAULT, Form.class, Form.UNQUALIFIED, Form::getAttrValueAsForm);
		AttrParser.register(AttrParser.Names.FINAL, Deque.class, Final.class, Final::getAttrValueAsFinals);
		AttrParser.register(AttrParser.Names.FINAL_DEFAULT, Final.class, Final.DEFAULT, Final::getAttrValueAsFinal);
		AttrParser.register(AttrParser.Names.FORM, Form.class, null, Form::getAttrValueAsForm);
		AttrParser.register(AttrParser.Names.ID, NodeHelper::getAttrValueAsNCName);
		AttrParser.register(AttrParser.Names.NAMESPACE, NodeHelper::getAttrValueAsAnyUri);
		AttrParser.register(AttrParser.Names.SCHEMA_LOCATION, NodeHelper::getAttrValueAsAnyUri);
		AttrParser.register(AttrParser.Names.TARGET_NAMESPACE, NodeHelper::getAttrValueAsAnyUri);
		AttrParser.register(AttrParser.Names.VERSION, NodeHelper::getAttrValueAsToken);
		AttrParser.register(AttrParser.Names.XPATH_DEFAULT_NAMESPACE, Schema::getAttrValueAsXPathDefaultNamespace);
		TagParser.register(TagParser.Names.IMPORT, Import.parser, Import.class, Import::parse);
		TagParser.register(TagParser.Names.INCLUDE, Include.parser, Include.class, Include::parse);
		TagParser.register(TagParser.Names.OVERRIDE, Overrides.parser, Overrides.class, Overrides::parse);
		TagParser.register(TagParser.Names.REDEFINE, Redefine.parser, Redefine.class, Redefine::parse);
		TagParser.register(TagParser.Names.DEFAULT_OPEN_CONTENT, DefaultOpenContent.parser, DefaultOpenContent.class, DefaultOpenContent::parse);
		VisitorHelper.register(Schema.class, Schema::visit);
	}

	private static final DocumentResolver DEFAULT_DOCUMENT_RESOLVER = new DefaultDocumentResolver();
	private static final Map<Class<? extends SchemaComponent>, BiFunction<Schema, QName, Deferred<? extends SchemaComponent>>> FINDERS;
	private static final Schema EMPTY = new Schema();
	private static final String XPATH_DEFAULT_NAMESPACE_SCHEMA_DEFAULT = "##local";
	private static final SequenceParser parser = new SequenceParser()
			.optionalAttributes(AttrParser.ATTRIBUTE_FORM_DEFAULT, AttrParser.BLOCK_DEFAULT, AttrParser.DEFAULT_ATTRIBUTES, AttrParser.XPATH_DEFAULT_NAMESPACE, AttrParser.ELEMENT_FORM_DEFAULT, AttrParser.FINAL_DEFAULT, AttrParser.ID, AttrParser.TARGET_NAMESPACE, AttrParser.VERSION, AttrParser.XML_LANG)
			.elements(0, Integer.MAX_VALUE, TagParser.SCHEMA.imports(), TagParser.SCHEMA.include(), TagParser.SCHEMA.override(), TagParser.SCHEMA.redefine(), TagParser.ANNOTATION)
			.elements(0, 1, TagParser.SCHEMA.defaultOpenContent())
			.elements(0, Integer.MAX_VALUE, TagParser.ANNOTATION)
			.elements(0, Integer.MAX_VALUE, TagParser.SIMPLE_TYPE, TagParser.COMPLEX_TYPE, TagParser.GROUP, TagParser.ATTRIBUTE_GROUP, TagParser.ELEMENT, TagParser.ATTRIBUTE, TagParser.NOTATION, TagParser.ANNOTATION);
	static final Schema XSD = new Schema(NodeHelper.newSchemaDocument(XMLConstants.W3C_XML_SCHEMA_NS_URI)) {

		final Deque<TypeDefinition> typeDefinitions = new DeferredArrayDeque<>(() -> Deques.asDeque(ComplexType.xsAnyType(), SimpleType.xsAnySimpleType(), SimpleType.xsAnyAtomicType(), SimpleType.xsString(), SimpleType.xsBoolean(), SimpleType.xsFloat(), SimpleType.xsDouble(), SimpleType.xsDecimal(), SimpleType.xsDuration(), SimpleType.xsDateTime(), SimpleType.xsTime(), SimpleType.xsDate(), SimpleType.xsGYearMonth(), SimpleType.xsGYear(), SimpleType.xsGMonthDay(), SimpleType.xsGDay(), SimpleType.xsGMonth(), SimpleType.xsHexBinary(), SimpleType.xsBase64Binary(), SimpleType.xsAnyURI(), SimpleType.xsQName(), SimpleType.xsNOTATION(), SimpleType.xsNormalizedString(), SimpleType.xsToken(), SimpleType.xsLanguage(), SimpleType.xsIDREFS(), SimpleType.xsENTITIES(), SimpleType.xsNMTOKEN(), SimpleType.xsNMTOKENS(), SimpleType.xsName(), SimpleType.xsNCName(), SimpleType.xsID(), SimpleType.xsIDREF(), SimpleType.xsENTITY(), SimpleType.xsInteger(), SimpleType.xsNonPositiveInteger(), SimpleType.xsNegativeInteger(), SimpleType.xsLong(), SimpleType.xsInt(), SimpleType.xsShort(), SimpleType.xsByte(), SimpleType.xsNonNegativeInteger(), SimpleType.xsUnsignedLong(), SimpleType.xsUnsignedInt(), SimpleType.xsUnsignedShort(), SimpleType.xsUnsignedByte(), SimpleType.xsPositiveInteger(), SimpleType.xsYearMonthDuration(), SimpleType.xsDayTimeDuration(), SimpleType.xsDateTimeStamp()));

		@Override
		String location() {
			return "datatypes.xsd";
		}

		@Override
		public Deque<TypeDefinition> typeDefinitions() {
			return typeDefinitions;
		}

	};
	static final Schema XSI = new Schema(NodeHelper.newSchemaDocument(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI)) {

		final Deque<Attribute> attributeDeclarations = new DeferredArrayDeque<>(() -> Deques.asDeque(Attribute.xsiNil(), Attribute.xsiType(), Attribute.xsiSchemaLocation(), Attribute.xsiNoNamespaceSchemaLocation()));

		@Override
		public Deque<Attribute> attributeDeclarations() {
			return attributeDeclarations;
		}

	};

	private final Schema parent;
	private final DocumentResolver documentResolver;
	private final NamespaceContext namespaceContext;
	private final Document document;
	private final Map<Map.Entry<String, URI>, Document> schemaDocumentCache;
	private final Map<Document, Schema> schemaCache;
	private final Result result;
	private final Deque<Import> imports;
	private final Deque<Include> includes;
	private final Deque<Overrides> overrides;
	private final Deque<Redefine> redefines;
	private final Deferred<DefaultOpenContent> defaultOpenContent;
	private final Def<TypeDefinition> typeDefinitions;
	private final Def<Attribute> attributeDeclarations;
	private final Def<ModelGroup> modelGroupDefinitions;
	private final Def<AttributeGroup> attributeGroupDefinitions;
	private final Def<Element> elementDeclarations;
	private final Def<Notation> notationDeclarations;
	private final Deque<IdentityConstraint> identityConstraintDefinitions;
	private final Def<Annotation> annotations;
	private final Form attributeFormDefault;
	private final Block blockDefault;
	private final Deferred<AttributeGroup> defaultAttributes;
	private final String xpathDefaultNamespace;
	private final Form elementFormDefault;
	private final Final finalDefault;
	private final String version;
	private final String targetNamespace;
	private final String location;
	private final Deferred<Set<Schema>> constituentSchemas = Deferred.of(() -> {
		if (EMPTY == this) {
			return Collections.emptySet();
		}
		final Set<Schema> schemas = getConstituentSchemas();
		schemas.remove(this);
		return Collections.unmodifiableSet(schemas);
	});

	private Schema() {
		this.parent = this;
		this.documentResolver = DEFAULT_DOCUMENT_RESOLVER;
		this.namespaceContext = null;
		this.document = NodeHelper.newDocument();
		this.schemaDocumentCache = Collections.emptyMap();
		this.schemaCache = Collections.emptyMap();
		this.result = null;
		this.imports = Deques.emptyDeque();
		this.includes = Deques.emptyDeque();
		this.overrides = Deques.emptyDeque();
		this.redefines = Deques.emptyDeque();
		this.defaultOpenContent = null;
		this.typeDefinitions = new Def<>();
		this.attributeDeclarations = new Def<>();
		this.modelGroupDefinitions = new Def<>();
		this.attributeGroupDefinitions = new Def<>();
		this.elementDeclarations = new Def<>();
		this.notationDeclarations = new Def<>();
		this.identityConstraintDefinitions = Deques.emptyDeque();
		this.annotations = new Def<>();
		this.attributeFormDefault = AttrParser.ATTRIBUTE_FORM_DEFAULT.getDefaultValue();
		this.blockDefault = AttrParser.BLOCK_DEFAULT.getDefaultValue();
		this.defaultAttributes = null;
		this.xpathDefaultNamespace = XPATH_DEFAULT_NAMESPACE_SCHEMA_DEFAULT;
		this.elementFormDefault = AttrParser.ELEMENT_FORM_DEFAULT.getDefaultValue();
		this.finalDefault = AttrParser.FINAL_DEFAULT.getDefaultValue();
		this.version = null;
		this.targetNamespace = null;
		this.location = null;
	}

	private Schema(final Schema parent, final DocumentResolver documentResolver, final NamespaceContext namespaceContext, final Document document, final String location, final Map<Map.Entry<String, URI>, Document> schemaDocumentCache, final Map<Document, Schema> schemaCache) {
		this.parent = parent == null ? this : parent;
		this.documentResolver = documentResolver;
		this.namespaceContext = namespaceContext;
		this.document = document;
		this.location = location;
		this.schemaDocumentCache = schemaDocumentCache;
		this.schemaCache = schemaCache;
		this.result = parser.parse(this, document.getDocumentElement());
		this.imports = result.parseAll(TagParser.SCHEMA.imports());
		this.includes = result.parseAll(TagParser.SCHEMA.include());
		this.overrides = result.parseAll(TagParser.SCHEMA.override());
		this.redefines = result.parseAll(TagParser.SCHEMA.redefine());
		this.defaultOpenContent = result.parse(TagParser.SCHEMA.defaultOpenContent());
		this.typeDefinitions = new Def<>(result.parseAll(TagParser.COMPLEX_TYPE, TagParser.SIMPLE_TYPE),
				s -> s.typeDefinitions,
				t -> checkIfUnique(t, TypeDefinition::name, TypeDefinition::targetNamespace));
		this.attributeDeclarations = new Def<>(result.parseAll(TagParser.ATTRIBUTE),
				s -> s.attributeDeclarations,
				a -> checkIfUnique(a, Attribute::name, Attribute::targetNamespace));
		this.attributeGroupDefinitions = new Def<>(result.parseAll(TagParser.ATTRIBUTE_GROUP),
				s -> s.attributeGroupDefinitions,
				a -> checkIfUnique(a, AttributeGroup::name, AttributeGroup::targetNamespace));
		this.modelGroupDefinitions = new Def<>(result.parseAll(TagParser.GROUP),
				s -> s.modelGroupDefinitions,
				g -> checkIfUnique(g, ModelGroup::name, ModelGroup::targetNamespace));
		this.elementDeclarations = new Def<>(result.parseAll(TagParser.ELEMENT),
				s -> s.elementDeclarations,
				e -> checkIfUnique(e, Element::name, Element::targetNamespace));
		this.notationDeclarations = new Def<>(result.parseAll(TagParser.NOTATION),
				s -> s.notationDeclarations,
				n -> checkIfUnique(n, Notation::name, Notation::targetNamespace));
		this.identityConstraintDefinitions = new DeferredArrayDeque<>(() -> {
			final Deque<IdentityConstraint> x = new ArrayDeque<>();
			this.elementDeclarations().forEach(e -> x.addAll(e.identityConstraintDefinitions()));
			this.typeDefinitions().stream().filter(ComplexType.class::isInstance).map(ComplexType.class::cast).forEach(c -> {
				if (c.contentType() != null && c.contentType().particle() != null) {
					final Particle p = c.contentType().particle();
					if (p.term() instanceof Element) {
						x.addAll(((Element) p.term()).identityConstraintDefinitions());
					} else if (p.term() instanceof ModelGroup) {
						final Deque<ModelGroup> d = new ArrayDeque<>(Collections.singleton((ModelGroup) p.term()));
						while (!d.isEmpty()) {
							final ModelGroup g = d.pop();
							g.particles().forEach(p2 -> {
								if (p2.term() instanceof ModelGroup) {
									d.push((ModelGroup) p2.term());
								} else if (p2.term() instanceof Element) {
									x.addAll(((Element) p2.term()).identityConstraintDefinitions());
								}
							});
						}
					}
				}
			});
			return x;
		});
		this.annotations = new Def<>(new DeferredArrayDeque<>(() -> {
			final AnnotationSet annotationSet = Annotation.of(result);
			annotationSet.addAll(this.imports, Import::annotations);
			annotationSet.addAll(this.includes, Include::annotations);
			annotationSet.addAll(this.overrides, Overrides::annotations);
			annotationSet.addAll(this.redefines, Redefine::annotations);
			if (defaultOpenContent != null) {
				annotationSet.add(defaultOpenContent, DefaultOpenContent::annotations);
			}
			return annotationSet.resolve(result.node());
		}), s -> s.annotations);
		this.attributeFormDefault = result.value(AttrParser.ATTRIBUTE_FORM_DEFAULT);
		this.blockDefault = result.value(AttrParser.BLOCK_DEFAULT);
		final QName defaultAttributesName = result.value(AttrParser.DEFAULT_ATTRIBUTES);
		this.defaultAttributes = defaultAttributesName != null
				? find(defaultAttributesName, AttributeGroup.class)
				: null;
		this.xpathDefaultNamespace = Optional.ofNullable(result.value(AttrParser.XPATH_DEFAULT_NAMESPACE)).orElse(XPATH_DEFAULT_NAMESPACE_SCHEMA_DEFAULT);
		this.elementFormDefault = result.value(AttrParser.ELEMENT_FORM_DEFAULT);
		this.finalDefault = result.value(AttrParser.FINAL_DEFAULT);
		this.version = result.value(AttrParser.VERSION);
		this.targetNamespace = NodeHelper.requireNonEmpty(document.getDocumentElement(), result.value(AttrParser.TARGET_NAMESPACE));
		if (this.targetNamespace != null && this.targetNamespace.isEmpty()) {
			throw new IllegalArgumentException("@targetNamespace may not be empty string - must be null or non-empty");
		}
		// Add self to cache
		if (targetNamespace != null || location != null) {
			this.schemaDocumentCache.put(new SimpleImmutableEntry<>(targetNamespace, location != null ? URI.create(location) : null), document);
			this.schemaCache.put(document, this);
		}
	}

	public Schema(final File file) throws IOException, SAXException {
		this(loadDocFromFile(file));
	}

	public Schema(final Document document) {
		this(DEFAULT_DOCUMENT_RESOLVER, document);
	}

	public Schema(final NamespaceContext namespaceContext, final Document document) {
		this(DEFAULT_DOCUMENT_RESOLVER, namespaceContext, document);
	}

	public Schema(final DocumentResolver documentResolver, final Document document) {
		this(documentResolver, null, document);
	}

	public Schema(final DocumentResolver documentResolver, final NamespaceContext namespaceContext, final Document document) {
		this(null, documentResolver, namespaceContext, document, document.getDocumentURI(), new HashMap<>(), new HashMap<>());
	}

	private static <T> Deferred<T> deferred(final Optional<T> opt) {
		return opt.map(t -> (Deferred<T>) () -> t).orElse(null);
	}

	private static <T extends SchemaComponent> void checkIfUnique(final Deque<T> ls, final Function<T, String> name, final Function<T, String> targetNamespace) {
		final Map<QName, T> names = new HashMap<>(ls.size());
		for (final T t : ls) {
			final String targetNs = targetNamespace.apply(t);
			final QName q = new QName(targetNs == null ? XMLConstants.NULL_NS_URI : targetNs, name.apply(t));
			final T dup = names.put(q, t);
			if (dup != null) {
				throw new ParseException("Duplicate declaration: " + q);
			}
		}
	}

	private static Document loadDocFromFile(final File file) throws SAXException, IOException {
		try (final FileInputStream stream = new FileInputStream(file)) {
			final InputSource source = new InputSource(stream);
			source.setSystemId(file.toPath().toUri().toString());
			return NodeHelper.newDocumentBuilder().parse(source);
		}
	}

	private static String getAttrValueAsXPathDefaultNamespace(final Attr attr) {
		final String value = NodeHelper.collapseWhitespace(attr.getValue());
		switch (value) {
		case XPATH_DEFAULT_NAMESPACE_SCHEMA_DEFAULT:
		case "##defaultNamespace":
		case "##targetNamespace":
			return value;
		default:
			return NodeHelper.getNodeValueAsAnyUri(attr, value);
		}
	}

	private Set<Schema> getConstituentSchemas() {
		final Set<Schema> schemas = new LinkedHashSet<>();
		imports.forEach(i -> schemas.add(i.importedSchema()));
		includes.forEach(i -> schemas.add(i.includedSchema()));
		overrides.forEach(o -> schemas.add(o.includedSchema()));
		redefines.forEach(r -> schemas.add(r.includedSchema()));
		return schemas;
	}

	private void findAllConstituentSchemas(final Set<Schema> set) {
		if (set.add(this)) {
			imports.forEach(i -> i.importedSchema().findAllConstituentSchemas(set));
			includes.forEach(i -> i.includedSchema().findAllConstituentSchemas(set));
			overrides.forEach(o -> o.includedSchema().findAllConstituentSchemas(set));
			redefines.forEach(r -> r.includedSchema().findAllConstituentSchemas(set));
		}
	}

	static {
		final BiFunction<Schema, QName, Deferred<TypeDefinition>> findIntrinsicSimpleType = (schema, name) -> {
			final String localName = name.getLocalPart();
			if (localName.equals(SimpleType.xsAnyAtomicType().name())) {
				throw new ParseException(SimpleType.xsAnyAtomicType().name() + " may not be used as a simpleType");
			} else if (localName.equals(SimpleType.xsAnySimpleType().name())) {
				return SimpleType::xsAnySimpleType;
			} else {
				return Deferred.of(() -> SimpleType.findPrimitiveOrBuiltinType(name.getLocalPart()));
			}
		};
		final Map<Class<? extends SchemaComponent>, BiFunction<Schema, QName, Deferred<? extends SchemaComponent>>> finders = new HashMap<>();
		finders.put(SimpleType.class, (schema, name) -> {
			if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(name.getNamespaceURI())) {
				return findIntrinsicSimpleType.apply(schema, name);
			}
			return deferred(schema.typeDefinitions().stream().filter(SimpleType.class::isInstance).filter(s -> NodeHelper.equalsName(name, s)).findAny());
		});
		finders.put(ComplexType.class, (schema, name) -> {
			if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(name.getNamespaceURI()) && ComplexType.ANYTYPE_NAME.equals(name.getLocalPart())) {
				return ComplexType::xsAnyType;
			}
			return deferred(schema.typeDefinitions().stream().filter(ComplexType.class::isInstance).filter(c -> NodeHelper.equalsName(name, c)).findAny());
		});
		finders.put(TypeDefinition.class, (schema, name) -> {
			if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(name.getNamespaceURI()) && ComplexType.ANYTYPE_NAME.equals(name.getLocalPart())) {
				return ComplexType::xsAnyType;
			} else if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(name.getNamespaceURI())) {
				return findIntrinsicSimpleType.apply(schema, name);
			}
			return schema.typeDefinitions().stream().filter(t -> NodeHelper.equalsName(name, t)).map(t -> (Deferred<TypeDefinition>) () -> t).findAny().orElseThrow(() -> new ParseException("Type definition with name " + name + " not found"));
		});
		finders.put(AttributeGroup.class, (schema, name) -> deferred(schema.attributeGroupDefinitions().stream().filter(a -> NodeHelper.equalsName(name, a)).findAny()));
		finders.put(Attribute.class, (schema, name) -> deferred(schema.attributeDeclarations().stream().filter(a -> NodeHelper.equalsName(name, a)).findAny()));
		finders.put(ModelGroup.class, (schema, name) -> deferred(schema.modelGroupDefinitions().stream().filter(m -> NodeHelper.equalsName(name, m)).findAny()));
		finders.put(Element.class, (schema, name) -> deferred(schema.elementDeclarations().stream().filter(e -> NodeHelper.equalsName(name, e)).findAny()));
		FINDERS = Collections.unmodifiableMap(finders);
	}

	@SuppressWarnings("unchecked")
	<T extends SchemaComponent> Deferred<T> find(final QName name, final Class<? extends T> cls) {
		return Deferred.of(() -> {
			final Deferred<? extends SchemaComponent> def = FINDERS.get(cls).apply(parent, name);
			if (def != null) {
				final T t = (T) def.get();
				if (t != null) {
					return t;
				}
			}
			throw new ParseException(cls.getSimpleName() + " with name " + name + " not found");
		});
	}

	Schema findSchema(final DocumentResolver resolver, final boolean cache, final String namespace, final String schemaLocation) throws Exception {
		final URI resourceUri = resolver.resolveUri(document.getDocumentURI(), namespace, schemaLocation);
		final URI normalizedResourceUri = resourceUri != null ? resourceUri.normalize() : null;
		final Map.Entry<String, URI> key = new SimpleImmutableEntry<>(namespace, normalizedResourceUri);
		Document doc = null;
		final Consumer<Exception> reportException = e -> Reporting.report("Could not resolve schema with " + (namespace != null ? "namespace " + NodeHelper.toStringNamespace(namespace) : "") + (schemaLocation != null ? (namespace != null ? " and " : "") + "schemaLocation " + schemaLocation : ""), e);
		if (cache) {
			synchronized (schemaDocumentCache) {
				doc = schemaDocumentCache.get(key);
				if (doc == null) {
					try {
						doc = resolver.resolve(normalizedResourceUri);
					} catch (final Exception e) {
						reportException.accept(e);
						throw e;
					}
					if (doc != null) {
						schemaDocumentCache.put(key, doc);
					}
				}
			}
		} else {
			try {
				doc = resolver.resolve(normalizedResourceUri);
			} catch (final Exception e) {
				reportException.accept(e);
				throw e;
			}
		}
		if (doc == null) {
			return Schema.EMPTY;
		}
		synchronized (schemaCache) {
			final Schema schema = schemaCache.get(doc);
			if (schema != null) {
				return schema;
			}
			return new Schema(parent, documentResolver(), namespaceContext(), doc, schemaLocation != null ? schemaLocation : doc.getDocumentURI(), schemaDocumentCache, schemaCache);
		}
	}

	void visit(final Visitor visitor) {
		if (visitor.visit(null, document, this)) {
			visitor.onSchema(document, this);
			annotations().forEach(a -> a.visit(visitor));
			typeDefinitions().forEach(t -> ComplexType.visitTypeDefinition(t, visitor));
			attributeDeclarations().forEach(a -> a.visit(visitor));
			elementDeclarations().forEach(e -> e.visit(visitor));
			attributeGroupDefinitions().forEach(a -> a.visit(visitor));
			modelGroupDefinitions().forEach(m -> m.visit(visitor));
			notationDeclarations().forEach(n -> n.visit(visitor));
			identityConstraintDefinitions().forEach(i -> i.visit(visitor));
		}
	}

	DocumentResolver documentResolver() {
		return documentResolver;
	}

	NamespaceContext namespaceContext() {
		return namespaceContext;
	}

	String location() {
		return location;
	}

	Deferred<DefaultOpenContent> defaultOpenContent() {
		return defaultOpenContent;
	}

	Form attributeFormDefault() {
		return attributeFormDefault;
	}

	Block blockDefault() {
		return blockDefault;
	}

	Deferred<AttributeGroup> defaultAttributes() {
		return defaultAttributes;
	}

	String xpathDefaultNamespace() {
		return xpathDefaultNamespace;
	}

	Form elementFormDefault() {
		return elementFormDefault;
	}

	Final finalDefault() {
		return finalDefault;
	}

	String version() {
		return version;
	}

	String targetNamespace() {
		return targetNamespace;
	}

	/** @return The simple and complex type definitions corresponding to all the &lt;simpleType&gt; and &lt;complexType&gt; element information items in the [children], if any, plus any definitions brought in via &lt;include&gt; (see Assembling a schema for a single target namespace from multiple schema definition documents (&lt;include&gt;) (§4.2.3)), &lt;override&gt; (see Overriding component definitions (&lt;override&gt;) (§4.2.5)), &lt;redefine&gt; (see Including modified component definitions (&lt;redefine&gt;) (§4.2.4)), and &lt;import&gt; (see References to schema components across namespaces (&lt;import&gt;) (§4.2.6)). */
	public Deque<TypeDefinition> typeDefinitions() {
		return Deques.unmodifiableDeque(typeDefinitions.all);
	}

	/** @return The (top-level) attribute declarations corresponding to all the &lt;attribute&gt; element information items in the [children], if any, plus any declarations brought in via &lt;include&gt;, &lt;override&gt;, &lt;redefine&gt;, and &lt;import&gt;. */
	public Deque<Attribute> attributeDeclarations() {
		return Deques.unmodifiableDeque(attributeDeclarations.all);
	}

	/** @return The (top-level) element declarations corresponding to all the &lt;element&gt; element information items in the [children], if any, plus any declarations brought in via &lt;include&gt;, &lt;override&gt;, &lt;redefine&gt;, and &lt;import&gt;. */
	public Deque<Element> elementDeclarations() {
		return Deques.unmodifiableDeque(elementDeclarations.all);
	}

	/** @return The attribute group definitions corresponding to all the &lt;attributeGroup&gt; element information items in the [children], if any, plus any definitions brought in via &lt;include&gt;, &lt;override&gt;, &lt;redefine&gt;, and &lt;import&gt;. */
	public Deque<AttributeGroup> attributeGroupDefinitions() {
		return Deques.unmodifiableDeque(attributeGroupDefinitions.all);
	}

	/** @return The model group definitions corresponding to all the &lt;group&gt; element information items in the [children], if any, plus any definitions brought in via &lt;include&gt;, &lt;redefine&gt; and &lt;import&gt;. */
	public Deque<ModelGroup> modelGroupDefinitions() {
		return Deques.unmodifiableDeque(modelGroupDefinitions.all);
	}

	/** @return The notation declarations corresponding to all the &lt;notation&gt; element information items in the [children], if any, plus any declarations brought in via &lt;include&gt;, &lt;override&gt;, &lt;redefine&gt;, and &lt;import&gt;. */
	public Deque<Notation> notationDeclarations() {
		return Deques.unmodifiableDeque(notationDeclarations.all);
	}

	/** @return The identity-constraint definitions corresponding to all the &lt;key&gt;, &lt;keyref&gt;, and &lt;unique&gt; element information items anywhere within the [children], if any, plus any definitions brought in via &lt;include&gt;, &lt;override&gt;, &lt;redefine&gt;, and &lt;import&gt;. */
	public Deque<IdentityConstraint> identityConstraintDefinitions() {
		return Deques.unmodifiableDeque(identityConstraintDefinitions);
	}

	/** @return The ·annotation mapping· of the set of elements containing the &lt;schema&gt; and all the &lt;include&gt;, &lt;redefine&gt;, &lt;override&gt;, &lt;import&gt;, and &lt;defaultOpenContent&gt; [children], if any, as defined in XML Representation of Annotation Schema Components (§3.15.2). */
	@Override
	public Deque<Annotation> annotations() {
		return Deques.unmodifiableDeque(annotations.all);
	}

}
