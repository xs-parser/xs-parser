package xs.parser.v;

import java.io.*;
import java.util.*;
import org.junit.*;
import org.w3c.dom.*;
import org.w3c.dom.Element;
import xs.parser.*;
import xs.parser.internal.util.*;

public class VisitorTests {

	@Test
	public void testVisitExceptions() {
		final SchemaComponent s = new SchemaComponent() { };
		Assert.assertThrows(NullPointerException.class, () -> Visitors.visit(s, null));
		final Visitor visitor = new DefaultVisitor();
		Assert.assertThrows(IllegalArgumentException.class, () -> Visitors.visit(s, visitor));
		final AnnotatedComponent a = new AnnotatedComponent() {

			@Override
			public Deque<Annotation> annotations() {
				return Deques.emptyDeque();
			}

		};
		Assert.assertThrows(IllegalArgumentException.class, () -> Visitors.visit(a, visitor));
	}

	@Test
	public void testDeepCloneNode() throws Exception {
		final Schema schema = new Schema(new File("src/test/resources/schema/base.xsd"));
		final Node schemaNode = Visitors.deepCloneNode(schema);
		Assert.assertNotNull(schemaNode);
		Assert.assertTrue(schemaNode instanceof Document);
		Arrays.asList(schema.attributeDeclarations(), schema.attributeGroupDefinitions(), schema.elementDeclarations(), schema.identityConstraintDefinitions(), schema.modelGroupDefinitions(), schema.annotations(), schema.typeDefinitions()).forEach(a ->
				a.forEach(d -> {
					final Node node = Visitors.deepCloneNode(d);
					Assert.assertNotNull(node);
					Assert.assertTrue(node instanceof Element);
				}));
	}

}
