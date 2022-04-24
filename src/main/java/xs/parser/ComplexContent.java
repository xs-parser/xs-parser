package xs.parser;

import java.util.*;
import xs.parser.Annotation.*;
import xs.parser.ComplexType.*;
import xs.parser.internal.*;
import xs.parser.internal.SequenceParser.*;

/**
 * <pre>
 * &lt;complexContent
 *   id = ID
 *   mixed = boolean
 *   {any attributes with non-schema namespace . . .}&gt;
 *   Content: (annotation?, (restriction | extension))
 * &lt;/complexContent&gt;
 * </pre>
 */
public class ComplexContent {

	/**
	 * &lt;assert
	 *   id = ID
	 *   test = an XPath expression
	 *   xpathDefaultNamespace = (anyURI | (##defaultNamespace | ##targetNamespace | ##local))
	 *   {any attributes with non-schema namespace . . .}&gt;
	 *   Content: (annotation?)
	 * &lt;/assert&gt;
	 */
	public static class Assert {

		protected static final SequenceParser parser = new SequenceParser()
				.requiredAttributes(AttributeValue.TEST)
				.optionalAttributes(AttributeValue.ID, AttributeValue.XPATHDEFAULTNAMESPACE)
				.elements(0, 1, ElementValue.ANNOTATION);

		private Assert() { }

	}

	protected static final SequenceParser parser = new SequenceParser()
			.optionalAttributes(AttributeValue.ID, AttributeValue.MIXED)
			.elements(0, 1, ElementValue.ANNOTATION)
			.elements(1, 1, ElementValue.COMPLEX_RESTRICTION, ElementValue.COMPLEX_EXTENSION);

	private final Deque<Annotation> annotations;
	private final Boolean mixed;
	private final DerivationMethod derivationMethod;
	private final ComplexDerivation derivation;

	ComplexContent(final Deque<Annotation> annotations, final Boolean mixed, final DerivationMethod derivationMethod, final ComplexDerivation derivation) {
		this.annotations = Objects.requireNonNull(annotations);
		this.mixed = mixed;
		this.derivationMethod = derivationMethod;
		this.derivation = derivation;
	}

	protected static ComplexContent parse(final Result result) {
		final Boolean mixed = result.value(AttributeValue.MIXED);
		final ComplexDerivation restriction = result.parse(ElementValue.COMPLEX_RESTRICTION);
		final ComplexDerivation extension = result.parse(ElementValue.COMPLEX_EXTENSION);
		final DerivationMethod derivationMethod;
		final Deque<Annotation> annotations;
		if (restriction != null) {
			derivationMethod = DerivationMethod.RESTRICTION;
			annotations = new AnnotationsBuilder(result).add(restriction::annotations).build();
		} else {
			derivationMethod = DerivationMethod.EXTENSION;
			annotations = new AnnotationsBuilder(result).add(extension::annotations).build();
		}
		return new ComplexContent(annotations, mixed, derivationMethod, restriction != null ? restriction : extension);
	}

	protected Deque<Annotation> annotations() {
		return annotations;
	}

	protected ComplexDerivation derivation() {
		return derivation;
	}

	protected Boolean mixed() {
		return mixed;
	}

	protected DerivationMethod derivationMethod() {
		return derivationMethod;
	}

}
