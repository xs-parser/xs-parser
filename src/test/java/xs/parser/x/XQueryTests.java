package xs.parser.x;

import java.io.*;
import java.nio.file.*;
import java.util.stream.*;
import org.junit.*;
import xs.parser.*;

public class XQueryTests {

	private final NodeSet root;
	private final Schema schema;
	private final long schemaCount;

	public XQueryTests() throws Exception {
		this.schema = new Schema(new File("src/test/resources/schema/importer.xsd"));
		this.root = NodeSet.of(schema);
		this.schemaCount = Files.walk(Paths.get("src/test/resources/schema"))
				.filter(Files::isRegularFile)
				.collect(Collectors.counting()) - 1;
	}

	@Test
	public void testSchemaSize() throws Exception {
		final NodeSet schemaCountResult = root.xquery("count(collection()/xs:schema)");
		Assert.assertEquals(1, schemaCountResult.size());
		Assert.assertTrue(schemaCountResult.isAtomic());
		schemaCountResult.split().forEach(n -> {
			Assert.assertTrue(n.isAtomic());
			Assert.assertEquals(schemaCount, n.getLongValue());
		});
	}

	@Test
	public void testSimpleTypes() throws Exception {
		final NodeSet schemas = root.xquery("fn:collection()/xs:schema");
		Assert.assertEquals(schemaCount, schemas.size());
		final NodeSet simpleTypes = schemas.xquery("xs:simpleType");
		Assert.assertEquals(schema.typeDefinitions().stream().filter(SimpleType.class::isInstance).count(), simpleTypes.size());
		Assert.assertEquals(simpleTypes.split().count(), simpleTypes.size());
		simpleTypes.split().forEach(n -> {
			Assert.assertFalse(n.isAtomic());
			final NodeSet copy = n.xquery("collection()/ancestor::xs:schema/xs:simpleType");
			Assert.assertEquals(simpleTypes.size(), copy.size());
		});
		final NodeSet simpleTypeNames = schemas.xquery("xs:simpleType/string(@name)");
		Assert.assertFalse(simpleTypeNames.isAtomic());
		Assert.assertEquals(simpleTypes.size(), simpleTypeNames.size());
		Assert.assertEquals(simpleTypeNames.size(), simpleTypeNames.split().count());
		simpleTypeNames.split().forEach(n -> {
			Assert.assertTrue(n.isAtomic());
			Assert.assertNotNull(n.getStringValue());
		});
	}

	@Test
	public void testSchemaFileStrings() throws Exception {
		final NodeSet col = root.xquery("collection()/xs:schema");
		Assert.assertEquals(schemaCount, col.size());
		Assert.assertFalse(col.isAtomic());
		col.split().forEach(n -> {
			Assert.assertFalse(n.isAtomic());
			Assert.assertNotNull(n.getSingleNodeValue());
		});
	}

}
