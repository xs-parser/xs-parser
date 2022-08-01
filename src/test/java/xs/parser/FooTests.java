package xs.parser;

import java.io.*;
import org.junit.*;
import org.xml.sax.*;

public class FooTests {

	@Test
	public void testFoo() throws IOException, SAXException {
		final Schema fooSchema = new Schema(new File("src/test/resources/foo/foo.xsd"));
		Assert.assertEquals(3, fooSchema.typeDefinitions().size());
		Assert.assertEquals(2, fooSchema.elementDeclarations().size());
		Assert.assertEquals(1, ((ComplexType) fooSchema.elementDeclarations().getFirst().typeDefinition()).assertions().size());
		Assert.assertEquals("foo", fooSchema.elementDeclarations().getFirst().typeDefinition().annotations().getFirst().userInformation().getFirst().getTextContent());
	}

}
