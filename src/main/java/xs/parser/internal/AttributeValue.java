package xs.parser.internal;

import java.util.*;
import java.util.function.*;
import java.util.regex.*;
import javax.xml.*;
import javax.xml.namespace.*;
import org.w3c.dom.*;
import xs.parser.*;
import xs.parser.Attribute.*;
import xs.parser.ComplexType.OpenContent.*;
import xs.parser.Schema.*;
import xs.parser.TypeDefinition.*;
import xs.parser.Wildcard.*;
import xs.parser.internal.util.*;

public final class AttributeValue<T> {

	private static final String LIST_SEP = " ";
	private static final Document doc = NodeHelper.newDocument();
	private static final Function<Node, Boolean> booleanParser = n -> "true".equals(n.getNodeValue()) || "1".equals(n.getNodeValue());
	private static final Function<Node, String> idParser = n -> {
		return n.getNodeValue(); // TODO: impl
	};
	private static final Predicate<String> anyUriTest = s -> {
		return true; // TODO: impl
	};
	private static final Function<Node, String> anyUriParser = n -> {
		return n.getNodeValue(); // TODO: impl
	};
	private static final Function<Node, String> targetNamespaceParser = n -> {
		assertTrue(!n.getNodeValue().isEmpty(),
				"targetNamespace must not be empty");
		return anyUriParser.apply(n);
	};
	private static final Function<Node, String> ncNameParser = n -> {
		final String ncName = n.getNodeValue();
		assertTrue(!ncName.contains(":"),
				n.getLocalName() + " must not contain ':' character(s)");
		return ncName; // TODO: impl via pattern
	};
	private static final Function<Node, Deque<String>> stringsParser = n -> {
		final String[] values = n.getNodeValue().split(LIST_SEP);
		final Deque<String> s = new ArrayDeque<>(values.length);
		for (final String value : values) {
			s.add(value);
		}
		return Deques.unmodifiableDeque(s);
	};
	private static final Function<Node, Deque<QName>> qnamesParser = n -> {
		final String[] names = n.getNodeValue().split(LIST_SEP);
		final Deque<QName> qnames = new ArrayDeque<>(names.length);
		for (final String nm : names) {
			qnames.add(NodeHelper.qname(n, nm));
		}
		return Deques.unmodifiableDeque(qnames);
	};
	private static final Function<Node, String> xpathDefaultNamespaceParser = n -> {
		switch (n.getNodeValue()) {
		case "##defaultNamespace":
		case "##targetNamespace":
		case "##local":
			return n.getNodeValue();
		default:
			return anyUriParser.apply(n);
		}
	};
	private static final Function<Node, Deque<String>> anyNamespaceParser = n -> {
		switch (n.getNodeValue()) {
		case "##any":
		case "##other":
			return Deques.singletonDeque(n.getNodeValue());
		default:
			final String[] values = n.getNodeValue().split(LIST_SEP);
			final Deque<String> ls = new ArrayDeque<>();
			for (final String v : values) {
				switch (v) {
				case "##targetNamespace":
				case "##local":
					ls.add(v);
					break;
				default:
					anyUriTest.test(v);
					ls.add(v);
					break;
				}
			}
			return Deques.unmodifiableDeque(ls);
		}
	};
	private static final Function<Node, Deque<Block>> blockParser = n -> {
		if ("#all".equals(n.getNodeValue())) {
			return Deques.singletonDeque(Block.ALL);
		}
		final String[] values = n.getNodeValue().split(LIST_SEP);
		final Deque<Block> ls = new ArrayDeque<>();
		for (final String v : values) {
			final Block b = Block.getByName(v);
			if (Block.ALL.equals(b)) {
				throw new SchemaParseException(n, Block.ALL + " cannot be present in @block");
			}
			ls.add(b);
		}
		return Deques.unmodifiableDeque(ls);
	};
	private static final Function<Node, Deque<Final>> finalParser = n -> {
		switch (n.getNodeValue()) {
		case "#all":
			return Deques.singletonDeque(Final.ALL);
		default:
			final String[] values = n.getNodeValue().split(LIST_SEP);
			final Deque<Final> ls = new ArrayDeque<>();
			for (final String v : values) {
				final Final b = Final.getByName(v);
				if (Final.ALL.equals(b)) {
					throw new SchemaParseException(n, Final.ALL + " cannot be present in List of @final");
				}
				ls.add(b);
			}
			return Deques.unmodifiableDeque(ls);
		}
	};
	private static final Pattern NON_NEGATIVE_INTEGER = Pattern.compile("^(\\+?\\d+|-?0+)$");

	public static final AttributeValue<String> ID = new AttributeValue<>(new QName("id"), null, idParser);
	public static final AttributeValue<String> TARGETNAMESPACE = new AttributeValue<>(new QName("targetNamespace"), null, targetNamespaceParser);
	public static final AttributeValue<String> VERSION = new AttributeValue<>(new QName("version"), null, Node::getNodeValue);
	public static final AttributeValue<String> NAMESPACE = new AttributeValue<>(new QName("namespace"), null, anyUriParser);
	public static final AttributeValue<Deque<String>> ANY_NAMESPACE = new AttributeValue<>(new QName("namespace"), Deques.emptyDeque(), anyNamespaceParser);
	public static final AttributeValue<Form> ATTRIBUTEFORMDEFAULT = new AttributeValue<>(new QName("attributeFormDefault"), Form.UNQUALIFIED, Form::getByName);
	public static final AttributeValue<Block> BLOCKDEFAULT = new AttributeValue<>(new QName("blockDefault"), Block.DEFAULT, Block::getByName);
	public static final AttributeValue<QName> DEFAULTATTRIBUTES = new AttributeValue<>(new QName("defaultAttributes"), null, NodeHelper::getNodeValueAsQName);
	public static final AttributeValue<Form> ELEMENTFORMDEFAULT = new AttributeValue<>(new QName("elementFormDefault"), Form.UNQUALIFIED, Form::getByName);
	public static final AttributeValue<Final> FINALDEFAULT = new AttributeValue<>(new QName("finalDefault"), Final.DEFAULT, Final::getByName);
	public static final AttributeValue<Boolean> APPLIESTOEMPTY = new AttributeValue<>(new QName("appliesToEmpty"), false, booleanParser);
	public static final AttributeValue<Deque<String>> NOTNAMESPACE = new AttributeValue<>(new QName("notNamespace"), Deques.emptyDeque(), stringsParser); // TODO: Predicate
	public static final AttributeValue<Deque<String>> NOTQNAME = new AttributeValue<>(new QName("notQName"), Deques.emptyDeque(), stringsParser); // TODO: Predicate
	public static final AttributeValue<String> SCHEMALOCATION = new AttributeValue<>(new QName("schemaLocation"), null, anyUriParser);
	public static final AttributeValue<String> NAME = new AttributeValue<>(new QName("name"), null, ncNameParser);
	public static final AttributeValue<QName> TYPE = new AttributeValue<>(new QName("type"), null, NodeHelper::getNodeValueAsQName);
	public static final AttributeValue<String> DEFAULT = new AttributeValue<>(new QName("default"), null, Node::getNodeValue);
	public static final AttributeValue<QName> BASE = new AttributeValue<>(new QName("base"), null, NodeHelper::getNodeValueAsQName);
	public static final AttributeValue<Boolean> ABSTRACT = new AttributeValue<>(new QName("abstract"), false, booleanParser);
	public static final AttributeValue<Deque<Block>> BLOCK = new AttributeValue<>(new QName("block"), Deques.emptyDeque(), blockParser);
	public static final AttributeValue<Deque<Final>> FINAL = new AttributeValue<>(new QName("final"), Deques.emptyDeque(), finalParser);
	public static final AttributeValue<Boolean> NILLABLE = new AttributeValue<>(new QName("nillable"), false, booleanParser);
	public static final AttributeValue<Deque<QName>> SUBSTITUTIONGROUP = new AttributeValue<>(new QName("substitutionGroup"), null, qnamesParser);
	public static final AttributeValue<Boolean> MIXED = new AttributeValue<>(new QName("mixed"), null, booleanParser);
	public static final AttributeValue<Mode> MODE = new AttributeValue<>(new QName("mode"), Mode.INTERLEAVE, Mode::getByName);
	public static final AttributeValue<Boolean> DEFAULTATTRIBUTESAPPLY = new AttributeValue<>(new QName("defaultAttributesApply"), true, booleanParser);
	public static final AttributeValue<QName> ITEMTYPE = new AttributeValue<>(new QName("itemType"), null, NodeHelper::getNodeValueAsQName);
	public static final AttributeValue<Deque<QName>> MEMBERTYPES = new AttributeValue<>(new QName("memberTypes"), null, qnamesParser);
	public static final AttributeValue<String> VALUE = new AttributeValue<>(new QName("value"), null, Node::getNodeValue);
	public static final AttributeValue<QName> REF = new AttributeValue<>(new QName("ref"), null, NodeHelper::getNodeValueAsQName);
	public static final AttributeValue<QName> REFER = new AttributeValue<>(new QName("refer"), null, NodeHelper::getNodeValueAsQName);
	public static final AttributeValue<Use> USE = new AttributeValue<>(new QName("use"), Use.OPTIONAL, Use::getByName);
	public static final AttributeValue<String> FIXED = new AttributeValue<>(new QName("fixed"), null, Node::getNodeValue);
	public static final AttributeValue<Boolean> INHERITABLE = new AttributeValue<>(new QName("inheritable"), null, booleanParser);
	public static final AttributeValue<Form> FORM = new AttributeValue<>(new QName("form"), null, Form::getByName);
	public static final AttributeValue<String> MAXOCCURS = new AttributeValue<>(new QName("maxOccurs"), "1", n -> {
		assertTrue("unbounded".equals(n.getNodeValue()) || NON_NEGATIVE_INTEGER.matcher(n.getNodeValue()).matches(),
				"maxOccurs must be either 'unbounded' or a non-negative integer");
		return n.getNodeValue();
	});
	public static final AttributeValue<String> MINOCCURS = new AttributeValue<>(new QName("minOccurs"), "1", n -> {
		assertTrue(NON_NEGATIVE_INTEGER.matcher(n.getNodeValue()).matches(),
				"minOccurs must be a non-negative integer");
		return n.getNodeValue();
	});
	public static final AttributeValue<ProcessContents> PROCESSCONTENTS = new AttributeValue<>(new QName("processContents"), ProcessContents.STRICT, ProcessContents::getByName);
	public static final AttributeValue<String> PUBLIC = new AttributeValue<>(new QName("public"), null, Node::getNodeValue);
	public static final AttributeValue<String> SYSTEM = new AttributeValue<>(new QName("system"), null, anyUriParser);
	public static final AttributeValue<String> TEST = new AttributeValue<>(new QName("test"), null, Node::getNodeValue);
	public static final AttributeValue<String> XPATH = new AttributeValue<>(new QName("xpath"), null, Node::getNodeValue);
	public static final AttributeValue<String> XPATHDEFAULTNAMESPACE = new AttributeValue<>(new QName("xpathDefaultNamespace"), "##local", xpathDefaultNamespaceParser);
	public static final AttributeValue<String> SOURCE = new AttributeValue<>(new QName("source"), null, anyUriParser);
	public static final AttributeValue<String> XML_LANG = new AttributeValue<>(new QName(XMLConstants.XML_NS_URI, "lang", "xml"), null, Node::getNodeValue);

	// TODO: cleanup comments regard ncname
	// [\i-[:]][\c-[:]]*
	// static final Pattern NCNAME_PATTERN = Pattern.compile("");

	private final QName name;
	private final T defaultValue;
	private final Attr defaultValueAttr;
	private final Function<Node, T> parser;

	private AttributeValue(final QName name, final T defaultValue, final Function<Node, T> parser) {
		this.name = name;
		this.defaultValue = defaultValue;
		if (defaultValue != null) {
			this.defaultValueAttr = doc.createAttributeNS(name.getNamespaceURI(), name.getLocalPart());
			this.defaultValueAttr.setNodeValue(defaultValue.toString());
		} else {
			this.defaultValueAttr = null;
		}
		this.parser = parser;
	}

	private static void assertTrue(final boolean b, final String message) {
		if (!b) {
			throw new AssertionError(message);
		}
	}

	public QName getName() {
		return name;
	}

	public Attr defaultValueAsAttr() {
		return defaultValueAttr;
	}

	public T defaultValue() {
		return defaultValue;
	}

	public T parse(final Node node) {
		return parser.apply(node);
	}

	@Override
	public String toString() {
		return getName().toString();
	}

}