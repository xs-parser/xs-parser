package xs.parser;

import java.io.*;
import java.util.*;
import javax.xml.namespace.*;
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
		final ComplexType superCompoType = (ComplexType) fooSchema.typeDefinitions().stream().filter(t -> "superCompoType".equals(t.name())).findAny().get();
		Assert.assertEquals("openContent 'r z'", Set.of("defined", new QName("r", "y")), superCompoType.contentType().openContent().wildcard().namespaceConstraint().disallowedNames());
		final ComplexType sub2CompoType = (ComplexType) fooSchema.typeDefinitions().stream().filter(t -> "sub2CompoType".equals(t.name())).findAny().get();
		Assert.assertEquals("openContent 'q z'", Set.of(new QName("q", "z")), sub2CompoType.contentType().openContent().wildcard().namespaceConstraint().disallowedNames());
	}

}
