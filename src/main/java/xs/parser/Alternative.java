package xs.parser;

import java.util.*;
import javax.xml.namespace.*;
import org.w3c.dom.*;
import xs.parser.Assertion.*;
import xs.parser.internal.*;
import xs.parser.internal.util.*;
import xs.parser.internal.util.SequenceParser.*;

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
 *       <td>{@link Alternative#typeDefinition()}</td>
 *       <td>{type definition}</td>
 *       <td>A Type Definition component. Required.</td>
 *     </tr>
 *   </tbody>
 * </table>
 */
public class Alternative implements AnnotatedComponent {

	private static final SequenceParser parser = new SequenceParser()
			.optionalAttributes(AttrParser.ID, AttrParser.TEST, AttrParser.TYPE, AttrParser.XPATH_DEFAULT_NAMESPACE)
			.elements(0, 1, TagParser.ANNOTATION)
			.elements(0, 1, TagParser.COMPLEX_TYPE, TagParser.SIMPLE_TYPE);

	private final Node node;
	private final Deque<Annotation> annotations;
	private final XPathExpression test;
	private final Deferred<? extends TypeDefinition> typeDefinition;

	Alternative(final Node node, final Deque<Annotation> annotations, final XPathExpression test, final Deferred<? extends TypeDefinition> typeDefinition) {
		this.node = Objects.requireNonNull(node);
		this.annotations = Objects.requireNonNull(annotations);
		this.test = test;
		this.typeDefinition = Objects.requireNonNull(typeDefinition);
	}

	private static Alternative parse(final Result result) {
		final String expression = result.value(AttrParser.TEST);
		final String xpathDefaultNamespace = result.value(AttrParser.XPATH_DEFAULT_NAMESPACE);
		final XPathExpression test = expression != null ? new XPathExpression(result, xpathDefaultNamespace, expression) : null;
		final Deferred<? extends TypeDefinition> typeDefinition;
		final QName typeName = result.value(AttrParser.TYPE);
		if (typeName != null) {
			typeDefinition = result.schema().find(typeName, TypeDefinition.class);
		} else {
			final TypeDefinition type = result.parse(TagParser.COMPLEX_TYPE, TagParser.SIMPLE_TYPE);
			if (type == null) {
				throw new Schema.ParseException(result.node(), "Type definition not found");
			}
			typeDefinition = () -> type;
		}
		return new Alternative(result.node(), result.annotations(), test, typeDefinition);
	}

	static void register() {
		TagParser.register(TagParser.Names.ALTERNATIVE, parser, Alternative.class, Alternative::parse);
	}

	/** @return If the test [attribute] is not present, then ·absent·; otherwise an XPath Expression property record, as described in section XML Representation of Assertion Schema Components (§3.13.2), with &lt;alternative&gt; as the "host element" and test as the designated expression [attribute]. */
	public XPathExpression test() {
		return test;
	}

	/** @return The type definition ·resolved· to by the ·actual value· of the type [attribute], if one is present, otherwise the type definition corresponding to the complexType or simpleType among the [children] of the &lt;alternative&gt; element. */
	public TypeDefinition typeDefinition() {
		return typeDefinition.get();
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
