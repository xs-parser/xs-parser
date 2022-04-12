package xs.parser;

import java.util.*;
import javax.xml.namespace.*;
import org.w3c.dom.*;
import xs.parser.Assertion.*;
import xs.parser.internal.*;
import xs.parser.internal.SequenceParser.*;

/**
 * <pre>
 * &lt;alternative
 *   id = ID
 *   test = an XPath expression
 *   type = QName
 *   xpathDefaultNamespace = (anyURI | (##defaultNamespace | ##targetNamespace | ##local))
 *   {any attributes with non-schema namespace . . .}&gt;
 *   Content: (annotation?, (simpleType | complexType)?)
 * &lt;/alternative&gt;
 * </pre>
 *
 * <table>
 *   <caption style="font-size: large; text-align: left">Schema Component: Type Alternative, a kind of Annotated Component</caption>
 *   <thead>
 *     <tr>
 *       <th style="text-align: left">Method</th>
 *       <th style="text-align: left">Property</th>
 *       <th style="text-align: left">Representation</th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <td>{@link Alternative#annotations()}</td>
 *       <td>{annotations}</td>
 *       <td>A sequence of Annotation components.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link Alternative#test()}</td>
 *       <td>{test}</td>
 *       <td>An XPath Expression property record. Optional.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link Alternative#type()}</td>
 *       <td>{type definition}</td>
 *       <td>A Type Definition component. Required.</td>
 *     </tr>
 *   </tbody>
 * </table>
 */
public class Alternative implements AnnotatedComponent {

	protected static final SequenceParser parser = new SequenceParser()
			.optionalAttributes(AttributeValue.ID, AttributeValue.TEST, AttributeValue.TYPE, AttributeValue.XPATHDEFAULTNAMESPACE)
			.elements(0, 1, ElementValue.ANNOTATION)
			.elements(0, 1, ElementValue.COMPLEXTYPE, ElementValue.SIMPLETYPE);

	private final Node node;
	private final Deque<Annotation> annotations;
	private final XPathExpression test;
	private final Deferred<? extends TypeDefinition> type;

	Alternative(final Node node, final Deque<Annotation> annotations, final XPathExpression test, final Deferred<? extends TypeDefinition> type) {
		this.node = Objects.requireNonNull(node);
		this.annotations = Objects.requireNonNull(annotations);
		this.test = test;
		this.type = Objects.requireNonNull(type);
	}

	protected static Alternative parse(final Result result) {
		final String expression = result.value(AttributeValue.TEST);
		final String xpathDefaultNamespace = result.value(AttributeValue.XPATHDEFAULTNAMESPACE);
		final XPathExpression test = expression != null ? new XPathExpression(result, xpathDefaultNamespace, expression) : null;
		final Deferred<? extends TypeDefinition> type;
		final QName typeName = result.value(AttributeValue.TYPE);
		if (typeName != null) {
			type = result.schema().find(typeName, TypeDefinition.class);
		} else {
			final ComplexType complexType = result.parse(ElementValue.COMPLEXTYPE);
			if (complexType != null) {
				type = Deferred.value(complexType);
			} else {
				final SimpleType simpleType = result.parse(ElementValue.SIMPLETYPE);
				type = Deferred.value(Objects.requireNonNull(simpleType));
			}
		}
		return new Alternative(result.node(), result.annotations(), test, type);
	}

	/** @return If the test [attribute] is not present, then ·absent·; otherwise an XPath Expression property record, as described in section XML Representation of Assertion Schema Components (§3.13.2), with &lt;alternative&gt; as the "host element" and test as the designated expression [attribute]. */
	public XPathExpression test() {
		return test;
	}

	/** @return The type definition ·resolved· to by the ·actual value· of the type [attribute], if one is present, otherwise the type definition corresponding to the complexType or simpleType among the [children] of the &lt;alternative&gt; element. */
	public TypeDefinition type() {
		return type.get();
	}

	@Override
	public Node node() {
		return node;
	}

	/** @return The ·annotation mapping· of the &lt;alternative&gt; element, as defined in XML Representation of Annotation Schema Components (§3.15.2). */
	@Override
	public Deque<Annotation> annotations() {
		return Deques.unmodifiableDeque(annotations);
	}

}