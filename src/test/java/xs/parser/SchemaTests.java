package xs.parser;

import java.io.*;
import java.net.*;
import java.time.*;
import java.util.*;
import java.util.stream.*;
import javax.xml.*;
import org.junit.*;
import org.w3c.dom.*;
import xs.parser.ConstrainingFacet.*;
import xs.parser.Schema.*;
import xs.parser.internal.util.*;
import xs.parser.x.*;

public class SchemaTests {

	@Test
	public void testSchemaComponents() throws Exception {
		final Instant start = Instant.now();
		// Test build schema
		final Schema schema = new Schema(new File("src/test/resources/schema/base.xsd"));
		System.out.println("xs:simpleType     " + schema.typeDefinitions().stream().filter(SimpleType.class::isInstance).count());
		System.out.println("xs:attribute      " + schema.attributeDeclarations().size());
		System.out.println("xs:complexType    " + schema.typeDefinitions().stream().filter(ComplexType.class::isInstance).count());
		System.out.println("xs:group          " + schema.modelGroupDefinitions().size());
		System.out.println("xs:attributeGroup " + schema.attributeGroupDefinitions().size());
		System.out.println("xs:element        " + schema.elementDeclarations().size());
		System.out.println("xs:notation       " + schema.notationDeclarations().size());
		System.out.println("xs:annotation     " + schema.annotations().size());
		System.out.println("@version          " + schema.node().getAttributes().getNamedItem("version"));
		System.out.println("@targetNamespace  " + schema.node().getAttributes().getNamedItem("targetNamespace"));
		// Test no global duplicates
		Assert.assertEquals(schema.typeDefinitions().stream().distinct().count(), schema.typeDefinitions().size());
		Assert.assertEquals(schema.attributeDeclarations().stream().distinct().count(), schema.attributeDeclarations().size());
		Assert.assertEquals(schema.modelGroupDefinitions().stream().distinct().count(), schema.modelGroupDefinitions().size());
		Assert.assertEquals(schema.attributeGroupDefinitions().stream().distinct().count(), schema.attributeGroupDefinitions().size());
		Assert.assertEquals(schema.elementDeclarations().stream().distinct().count(), schema.elementDeclarations().size());
		Assert.assertEquals(schema.notationDeclarations().stream().distinct().count(), schema.notationDeclarations().size());
		Assert.assertEquals(schema.identityConstraintDefinitions().stream().distinct().count(), schema.identityConstraintDefinitions().size());
		// Test documentation
		schema.typeDefinitions().stream().filter(s -> !s.annotations().isEmpty()).forEach(s -> {
			for (final Annotation a : s.annotations()) {
				for (final Node doc : a.userInformation()) {
					Assert.assertNotNull(s.annotations().toString(), doc);
				}
			}
		});
		// Test complexTypes
		schema.typeDefinitions().stream().filter(ComplexType.class::isInstance).map(ComplexType.class::cast).forEach(c -> {
			Assert.assertTrue(c.toString(), c.contentType().particle() != null || ComplexType.DerivationMethod.RESTRICTION.equals(c.derivationMethod()));
			if (ComplexType.Variety.EMPTY.equals(c.contentType().variety())) {
				Assert.assertEquals(ComplexType.DerivationMethod.RESTRICTION, c.derivationMethod());
				Assert.assertNull(c.contentType().openContent());
				Assert.assertNull(c.contentType().simpleType());
				Assert.assertNull(c.contentType().particle());
			} else {
				Assert.assertNull(c.contentType().simpleType());
				Assert.assertNotNull(c.contentType().variety() + ", " + c.toString(), c.contentType().particle());
				Assert.assertTrue(c.toString(), c.contentType().particle().term() instanceof ModelGroup);
				final ModelGroup m = (ModelGroup) c.contentType().particle().term();
				Assert.assertNotEquals(0, m.particles().size());
				if (m.particles().getFirst().term() instanceof ModelGroup) {
					Assert.assertEquals(ModelGroup.Compositor.SEQUENCE, m.compositor());
					Assert.assertTrue("" + m.particles().size(), m.particles().size() == 1 || m.particles().size() == 2);
					final Deque<Particle> particles = new ArrayDeque<>(m.particles());
					while (!particles.isEmpty()) {
						final Particle p = particles.poll();
						Assert.assertNotNull(p.term());
						if (p.term() instanceof ModelGroup) {
							particles.addAll(((ModelGroup) p.term()).particles());
						} else {
							Assert.assertNotNull(p.toString(), p.term());
						}
					}
				} else {
					for (final Particle p : m.particles()) {
						Assert.assertNotNull(p.toString(), p.term());
					}
				}
			}
		});
		Assert.assertEquals(3, schema.typeDefinitions().stream().filter(ComplexType.class::isInstance).count());
		// Test particle term
		schema.typeDefinitions().stream().filter(ComplexType.class::isInstance).map(ComplexType.class::cast).forEach(c -> {
			if (c.contentType() != null) { // It is legal for contentType to be null
				final Particle root = c.contentType().particle();
				if (root != null) { // It is also legal for the particle to be null (TODO: is it?)
					final Deque<Particle> d = new ArrayDeque<>();
					d.add(root);
					while (!d.isEmpty()) {
						final Particle p = d.removeFirst();
						Assert.assertNotNull("{" + c.targetNamespace() + "}" + c.name(), p.term());
						if (p.term() instanceof ModelGroup) {
							d.addAll(((ModelGroup) p.term()).particles());
						} else if (p.term() instanceof Element) {
							// Do nothing
						} else if (p.term() instanceof Wildcard) {
							Assert.fail();
						} else {
							Assert.fail(p.term().getClass().getName());
						}
					}
				}
			}
		});
		// Test attribute uses
		schema.typeDefinitions().stream().filter(ComplexType.class::isInstance).map(ComplexType.class::cast).forEach(c -> {
			// Force lazy evaluation
			Assert.assertNotNull(c.attributeUses());
		});
		schema.attributeGroupDefinitions().forEach(a -> {
			// Force lazy evaluation
			Assert.assertNotNull(a.attributeUses());
		});
		System.out.println("Runtime: " + (Instant.now().toEpochMilli() - start.toEpochMilli()) + "ms");
	}

	@Test
	public void testSchemaXPath() throws Exception {
		final Instant start = Instant.now();
		final Schema schema = new Schema(new File("src/test/resources/schema/base.xsd"));
		// Test XPath evaluation
		final NodeSet schemaRoot = NodeSet.of(NodeSet.DEFAULT_NAMESPACE_CONTEXT, schema);
		final NodeSet simpleTypes = schemaRoot.xpath("/xs:schema/xs:simpleType");
		Assert.assertEquals("xs:simpleType", schema.typeDefinitions().stream().filter(SimpleType.class::isInstance).count(), simpleTypes.size());
		Assert.assertEquals("xs:simpleType", simpleTypes.size(), schemaRoot.xpath("/xs:schema/xs:simpleType").size());
		final NodeSet attributes = schemaRoot.xpath("/xs:schema/xs:attribute");
		Assert.assertEquals("xs:attribute", schema.attributeDeclarations().size(), attributes.size());
		Assert.assertEquals("xs:attribute", attributes.size(), schemaRoot.xpath("/xs:schema/xs:attribute").size());
		final NodeSet complexTypes = schemaRoot.xpath("/xs:schema/xs:complexType");
		Assert.assertEquals("xs:complexType", schema.typeDefinitions().stream().filter(ComplexType.class::isInstance).count(), complexTypes.size());
		Assert.assertEquals("xs:complexType", complexTypes.size(), schemaRoot.xpath("/xs:schema/xs:complexType").size());
		final NodeSet elements = schemaRoot.xpath("/xs:schema/xs:element");
		Assert.assertEquals("xs:element", schema.elementDeclarations().size(), elements.size());
		Assert.assertEquals("xs:element", elements.size(), schemaRoot.xpath("/xs:schema/xs:element").size());
		final NodeSet attributeGroups = schemaRoot.xpath("/xs:schema/xs:attributeGroup");
		Assert.assertEquals("xs:attributeGroup", schema.attributeGroupDefinitions().size(), attributeGroups.size());
		Assert.assertEquals("xs:attributeGroup", attributeGroups.size(), schemaRoot.xpath("/xs:schema/xs:attributeGroup").size());
		// Misc. XPath tests
		Assert.assertEquals(0, schemaRoot.xpath("/xs:schema/xs:element/@name").xpath("ancestor::xs:schema/@targetNamespace").size());
		Assert.assertEquals(1, schemaRoot.xpath("/xs:schema/xs:simpleType").split().findFirst().get().xpath("@name").size());
		System.out.println("XPath runtime: " + (Instant.now().toEpochMilli() - start.toEpochMilli()) + "ms");
	}

	@Test
	public void testListFacets() throws Exception {
		final Schema schema = new Schema(new File("src/test/resources/schema/base.xsd"));
		final SimpleType aSimpleList = schema.typeDefinitions().stream().filter(t -> t.name().equals("aSimpleList")).findAny().map(SimpleType.class::cast).get();
		Assert.assertEquals(SimpleType.Variety.LIST, aSimpleList.variety());
		final WhiteSpace whiteSpace = aSimpleList.facets().stream().filter(WhiteSpace.class::isInstance).findAny().map(WhiteSpace.class::cast).orElseThrow(() -> new AssertionError(aSimpleList.facets().toString()));
		Assert.assertEquals(WhiteSpace.Value.COLLAPSE, whiteSpace.value());
		final MaxLength maxLength = aSimpleList.facets().stream().filter(MaxLength.class::isInstance).findAny().map(MaxLength.class::cast).orElseThrow(() -> new AssertionError(aSimpleList.facets().toString()));
		Assert.assertEquals("10", maxLength.value());
		final MinLength minLength = aSimpleList.facets().stream().filter(MinLength.class::isInstance).findAny().map(MinLength.class::cast).orElseThrow(() -> new AssertionError(aSimpleList.facets().toString()));
		Assert.assertEquals("2", minLength.value());
	}

	@Test
	public void testSchemaOverride() throws Exception {
		final Schema schema = new Schema(new File("src/test/resources/schema/overrider.xsd"));
		final Optional<SimpleType> mySimpleType = schema.typeDefinitions().stream().filter(SimpleType.class::isInstance).map(SimpleType.class::cast).filter(s -> "mySimpleType".equals(s.name())).findAny();
		Assert.assertTrue("Did not find mySimpleType in schema: " + schema.typeDefinitions(), mySimpleType.isPresent());
		Assert.assertEquals(SimpleType.Variety.ATOMIC, mySimpleType.get().variety());
		final SimpleType base = (SimpleType) mySimpleType.get().baseType();
		Assert.assertEquals(base.toString(), SimpleType.Variety.ATOMIC, base.variety());
		Assert.assertEquals(XMLConstants.W3C_XML_SCHEMA_NS_URI, base.targetNamespace());
		Assert.assertEquals("int", base.name());
		final Optional<SimpleType> mySimpleType2 = schema.typeDefinitions().stream().filter(SimpleType.class::isInstance).map(SimpleType.class::cast).filter(s -> "mySimpleType2".equals(s.name())).findAny();
		Assert.assertTrue("Did not find mySimpleType2 in schema: " + schema.typeDefinitions(), mySimpleType2.isPresent());
		Assert.assertEquals(SimpleType.Variety.ATOMIC, mySimpleType2.get().variety());
		final SimpleType base2 = (SimpleType) mySimpleType2.get().baseType();
		Assert.assertEquals(SimpleType.Variety.ATOMIC, base2.variety());
		Assert.assertEquals(XMLConstants.W3C_XML_SCHEMA_NS_URI, base2.targetNamespace());
		Assert.assertEquals("string", base2.name());
	}

	@Test
	public void testSchemaInclude() throws Exception {
		final Schema schema = new Schema(new File("src/test/resources/schema/includer.xsd"));
		Assert.assertEquals(5, schema.typeDefinitions().stream().filter(SimpleType.class::isInstance).count());
		final Optional<SimpleType> mySimpleType = schema.typeDefinitions().stream().filter(SimpleType.class::isInstance).map(SimpleType.class::cast).filter(s -> "mySimpleType".equals(s.name())).findAny();
		Assert.assertTrue("Did not find mySimpleType in schema: " + schema.typeDefinitions(), mySimpleType.isPresent());
		Assert.assertEquals(SimpleType.Variety.ATOMIC, mySimpleType.get().variety());
		final SimpleType base = (SimpleType) mySimpleType.get().baseType();
		Assert.assertEquals(SimpleType.Variety.ATOMIC, base.variety());
		Assert.assertEquals(XMLConstants.W3C_XML_SCHEMA_NS_URI, base.targetNamespace());
		Assert.assertEquals("string", base.name());
		final Optional<SimpleType> mySimpleType2 = schema.typeDefinitions().stream().filter(SimpleType.class::isInstance).map(SimpleType.class::cast).filter(s -> "mySimpleType2".equals(s.name())).findAny();
		Assert.assertTrue("Did not find mySimpleType2 in schema: " + schema.typeDefinitions(), mySimpleType2.isPresent());
		Assert.assertEquals(SimpleType.Variety.ATOMIC, mySimpleType2.get().variety());
		final SimpleType base2 = (SimpleType) mySimpleType2.get().baseType();
		Assert.assertEquals(SimpleType.Variety.ATOMIC, base2.variety());
		Assert.assertEquals(XMLConstants.W3C_XML_SCHEMA_NS_URI, base2.targetNamespace());
		Assert.assertEquals("string", base2.name());
	}

	@Test
	public void testSchemaImport() throws Exception {
		final Schema schema = new Schema(new File("src/test/resources/schema/importer.xsd"));
		Assert.assertEquals("xs:complexType " + schema.typeDefinitions(), 5, schema.typeDefinitions().stream().filter(ComplexType.class::isInstance).count());
		Assert.assertEquals("xs:simpleType " + schema.typeDefinitions(), 6, schema.typeDefinitions().stream().filter(SimpleType.class::isInstance).count());
		System.out.println(schema.elementDeclarations().stream().map(Element::name).collect(Collectors.toList()));
		Assert.assertEquals("xs:element " + schema.elementDeclarations(), 2, schema.elementDeclarations().size());
	}

	@Test
	public void testSchemaImportFromResource() throws Exception {
		final DocumentResolver resolver = new DocumentResolver() {

			@Override
			public Document resolve(final URI resourceUri) throws Exception {
				if (resourceUri.toString().endsWith("overrider.xsd")) {
					return super.resolve(URI.create("/schema/overrider.xsd"));
				} else if (resourceUri.toString().endsWith("includer.xsd")) {
					return super.resolve(URI.create("/schema/includer.xsd"));
				} else if (resourceUri.toString().endsWith("base.xsd")) {
					return super.resolve(URI.create("/schema/base.xsd"));
				} else {
					throw new AssertionError(resourceUri.toString());
				}
			}

		};
		final Schema schema = new Schema(resolver, NodeHelper.newDocumentBuilder().parse(new File("src/test/resources/schema/importer.xsd")));
		Assert.assertEquals("xs:complexType " + schema.typeDefinitions(), 5, schema.typeDefinitions().stream().filter(ComplexType.class::isInstance).count());
		Assert.assertEquals("xs:simpleType " + schema.typeDefinitions(), 6, schema.typeDefinitions().stream().filter(SimpleType.class::isInstance).count());
		System.out.println(schema.elementDeclarations().stream().map(Element::name).collect(Collectors.toList()));
		Assert.assertEquals("xs:element " + schema.elementDeclarations(), 2, schema.elementDeclarations().size());
	}

	@Test
	public void testSchemaRedefine() throws Exception {
		final Schema schema = new Schema(new File("src/test/resources/schema/redefiner.xsd"));
		// xs:simpleType redefinition
		{
			Assert.assertEquals("xs:simpleType", 5, schema.typeDefinitions().stream().filter(SimpleType.class::isInstance).count());
			final SimpleType mySimpleType2 = (SimpleType) schema.typeDefinitions().stream()
					.filter(s -> "mySimpleType2".equals(s.name()) && "https://my.test".equals(s.targetNamespace()))
					.findAny()
					.get();
			Assert.assertEquals("xs:pattern", Collections.singleton("123"), SimpleTypeTests.facetOf(mySimpleType2, ConstrainingFacet.Pattern.class).value());
			Assert.assertEquals("xs:maxLength", "222", SimpleTypeTests.facetOf(mySimpleType2, ConstrainingFacet.MaxLength.class).value());
			final SimpleType anotherSimpleType = (SimpleType) schema.typeDefinitions().stream().filter(s -> "anotherSimpleType".equals(s.name()) && "https://my.test".equals(s.targetNamespace())).findAny().get();
			Assert.assertEquals(SimpleType.Variety.ATOMIC, anotherSimpleType.variety());
			Assert.assertEquals(XMLConstants.W3C_XML_SCHEMA_NS_URI, anotherSimpleType.baseType().targetNamespace());
			Assert.assertEquals("int", anotherSimpleType.baseType().name());
			Assert.assertEquals("xs:complexType", 5, schema.typeDefinitions().stream().filter(ComplexType.class::isInstance).count());
		}
		// xs:complexType redefinition
		{
			final ComplexType c1 = (ComplexType) schema.typeDefinitions().stream().filter(t -> "c1".equals(t.name())).findFirst().get();
			Assert.assertEquals(ComplexType.Variety.ELEMENT_ONLY, c1.contentType().variety());
			final Particle p = c1.contentType().particle();
			final ModelGroup grp = (ModelGroup) p.term();
			Assert.assertEquals(1, grp.particles().size());
			final Particle pElem = grp.particles().getFirst();
			final Element elem = (Element) pElem.term();
			Assert.assertNotEquals(c1, elem.type());
			Assert.assertEquals("e2", elem.name());
			final ComplexType originalC1 = (ComplexType) schema.typeDefinitions().stream().filter(t -> ("_" + c1.name()).equals(t.name())).findFirst().get();
			Assert.assertEquals(originalC1, elem.type());
		}
		// xs:attributeGroup redefinition
		{
			final AttributeGroup a1 = schema.attributeGroupDefinitions().stream().filter(a -> "a1".equals(a.name())).findFirst().get();
			Assert.assertEquals(1, a1.attributeUses().size());
			Assert.assertNull(a1.attributeWildcard());
			final AttributeGroup originalA1 = schema.attributeGroupDefinitions().stream().filter(a -> ("_" + a1.name()).equals(a.name())).findFirst().get();
			Assert.assertTrue(String.valueOf(originalA1.attributeUses().size()), originalA1.attributeUses().isEmpty());
			Assert.assertNotNull(originalA1.attributeWildcard());
		}
		// xs:group redefinition
		{
			final ModelGroup g1 = schema.modelGroupDefinitions().stream().filter(g -> "g1".equals(g.name())).findFirst().get();
			Assert.assertEquals(ModelGroup.Compositor.SEQUENCE, g1.compositor());
			Assert.assertEquals(1, g1.particles().size());
			final Element e2 = (Element) g1.particles().getFirst().term();
			Assert.assertEquals("e2", e2.name());
			final ComplexType c2 = (ComplexType) schema.typeDefinitions().stream().filter(t -> "c2".equals(t.name())).findFirst().get();
			final ModelGroup g2 = schema.modelGroupDefinitions().stream().filter(g -> "g2".equals(g.name())).findFirst().get();
			Assert.assertEquals(c2, ((Element) g2.particles().getFirst().term()).type());
			Assert.assertEquals(c2, e2.type());
			final ModelGroup originalG1 = schema.modelGroupDefinitions().stream().filter(g -> ("_" + g1.name()).equals(g.name())).findFirst().get();
			Assert.assertEquals(ModelGroup.Compositor.CHOICE, originalG1.compositor());
			Assert.assertEquals(Wildcard.class, originalG1.particles().getFirst().term().getClass());
		}
	}

}
