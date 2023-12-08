package xs.parser.internal;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import javax.xml.*;
import javax.xml.namespace.*;
import org.w3c.dom.*;
import xs.parser.*;
import xs.parser.Element;
import xs.parser.Notation;
import xs.parser.internal.util.*;
import xs.parser.internal.util.SequenceParser.*;

public class TagParser<T> {

	private static class Value<U> {

		private final QName name;
		private final SequenceParser sequenceParser;
		private final Function<Result, U> parseMethod;

		Value(final QName name, final SequenceParser sequenceParser, final Function<Result, U> parseMethod) {
			this.name = Objects.requireNonNull(name);
			this.sequenceParser = Objects.requireNonNull(sequenceParser);
			this.parseMethod = Objects.requireNonNull(parseMethod);
		}

	}

	static class Key {

		private final QName name;
		private final Class<?> collectionClass;
		private final Class<?> cls;

		Key(final QName name, final Class<?> collectionClass, final Class<?> cls) {
			this.name = Objects.requireNonNull(name);
			this.collectionClass = collectionClass;
			this.cls = Objects.requireNonNull(cls);
		}

		Key(final QName name, final Class<?> cls) {
			this(name, null, cls);
		}

		@Override
		public boolean equals(final Object other) {
			if (this == other) {
				return true;
			} else if (other instanceof Key) {
				final Key k = (Key) other;
				return k.name.equals(name) && Objects.equals(k.collectionClass, collectionClass) && k.cls.equals(cls);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, collectionClass, cls);
		}

	}

	public static final class Names {

		private Names() { }

		public static final String ALL = "all";
		public static final String ALTERNATIVE = "alternative";
		public static final String ANNOTATION = "annotation";
		public static final String ANY = "any";
		public static final String ANY_ATTRIBUTE = "anyAttribute";
		public static final String APPINFO = "appinfo";
		public static final String ASSERT = "assert";
		public static final String ASSERTION = "assertion";
		public static final String ATTRIBUTE = "attribute";
		public static final String ATTRIBUTE_GROUP = "attributeGroup";
		public static final String CHOICE = "choice";
		public static final String COMPLEX_CONTENT = "complexContent";
		public static final String COMPLEX_TYPE = "complexType";
		public static final String DEFAULT_OPEN_CONTENT = "defaultOpenContent";
		public static final String DOCUMENTATION = "documentation";
		public static final String ELEMENT = "element";
		public static final String EXTENSION = "extension";
		public static final String FIELD = "field";
		public static final String GROUP = "group";
		public static final String IMPORT = "import";
		public static final String INCLUDE = "include";
		public static final String KEY = "key";
		public static final String KEYREF = "keyref";
		public static final String LIST = "list";
		public static final String NOTATION = "notation";
		public static final String OPEN_CONTENT = "openContent";
		public static final String OVERRIDE = "override";
		public static final String REDEFINE = "redefine";
		public static final String RESTRICTION = "restriction";
		public static final String SELECTOR = "selector";
		public static final String SEQUENCE = "sequence";
		public static final String SIMPLE_CONTENT = "simpleContent";
		public static final String SIMPLE_TYPE = "simpleType";
		public static final String UNION = "union";
		public static final String UNIQUE = "unique";

		// Constraining facet names
		public static final String LENGTH = "length";
		public static final String MAX_LENGTH = "maxLength";
		public static final String MIN_LENGTH = "minLength";
		public static final String PATTERN = "pattern";
		public static final String ENUMERATION = "enumeration";
		public static final String WHITE_SPACE = "whiteSpace";
		public static final String MAX_INCLUSIVE = "maxInclusive";
		public static final String MAX_EXCLUSIVE = "maxExclusive";
		public static final String MIN_EXCLUSIVE = "minExclusive";
		public static final String MIN_INCLUSIVE = "minInclusive";
		public static final String TOTAL_DIGITS = "totalDigits";
		public static final String FRACTION_DIGITS = "fractionDigits";
		public static final String EXPLICIT_TIMEZONE = "explicitTimezone";

	}

	public static class AtParsers<T> extends TagParser<T> {

		private AtParsers(final Deferred<Value<T>> value) {
			super(value);
		}

		public TagParser<AttributeUse> use() {
			return Objects.requireNonNull(defer(Names.ATTRIBUTE, AttributeUse.class));
		}

	}

	public static class AoParsers<T> extends TagParser<T> {

		private static final TagParser<Annotation.Appinfo> APPINFO = defer(Names.APPINFO, Annotation.Appinfo.class);
		private static final TagParser<Annotation.Documentation> DOCUMENTATION = defer(Names.DOCUMENTATION, Annotation.Documentation.class);

		private AoParsers(final Deferred<Value<T>> value) {
			super(value);
		}

		public TagParser<Annotation.Appinfo> appinfo() {
			return APPINFO;
		}

		public TagParser<Annotation.Documentation> documentation() {
			return DOCUMENTATION;
		}

	}

	public static class CnParsers<U, R, E> extends TagParser<U> {

		private final Class<R> rClass;
		private final Class<E> eClass;

		private CnParsers(final Deferred<Value<U>> value, final Class<R> rClass, final Class<E> eClass) {
			super(value);
			this.rClass = rClass;
			this.eClass = eClass;
		}

		public TagParser<R> restriction() {
			return defer(Names.RESTRICTION, rClass);
		}

		public TagParser<E> extension() {
			return defer(Names.EXTENSION, eClass);
		}

	}

	public static class CtParsers<T> extends TagParser<T> {

		private CtParsers(final Deferred<Value<T>> value) {
			super(value);
		}

		public TagParser<ComplexType.Assert> asserts() {
			return defer(Names.ASSERT, ComplexType.Assert.class);
		}

		public CnParsers<ComplexType.ComplexContent, ComplexType.ComplexContent.Derivation, ComplexType.ComplexContent.Derivation> complexContent() {
			return defer(Names.COMPLEX_CONTENT, ComplexType.ComplexContent.class);
		}

		public TagParser<ComplexType.OpenContent> openContent() {
			return defer(Names.OPEN_CONTENT, ComplexType.OpenContent.class);
		}

		public CnParsers<ComplexType.SimpleContent, ComplexType.SimpleContent.Restriction, ComplexType.SimpleContent.Extension> simpleContent() {
			return defer(Names.SIMPLE_CONTENT, ComplexType.SimpleContent.class);
		}

	}

	public static class PtParsers<T> extends TagParser<T> {

		private final String name;

		private PtParsers(final Deferred<Value<T>> value, final String name) {
			super(value);
			this.name = name;
		}

		public TagParser<Particle> use() {
			return Objects.requireNonNull(defer(name, Particle.class));
		}

	}

	public static final class FctParsers {

		private FctParsers() { }

		public TagParser<ConstrainingFacet.Length> length() {
			return defer(Names.LENGTH, ConstrainingFacet.Length.class);
		}

		public TagParser<ConstrainingFacet.MaxLength> maxLength() {
			return defer(Names.MAX_LENGTH, ConstrainingFacet.MaxLength.class);
		}

		public TagParser<ConstrainingFacet.MinLength> minLength() {
			return defer(Names.MIN_LENGTH, ConstrainingFacet.MinLength.class);
		}

		public TagParser<ConstrainingFacet.Pattern> pattern() {
			return defer(Names.PATTERN, ConstrainingFacet.Pattern.class);
		}

		public TagParser<ConstrainingFacet.Enumeration> enumeration() {
			return defer(Names.ENUMERATION, ConstrainingFacet.Enumeration.class);
		}

		public TagParser<ConstrainingFacet.WhiteSpace> whiteSpace() {
			return defer(Names.WHITE_SPACE, ConstrainingFacet.WhiteSpace.class);
		}

		public TagParser<ConstrainingFacet.MaxInclusive> maxInclusive() {
			return defer(Names.MAX_INCLUSIVE, ConstrainingFacet.MaxInclusive.class);
		}

		public TagParser<ConstrainingFacet.MaxExclusive> maxExclusive() {
			return defer(Names.MAX_EXCLUSIVE, ConstrainingFacet.MaxExclusive.class);
		}

		public TagParser<ConstrainingFacet.MinExclusive> minExclusive() {
			return defer(Names.MIN_EXCLUSIVE, ConstrainingFacet.MinExclusive.class);
		}

		public TagParser<ConstrainingFacet.MinInclusive> minInclusive() {
			return defer(Names.MIN_INCLUSIVE, ConstrainingFacet.MinInclusive.class);
		}

		public TagParser<ConstrainingFacet.TotalDigits> totalDigits() {
			return defer(Names.TOTAL_DIGITS, ConstrainingFacet.TotalDigits.class);
		}

		public TagParser<ConstrainingFacet.FractionDigits> fractionDigits() {
			return defer(Names.FRACTION_DIGITS, ConstrainingFacet.FractionDigits.class);
		}

		public TagParser<ConstrainingFacet.Assertions> assertion() {
			return defer(Names.ASSERTION, ConstrainingFacet.Assertions.class);
		}

		public TagParser<ConstrainingFacet.ExplicitTimezone> explicitTimezone() {
			return defer(Names.EXPLICIT_TIMEZONE, ConstrainingFacet.ExplicitTimezone.class);
		}

	}

	public static final class SchParsers {

		private SchParsers() { }

		public TagParser<Schema.DefaultOpenContent> defaultOpenContent() {
			return defer(Names.DEFAULT_OPEN_CONTENT, Schema.DefaultOpenContent.class);
		}

		public TagParser<Schema.Import> imports() {
			return defer(Names.IMPORT, Schema.Import.class);
		}

		public TagParser<Schema.Include> include() {
			return defer(Names.INCLUDE, Schema.Include.class);
		}

		public TagParser<Schema.Overrides> override() {
			return defer(Names.OVERRIDE, Schema.Overrides.class);
		}

		public TagParser<Schema.Redefine> redefine() {
			return defer(Names.REDEFINE, Schema.Redefine.class);
		}

	}

	public static class StParsers<T> extends TagParser<T> {

		private StParsers(final Deferred<Value<T>> value) {
			super(value);
		}

		public TagParser<SimpleType.Restriction> restriction() {
			return defer(Names.RESTRICTION, SimpleType.Restriction.class);
		}

		public TagParser<SimpleType.List> list() {
			return defer(Names.LIST, SimpleType.List.class);
		}

		public TagParser<SimpleType.Union> union() {
			return defer(Names.UNION, SimpleType.Union.class);
		}

	}

	private static final Map<Key, TagParser<?>> tagParsers = new ConcurrentHashMap<>();
	private static final Map<Key, Value<?>> values = new ConcurrentHashMap<>();

	private final Deferred<Value<T>> value;

	TagParser(final Deferred<Value<T>> value) {
		this.value = value;
	}

	@SuppressWarnings("unchecked")
	private static <T, P extends TagParser<T>> P defer(final String elementLocalName, final Class<T> cls) {
		final QName name = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, elementLocalName);
		final Key key = new Key(name, cls);
		TagParser<T> tagParser = (TagParser<T>) tagParsers.get(key);
		if (tagParser == null) {
			final Deferred<Value<T>> value = () -> (Value<T>) Objects.requireNonNull(values.get(key), cls.toString());
			switch (elementLocalName) {
			case Names.ANNOTATION:
				tagParser = new AoParsers<>(value);
				break;
			case Names.ATTRIBUTE:
				tagParser = new AtParsers<>(value);
				break;
			case Names.COMPLEX_CONTENT:
				tagParser = new CnParsers<>(value, ComplexType.ComplexContent.Derivation.class, ComplexType.ComplexContent.Derivation.class);
				break;
			case Names.COMPLEX_TYPE:
				tagParser = new CtParsers<>(value);
				break;
			case Names.ELEMENT:
			case Names.GROUP:
				tagParser = new PtParsers<>(value, elementLocalName);
				break;
			case Names.SIMPLE_CONTENT:
				tagParser = new CnParsers<>(value, ComplexType.SimpleContent.Restriction.class, ComplexType.SimpleContent.Extension.class);
				break;
			case Names.SIMPLE_TYPE:
				tagParser = new StParsers<>(value);
				break;
			default:
				tagParser = new TagParser<>(value);
				break;
			}
			if (tagParsers.putIfAbsent(key, tagParser) != null) {
				throw new IllegalStateException("Collision of deferred instances " + cls.getCanonicalName());
			}
		}
		return (P) tagParser;
	}

	public static <T> void register(final String elementLocalName, final SequenceParser sequenceParser, final Class<? extends T> cls, final Function<Result, T> parseMethod) {
		final QName name = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, elementLocalName);
		final Key key = new Key(name, cls);
		if (values.putIfAbsent(key, new Value<>(name, sequenceParser, parseMethod)) != null) {
			throw new IllegalStateException(name + " is already registered");
		}
	}

	public static final TagParser<Particle> ALL = defer(Names.ALL, Particle.class);
	public static final TagParser<Alternative> ALTERNATIVE = defer(Names.ALTERNATIVE, Alternative.class);
	public static final AoParsers<Annotation> ANNOTATION = defer(Names.ANNOTATION, Annotation.class);
	public static final TagParser<Wildcard.Any> ANY = defer(Names.ANY, Wildcard.Any.class);
	public static final TagParser<Wildcard> ANY_ATTRIBUTE = defer(Names.ANY_ATTRIBUTE, Wildcard.class);
	public static final TagParser<Assertion> ASSERTION = defer(Names.ASSERTION, Assertion.class);
	public static final AtParsers<Attribute> ATTRIBUTE = defer(Names.ATTRIBUTE, Attribute.class);
	public static final TagParser<AttributeGroup> ATTRIBUTE_GROUP = defer(Names.ATTRIBUTE_GROUP, AttributeGroup.class);
	public static final TagParser<Particle> CHOICE = defer(Names.CHOICE, Particle.class);
	public static final CtParsers<ComplexType> COMPLEX_TYPE = defer(Names.COMPLEX_TYPE, ComplexType.class);
	public static final FctParsers FACETS = new FctParsers();
	public static final PtParsers<Element> ELEMENT = defer(Names.ELEMENT, Element.class);
	public static final TagParser<Assertion.XPathExpression> FIELD = defer(Names.FIELD, Assertion.XPathExpression.class);
	public static final PtParsers<ModelGroup> GROUP = defer(Names.GROUP, ModelGroup.class);
	public static final TagParser<IdentityConstraint> KEY = defer(Names.KEY, IdentityConstraint.class);
	public static final TagParser<IdentityConstraint> KEYREF = defer(Names.KEYREF, IdentityConstraint.class);
	public static final TagParser<Notation> NOTATION = defer(Names.NOTATION, Notation.class);
	public static final SchParsers SCHEMA = new SchParsers();
	public static final TagParser<Assertion.XPathExpression> SELECTOR = defer(Names.SELECTOR, Assertion.XPathExpression.class);
	public static final TagParser<Particle> SEQUENCE = defer(Names.SEQUENCE, Particle.class);
	public static final StParsers<SimpleType> SIMPLE_TYPE = defer(Names.SIMPLE_TYPE, SimpleType.class);
	public static final TagParser<IdentityConstraint> UNIQUE = defer(Names.UNIQUE, IdentityConstraint.class);

	public QName getName() {
		return value.get().name;
	}

	public SequenceParser getSequenceParser() {
		return value.get().sequenceParser;
	}

	public T parse(final Result result) {
		return value.get().parseMethod.apply(result);
	}

	public boolean equalsName(final Node node) {
		return getName().getNamespaceURI().equals(NodeHelper.namespaceUri(node)) && getName().getLocalPart().equals(node.getLocalName());
	}

	@Override
	public String toString() {
		return getName().toString();
	}

}
