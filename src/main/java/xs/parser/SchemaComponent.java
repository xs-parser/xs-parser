package xs.parser;

import org.w3c.dom.*;
import xs.parser.v.*;

public interface SchemaComponent {

	/** @return The underlying XDM node that this Schema Component was constructed from. */
	public Node node();

	/**
	 * TODO
	 */
	public void visit(Visitor visitor);

}
