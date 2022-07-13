package xs.parser.internal.util;

import java.io.*;
import java.math.*;
import java.net.*;
import java.util.*;
import java.util.function.*;
import java.util.regex.*;
import java.util.stream.*;
import javax.xml.*;
import javax.xml.namespace.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;
import xs.parser.*;
import xs.parser.Element;
import xs.parser.Notation;

public final class NodeHelper {

	private static final String XML_NAME_FIRST_CHAR = "A-Z_a-z\u00C0-\u00D6\u00D8-\u00F6\u00F8-\u02FF\u0370-\u037D\u037F-\u1FFF\u200C-\u200D\u2070-\u218F\u2C00-\u2FEF\u3001-\uD7FF\uF900-\uFDCF\uFDF0-\uFFFD"; // First character of an XML name, see below regular expressions
	private static final String XML_NAME_CHARS = "-.0-9A-Z_a-z\u00B7\u00C0-\u00D6\u00D8-\u00F6\u00F8-\u037D\u037F-\u1FFF\u200C-\u200D\u203F\u2040\u2070-\u218F\u2C00-\u2FEF\u3001-\uD7FF\uF900-\uFDCF\uFDF0-\uFFFD"; // Any subsequent character of an XML name, see below regular expressions
	private static final Pattern INTEGER_PATTERN = Pattern.compile("[-+]?\\d+");
	private static final Pattern DECIMAL_PATTERN = Pattern.compile("[-+]?(\\d+(\\.\\d*)?|\\.\\d+)");
	private static final Pattern DURATION_PATTERN = Pattern.compile("-?P(((\\d+Y(\\d+M)?(\\d+D)?|(\\d+M)(\\d+D)?|(\\d+D))(T((\\d+H)(\\d+M)?(\\d+(\\.\\d+)?S)?|(\\d+M)(\\d+(\\.\\d+)?S)?|(\\d+(\\.\\d+)?S)))?)|(T((\\d+H)(\\d+M)?(\\d+(\\.\\d+)?S)?|(\\d+M)(\\d+(\\.\\d+)?S)?|(\\d+(\\.\\d+)?S))))");
	private static final Pattern DATE_TIME_PATTERN = Pattern.compile(
			"-?([1-9]\\d{3,}|0\\d{3})"
			+ "-(0[1-9]|1[0-2])"
			+ "-(0[1-9]|[12]\\d|3[01])"
			+ "T(([01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d(\\.\\d+)?|(24:00:00(\\.0+)?))"
			+ "(Z|[-+]((0\\d|1[0-3]):[0-5]\\d|14:00))?");
	private static final Pattern TIME_PATTERN = Pattern.compile("(([01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d(\\.\\d+)?|(24:00:00(\\.0+)?))(Z|[-+]((0\\d|1[0-3]):[0-5]\\d|14:00))?");
	private static final Pattern DATE_PATTERN = Pattern.compile("-?([1-9]\\d{3,}|0\\d{3})-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])(Z|[-+]((0\\d|1[0-3]):[0-5]\\d|14:00))?");
	private static final Pattern HEX_BINARY_PATTERN = Pattern.compile("([0-9a-fA-F]{2})*");
	private static final Pattern BASE_64_BINARY_PATTERN = Pattern.compile("((([A-Za-z0-9+/] ?){4})*+(([A-Za-z0-9+/] ?){3}[A-Za-z0-9+/]|([A-Za-z0-9+/] ?){2}[AEIMQUYcgkosw048] ?=|[A-Za-z0-9+/] ?[AQgw] ?= ?=))?");
	private static final Pattern G_YEAR_MONTH_PATTERN = Pattern.compile("-?([1-9]\\d{3,}|0\\d{3})-(0[1-9]|1[0-2])(Z|[-+]((0\\d|1[0-3]):[0-5]\\d|14:00))?");
	private static final Pattern G_YEAR_PATTERN = Pattern.compile("-?([1-9]\\d{3,}|0\\d{3})(Z|[-+]((0\\d|1[0-3]):[0-5]\\d|14:00))?");
	private static final Pattern G_MONTH_DAY_PATTERN = Pattern.compile("--(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])(Z|[-+]((0\\d|1[0-3]):[0-5]\\d|14:00))?");
	private static final Pattern G_DAY_PATTERN = Pattern.compile("---(0[1-9]|[12]\\d|3[01])(Z|[-+]((0\\d|1[0-3]):[0-5]\\d|14:00))?");
	private static final Pattern G_MONTH_PATTERN = Pattern.compile("--(0[1-9]|1[0-2])(Z|[-+]((0\\d|1[0-3]):[0-5]\\d|14:00))?");
	private static final Pattern LANGUAGE_PATTERN = Pattern.compile("[a-zA-Z]{1,8}(-[a-zA-Z0-9]{1,8})*+");
	private static final Pattern NCNAME_PATTERN = Pattern.compile("^[" + XML_NAME_FIRST_CHAR + "][" + XML_NAME_CHARS + "]*$");
	private static final Pattern NON_NEGATIVE_INTEGER_PATTERN = Pattern.compile("^((-0+)|([+]?\\d+))$");
	private static final Pattern POSITIVE_INTEGER_PATTERN = Pattern.compile("^[+]?0*[1-9]\\d*$");
	private static final Pattern QNAME_PATTERN = Pattern.compile("^(([" + XML_NAME_FIRST_CHAR + "][" + XML_NAME_CHARS + "]*):)?([" + XML_NAME_FIRST_CHAR + "][" + XML_NAME_CHARS + "]*)$");
	private static final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
	private static BiFunction<Node, String, Schema.ParseException> newParseExceptionNodeString;
	private static BiConsumer<Schema, Set<Schema>> schemaFindAllConstituentSchemas;
	public static final String LIST_SEP = " ";

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
			return Objects.requireNonNull(node.getNodeValue());
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

	public static boolean equalsName(final QName q, final Notation n) {
		return equalsQualifiedName(q, n.name(), n.targetNamespace());
	}

	/**
	 * Tests whether the parent of the given result is the schema element.
	 * @param result the result
	 * @return {@code true} if the provided result's {@link SequenceParser.Result#node()} is a direct child of {@link SequenceParser.Result#schema()}
	 */
	public static boolean isParentSchemaElement(final SequenceParser.Result result) {
		return result.node().getOwnerDocument().getDocumentElement().equals(result.parent().node());
	}

	public static Document ownerDocument(final Node node) {
		return node instanceof Document ? (Document) node : node.getOwnerDocument();
	}

	public static DocumentBuilder newDocumentBuilder() {
		return newDocumentBuilder0();
	}

	public static void setNewParseException(final BiFunction<Node, String, Schema.ParseException> fn) {
		if (newParseExceptionNodeString != null) {
			throw new IllegalStateException("ParseException already set");
		}
		newParseExceptionNodeString = fn;
	}

	public static Schema.ParseException newParseException(final Node node, final String message) {
		return newParseExceptionNodeString.apply(node, message);
	}

	public static Schema.ParseException newFacetException(final Node node, final String value, final String typeName) {
		return newParseException(node, '\'' + value + "' is not a valid value for '" + typeName + '\'');
	}

	public static void setSchemaFindAllConstituentSchemas(final BiConsumer<Schema, Set<Schema>> fn) {
		if (schemaFindAllConstituentSchemas != null) {
			throw new IllegalStateException("schemaFindAllConstituentSchemas already set");
		}
		schemaFindAllConstituentSchemas = fn;
	}

	public static void findAllConstituentSchemas(final Schema schema, final Set<Schema> schemas) {
		schemaFindAllConstituentSchemas.accept(schema, schemas);
	}

	public static String namespaceUri(final Node node) {
		final String nsUri = node.getNamespaceURI();
		return nsUri == null ? XMLConstants.NULL_NS_URI : nsUri;
	}

	public static Document newDocument() {
		return newDocumentBuilder0().newDocument();
	}

	public static Document newSchemaDocument(final String targetNamespace) {
		final Document doc = newDocumentBuilder0().newDocument();
		doc.appendChild(doc.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, "xs:schema"));
		doc.getDocumentElement().setAttributeNS(null, "targetNamespace", targetNamespace);
		return doc;
	}

	public static Node newSchemaNode(final Document doc, final String elementLocalName, final String localName) {
		final org.w3c.dom.Element elem = doc.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, "xs:" + Objects.requireNonNull(elementLocalName));
		doc.getDocumentElement().appendChild(elem);
		elem.setAttributeNS(null, "name", Objects.requireNonNull(localName));
		return elem;
	}

	public static String toStringNamespace(final String namespace) {
		return namespace == null ? "null" : '"' + namespace + '"';
	}

	public static String collapseWhitespace(final String value) {
		final StringBuilder builder = new StringBuilder(value.length());
		int lastIndexOfWhitespace = -1;
		int lastIndexOfNonWhitespace = -1;
		int charCount;
		for (int i = 0; i < value.length(); i += charCount) {
			final int codePoint = value.codePointAt(i);
			switch (codePoint) {
			case ' ':
			case '\r':
			case '\n':
			case '\t':
				if (lastIndexOfWhitespace != i - 1) {
					builder.append(' ');
				}
				lastIndexOfWhitespace = i;
				charCount = 1;
				break;
			default:
				builder.appendCodePoint(codePoint);
				lastIndexOfNonWhitespace = builder.length();
				charCount = Character.charCount(codePoint);
				break;
			}
		}
		if (builder.length() > 0 && lastIndexOfNonWhitespace < builder.length()) {
			return builder.substring(0, lastIndexOfNonWhitespace);
		}
		return builder.toString();
	}

	public static String requireNonEmpty(final Node ownerNode, final String targetNamespace) {
		if (XMLConstants.NULL_NS_URI.equals(targetNamespace)) {
			throw newParseException(ownerNode, "targetNamespace must either be absent or a non-empty string");
		}
		return targetNamespace;
	}

	public static String getAttrValueAsAnyUri(final Attr attr) {
		return getNodeValueAsAnyUri(attr, collapseWhitespace(attr.getValue()));
	}

	public static String getNodeValueAsAnyUri(final Node node, final String value) {
		try {
			new URI(value); // TODO: parse as IRI, RFC3987
			return value;
		} catch (final URISyntaxException e) {
			throw newFacetException(node, value, SimpleType.xsAnyURI().name());
		}
	}

	public static boolean getAttrValueAsBoolean(final Attr attr) {
		return getNodeValueAsBoolean(attr, collapseWhitespace(attr.getValue()));
	}

	public static boolean getNodeValueAsBoolean(final Node node, final String value) {
		switch (value) {
		case "true":
		case "1":
			return true;
		case "false":
		case "0":
			return false;
		default:
			throw newFacetException(node, value, SimpleType.xsBoolean().name());
		}
	}

	public static String getAttrValueAsLanguage(final Attr attr) {
		final String value = collapseWhitespace(attr.getValue());
		if (LANGUAGE_PATTERN.matcher(value).matches()) {
			return value;
		}
		throw newFacetException(attr, value, SimpleType.xsLanguage().name());
	}

	public static String getAttrValueAsNCName(final Attr attr) {
		final String value = collapseWhitespace(attr.getValue());
		if (NCNAME_PATTERN.matcher(value).matches()) {
			return value;
		}
		throw newFacetException(attr, value, SimpleType.xsNCName().name());
	}

	public static QName getAttrValueAsQName(final Attr attr) {
		return getNodeValueAsQName(attr, collapseWhitespace(attr.getValue()));
	}

	public static QName getNodeValueAsQName(final Node node, final String value) {
		final Matcher m = QNAME_PATTERN.matcher(value);
		if (m.find()) {
			final String prefix = m.group(2);
			final String localName = m.group(3);
			String uri = node.lookupNamespaceURI(prefix);
			if (XMLConstants.XML_NS_PREFIX.equals(prefix)) {
				// "xml" must always be bound to its usual namespace, or is otherwise implicitly declared
				if (uri != null && !XMLConstants.XML_NS_URI.equals(uri)) {
					throw newParseException(node, "'xml' prefix must be bound to its usual namespace, '" + XMLConstants.XML_NS_URI + "', but was bound to '" + uri + '\'');
				}
				uri = XMLConstants.XML_NS_URI;
			} else if (uri == null && prefix != null) {
				throw newParseException(node, '\'' + value + "' does not name a valid namespace URI for prefix '" + prefix + '\'');
			}
			return new QName(uri == null ? XMLConstants.NULL_NS_URI : uri,
					localName,
					prefix == null ? XMLConstants.DEFAULT_NS_PREFIX : prefix);
		} else {
			throw newFacetException(node, value, SimpleType.xsQName().name());
		}
	}

	public static Deque<QName> getAttrValueAsQNames(final Attr attr) {
		final String[] values = collapseWhitespace(attr.getValue()).split(LIST_SEP);
		final Deque<QName> names = Stream.of(values)
				.map(name -> NodeHelper.getNodeValueAsQName(attr, name))
				.collect(Collectors.toCollection(ArrayDeque::new));
		return Deques.unmodifiableDeque(names);
	}

	public static BigInteger getAttrValueAsPositiveInteger(final Attr attr) {
		final String value = collapseWhitespace(attr.getValue());
		if (POSITIVE_INTEGER_PATTERN.matcher(value).matches()) {
			return new BigInteger(value);
		}
		throw newFacetException(attr, value, SimpleType.xsPositiveInteger().name());
	}

	public static BigInteger getAttrValueAsNonNegativeInteger(final Attr attr) {
		return getNodeValueAsNonNegativeInteger(attr, collapseWhitespace(attr.getValue()));
	}

	public static BigInteger getNodeValueAsNonNegativeInteger(final Node node, final String value) {
		if (NON_NEGATIVE_INTEGER_PATTERN.matcher(value).matches()) {
			return new BigInteger(value);
		}
		throw newFacetException(node, value, SimpleType.xsNonNegativeInteger().name());
	}

	public static String getAttrValueAsString(final Attr attr) {
		return attr.getValue();
	}

	public static String getAttrValueAsToken(final Attr attr) {
		return collapseWhitespace(attr.getValue());
	}

	public static BigInteger getNodeValueAsInteger(final Node node, final String value) {
		if (INTEGER_PATTERN.matcher(value).matches()) {
			return new BigInteger(value);
		}
		throw newFacetException(node, value, SimpleType.xsInteger().name());
	}

	public static BigDecimal getNodeValueAsDecimal(final Node node, final String value) {
		if (DECIMAL_PATTERN.matcher(value).matches()) {
			return new BigDecimal(value);
		}
		throw newFacetException(node, value, SimpleType.xsDecimal().name());
	}

	public static String getNodeValueAsDuration(final Node node, final String value) {
		if (DURATION_PATTERN.matcher(value).matches()) {
			return value;
		}
		throw newFacetException(node, value, SimpleType.xsDuration().name());
	}

	public static String getNodeValueAsDateTime(final Node node, final String value) {
		if (DATE_TIME_PATTERN.matcher(value).matches()) {
			return value;
		}
		throw newFacetException(node, value, SimpleType.xsDateTime().name());
	}

	public static String getNodeValueAsTime(final Node node, final String value) {
		if (TIME_PATTERN.matcher(value).matches()) {
			return value;
		}
		throw newFacetException(node, value, SimpleType.xsTime().name());
	}

	public static String getNodeValueAsDate(final Node node, final String value) {
		if (DATE_PATTERN.matcher(value).matches()) {
			return value;
		}
		throw newFacetException(node, value, SimpleType.xsDate().name());
	}

	public static String getNodeValueAsHexBinary(final Node node, final String value) {
		if (HEX_BINARY_PATTERN.matcher(value).matches()) {
			return value;
		}
		throw newFacetException(node, value, SimpleType.xsHexBinary().name());
	}

	public static String getNodeValueAsBase64Binary(final Node node, final String value) {
		if (BASE_64_BINARY_PATTERN.matcher(value).matches()) {
			return value;
		}
		throw newFacetException(node, value, SimpleType.xsBase64Binary().name());
	}

	public static String getNodeValueAsGYearMonth(final Node node, final String value) {
		if (G_YEAR_MONTH_PATTERN.matcher(value).matches()) {
			return value;
		}
		throw newFacetException(node, value, SimpleType.xsGYearMonth().name());
	}

	public static String getNodeValueAsGYear(final Node node, final String value) {
		if (G_YEAR_PATTERN.matcher(value).matches()) {
			return value;
		}
		throw newFacetException(node, value, SimpleType.xsGYear().name());
	}

	public static String getNodeValueAsGMonthDay(final Node node, final String value) {
		if (G_MONTH_DAY_PATTERN.matcher(value).matches()) {
			return value;
		}
		throw newFacetException(node, value, SimpleType.xsGMonthDay().name());
	}

	public static String getNodeValueAsGDay(final Node node, final String value) {
		if (G_DAY_PATTERN.matcher(value).matches()) {
			return value;
		}
		throw newFacetException(node, value, SimpleType.xsGDay().name());
	}

	public static String getNodeValueAsGMonth(final Node node, final String value) {
		if (G_MONTH_PATTERN.matcher(value).matches()) {
			return value;
		}
		throw newFacetException(node, value, SimpleType.xsGMonth().name());
	}

}
