package xs.parser;

import java.util.*;
import javax.xml.*;
import javax.xml.namespace.*;
import org.junit.*;
import org.w3c.dom.*;

public class AttributeTests {

	@Test
	public void testDisallowedAttribute() {
		Assert.assertThrows(Schema.ParseException.class, () -> Utilities.stringToSchema(
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
		Assert.assertThrows(Schema.ParseException.class, () -> Utilities.stringToSchema(
				Utilities.PROLOG_UTF8
				+ "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>"
				+ "<xs:include/>"
				+ "</xs:schema>"
		));
	}

	@Test
	public void testXsiAttributes() {
		Assert.assertEquals(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, Attribute.xsiNil().targetNamespace());
		Assert.assertEquals("nil", Attribute.xsiNil().name());
		Assert.assertNotNull(Attribute.xsiNil().type());
		Assert.assertEquals(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, Attribute.xsiNoNamespaceSchemaLocation().targetNamespace());
		Assert.assertEquals("noNamespaceSchemaLocation", Attribute.xsiNoNamespaceSchemaLocation().name());
		Assert.assertNotNull(Attribute.xsiNoNamespaceSchemaLocation().type());
		Assert.assertEquals(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, Attribute.xsiSchemaLocation().targetNamespace());
		Assert.assertEquals("schemaLocation", Attribute.xsiSchemaLocation().name());
		Assert.assertNotNull(Attribute.xsiSchemaLocation().type());
		Assert.assertEquals(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, Attribute.xsiType().targetNamespace());
		Assert.assertEquals("type", Attribute.xsiType().name());
		Assert.assertNotNull(Attribute.xsiType().type());
	}

}
