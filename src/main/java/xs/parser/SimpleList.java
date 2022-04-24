package xs.parser;

import java.util.*;
import javax.xml.namespace.*;
import xs.parser.internal.*;
import xs.parser.internal.SequenceParser.*;

/**
 * <pre>
 * &lt;list
 *   id = ID
 *   itemType = QName
 *   {any attributes with non-schema namespace . . .}&gt;
 *   Content: (annotation?, simpleType?)
 * &lt;/list&gt;
 * </pre>
 */
public class SimpleList {

	protected static final SequenceParser parser = new SequenceParser()
			.optionalAttributes(AttributeValue.ID, AttributeValue.ITEMTYPE)
			.elements(0, 1, ElementValue.ANNOTATION)
			.elements(0, 1, ElementValue.SIMPLETYPE);

	private final Deque<Annotation> annotations;
	private final Deferred<SimpleType> itemType;

	SimpleList(final Deque<Annotation> annotations, final Deferred<SimpleType> itemType) {
		this.annotations = Objects.requireNonNull(annotations);
		this.itemType = Objects.requireNonNull(itemType);
	}

	protected static SimpleList parse(final Result result) {
		final QName itemTypeName = result.value(AttributeValue.ITEMTYPE);
		if (itemTypeName != null) {
			return new SimpleList(result.annotations(), result.schema().find(itemTypeName, SimpleType.class));
		}
		final SimpleType itemSimpleType = result.parse(ElementValue.SIMPLETYPE);
		return new SimpleList(result.annotations(), Deferred.value(itemSimpleType));
	}

	protected Deque<Annotation> annotations() {
		return annotations;
	}

	protected Deferred<SimpleType> itemType() {
		return itemType;
	}

}