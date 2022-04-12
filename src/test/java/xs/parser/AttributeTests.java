package xs.parser;

import java.util.*;
import javax.xml.namespace.*;
import org.junit.*;
import org.w3c.dom.*;

public class AttributeTests {

	@Test
	public void testDisallowedAttribute() {
		Assert.assertThrows(SchemaParseException.class, () -> Utilities.stringToSchema(
				Utilities.PROLOG_UTF8
				+ "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema' thisNameIsNotAllowed='123'>"
				+ "</xs:schema>"
		));
	}

	@Test
	public void testAllowedNonSchemaAttribute() throws Exception {
		final Schema schema = Utilities.stringToSchema(
				Utilities.PROLOG_UTF8
				+ "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema' xmlns:t='https://test' t:thisNameIsAllowed='123'>"
				+ "</xs:schema>"
		);
		final Node attr = schema.node().getAttributes().getNamedItemNS("https://test", "thisNameIsAllowed");
		Assert.assertEquals(
				Collections.singletonMap(new QName("https://test", "thisNameIsAllowed"), "123"),
				Collections.singletonMap(new QName(attr.getNamespaceURI(), attr.getLocalName()), attr.getNodeValue()));
	}

	@Test
	public void testMissingRequiredAttribute() {
		Assert.assertThrows(SchemaParseException.class, () -> Utilities.stringToSchema(
				Utilities.PROLOG_UTF8
				+ "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>"
				+ "<xs:include/>"
				+ "</xs:schema>"
		));
	}

}