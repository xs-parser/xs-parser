package xs.parser.internal.util;

import java.io.*;
import java.util.*;
import javax.xml.*;
import javax.xml.namespace.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;
import xs.parser.*;
import xs.parser.Element;
import xs.parser.internal.*;

public final class NodeHelper {

	private static final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

	static {
		documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
		try {
			documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		} catch (final ParserConfigurationException e) {
			throw new ExceptionInInitializerError(e);
		}
		documentBuilderFactory.setExpandEntityReferences(false);
		documentBuilderFactory.setNamespaceAware(true);
	}

	private NodeHelper() { }

	private static boolean equalsQualifiedName(final QName q, final String name, final String targetNamespace) {
		return Objects.equals(q.getLocalPart(), name)
				&& (Objects.equals(q.getNamespaceURI(), targetNamespace)
						|| (XMLConstants.NULL_NS_URI.equals(q.getNamespaceURI()) && targetNamespace == null));
	}

	@SuppressWarnings("unchecked")
	private static <X extends Throwable> Writer toString0(final Node node, final Writer writer) throws X {
		try {
			final TransformerFactory factory = TransformerFactory.newInstance();
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			final Transformer transformer = factory.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.transform(new DOMSource(node), new StreamResult(writer));
			return writer;
		} catch (final TransformerException e) {
			throw (X) e;
		}
	}

	@SuppressWarnings("unchecked")
	private static <X extends Throwable> DocumentBuilder newDocumentBuilder0() throws X {
		try {
			return documentBuilderFactory.newDocumentBuilder();
		} catch (final ParserConfigurationException e) {
			throw (X) e;
		}
	}

	public static String toString(final Node node) {
		switch (node.getNodeType()) {
		case Node.ATTRIBUTE_NODE:
		case Node.CDATA_SECTION_NODE:
		case Node.COMMENT_NODE:
		case Node.TEXT_NODE:
		case Node.PROCESSING_INSTRUCTION_NODE:
			return node.getNodeValue();
		default:
			return toString0(node, new StringWriter()).toString();
		}
	}

	public static boolean equalsName(final QName q, final TypeDefinition t) {
		return equalsQualifiedName(q, t.name(), t.targetNamespace());
	}

	public static boolean equalsName(final QName q, final Attribute a) {
		return equalsQualifiedName(q, a.name(), a.targetNamespace());
	}

	public static boolean equalsName(final QName q, final Element e) {
		return equalsQualifiedName(q, e.name(), e.targetNamespace());
	}

	public static boolean equalsName(final QName q, final AttributeGroup a) {
		return equalsQualifiedName(q, a.name(), a.targetNamespace());
	}

	public static boolean equalsName(final QName q, final ModelGroup m) {
		return equalsQualifiedName(q, m.name(), m.targetNamespace());
	}

	/**
	 * Tests whether the parent of the given result is the schema element.
	 * @param result the result
	 * @return {@code true} if the provided result's {@link SequenceParser.Result#node()} is a direct child of {@link SequenceParser.Result#schema()}
	 */
	public static boolean isParentSchemaElement(final SequenceParser.Result result) {
		return result.node().getOwnerDocument().getDocumentElement().equals(result.parent().node());
	}

	public static Document ownerDocument(final Node thisNode) {
		return thisNode instanceof Document ? (Document) thisNode : thisNode.getOwnerDocument();
	}

	public static DocumentBuilder newDocumentBuilder() {
		return newDocumentBuilder0();
	}

	public static QName getNodeValueAsQName(final Node thisNode) {
		return qname(thisNode, thisNode.getNodeValue());
	}

	public static QName qname(final Node thisNode, final String qualifiedName) {
		if (qualifiedName == null || qualifiedName.isEmpty()) {
			throw new IllegalArgumentException(toString(thisNode));
		}
		final int separator = qualifiedName.indexOf(':');
		final String prefix = separator == -1 ? null : qualifiedName.substring(0, separator);
		final String localPart = separator == -1 ? qualifiedName : qualifiedName.substring(separator + 1);
		if (localPart.isEmpty()) {
			throw new IllegalArgumentException(toString(thisNode));
		} else if (XMLConstants.XML_NS_PREFIX.equals(prefix)) {
			return new QName(XMLConstants.XML_NS_URI, localPart, XMLConstants.XML_NS_PREFIX);
		}
		final String nsUri = thisNode.lookupNamespaceURI(prefix);
		if (prefix != null && nsUri == null) {
			throw new SchemaParseException(thisNode, "No XML namespace URI for prefix " + prefix);
		}
		return prefix == null
				? new QName(nsUri, localPart)
				: new QName(nsUri, localPart, prefix);
	}

	public static String namespaceUri(final Node node) {
		final String nsUri = node.getNamespaceURI();
		return nsUri == null ? XMLConstants.NULL_NS_URI : nsUri;
	}

	public static Document newDocument() {
		return newDocumentBuilder0().newDocument();
	}

	public static Node newNode(final Document doc, final ElementValue<?> e, final String prefix, final String namespaceUri, final String localName) {
		final org.w3c.dom.Element node = doc.createElementNS(e.getName().getNamespaceURI(), prefix + ':' + e.getName().getLocalPart());
		node.setPrefix(e.getName().getPrefix());
		node.setAttribute(XMLConstants.XMLNS_ATTRIBUTE + ':' + prefix, namespaceUri);
		if (localName != null) {
			node.setAttribute("name", localName);
		}
		return node;
	}

	public static String toStringNamespace(final String namespace) {
		return namespace == null ? "null" : '"' + namespace + '"';
	}

	public static String validateTargetNamespace(final String targetNamespace) {
		if (XMLConstants.NULL_NS_URI.equals(targetNamespace)) {
			throw new SchemaParseException("targetNamespace must either be absent or a non-empty string");
		}
		return targetNamespace;
	}

}
