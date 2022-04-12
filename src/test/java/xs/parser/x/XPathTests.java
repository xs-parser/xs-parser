package xs.parser.x;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;
import org.junit.runners.Parameterized.*;
import xs.parser.*;
import xs.parser.internal.*;

@RunWith(Parameterized.class)
public class XPathTests {

	private final long schemaCount;
	private final Schema schema;
	private final Function<Schema, NodeSet> nodeSetFn;
	private final NodeSet nodeSet;

	public XPathTests(final String name, final Function<Schema, NodeSet> nodeSetFn) throws Exception {
		this.schema = new Schema(new File("src/test/resources/schema/importer.xsd"));
		this.schemaCount = Files.walk(Paths.get("src/test/resources/schema"))
				.filter(Files::isRegularFile)
				.collect(Collectors.counting()) - 1;
		this.nodeSetFn = nodeSetFn;
		this.nodeSet = nodeSetFn.apply(schema);
	}

	@Parameters(name = "{0}")
	public static Collection<Object[]> nodeSetFns() {
		return Arrays.asList(new Object[][] {
			{ "Saxon XPath", (Function<Schema, NodeSet>) s -> new SaxonNodeSet(NodeSet.DEFAULT_NAMESPACE_CONTEXT, s) },
			{ "JAXP XPath", (Function<Schema, NodeSet>) s -> new JaxpNodeSet(NodeSet.DEFAULT_NAMESPACE_CONTEXT, s) }
		});
	}

	@Test
	public void testResultSize() throws Exception {
		final NodeSet col = nodeSet.xpath("fn:collection()");
		Assert.assertEquals(schemaCount, col.size());
		Assert.assertEquals(col.size(), col.stream().map(n -> NodeHelper.ownerDocument(n).getDocumentURI()).distinct().count());
	}

	@Test
	public void testComplexTypes() {
		Assert.assertEquals(schema.typeDefinitions().stream().filter(ComplexType.class::isInstance).count(), nodeSet.xpath("fn:collection()/xs:schema/xs:complexType").size());
	}

	@Test
	public void testSimpleTypes() {
		final Set<TypeDefinition> typedefs = new HashSet<>(schema.typeDefinitions());
		Assert.assertEquals(typedefs.size(), schema.typeDefinitions().size());
		final NodeSet xpath = nodeSet.xpath("fn:collection()/xs:schema/xs:simpleType/@name");
		typedefs.removeIf(t -> t instanceof ComplexType);
		typedefs.removeIf(t -> xpath.stream().filter(n -> t.name().equals(n.getNodeValue())).count() > 0);
		Assert.assertEquals(typedefs.toString(), 0, typedefs.size());
		Assert.assertEquals(schema.typeDefinitions().stream().filter(SimpleType.class::isInstance).count(), nodeSet.xpath("fn:collection()/xs:schema/xs:simpleType").size());
		final List<SimpleType> simpleTypeEnums = schema.typeDefinitions().stream()
				.filter(SimpleType.class::isInstance)
				.map(SimpleType.class::cast)
				.filter(s -> s.facets().stream().anyMatch(f -> f instanceof ConstrainingFacet.Enumeration))
				.collect(Collectors.toList());
		final NodeSet enumNamesAsNode = nodeSet.xpath("fn:collection()/xs:schema/xs:simpleType[xs:restriction/xs:enumeration]/@name");
		Assert.assertEquals(simpleTypeEnums.size(), enumNamesAsNode.size());
		enumNamesAsNode.split().forEach(n -> {
			Assert.assertTrue(simpleTypeEnums.stream().anyMatch(s -> s.name().equals(n.getSingleNodeValue().getNodeValue())));
		});
		final NodeSet enumNamesAsString = nodeSet.xpath("string(fn:collection()/xs:schema/xs:simpleType[xs:restriction/xs:enumeration]/@name)");
		Assert.assertEquals(simpleTypeEnums.size(), enumNamesAsString.size());
		enumNamesAsString.split().forEach(n -> {
			Assert.assertTrue(n.isAtomic());
			Assert.assertTrue(simpleTypeEnums.stream().anyMatch(s -> s.name().equals(n.getStringValue())));
		});
	}

	@Test
	public void testAttributes() {
		Assert.assertEquals(schema.attributeDeclarations().size(), nodeSet.xpath("fn:collection()/xs:schema/xs:attribute").size());
	}

	@Test
	public void testAttributeGroups() {
		Assert.assertEquals(schema.attributeGroupDefinitions().size(), nodeSet.xpath("fn:collection()/xs:schema/xs:attributeGroup").size());
	}

	@Test
	public void testElements() {
		Assert.assertEquals(schema.elementDeclarations().size(), nodeSet.xpath("fn:collection()/xs:schema/xs:element").size());
	}

	@Test
	public void testSchemaImport() {
		final NodeSet schemas = nodeSet.xpath("fn:collection()/xs:schema");
		final NodeSet elements = schemas.xpath("xs:element");
		elements.split().forEach(col -> {
			Assert.assertEquals(elements.size(), col.xpath("fn:collection()").split().count());
			Assert.assertEquals(elements.size(), col.xpath("fn:collection()").size());
		});
		Assert.assertEquals(elements.size() + schemas.size(), nodeSet.xpath("fn:collection()/xs:schema|fn:collection()/xs:schema/xs:element").size());
	}

	@Test
	public void testStartsWith() throws Exception {
		final Schema base = new Schema(new File("src/test/resources/schema/base.xsd"));
		final NodeSet root = nodeSetFn.apply(base);
		if (root instanceof SaxonNodeSet) {
			final NodeSet startsWithResult1 = root.xpath("starts-with((), ())");
			Assert.assertTrue(startsWithResult1.toString(), startsWithResult1.getBooleanValue()); // Always true according to XPath 2.0 functions spec
		}
		final NodeSet startsWithResult2 = root.xpath("starts-with('MyValue', 'my')");
		Assert.assertFalse(startsWithResult2.toString(), startsWithResult2.getBooleanValue()); // Check case-insensitivity
		final NodeSet startsWithResult3 = root.xpath("starts-with('MyValue', 'My')");
		Assert.assertTrue(startsWithResult3.toString(), startsWithResult3.getBooleanValue()); // Check case-sensitivity
	}

	@Test
	public void testEmpty() throws Exception {
		final Schema base = new Schema(new File("src/test/resources/schema/base.xsd"));
		final NodeSet n = NodeSet.of(base).xpath("/this/should/be/empty");
		Assert.assertTrue(n.isEmpty());
		Assert.assertFalse(n.isPresent());
		Assert.assertFalse(n.isAtomic());
		Assert.assertEquals(0, n.size());
		Assert.assertEquals(0L, n.stream().count());
		Assert.assertEquals(0L, n.split().count());
	}

	@Test
	public void testFnCollection() {
		final NodeSet n = nodeSet.xpath("fn:collection()/xs:schema");
		Assert.assertEquals(schemaCount, n.size());
	}

	@Test
	public void testFnUriCollection() {
		final NodeSet n = nodeSet.xpath("fn:uri-collection()");
		Assert.assertEquals(schemaCount, n.size());
		final long uriCount = n.split().map(x -> x.isAtomic() ? x.getStringValue() : x.getSingleNodeValue().getNodeValue()).distinct().count();
		Assert.assertEquals(schemaCount, uriCount);
	}

}