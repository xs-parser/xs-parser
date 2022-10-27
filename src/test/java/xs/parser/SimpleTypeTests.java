package xs.parser;

import java.util.*;
import java.util.stream.*;
import org.junit.*;

public class SimpleTypeTests {

	private final Schema schema;

	public SimpleTypeTests() throws Exception {
		this.schema = Utilities.stringToSchema(
				Utilities.PROLOG_UTF8
				+ "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>"
				+ "  <xs:simpleType name='TestA'>"
				+ "    <xs:annotation>"
				+ "      <xs:documentation>simpleType user information</xs:documentation>"
				+ "    </xs:annotation>"
				+ "    <xs:restriction>"
				+ "      <xs:annotation>"
				+ "        <xs:documentation>restriction user information</xs:documentation>"
				+ "      </xs:annotation>"
				+ "      <xs:simpleType>"
				+ "        <xs:restriction base='xs:int'>"
				+ "          <xs:minInclusive value='2'/>"
				+ "          <xs:maxInclusive value='10'/>"
				+ "          <xs:pattern value='[A-Z]'/>"
				+ "        </xs:restriction>"
				+ "      </xs:simpleType>"
				+ "      <xs:maxInclusive value='12'/>"
				+ "      <xs:pattern value='[a-z]'/>"
				+ "      <xs:pattern value='[y-z]'/>"
				+ "    </xs:restriction>"
				+ "  </xs:simpleType>"
				+ "  <xs:simpleType name='TestB'>"
				+ "    <xs:restriction base='TestA'>"
				+ "      <xs:totalDigits value='123'/>"
				+ "      <xs:minInclusive value='1'/>"
				+ "    </xs:restriction>"
				+ "  </xs:simpleType>"
				+ "  <xs:simpleType name='TestC'>"
				+ "    <xs:restriction base='xs:boolean'/>"
				+ "  </xs:simpleType>"
				+ "  <xs:simpleType name='TestD'>"
				+ "    <xs:restriction base='TestE'>"
				+ "      <xs:enumeration value='C'/>"
				+ "      <xs:enumeration value='D'/>"
				+ "    </xs:restriction>"
				+ "  </xs:simpleType>"
				+ "  <xs:simpleType name='TestE'>"
				+ "    <xs:restriction base='xs:string'>"
				+ "      <xs:enumeration value='A'/>"
				+ "      <xs:enumeration value='B'/>"
				+ "    </xs:restriction>"
				+ "  </xs:simpleType>"
				+ "</xs:schema>");
	}

	public static <T extends ConstrainingFacet> T facetOf(final SimpleType s, final Class<T> cls) {
		Assert.assertTrue(s.facets().stream().map(c -> c.getClass().getSimpleName() + ' ' + c.value()).collect(Collectors.joining(", ")).toString(), s.facets().stream().filter(cls::isInstance).count() <= 1);
		return s.facets().stream().filter(cls::isInstance).map(cls::cast).findAny().orElse(null);
	}

	@Test
	public void testAnonRestriction() {
		Assert.assertEquals(5, schema.typeDefinitions().size());
		final SimpleType testA = (SimpleType) schema.typeDefinitions().stream().filter(s -> s.name().equals("TestA")).findAny().get();
		Assert.assertEquals(4, testA.fundamentalFacets().size());
		Assert.assertNotNull(testA.facets());
		Assert.assertEquals(facetOf(testA, ConstrainingFacet.Pattern.class).value().toString(), 3, facetOf(testA, ConstrainingFacet.Pattern.class).value().size());
		final Iterator<String> patterns = facetOf(testA, ConstrainingFacet.Pattern.class).value().iterator();
		Assert.assertEquals("[\\-+]?[0-9]+", patterns.next());
		Assert.assertEquals("[A-Z]", patterns.next());
		Assert.assertEquals("[a-z]|[y-z]", patterns.next());
		Assert.assertEquals(12, facetOf(testA, ConstrainingFacet.MaxInclusive.class).value().intValue());
		Assert.assertEquals(2, facetOf(testA, ConstrainingFacet.MinInclusive.class).value().intValue());
		final SimpleType testB = (SimpleType) schema.typeDefinitions().stream().filter(s -> s.name().equals("TestB")).findAny().get();
		Assert.assertEquals(4, testB.fundamentalFacets().size());
		Assert.assertNotNull(testB.facets());
		Assert.assertEquals(facetOf(testA, ConstrainingFacet.Pattern.class).value(), facetOf(testB, ConstrainingFacet.Pattern.class).value());
		Assert.assertEquals(12, facetOf(testB, ConstrainingFacet.MaxInclusive.class).value().intValue());
		Assert.assertEquals(1, facetOf(testB, ConstrainingFacet.MinInclusive.class).value().intValue());
		Assert.assertEquals(123, facetOf(testB, ConstrainingFacet.TotalDigits.class).value().intValue());
	}

	@Test
	public void testSimpleTypeDerived() {
		final SimpleType intType = SimpleType.xsInt();
		Assert.assertEquals(-2147483648, facetOf(intType, ConstrainingFacet.MinInclusive.class).value().intValue());
		Assert.assertEquals(2147483647, facetOf(intType, ConstrainingFacet.MaxInclusive.class).value().intValue());
		Assert.assertTrue(facetOf(intType, ConstrainingFacet.Pattern.class).value().contains("[\\-+]?[0-9]+"));
		final ConstrainingFacet.FractionDigits fractionDigits = facetOf(intType, ConstrainingFacet.FractionDigits.class);
		Assert.assertEquals(true, fractionDigits.fixed());
		Assert.assertEquals(0, fractionDigits.value().intValue());
		final SimpleType testA = (SimpleType) schema.typeDefinitions().stream().filter(s -> s.name().equals("TestA")).findAny().get();
		Assert.assertEquals(intType, testA.baseTypeDefinition().baseTypeDefinition());
		final SimpleType decimalType = SimpleType.xsDecimal();
		Assert.assertEquals(decimalType, testA.primitiveTypeDefinition());
		final SimpleType testB = (SimpleType) schema.typeDefinitions().stream().filter(s -> s.name().equals("TestB")).findAny().get();
		Assert.assertEquals(testA, testB.baseTypeDefinition());
		Assert.assertEquals(decimalType, testB.primitiveTypeDefinition());
	}

	@Test
	public void testBooleanPrimitive() {
		final SimpleType booleanType = SimpleType.xsBoolean();
		final SimpleType testC = (SimpleType) schema.typeDefinitions().stream().filter(s -> s.name().equals("TestC")).findAny().get();
		Assert.assertEquals(booleanType, testC.primitiveTypeDefinition());
		Assert.assertEquals(4, booleanType.fundamentalFacets().size());
		Assert.assertEquals(booleanType.fundamentalFacets(), testC.fundamentalFacets());
	}

	@Test
	public void testEnumerations() {
		final SimpleType testD = (SimpleType) schema.typeDefinitions().stream().filter(s -> s.name().equals("TestD")).findAny().get();
		Assert.assertEquals(new LinkedHashSet<>(Arrays.asList("C", "D")), facetOf(testD, ConstrainingFacet.Enumeration.class).value());
		final SimpleType testE = (SimpleType) schema.typeDefinitions().stream().filter(s -> s.name().equals("TestE")).findAny().get();
		Assert.assertEquals(new LinkedHashSet<>(Arrays.asList("A", "B")), facetOf(testE, ConstrainingFacet.Enumeration.class).value());
	}

	@Test
	public void testAnnotations() {
		final SimpleType testA = (SimpleType) schema.typeDefinitions().stream().filter(s -> s.name().equals("TestA")).findAny().get();
		Assert.assertEquals("simpleType user information", testA.annotations().getFirst().userInformation().getFirst().getTextContent());
		Assert.assertEquals("restriction user information", testA.annotations().getLast().userInformation().getFirst().getTextContent());
	}

}
