package xs.parser;

import java.util.*;
import java.util.stream.*;
import javax.xml.namespace.*;
import xs.parser.internal.*;
import xs.parser.internal.SequenceParser.*;

/**
 * <pre>
 * &lt;union
 *   id = ID
 *   memberTypes = List of QName
 *   {any attributes with non-schema namespace . . .}&gt;
 *   Content: (annotation?, simpleType*)
 * &lt;/union&gt;
 * </pre>
 */
public class SimpleUnion {

	protected static final SequenceParser parser = new SequenceParser()
			.optionalAttributes(AttributeValue.ID, AttributeValue.MEMBERTYPES)
			.elements(0, 1, ElementValue.ANNOTATION)
			.elements(0, Integer.MAX_VALUE, ElementValue.SIMPLETYPE);

	private final Deque<Annotation> annotations;
	private final Deque<SimpleType> memberTypes;

	private SimpleUnion(final Deque<Annotation> annotations, final Deque<SimpleType> memberTypes) {
		this.annotations = Objects.requireNonNull(annotations);
		this.memberTypes = Objects.requireNonNull(memberTypes);
	}

	protected static SimpleUnion parse(final Result result) {
		final Deque<QName> memberTypes = result.value(AttributeValue.MEMBERTYPES);
		if (memberTypes != null) {
			final Deque<SimpleType> memberTypesValues = DeferredArrayDeque.of(memberTypes.stream()
					.map(memberType -> result.schema().find(memberType, SimpleType.class))
					.collect(Collectors.toCollection(ArrayDeque::new)));
			return new SimpleUnion(result.annotations(), memberTypesValues);
		}
		final Deque<SimpleType> memberTypesElem = result.parseAll(ElementValue.SIMPLETYPE);
		return new SimpleUnion(result.annotations(), memberTypesElem);
	}

	protected Deque<Annotation> annotations() {
		return annotations;
	}

	public Deque<SimpleType> memberTypes() {
		return memberTypes;
	}

}