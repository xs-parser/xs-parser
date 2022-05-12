package xs.parser;

import java.util.*;

public interface AnnotatedComponent extends SchemaComponent {

	public Deque<Annotation> annotations();

}
