package xs.parser;

import static org.junit.Assert.*;

import org.junit.*;

public class ComplexTypeTests {

	private final Schema schema;

	public ComplexTypeTests() throws Exception {
		this.schema = Utilities.stringToSchema(
				Utilities.PROLOG_UTF8
				+ "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>"
				+ "  <xs:complexType name='TestA'>"
				+ "    <xs:sequence>"
				+ "      <xs:element name='Name' type='Enum' />"
				+ "    </xs:sequence>"
				+ "  </xs:complexType>"
				+ "  <xs:complexType name='TestB'>"
				+ "    <xs:annotation>"
				+ "      <xs:documentation>complexType user information</xs:documentation>"
				+ "    </xs:annotation>"
				+ "    <xs:complexContent>"
				+ "      <xs:annotation>"
				+ "        <xs:documentation>complexContent user information</xs:documentation>"
				+ "      </xs:annotation>"
				+ "      <xs:extension base='TestA' />"
				+ "    </xs:complexContent>"
				+ "  </xs:complexType>"
				+ "  <xs:simpleType name='Enum'>"
				+ "    <xs:restriction base='xs:string'>"
				+ "      <xs:enumeration value='HELLO' />"
				+ "    </xs:restriction>"
				+ "  </xs:simpleType>"
				+ "</xs:schema>");
	}

	@Test
	public void test() {
		for (final TypeDefinition type : schema.typeDefinitions()) {
			for (TypeDefinition base = type; base != ComplexType.xsAnyType(); base = base.baseTypeDefinition()) {
				assertNotNull(base);
			}
		}
	}

	@Test
	public void testAnnotations() {
		final ComplexType testA = (ComplexType) schema.typeDefinitions().stream().filter(t -> "TestB".equals(t.name())).findAny().get();
		Assert.assertEquals("complexType user information", testA.annotations().getFirst().userInformation().getFirst().getTextContent());
		Assert.assertEquals("complexContent user information", testA.annotations().getLast().userInformation().getFirst().getTextContent());
	}

}
