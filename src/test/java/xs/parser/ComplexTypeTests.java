package xs.parser;

import static org.junit.Assert.*;

import org.junit.*;

public class ComplexTypeTests {

	private final Schema schema;

	public ComplexTypeTests() throws Exception {
		this.schema = Utilities.stringToSchema(
				Utilities.PROLOG_UTF8
				+ "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>"
				+ "<xs:complexType name='TestA'>"
				+ "<xs:sequence>"
				+ "<xs:element name='Name' type='Enum'/>"
				+ "</xs:sequence>"
				+ "</xs:complexType>"
				+ "<xs:simpleType name='Enum'>"
				+ "<xs:restriction base='xs:string'>"
				+ "<xs:enumeration value='HELLO'/>"
				+ "</xs:restriction>"
				+ "</xs:simpleType>"
				+ "</xs:schema>");
	}

	@Test
	public void test() {
		for (final TypeDefinition type : schema.typeDefinitions()) {
			for (TypeDefinition base = type; base != ComplexType.xsAnyType(); base = base.baseType()) {
				assertNotNull(base);
			}
		}
	}

}