package xs.parser;

import org.junit.*;
import xs.parser.Schema.*;

public class ImportTests {

	@Test
	public void testImportNotTargetNamespace() throws Exception {
		final DocumentResolver resolver = Utilities.stringResolver(Utilities.PROLOG_UTF8
				+ "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema' targetNamespace='https://test.2'>"
				+ "<xs:element name='test2' form='qualified'/>"
				+ "<xs:element name='test3' targetNamespace='https://test.3'/>"
				+ "<xs:element name='test4' targetNamespace='https://test.4'/>"
				+ "</xs:schema>");
		Assert.assertThrows(ParseException.class, () -> new Schema(resolver, Utilities.stringToDocument(
				Utilities.PROLOG_UTF8
				+ "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema' targetNamespace='https://test.1'>"
				+ "<xs:import namespace='https://test.3' schemaLocation='Test.xsd'/>"
				+ "<xs:import namespace='https://test.4' schemaLocation='Test.xsd'/>"
				+ "</xs:schema>"
		)));
	}

	@Test
	public void testImportTypeFromParentSchema() throws Exception {
		final DocumentResolver resolver = Utilities.stringResolver(Utilities.PROLOG_UTF8
						+ "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema' xmlns:t='https://test.1' targetNamespace='https://test.2'>"
						+ "<xs:simpleType name='B'>"
						+ "<xs:restriction base='t:A'/>"
						+ "</xs:simpleType>"
						+ "</xs:schema>");
		Assert.assertThrows(ParseException.class, () -> new Schema(resolver, Utilities.stringToDocument(
				Utilities.PROLOG_UTF8
				+ "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema' targetNamespace='https://test.1'>"
				+ "<xs:import namespace='https://test.1' schemaLocation='Test.xsd'/>"
				+ "<xs:simpleType name='A'>"
				+ "<xs:restriction base='xs:int'/>"
				+ "</xs:simpleType>"
				+ "</xs:schema>"
		)));
	}

	@Test
	public void testTargetNamespaceWithImport() throws Exception {
		final DocumentResolver resolver = Utilities.stringResolver(Utilities.PROLOG_UTF8
				+ "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema' targetNamespace='https://test.2'>"
				+ "<xs:simpleType name='myType2'>"
				+ "<xs:restriction base='xs:int'/>"
				+ "</xs:simpleType>"
				+ "</xs:schema>");
		final Schema schema = new Schema(resolver, Utilities.stringToDocument(
				Utilities.PROLOG_UTF8
				+ "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema' xmlns:t='https://test.2' targetNamespace='https://test.1'>"
				+ "<xs:import namespace='https://test.2' schemaLocation='Test.xsd'/>"
				+ "<xs:simpleType name='myType1'>"
				+ "<xs:restriction base='t:myType2'/>"
				+ "</xs:simpleType>"
				+ "</xs:schema>"
		));
		final SimpleType myType1 = (SimpleType) schema.typeDefinitions().stream().filter(s -> s.name().equals("myType1")).findAny().get();
		Assert.assertEquals("https://test.1", myType1.targetNamespace());
		final SimpleType myType2 = (SimpleType) schema.typeDefinitions().stream().filter(s -> s.name().equals("myType2")).findAny().get();
		Assert.assertEquals("https://test.2", myType2.targetNamespace());
	}

}
