package xs.parser.v;

import java.util.*;
import org.junit.*;
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

}
