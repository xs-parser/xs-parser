package xs.parser;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.AbstractMap.*;
import java.util.function.*;
import javax.xml.*;
import javax.xml.namespace.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import xs.parser.TypeDefinition.*;
import xs.parser.internal.*;
import xs.parser.internal.SequenceParser.*;

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

		private Block(final String value) {
			this.name = value;
		}

		public static Block getByName(final Node node) {
			return getByName(node.getNodeValue());
		}

		public static Block getByName(final String name) {
			for (final Block b : values()) {
				if (b.getName().equals(name)) {
					return b;
				}
			}
			throw new IllegalArgumentException(name.toString());
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
	public static class DefaultOpenContent extends OpenContent {

		protected static final SequenceParser parser = new SequenceParser()
				.optionalAttributes(AttributeValue.APPLIESTOEMPTY, AttributeValue.ID, AttributeValue.MODE)
				.elements(0, 1, ElementValue.ANNOTATION)
				.elements(1, 1, ElementValue.ANY);

		private final boolean appliesToEmpty;

		private DefaultOpenContent(final Deque<Annotation> annotations, final boolean appliesToEmpty, final Mode mode, final Particle<Wildcard> wildcard) {
			super(annotations, mode, wildcard);
			this.appliesToEmpty = appliesToEmpty;
		}

		protected static DefaultOpenContent parse(final Result result) {
			final boolean appliesToEmpty = result.value(AttributeValue.APPLIESTOEMPTY);
			final Mode mode = result.value(AttributeValue.MODE);
			final Particle<Wildcard> wildcard = result.parse(ElementValue.ANY);
			return new DefaultOpenContent(result.annotations(), appliesToEmpty, mode, wildcard);
		}

		public boolean appliesToEmpty() {
			return appliesToEmpty;
		}

	}

	public enum Form {

		UNQUALIFIED("unqualified"),
		QUALIFIED("qualified");

		private final String name;

		private Form(final String value) {
			this.name = value;
		}

		public static Form getByName(final Node node) {
			final String name = node.getNodeValue();
			for (final Form f : values()) {
				if (f.getName().equals(name)) {
					return f;
				}
			}
			throw new IllegalArgumentException(name.toString());
		}

		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return getName();
		}

	}

	public static class DocumentResolver {

		public URI resolveUri(final String baseUri, final String namespace, final String schemaLocation) {
			if (schemaLocation != null) {
				try {
					final URI schemaLocationUri = new URI(schemaLocation);
					if (schemaLocationUri != null) {
						if (baseUri == null) {
							return schemaLocationUri;
						} else {
							try {
								final URI bUri = new URI(baseUri);
								if (bUri != null) {
									return bUri.resolve(schemaLocationUri);
								}
							} catch (final URISyntaxException e) {
								return schemaLocationUri;
							}
						}
					}
				} catch (final URISyntaxException e) {
					return null;
				}
			}
			return null;
		}

		public Document resolve(final URI resourceUri) throws Exception {
			if (resourceUri == null) {
				return null;
			} else if (resourceUri.getScheme() != null) {
				final InputSource source = new InputSource(resourceUri.toString());
				return NodeHelper.newDocumentBuilder().parse(source);
			} else {
				final Path path = Paths.get(resourceUri.toString());
				if (Files.isRegularFile(path)) {
					try (final FileReader reader = new FileReader(path.toFile())) {
						final InputSource source = new InputSource(reader);
						source.setSystemId(path.toUri().toString());
						return NodeHelper.newDocumentBuilder().parse(source);
					}
				} else {
					final InputStream is = getClass().getResourceAsStream(resourceUri.toString());
					return is != null ? NodeHelper.newDocumentBuilder().parse(new InputSource(is)) : null;
				}
			}
		}

	}

	private class Def<T extends SchemaComponent> {

		private final Deferred<Deque<T>> declared;
		private final Deferred<Deque<T>> constituents;
		private final Deferred<Deque<T>> all;

		Def(final Supplier<Deque<T>> supplier, final Function<Schema, Def<T>> mapper) {
			this(supplier, mapper, x -> { });
		}

		Def(final Supplier<Deque<T>> supplier, final Function<Schema, Def<T>> mapper, final Consumer<Deque<T>> after) {
			this.declared = Deferred.of(supplier);
			final Set<Schema> schemas = new LinkedHashSet<>();
			schemas.add(Schema.this);
			this.constituents = findAll(schemas, mapper);
			this.all = constituents.map(c -> {
				final Deque<T> d = declared.get();
				final Deque<T> all = new DeferredArrayDeque<>(d.size(), c);
				all.addAll(d);
				after.accept(all);
				return all;
			});
		}

		Def() {
			this.declared = Deferred.value(Deques.emptyDeque());
			this.constituents = Deferred.value(Deques.emptyDeque());
			this.all = Deferred.value(Deques.emptyDeque());
		}

		private <U extends SchemaComponent> Deferred<Deque<U>> findAll(final Set<Schema> schemas, final Function<Schema, Def<U>> mapper) {
			return constituentSchemas.map(constituents -> {
				if (constituents.isEmpty()) {
					return Deques.emptyDeque();
				}
				int size = 0;
				final Deque<Deque<U>> values = new ArrayDeque<>();
				for (final Schema s : constituents) {
					if (schemas.add(s)) {
						final Def<U> def = mapper.apply(s);
						final Deque<U> decls = def.declared.get();
						final Deque<U> combine = new DeferredArrayDeque<>(decls.size(), def.findAll(schemas, mapper).get());
						combine.addAll(decls);
						values.add(combine);
						size += combine.size();
					}
				}
				final Deque<U> ls = new DeferredArrayDeque<>(size);
				for (final Deque<U> value : values) {
					ls.addAll(value);
				}
				return ls;
			});
		}

	}

	private static final DocumentResolver DEFAULT_DOCUMENT_RESOLVER = new DocumentResolver();
	private static final Map<Class<? extends SchemaComponent>, BiFunction<Schema, QName, Deferred<? extends SchemaComponent>>> FINDERS;
	static final Schema EMPTY = new Schema();
	protected static final SequenceParser parser = new SequenceParser()
			.optionalAttributes(AttributeValue.ATTRIBUTEFORMDEFAULT, AttributeValue.BLOCKDEFAULT, AttributeValue.DEFAULTATTRIBUTES, AttributeValue.XPATHDEFAULTNAMESPACE, AttributeValue.ELEMENTFORMDEFAULT, AttributeValue.FINALDEFAULT, AttributeValue.ID, AttributeValue.TARGETNAMESPACE, AttributeValue.VERSION, AttributeValue.XML_LANG)
			.elements(0, Integer.MAX_VALUE, ElementValue.IMPORT, ElementValue.INCLUDE, ElementValue.OVERRIDE, ElementValue.REDEFINE, ElementValue.ANNOTATION)
			.elements(0, 1, ElementValue.DEFAULTOPENCONTENT)
			.elements(0, Integer.MAX_VALUE, ElementValue.ANNOTATION)
			.elements(0, Integer.MAX_VALUE, ElementValue.SIMPLETYPE, ElementValue.COMPLEXTYPE, ElementValue.GROUP_DECL, ElementValue.ATTRIBUTEGROUP, ElementValue.ELEMENT_DECL, ElementValue.ATTRIBUTE_DECL, ElementValue.NOTATION, ElementValue.ANNOTATION);

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
	private final DefaultOpenContent defaultOpenContent;
	private final Def<TypeDefinition> typeDefinitions;
	private final Def<Attribute> attributeDeclarations;
	private final Def<ModelGroup> modelGroupDefinitions;
	private final Def<AttributeGroup> attributeGroupDefinitions;
	private final Def<Element> elementDeclarations;
	private final Def<Notation> notationDeclarations;
	private final Deferred<Deque<IdentityConstraint>> identityConstraintDefinitions;
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
		final Set<Schema> constituentSchemas = getConstituentSchemas();
		constituentSchemas.remove(this);
		return Collections.unmodifiableSet(constituentSchemas);
	});

	private Schema() {
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
		this.identityConstraintDefinitions = Deferred.value(Deques.emptyDeque());
		this.annotations = new Def<>();
		this.attributeFormDefault = AttributeValue.ATTRIBUTEFORMDEFAULT.defaultValue();
		this.blockDefault = AttributeValue.BLOCKDEFAULT.defaultValue();
		this.defaultAttributes = Deferred.none();
		this.xpathDefaultNamespace = AttributeValue.XPATHDEFAULTNAMESPACE.defaultValue();
		this.elementFormDefault = AttributeValue.ELEMENTFORMDEFAULT.defaultValue();
		this.finalDefault = AttributeValue.FINALDEFAULT.defaultValue();
		this.version = null;
		this.targetNamespace = null;
		this.location = null;
	}

	private Schema(final DocumentResolver documentResolver, final NamespaceContext namespaceContext, final Document document, final String location, final Map<Map.Entry<String, URI>, Document> schemaDocumentCache, final Map<Document, Schema> schemaCache) {
		this.documentResolver = documentResolver;
		this.namespaceContext = namespaceContext;
		this.document = document;
		this.location = location;
		this.schemaDocumentCache = schemaDocumentCache;
		this.schemaCache = schemaCache;
		this.result = parser.parse(this, document.getDocumentElement());
		this.imports = result.parseAll(ElementValue.IMPORT);
		this.includes = result.parseAll(ElementValue.INCLUDE);
		this.overrides = result.parseAll(ElementValue.OVERRIDE);
		this.redefines = result.parseAll(ElementValue.REDEFINE);
		this.defaultOpenContent = result.parse(ElementValue.DEFAULTOPENCONTENT);
		this.typeDefinitions = new Def<>(() -> result.parseAll(ElementValue.SIMPLETYPE, ElementValue.COMPLEXTYPE),
				s -> s.typeDefinitions,
				t -> checkIfUnique(t, TypeDefinition::name, TypeDefinition::targetNamespace));
		this.attributeDeclarations = new Def<>(() -> result.parseAll(ElementValue.ATTRIBUTE_DECL),
				s -> s.attributeDeclarations,
				a -> checkIfUnique(a, Attribute::name, Attribute::targetNamespace));
		this.attributeGroupDefinitions = new Def<>(() -> result.parseAll(ElementValue.ATTRIBUTEGROUP),
				s -> s.attributeGroupDefinitions,
				a -> checkIfUnique(a, AttributeGroup::name, AttributeGroup::targetNamespace));
		this.modelGroupDefinitions = new Def<>(() -> result.parseAll(ElementValue.GROUP_DECL),
				s -> s.modelGroupDefinitions,
				g -> checkIfUnique(g, ModelGroup::name, ModelGroup::targetNamespace));
		this.elementDeclarations = new Def<>(() -> result.parseAll(ElementValue.ELEMENT_DECL),
				s -> s.elementDeclarations,
				e -> checkIfUnique(e, Element::name, Element::targetNamespace));
		this.notationDeclarations = new Def<>(() -> result.parseAll(ElementValue.NOTATION),
				s -> s.notationDeclarations,
				n -> checkIfUnique(n, Notation::name, Notation::targetNamespace));
		this.identityConstraintDefinitions = Deferred.of(() -> {
			final Deque<IdentityConstraint> id = new ArrayDeque<>();
			this.elementDeclarations().forEach(e -> id.addAll(e.identityConstraints()));
			this.typeDefinitions().stream().filter(ComplexType.class::isInstance).map(ComplexType.class::cast).forEach(c -> {
				if (c.contentType() != null && c.contentType().particle() != null) {
					final Particle<?> p = c.contentType().particle();
					if (p.term() instanceof Element) {
						id.addAll(((Element) p.term()).identityConstraints());
					} else if (p.term() instanceof ModelGroup) {
						final Deque<ModelGroup> d = new ArrayDeque<>(Collections.singleton((ModelGroup) p.term()));
						while (!d.isEmpty()) {
							final ModelGroup g = d.pop();
							g.particles().forEach(p2 -> {
								if (p2.term() instanceof ModelGroup) {
									d.push((ModelGroup) p2.term());
								} else if (p2.term() instanceof Element) {
									id.addAll(((Element) p2.term()).identityConstraints());
								}
							});
						}
					}
				}
			});
			return id;
		});
		this.annotations = new Def<>(() -> result.parseAll(ElementValue.ANNOTATION), s -> s.annotations);
		this.attributeFormDefault = result.value(AttributeValue.ATTRIBUTEFORMDEFAULT);
		this.blockDefault = result.value(AttributeValue.BLOCKDEFAULT);
		final QName defaultAttributesName = result.value(AttributeValue.DEFAULTATTRIBUTES);
		this.defaultAttributes = defaultAttributesName != null
				? find(defaultAttributesName, AttributeGroup.class)
				: Deferred.none();
		this.xpathDefaultNamespace = result.value(AttributeValue.XPATHDEFAULTNAMESPACE);
		this.elementFormDefault = result.value(AttributeValue.ELEMENTFORMDEFAULT);
		this.finalDefault = result.value(AttributeValue.FINALDEFAULT);
		this.version = result.value(AttributeValue.VERSION);
		this.targetNamespace = NodeHelper.validateTargetNamespace(result.value(AttributeValue.TARGETNAMESPACE));
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
		this(documentFromFile(file));
	}

	public Schema(final InputStream is) throws IOException, SAXException {
		this(NodeHelper.newDocumentBuilder().parse(is));
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
		this(documentResolver, namespaceContext, document, document.getDocumentURI(), new HashMap<>(), new HashMap<>());
	}

	private static <T extends SchemaComponent> void checkIfUnique(final Deque<T> ls, final Function<T, String> name, final Function<T, String> targetNamespace) {
		final Map<QName, T> names = new HashMap<>(ls.size());
		for (final T t : ls) {
			final String targetNs = targetNamespace.apply(t);
			final QName q = new QName(targetNs == null ? XMLConstants.NULL_NS_URI : targetNs, name.apply(t));
			final T dup = names.put(q, t);
			if (dup != null) {
				throw new SchemaParseException(t.node(), "Duplicate declaration: " + q);
			}
		}
	}

	private static Document documentFromFile(final File file) throws SAXException, IOException {
		try (final FileReader reader = new FileReader(file)) {
			final InputSource source = new InputSource(reader);
			source.setSystemId(file.toPath().toUri().toString());
			return NodeHelper.newDocumentBuilder().parse(source);
		}
	}

	private Set<Schema> getConstituentSchemas() {
		final Set<Schema> constituentSchemas = new LinkedHashSet<>();
		imports.forEach(i -> constituentSchemas.add(i.importedSchema()));
		includes.forEach(i -> constituentSchemas.add(i.includedSchema()));
		overrides.forEach(o -> constituentSchemas.add(o.includedSchema()));
		redefines.forEach(r -> constituentSchemas.add(r.includedSchema()));
		return constituentSchemas;
	}

	// Used by XPathEvaluator via reflection
	@SuppressWarnings("unused")
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
				throw new SchemaParseException(SimpleType.xsAnyAtomicType().name() + " may not be used as a simpleType");
			} else if (localName.equals(SimpleType.xsAnySimpleType().name())) {
				return Deferred.of(SimpleType::xsAnySimpleType);
			} else {
				return Deferred.of(() -> SimpleType.findPrimitiveOrBuiltinType(name.getLocalPart()));
			}
		};
		final Map<Class<? extends SchemaComponent>, BiFunction<Schema, QName, Deferred<? extends SchemaComponent>>> finders = new HashMap<>();
		finders.put(SimpleType.class, (schema, name) -> {
			if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(name.getNamespaceURI())) {
				return findIntrinsicSimpleType.apply(schema, name);
			}
			return schema.typeDefinitions().stream().filter(SimpleType.class::isInstance).filter(s -> NodeHelper.equalsName(name, s)).findAny().map(Deferred::value).orElseGet(Deferred::none);
		});
		finders.put(ComplexType.class, (schema, name) -> {
			if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(name.getNamespaceURI()) && ComplexType.ANYTYPE_NAME.equals(name.getLocalPart())) {
				return Deferred.of(ComplexType::xsAnyType);
			}
			return schema.typeDefinitions().stream().filter(ComplexType.class::isInstance).filter(c -> NodeHelper.equalsName(name, c)).findAny().map(Deferred::value).orElseGet(Deferred::none);
		});
		finders.put(TypeDefinition.class, (schema, name) -> {
			if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(name.getNamespaceURI()) && ComplexType.ANYTYPE_NAME.equals(name.getLocalPart())) {
				return Deferred.of(ComplexType::xsAnyType);
			} else if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(name.getNamespaceURI())) {
				return findIntrinsicSimpleType.apply(schema, name);
			}
			return schema.typeDefinitions().stream().filter(t -> NodeHelper.equalsName(name, t)).map(Deferred::<TypeDefinition>value).findAny().orElseThrow(() -> new SchemaParseException("Failed to find type definition: " + name));
		});
		finders.put(AttributeGroup.class, (schema, name) -> schema.attributeGroupDefinitions().stream().filter(a -> NodeHelper.equalsName(name, a)).findAny().map(Deferred::value).orElseGet(Deferred::none));
		finders.put(Attribute.class, (schema, name) -> schema.attributeDeclarations().stream().filter(a -> NodeHelper.equalsName(name, a)).findAny().map(Deferred::value).orElseGet(Deferred::none));
		finders.put(ModelGroup.class, (schema, name) -> schema.modelGroupDefinitions().stream().filter(m -> NodeHelper.equalsName(name, m)).findAny().map(Deferred::value).orElseGet(Deferred::none));
		finders.put(Element.class, (schema, name) -> schema.elementDeclarations().stream().filter(e -> NodeHelper.equalsName(name, e)).findAny().map(Deferred::value).orElseGet(Deferred::none));
		FINDERS = Collections.unmodifiableMap(finders);
	}

	@SuppressWarnings("unchecked")
	<T extends SchemaComponent> Deferred<T> find(final QName name, final Class<? extends T> cls) {
		return Deferred.of(() -> {
			final BiFunction<Schema, QName, Deferred<? extends SchemaComponent>> fn = FINDERS.get(cls);
			if (fn == null) {
				throw new IllegalArgumentException("No finder for " + cls);
			}
			final T t = (T) fn.apply(this, name).get();
			if (t != null) {
				return t;
			}
			throw new SchemaParseException("Failed to find " + cls.getSimpleName() + ": " + name);
		});
	}

	protected Schema findSchema(final DocumentResolver resolver, final boolean cache, final String namespace, final String schemaLocation) throws Exception {
		final URI resourceUri = resolver.resolveUri(document.getDocumentURI(), namespace, schemaLocation);
		final URI normalizedResourceUri = resourceUri != null ? resourceUri.normalize() : null;
		final Map.Entry<String, URI> key = new SimpleImmutableEntry<>(namespace, normalizedResourceUri);
		Document doc = null;
		final Supplier<Document> resolveDocument = () -> {
			try {
				return resolver.resolve(normalizedResourceUri);
			} catch (final Exception e) {
				Reporting.report("Failed to resolve @namespace=\"" + (namespace == null ? "" : namespace) + "\" @schemaLocation=\"" + (schemaLocation == null ? "" : schemaLocation) + "\" " + e.toString(), e);
				return null;
			}
		};
		if (cache) {
			synchronized (schemaDocumentCache) {
				doc = schemaDocumentCache.get(key);
				if (doc == null) {
					doc = resolveDocument.get();
					if (doc != null) {
						schemaDocumentCache.put(key, doc);
					}
				}
			}
		} else {
			doc = resolveDocument.get();
		}
		if (doc == null) {
			return Schema.EMPTY;
		}
		synchronized (schemaCache) {
			final Schema schema = schemaCache.get(doc);
			if (schema != null) {
				return schema;
			}
			return new Schema(documentResolver(), namespaceContext(), doc, schemaLocation != null ? schemaLocation : doc.getDocumentURI(), schemaDocumentCache, schemaCache);
		}
	}

	protected DocumentResolver documentResolver() {
		return documentResolver;
	}

	protected NamespaceContext namespaceContext() {
		return namespaceContext;
	}

	protected String location() {
		return location;
	}

	protected DefaultOpenContent defaultOpenContent() {
		return defaultOpenContent;
	}

	protected Form attributeFormDefault() {
		return attributeFormDefault;
	}

	protected Block blockDefault() {
		return blockDefault;
	}

	protected AttributeGroup defaultAttributes() {
		return defaultAttributes.get();
	}

	protected String xpathDefaultNamespace() {
		return xpathDefaultNamespace;
	}

	protected Form elementFormDefault() {
		return elementFormDefault;
	}

	protected Final finalDefault() {
		return finalDefault;
	}

	protected String version() {
		return version;
	}

	protected String targetNamespace() {
		return targetNamespace;
	}

	/** @return The simple and complex type definitions corresponding to all the &lt;simpleType&gt; and &lt;complexType&gt; element information items in the [children], if any, plus any definitions brought in via &lt;include&gt; (see Assembling a schema for a single target namespace from multiple schema definition documents (&lt;include&gt;) (§4.2.3)), &lt;override&gt; (see Overriding component definitions (&lt;override&gt;) (§4.2.5)), &lt;redefine&gt; (see Including modified component definitions (&lt;redefine&gt;) (§4.2.4)), and &lt;import&gt; (see References to schema components across namespaces (&lt;import&gt;) (§4.2.6)). */
	public Deque<? extends TypeDefinition> typeDefinitions() {
		return Deques.unmodifiableDeque(typeDefinitions.all.get());
	}

	/** @return The (top-level) attribute declarations corresponding to all the &lt;attribute&gt; element information items in the [children], if any, plus any declarations brought in via &lt;include&gt;, &lt;override&gt;, &lt;redefine&gt;, and &lt;import&gt;. */
	public Deque<Attribute> attributeDeclarations() {
		return Deques.unmodifiableDeque(attributeDeclarations.all.get());
	}

	/** @return The (top-level) element declarations corresponding to all the &lt;element&gt; element information items in the [children], if any, plus any declarations brought in via &lt;include&gt;, &lt;override&gt;, &lt;redefine&gt;, and &lt;import&gt;. */
	public Deque<Element> elementDeclarations() {
		return Deques.unmodifiableDeque(elementDeclarations.all.get());
	}

	/** @return The attribute group definitions corresponding to all the &lt;attributeGroup&gt; element information items in the [children], if any, plus any definitions brought in via &lt;include&gt;, &lt;override&gt;, &lt;redefine&gt;, and &lt;import&gt;. */
	public Deque<AttributeGroup> attributeGroupDefinitions() {
		return Deques.unmodifiableDeque(attributeGroupDefinitions.all.get());
	}

	/** @return The model group definitions corresponding to all the &lt;group&gt; element information items in the [children], if any, plus any definitions brought in via &lt;include&gt;, &lt;redefine&gt; and &lt;import&gt;. */
	public Deque<ModelGroup> modelGroupDefinitions() {
		return Deques.unmodifiableDeque(modelGroupDefinitions.all.get());
	}

	/** @return The notation declarations corresponding to all the &lt;notation&gt; element information items in the [children], if any, plus any declarations brought in via &lt;include&gt;, &lt;override&gt;, &lt;redefine&gt;, and &lt;import&gt;. */
	public Deque<Notation> notationDeclarations() {
		return Deques.unmodifiableDeque(notationDeclarations.all.get());
	}

	/** @return The identity-constraint definitions corresponding to all the &lt;key&gt;, &lt;keyref&gt;, and &lt;unique&gt; element information items anywhere within the [children], if any, plus any definitions brought in via &lt;include&gt;, &lt;override&gt;, &lt;redefine&gt;, and &lt;import&gt;. */
	public Deque<IdentityConstraint> identityConstraintDefinitions() {
		return Deques.unmodifiableDeque(identityConstraintDefinitions.get());
	}

	@Override
	public Node node() {
		return document.getDocumentElement();
	}

	/** @return The ·annotation mapping· of the set of elements containing the &lt;schema&gt; and all the &lt;include&gt;, &lt;redefine&gt;, &lt;override&gt;, &lt;import&gt;, and &lt;defaultOpenContent&gt; [children], if any, as defined in XML Representation of Annotation Schema Components (§3.15.2). */
	@Override
	public Deque<Annotation> annotations() {
		return Deques.unmodifiableDeque(annotations.all.get());
	}

}