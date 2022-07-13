package xs.parser;

import org.junit.*;

public class BuiltinTypeTests {

	@Test
	public void testDerivations() {
		Assert.assertEquals(ComplexType.xsAnyType(), ComplexType.xsAnyType().baseTypeDefinition());
		Assert.assertEquals(ComplexType.xsAnyType(), SimpleType.xsAnySimpleType().baseTypeDefinition());
		Assert.assertEquals(SimpleType.xsAnySimpleType(), SimpleType.xsAnyAtomicType().baseTypeDefinition());
	}

}
