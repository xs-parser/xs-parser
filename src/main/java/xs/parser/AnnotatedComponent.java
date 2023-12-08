package xs.parser;

import java.util.*;

/**
 * Annotated Component covers all the different kinds of component which may have annotations.
 *
 * Annotation of schemas and schema components, with material for human or computer consumption, is provided for by allowing application information and human information at the beginning of most major schema elements, and anywhere at the top level of schemas. The XML representation for an annotation schema component is an &lt;annotation&gt; element information item.
 */
public interface AnnotatedComponent extends SchemaComponent {

	/** @return A sequence of Annotation components. */
	public Deque<Annotation> annotations();

}
