package xs.parser.internal;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import javax.xml.*;
import javax.xml.namespace.*;
import org.w3c.dom.*;
import xs.parser.*;
import xs.parser.internal.TagParser.*;
import xs.parser.internal.util.*;
import xs.parser.internal.util.SequenceParser.*;

@SuppressWarnings({"unchecked", "rawtypes"})
public class AttrParser<T> {

	private static class Value<T> {

		private final AttrValue<T> defaultValue;
		private final Function<Node, T> parseMethod;

		Value(final QName name, final T defaultValue, final Function<Node, T> parseMethod) {
			final Attr defaultValueAttr;
			if (defaultValue != null) {
				defaultValueAttr = NodeHelper.newDocument().createAttributeNS(name.getNamespaceURI(), name.getLocalPart());
				defaultValueAttr.setNodeValue(defaultValue.toString());
			} else {
				defaultValueAttr = null;
			}
			this.defaultValue = new AttrValue<>(defaultValueAttr, defaultValue);
			this.parseMethod = Objects.requireNonNull(parseMethod);
		}

	}

	public static class Names {

		private Names() { }

		public static final QName ABSTRACT = new QName("abstract");
		public static final QName APPLIES_TO_EMPTY = new QName("appliesToEmpty");
		public static final QName ATTRIBUTE_FORM_DEFAULT = new QName("attributeFormDefault");
		public static final QName BASE = new QName("base");
		public static final QName BLOCK = new QName("block");
		public static final QName BLOCK_DEFAULT = new QName("blockDefault");
		public static final QName DEFAULT_ATTRIBUTES = new QName("defaultAttributes");
		public static final QName DEFAULT_ATTRIBUTES_APPLY = new QName("defaultAttributesApply");
		public static final QName DEFAULT = new QName("default");
		public static final QName ELEMENT_FORM_DEFAULT = new QName("elementFormDefault");
		public static final QName FINAL = new QName("final");
		public static final QName FINAL_DEFAULT = new QName("finalDefault");
		public static final QName FIXED = new QName("fixed");
		public static final QName FORM = new QName("form");
		public static final QName ID = new QName("id");
		public static final QName INHERITABLE = new QName("inheritable");
		public static final QName ITEM_TYPE = new QName("itemType");
		public static final QName MAX_OCCURS = new QName("maxOccurs");
		public static final QName MEMBER_TYPES = new QName("memberTypes");
		public static final QName MIN_OCCURS = new QName("minOccurs");
		public static final QName MIXED = new QName("mixed");
		public static final QName MODE = new QName("mode");
		public static final QName NAME = new QName("name");
		public static final QName NAMESPACE = new QName("namespace");
		public static final QName NILLABLE = new QName("nillable");
		public static final QName NOT_NAMESPACE = new QName("notNamespace");
		public static final QName NOT_QNAME = new QName("notQName");
		public static final QName PROCESS_CONTENTS = new QName("processContents");
		public static final QName PUBLIC = new QName("public");
		public static final QName REF = new QName("ref");
		public static final QName REFER = new QName("refer");
		public static final QName SCHEMA_LOCATION = new QName("schemaLocation");
		public static final QName SOURCE = new QName("source");
		public static final QName SUBSTITUTION_GROUP = new QName("substitutionGroup");
		public static final QName SYSTEM = new QName("system");
		public static final QName TARGET_NAMESPACE = new QName("targetNamespace");
		public static final QName TEST = new QName("test");
		public static final QName TYPE = new QName("type");
		public static final QName USE = new QName("use");
		public static final QName VALUE = new QName("value");
		public static final QName VERSION = new QName("version");
		public static final QName XML_LANG = new QName(XMLConstants.XML_NS_URI, "lang", "xml");
		public static final QName XPATH = new QName("xpath");
		public static final QName XPATH_DEFAULT_NAMESPACE = new QName("xpathDefaultNamespace");

	}

	private static final Map<Key, AttrParser<?>> attrParsers = new ConcurrentHashMap<>();
	private static final Map<Key, Value<?>> values = new ConcurrentHashMap<>();
	public static final AttrParser<Boolean> ABSTRACT = defer(Names.ABSTRACT, Boolean.class);
	public static final AttrParser<Deque<String>> ANY_NAMESPACE = defer(Names.NAMESPACE, Deque.class, String.class);
	public static final AttrParser<Boolean> APPLIES_TO_EMPTY = defer(Names.APPLIES_TO_EMPTY, Boolean.class);
	public static final AttrParser<Schema.Form> ATTRIBUTE_FORM_DEFAULT = defer(Names.ATTRIBUTE_FORM_DEFAULT, Schema.Form.class);
	public static final AttrParser<QName> BASE = defer(Names.BASE, QName.class);
	public static final AttrParser<Deque<Schema.Block>> BLOCK = defer(Names.BLOCK, Deque.class, Schema.Block.class);
	public static final AttrParser<Schema.Block> BLOCK_DEFAULT = defer(Names.BLOCK_DEFAULT, Schema.Block.class);
	public static final AttrParser<QName> DEFAULT_ATTRIBUTES = defer(Names.DEFAULT_ATTRIBUTES, QName.class);
	public static final AttrParser<Boolean> DEFAULT_ATTRIBUTES_APPLY = defer(Names.DEFAULT_ATTRIBUTES_APPLY, Boolean.class);
	public static final AttrParser<Schema.Form> ELEMENT_FORM_DEFAULT = defer(Names.ELEMENT_FORM_DEFAULT, Schema.Form.class);
	public static final AttrParser<String> DEFAULT = defer(Names.DEFAULT, String.class);
	public static final AttrParser<Deque<TypeDefinition.Final>> FINAL = defer(Names.FINAL, Deque.class, TypeDefinition.Final.class);
	public static final AttrParser<TypeDefinition.Final> FINAL_DEFAULT = defer(Names.FINAL_DEFAULT, TypeDefinition.Final.class);
	public static final AttrParser<String> FIXED = defer(Names.FIXED, String.class);
	public static final AttrParser<Schema.Form> FORM = defer(Names.FORM, Schema.Form.class);
	public static final AttrParser<String> ID = defer(Names.ID, String.class);
	public static final AttrParser<Boolean> INHERITABLE = defer(Names.INHERITABLE, Boolean.class);
	public static final AttrParser<QName> ITEM_TYPE = defer(Names.ITEM_TYPE, QName.class);
	public static final AttrParser<String> MAX_OCCURS = defer(Names.MAX_OCCURS, String.class);
	public static final AttrParser<Deque<QName>> MEMBER_TYPES = defer(Names.MEMBER_TYPES, Deque.class, QName.class);
	public static final AttrParser<String> MIN_OCCURS = defer(Names.MIN_OCCURS, String.class);
	public static final AttrParser<Boolean> MIXED = defer(Names.MIXED, Boolean.class);
	public static final AttrParser<ComplexType.OpenContent.Mode> MODE = defer(Names.MODE, ComplexType.OpenContent.Mode.class);
	public static final AttrParser<String> NAME = defer(Names.NAME, String.class);
	public static final AttrParser<String> NAMESPACE = defer(Names.NAMESPACE, String.class);
	public static final AttrParser<Boolean> NILLABLE = defer(Names.NILLABLE, Boolean.class);
	public static final AttrParser<Deque<String>> NOT_NAMESPACE = defer(Names.NOT_NAMESPACE, Deque.class, String.class);
	public static final AttrParser<Deque<String>> NOT_QNAME = defer(Names.NOT_QNAME, Deque.class, String.class);
	public static final AttrParser<Wildcard.ProcessContents> PROCESS_CONTENTS = defer(Names.PROCESS_CONTENTS, Wildcard.ProcessContents.class);
	public static final AttrParser<String> PUBLIC = defer(Names.PUBLIC, String.class);
	public static final AttrParser<QName> REF = defer(Names.REF, QName.class);
	public static final AttrParser<QName> REFER = defer(Names.REFER, QName.class);
	public static final AttrParser<String> SCHEMA_LOCATION = defer(Names.SCHEMA_LOCATION, String.class);
	public static final AttrParser<String> SOURCE = defer(Names.SOURCE, String.class);
	public static final AttrParser<Deque<QName>> SUBSTITUTION_GROUP = defer(Names.SUBSTITUTION_GROUP, Deque.class, QName.class);
	public static final AttrParser<String> SYSTEM = defer(Names.SYSTEM, String.class);
	public static final AttrParser<String> TARGET_NAMESPACE = defer(Names.TARGET_NAMESPACE, String.class);
	public static final AttrParser<String> TEST = defer(Names.TEST, String.class);
	public static final AttrParser<QName> TYPE = defer(Names.TYPE, QName.class);
	public static final AttrParser<Attribute.Use> USE = defer(Names.USE, Attribute.Use.class);
	public static final AttrParser<String> VALUE = defer(Names.VALUE, String.class);
	public static final AttrParser<String> VERSION = defer(Names.VERSION, String.class);
	public static final AttrParser<String> XML_LANG = defer(Names.XML_LANG, String.class);
	public static final AttrParser<String> XPATH = defer(Names.XPATH, String.class);
	public static final AttrParser<String> XPATH_DEFAULT_NAMESPACE = defer(Names.XPATH_DEFAULT_NAMESPACE, String.class);

	private final QName name;
	private final Deferred<Value<T>> value;

	private AttrParser(final QName name, final Deferred<Value<T>> value) {
		this.name = Objects.requireNonNull(name);
		this.value = value;
	}

	private static AttrParser<?> defer(final QName name, final Key key) {
		AttrParser<?> attrParser = attrParsers.get(key);
		if (attrParser == null) {
			final Deferred<Value<Object>> value = () -> (Value<Object>) Objects.requireNonNull(values.get(key), name.toString());
			attrParser = new AttrParser<>(name, value);
			if (attrParsers.putIfAbsent(key, attrParser) != null) {
				throw new IllegalStateException("Collision of deferred instances " + name);
			}
		}
		return attrParser;
	}

	private static <T> AttrParser<Deque<T>> defer(final QName name, final Class<Deque> dequeClass, final Class<T> cls) {
		return (AttrParser<Deque<T>>) defer(name, new Key(name, dequeClass, cls));
	}

	private static <T> AttrParser<T> defer(final QName name, final Class<T> cls) {
		return (AttrParser<T>) defer(name, new Key(name, cls));
	}

	private static <T> void register(final QName name, final Key key, final T defaultValue, final Function<Node, T> parseMethod) {
		final Value<T> value = new Value<>(name, defaultValue, parseMethod);
		if (values.putIfAbsent(key, value) != null) {
			throw new IllegalStateException(name + " is already registered");
		}
	}

	public static <T> void register(final QName name, final Class<T> cls, final Function<Node, T> parseMethod) {
		register(name, new Key(name, cls), null, parseMethod);
	}

	public static <T> void register(final QName name, final Class<Deque> dequeClass, final Class<T> cls, final Function<Node, Deque<T>> parseMethod) {
		register(name, dequeClass, cls, Deques.emptyDeque(), parseMethod);
	}

	public static <T> void register(final QName name, final Class<Deque> dequeClass, final Class<T> cls, final Deque<T> defaultValue, final Function<Node, Deque<T>> parseMethod) {
		register(name, new Key(name, Objects.requireNonNull(dequeClass), cls), defaultValue, parseMethod);
	}

	public static void register(final QName name, final Boolean defaultValue) {
		register(name, new Key(name, Boolean.class), defaultValue, NodeHelper::getNodeValueAsBoolean);
	}

	public static void register(final QName name, final Function<Node, String> parseMethod) {
		register(name, new Key(name, String.class), null, parseMethod);
	}

	public static <T> void register(final QName name, final Class<T> cls, final T defaultValue, final Function<Node, T> parseMethod) {
		register(name, new Key(name, cls), defaultValue, parseMethod);
	}

	public T parse(final Node node) {
		return value.get().parseMethod.apply(node);
	}

	public T getDefaultValue() {
		return value.get().defaultValue.getValue();
	}

	public AttrValue getDefault() {
		return value.get().defaultValue;
	}

	public QName getName() {
		return name;
	}

	@Override
	public String toString() {
		return getName().toString();
	}

}
