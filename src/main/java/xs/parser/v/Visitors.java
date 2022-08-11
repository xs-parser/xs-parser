package xs.parser.v;

import java.util.*;
import xs.parser.*;
import xs.parser.internal.util.*;

public final class Visitors {

	private Visitors() { }

	/**
	 * Visits every descendant Schema component of the provided {@code schemaComponent} with the provided visitor.
	 * @param schemaComponent the Schema component
	 * @param visitor the visitor
	 * @throws NullPointerException if {@code schemaComponent} or {@code visitor} is {@code null}
	 * @throws IllegalArgumentException if {@code schemaComponent} is not a valid subclass of {@link SchemaComponent}
	 */
	public static void visit(final SchemaComponent schemaComponent, final Visitor visitor) {
		Objects.requireNonNull(visitor, "visitor");
		VisitorHelper.lookup(schemaComponent.getClass()).accept(schemaComponent, visitor);
	}

}
