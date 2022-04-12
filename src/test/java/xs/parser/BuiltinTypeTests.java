package xs.parser;

import org.junit.*;

public class BuiltinTypeTests {

	@Test
	public void testDerivations() {
		Assert.assertEquals(ComplexType.xsAnyType(), ComplexType.xsAnyType().baseType());
		Assert.assertEquals(ComplexType.xsAnyType(), SimpleType.xsAnySimpleType().baseType());
		Assert.assertEquals(SimpleType.xsAnySimpleType(), SimpleType.xsAnyAtomicType().baseType());
	}

}
