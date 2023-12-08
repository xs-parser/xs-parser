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

/**
 * XML Schema attribute parser.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class AttrParser<T> {

	private static class Value<T> {

		private final AttrValue<T> defaultValue;
		private final Function<Attr, T> parseMethod;

		Value(final QName name, final T defaultValue, final Function<Attr, T> parseMethod) {
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

	/** The names of schema attributes */
	public static class Names {

		private Names() { }

		/** The abstract attribute name */
		public static final QName ABSTRACT = new QName("abstract");
		/** The appliesToEmpty attribute name */
		public static final QName APPLIES_TO_EMPTY = new QName("appliesToEmpty");
		/** The attributeFormDefault attribute name */
		public static final QName ATTRIBUTE_FORM_DEFAULT = new QName("attributeFormDefault");
		/** The base attribute name */
		public static final QName BASE = new QName("base");
		/** The block attribute name */
		public static final QName BLOCK = new QName("block");
		/** The blockDefault attribute name */
		public static final QName BLOCK_DEFAULT = new QName("blockDefault");
		/** The defaultAttributes attribute name */
		public static final QName DEFAULT_ATTRIBUTES = new QName("defaultAttributes");
		/** The defaultAttributesApply attribute name */
		public static final QName DEFAULT_ATTRIBUTES_APPLY = new QName("defaultAttributesApply");
		/** The default attribute name */
		public static final QName DEFAULT = new QName("default");
		/** The elementFormDefault attribute name */
		public static final QName ELEMENT_FORM_DEFAULT = new QName("elementFormDefault");
		/** The final attribute name */
		public static final QName FINAL = new QName("final");
		/** The finalDefault attribute name */
		public static final QName FINAL_DEFAULT = new QName("finalDefault");
		/** The fixed attribute name */
		public static final QName FIXED = new QName("fixed");
		/** The form attribute name */
		public static final QName FORM = new QName("form");
		/** The id attribute name */
		public static final QName ID = new QName("id");
		/** The inheritable attribute name */
		public static final QName INHERITABLE = new QName("inheritable");
		/** The itemType attribute name */
		public static final QName ITEM_TYPE = new QName("itemType");
		/** The maxOccurs attribute name */
		public static final QName MAX_OCCURS = new QName("maxOccurs");
		/** The memberTypes attribute name */
		public static final QName MEMBER_TYPES = new QName("memberTypes");
		/** The minOccurs attribute name */
		public static final QName MIN_OCCURS = new QName("minOccurs");
		/** The mixed attribute name */
		public static final QName MIXED = new QName("mixed");
		/** The mode attribute name */
		public static final QName MODE = new QName("mode");
		/** The name attribute name */
		public static final QName NAME = new QName("name");
		/** The namespace attribute name */
		public static final QName NAMESPACE = new QName("namespace");
		/** The nillable attribute name */
		public static final QName NILLABLE = new QName("nillable");
		/** The notNamespace attribute name */
		public static final QName NOT_NAMESPACE = new QName("notNamespace");
		/** The notQName attribute name */
		public static final QName NOT_QNAME = new QName("notQName");
		/** The processContents attribute name */
		public static final QName PROCESS_CONTENTS = new QName("processContents");
		/** The public attribute name */
		public static final QName PUBLIC = new QName("public");
		/** The ref attribute name */
		public static final QName REF = new QName("ref");
		/** The refer attribute name */
		public static final QName REFER = new QName("refer");
		/** The schemaLocation attribute name */
		public static final QName SCHEMA_LOCATION = new QName("schemaLocation");
		/** The source attribute name */
		public static final QName SOURCE = new QName("source");
		/** The substitutionGroup attribute name */
		public static final QName SUBSTITUTION_GROUP = new QName("substitutionGroup");
		/** The system attribute name */
		public static final QName SYSTEM = new QName("system");
		/** The targetNamespace attribute name */
		public static final QName TARGET_NAMESPACE = new QName("targetNamespace");
		/** The test attribute name */
		public static final QName TEST = new QName("test");
		/** The type attribute name */
		public static final QName TYPE = new QName("type");
		/** The use attribute name */
		public static final QName USE = new QName("use");
		/** The value attribute name */
		public static final QName VALUE = new QName("value");
		/** The version attribute name */
		public static final QName VERSION = new QName("version");
		/** The xml:lang attribute name */
		public static final QName XML_LANG = new QName(XMLConstants.XML_NS_URI, "lang", "xml");
		/** The xpath attribute name */
		public static final QName XPATH = new QName("xpath");
		/** The xpathDefaultNamespace attribute name */
		public static final QName XPATH_DEFAULT_NAMESPACE = new QName("xpathDefaultNamespace");

	}

	private static final Map<Key, AttrParser<?>> attrParsers = new ConcurrentHashMap<>();
	private static final Map<Key, Value<?>> values = new ConcurrentHashMap<>();
	/** The parser of the abstract attribute */
	public static final AttrParser<Boolean> ABSTRACT = defer(Names.ABSTRACT, Boolean.class);
	/** The parser of the &lt;any&gt; namespace attribute */
	public static final AttrParser<Deque<String>> ANY_NAMESPACE = defer(Names.NAMESPACE, Deque.class, String.class);
	/** The parser of the appliesToEmpty attribute */
	public static final AttrParser<Boolean> APPLIES_TO_EMPTY = defer(Names.APPLIES_TO_EMPTY, Boolean.class);
	/** The parser of the attributeFormDefault attribute */
	public static final AttrParser<Schema.Form> ATTRIBUTE_FORM_DEFAULT = defer(Names.ATTRIBUTE_FORM_DEFAULT, Schema.Form.class);
	/** The parser of the base attribute */
	public static final AttrParser<QName> BASE = defer(Names.BASE, QName.class);
	/** The parser of the block attribute */
	public static final AttrParser<Deque<Schema.Block>> BLOCK = defer(Names.BLOCK, Deque.class, Schema.Block.class);
	/** The parser of the blockDefault attribute */
	public static final AttrParser<Schema.Block> BLOCK_DEFAULT = defer(Names.BLOCK_DEFAULT, Schema.Block.class);
	/** The parser of the defaultAttributes attribute */
	public static final AttrParser<QName> DEFAULT_ATTRIBUTES = defer(Names.DEFAULT_ATTRIBUTES, QName.class);
	/** The parser of the defaultAttributesApply attribute */
	public static final AttrParser<Boolean> DEFAULT_ATTRIBUTES_APPLY = defer(Names.DEFAULT_ATTRIBUTES_APPLY, Boolean.class);
	/** The parser of the elementFormDefault attribute */
	public static final AttrParser<Schema.Form> ELEMENT_FORM_DEFAULT = defer(Names.ELEMENT_FORM_DEFAULT, Schema.Form.class);
	/** The parser of the default attribute */
	public static final AttrParser<String> DEFAULT = defer(Names.DEFAULT, String.class);
	/** The parser of the final attribute */
	public static final AttrParser<Deque<TypeDefinition.Final>> FINAL = defer(Names.FINAL, Deque.class, TypeDefinition.Final.class);
	/** The parser of the finalDefault attribute */
	public static final AttrParser<TypeDefinition.Final> FINAL_DEFAULT = defer(Names.FINAL_DEFAULT, TypeDefinition.Final.class);
	/** The parser of the fixed attribute */
	public static final AttrParser<String> FIXED = defer(Names.FIXED, String.class);
	/** The parser of the form attribute */
	public static final AttrParser<Schema.Form> FORM = defer(Names.FORM, Schema.Form.class);
	/** The parser of the id attribute */
	public static final AttrParser<String> ID = defer(Names.ID, String.class);
	/** The parser of the inheritable attribute */
	public static final AttrParser<Boolean> INHERITABLE = defer(Names.INHERITABLE, Boolean.class);
	/** The parser of the itemType attribute */
	public static final AttrParser<QName> ITEM_TYPE = defer(Names.ITEM_TYPE, QName.class);
	/** The parser of the maxOccurs attribute */
	public static final AttrParser<Number> MAX_OCCURS = defer(Names.MAX_OCCURS, Number.class);
	/** The parser of the memberTypes attribute */
	public static final AttrParser<Deque<QName>> MEMBER_TYPES = defer(Names.MEMBER_TYPES, Deque.class, QName.class);
	/** The parser of the minOccurs attribute */
	public static final AttrParser<Number> MIN_OCCURS = defer(Names.MIN_OCCURS, Number.class);
	/** The parser of the mixed attribute */
	public static final AttrParser<Boolean> MIXED = defer(Names.MIXED, Boolean.class);
	/** The parser of the mode attribute */
	public static final AttrParser<ComplexType.OpenContent.Mode> MODE = defer(Names.MODE, ComplexType.OpenContent.Mode.class);
	/** The parser of the name attribute */
	public static final AttrParser<String> NAME = defer(Names.NAME, String.class);
	/** The parser of the namespace attribute */
	public static final AttrParser<String> NAMESPACE = defer(Names.NAMESPACE, String.class);
	/** The parser of the nillable attribute */
	public static final AttrParser<Boolean> NILLABLE = defer(Names.NILLABLE, Boolean.class);
	/** The parser of the notNamespace attribute */
	public static final AttrParser<Deque<String>> NOT_NAMESPACE = defer(Names.NOT_NAMESPACE, Deque.class, String.class);
	/** The parser of the notQName attribute */
	public static final AttrParser<Deque<String>> NOT_QNAME = defer(Names.NOT_QNAME, Deque.class, String.class);
	/** The parser of the processContents attribute */
	public static final AttrParser<Wildcard.ProcessContents> PROCESS_CONTENTS = defer(Names.PROCESS_CONTENTS, Wildcard.ProcessContents.class);
	/** The parser of the public attribute */
	public static final AttrParser<String> PUBLIC = defer(Names.PUBLIC, String.class);
	/** The parser of the ref attribute */
	public static final AttrParser<QName> REF = defer(Names.REF, QName.class);
	/** The parser of the refer attribute */
	public static final AttrParser<QName> REFER = defer(Names.REFER, QName.class);
	/** The parser of the schemaLocation attribute */
	public static final AttrParser<String> SCHEMA_LOCATION = defer(Names.SCHEMA_LOCATION, String.class);
	/** The parser of the source attribute */
	public static final AttrParser<String> SOURCE = defer(Names.SOURCE, String.class);
	/** The parser of the substitutionGroup attribute */
	public static final AttrParser<Deque<QName>> SUBSTITUTION_GROUP = defer(Names.SUBSTITUTION_GROUP, Deque.class, QName.class);
	/** The parser of the system attribute */
	public static final AttrParser<String> SYSTEM = defer(Names.SYSTEM, String.class);
	/** The parser of the targetNamespace attribute */
	public static final AttrParser<String> TARGET_NAMESPACE = defer(Names.TARGET_NAMESPACE, String.class);
	/** The parser of the test attribute */
	public static final AttrParser<String> TEST = defer(Names.TEST, String.class);
	/** The parser of the type attribute */
	public static final AttrParser<QName> TYPE = defer(Names.TYPE, QName.class);
	/** The parser of the use attribute */
	public static final AttrParser<Attribute.Use> USE = defer(Names.USE, Attribute.Use.class);
	/** The parser of the value attribute */
	public static final AttrParser<String> VALUE = defer(Names.VALUE, String.class);
	/** The parser of the version attribute */
	public static final AttrParser<String> VERSION = defer(Names.VERSION, String.class);
	/** The parser of the xml:lang attribute */
	public static final AttrParser<String> XML_LANG = defer(Names.XML_LANG, String.class);
	/** The parser of the xpath attribute */
	public static final AttrParser<String> XPATH = defer(Names.XPATH, String.class);
	/** The parser of the xpathDefaultNamespace attribute */
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

	private static <T> void register(final QName name, final Key key, final T defaultValue, final Function<Attr, T> parseMethod) {
		final Value<T> value = new Value<>(name, defaultValue, parseMethod);
		if (values.putIfAbsent(key, value) != null) {
			throw new IllegalStateException(name + " is already registered");
		}
	}

	public static <T> void register(final QName name, final Class<T> cls, final Function<Attr, T> parseMethod) {
		register(name, new Key(name, cls), null, parseMethod);
	}

	public static <T> void register(final QName name, final Class<Deque> dequeClass, final Class<T> cls, final Function<Attr, Deque<T>> parseMethod) {
		register(name, dequeClass, cls, Deques.emptyDeque(), parseMethod);
	}

	public static <T> void register(final QName name, final Class<Deque> dequeClass, final Class<T> cls, final Deque<T> defaultValue, final Function<Attr, Deque<T>> parseMethod) {
		register(name, new Key(name, Objects.requireNonNull(dequeClass), cls), defaultValue, parseMethod);
	}

	public static void register(final QName name, final Boolean defaultValue) {
		register(name, new Key(name, Boolean.class), defaultValue, NodeHelper::getAttrValueAsBoolean);
	}

	public static void register(final QName name, final Function<Attr, String> parseMethod) {
		register(name, new Key(name, String.class), null, parseMethod);
	}

	public static <T> void register(final QName name, final Class<T> cls, final T defaultValue, final Function<Attr, T> parseMethod) {
		register(name, new Key(name, cls), defaultValue, parseMethod);
	}

	public T parse(final Attr attr) {
		return value.get().parseMethod.apply(attr);
	}

	public T getDefaultValue() {
		return value.get().defaultValue.getValue();
	}

	/** @return The default attribute value */
	public AttrValue getDefault() {
		return value.get().defaultValue;
	}

	/** @return The qualified name of the attribute */
	public QName getName() {
		return name;
	}

	@Override
	public String toString() {
		return name.toString();
	}

}
