package xs.parser;

import java.io.*;
import javax.xml.*;
import org.junit.*;
import xs.parser.Schema.*;
import xs.parser.v.*;

public class IncludeTests {

	@Test
	public void testIncludeFileMultipleTimes() throws Exception {
		final DocumentResolver resolver = Utilities.stringResolver(Utilities.PROLOG_UTF8
				+ "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">"
				+ "  <xs:simpleType name=\"A\">"
				+ "    <xs:restriction base=\"xs:string\"/>"
				+ "  </xs:simpleType>"
				+ "</xs:schema>");
		final Schema schema = new Schema(resolver,
				Utilities.stringToDocument(
						Utilities.PROLOG_UTF8
						+ "<xs:schema xmlns:test=\"https://my.test\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\" targetNamespace=\"https://my.test\">"
						+ "  <xs:include schemaLocation=\"./Test.xsd\"/>"
						+ "  <xs:include schemaLocation=\"Test.xsd\"/>"
						+ "</xs:schema>"
				));
		Assert.assertEquals(1, schema.typeDefinitions().size());
		final SimpleType a = (SimpleType) schema.typeDefinitions().getFirst();
		Assert.assertEquals("https://my.test", a.targetNamespace());
		Assert.assertEquals("A", a.name());
		Assert.assertEquals(XMLConstants.W3C_XML_SCHEMA_NS_URI, a.baseTypeDefinition().targetNamespace());
		Assert.assertEquals("string", a.baseTypeDefinition().name());
	}

	@Test
	public void testIncludeSameFileWithDifferentNamesFails() throws Exception {
		final DocumentResolver resolver = Utilities.stringResolver(Utilities.PROLOG_UTF8
				+ "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">"
				+ "  <xs:simpleType name=\"A\">"
				+ "    <xs:restriction base=\"xs:string\"/>"
				+ "  </xs:simpleType>"
				+ "</xs:schema>");
		Assert.assertThrows(ParseException.class, () -> new Schema(resolver, Utilities.stringToDocument(
				Utilities.PROLOG_UTF8
				+ "<xs:schema xmlns:test=\"https://my.test\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\">"
				+ "  <xs:include schemaLocation=\"./Test.xsd\"/>"
				+ "  <xs:include schemaLocation=\"Test2.xsd\"/>"
				+ "</xs:schema>"
		)));
	}

	@Test
	public void testSelfRef() throws Exception {
		final Schema schema = new Schema(new File("src/test/resources/include/selfref.xsd"));
		Assert.assertEquals(4, schema.elementDeclarations().size());
		final Element element = schema.elementDeclarations().stream().filter(e -> "Example".equals(e.name())).findAny().get();
		Assert.assertEquals(ComplexType.xsAnyType(), element.typeDefinition());
		final Element elementA = schema.elementDeclarations().stream().filter(e -> "ExampleA".equals(e.name())).findAny().get();
		Assert.assertEquals(ComplexType.xsAnyType(), elementA.typeDefinition());
		final Element elementB = schema.elementDeclarations().stream().filter(e -> "ExampleB".equals(e.name())).findAny().get();
		Assert.assertEquals(ComplexType.xsAnyType(), elementB.typeDefinition());
		final Element elementC = schema.elementDeclarations().stream().filter(e -> "ExampleC".equals(e.name())).findAny().get();
		Assert.assertEquals(ComplexType.xsAnyType(), elementC.typeDefinition());
	}

	@Test
	public void testIncludeSame() throws Exception {
		final Schema schema = new Schema(Utilities.stringToDocument(
				Utilities.PROLOG_UTF8
				+ "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>"
				+ "  <xs:include schemaLocation='src/test/resources/schema/base.xsd'/>"
				+ "  <xs:include schemaLocation='src/test/resources/schema/base.xsd'/>"
				+ "</xs:schema>"));
		Assert.assertEquals(7, schema.typeDefinitions().size());
	}

	@Test
	public void testIncludeIncomplete() throws Exception {
		final String bSchemaText = Utilities.PROLOG_UTF8
				+ "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"a\" xmlns:a=\"a\">"
				+ "  <xs:complexType name=\"BType\">"
				+ "    <xs:sequence>"
				+ "      <xs:element name=\"BElement\" type=\"a:AType\" />"
				+ "    </xs:sequence>"
				+ "  </xs:complexType>"
				+ "</xs:schema>";
		Assert.assertThrows(ParseException.class, () -> new Schema(Utilities.stringToDocument(bSchemaText))); // Should fail to resolve a:AType
		final DocumentResolver resolver = Utilities.stringResolver(bSchemaText);
		final String aSchemaText = Utilities.PROLOG_UTF8
				+ "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"a\" xmlns:a=\"a\">"
				+ "  <xs:include schemaLocation=\"b.xsd\" />"
				+ "  <xs:complexType name=\"AType\">"
				+ "    <xs:sequence>"
				+ "      <xs:element name=\"AElement\" type=\"xs:string\" />"
				+ "    </xs:sequence>"
				+ "  </xs:complexType>"
				+ "  <xs:element name=\"AElement\" type=\"a:BType\" />"
				+ "</xs:schema>";
		final Schema schema = new Schema(resolver, Utilities.stringToDocument(aSchemaText));
		Visitors.visit(schema, new DefaultVisitor());
		final Element aElement = schema.elementDeclarations().getLast();
		Assert.assertEquals("AElement", aElement.name());
		Assert.assertEquals("BType", aElement.typeDefinition().name());
	}

}
