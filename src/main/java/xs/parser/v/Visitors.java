package xs.parser.v;

import java.util.*;
import org.w3c.dom.*;
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

	/**
	 * Returns a deep clone of the node backing the provided schema component.
	 * Note: this method is not a performant implementation and should only be used for rapid prototyping or ad-hoc situational use.
	 * @param schemaComponent The schema component
	 * @return a deep clone of the node backing the provided schema component
	 */
	public static Node deepCloneNode(final SchemaComponent schemaComponent) {
		final class CloneNodeVisitor extends DefaultVisitor {

			private Node node;

			@Override
			public boolean visit(final AnnotatedComponent context, final Node node, final SchemaComponent s) {
				this.node = node;
				return false;
			}

		}

		final CloneNodeVisitor cloneNodeVisitor = new CloneNodeVisitor();
		visit(schemaComponent, cloneNodeVisitor);
		return cloneNodeVisitor.node.cloneNode(true);
	}

}
