package xs.parser.v;

import java.util.*;
import xs.parser.*;
import xs.parser.internal.util.*;

/**
 * Utility class for visiting schema components
 */
public final class Visitors {

	private Visitors() { }

	/**
	 * Visits every descendant Schema component of the provided {@code schemaComponent} with the provided visitor.
	 * @param schemaComponent The Schema component
	 * @param visitor The visitor
	 * @throws NullPointerException If {@code schemaComponent} or {@code visitor} is {@code null}
	 * @throws IllegalArgumentException If {@code schemaComponent} is not a valid subclass of {@link SchemaComponent}
	 */
	public static void visit(final SchemaComponent schemaComponent, final Visitor visitor) {
		Objects.requireNonNull(visitor, "visitor");
		VisitorHelper.lookup(schemaComponent.getClass()).accept(schemaComponent, visitor);
	}

}
