package xs.parser.internal;

import java.lang.reflect.*;
import javax.xml.*;
import javax.xml.namespace.*;
import org.w3c.dom.Node;
import xs.parser.*;
import xs.parser.Annotation.*;
import xs.parser.Assertion.*;
import xs.parser.SimpleContent.*;
import xs.parser.internal.SequenceParser.*;

public final class ElementValue<T> {

	protected static class Parser<T> {

		private final Field parser;
		private final Method parse;

		private Parser(final Field parser, final Method parse) {
			// TODO: only available Java 9+
			// assert parser.canAccess(null);
			// assert parse.canAccess(null);
			this.parser = parser;
			this.parse = parse;
		}

		private static <R> Parser<R> of(final Class<?> cls) {
			return new Parser<>(getField(cls, "parser"), getMethod(cls, "parse", cls));
		}

		@SuppressWarnings("unchecked")
		private <X extends Throwable> T parse0(final Result result) throws X {
			try {
				return (T) parse.invoke(null, result);
			} catch (final InvocationTargetException e) {
				throw (X) e.getTargetException();
			} catch (final IllegalAccessException e) {
				throw (X) e;
			}
		}

		public SequenceParser get() {
			try {
				return (SequenceParser) parser.get(null);
			} catch (final ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
		}

		public T parse(final Result result) {
			return parse0(result);
		}

	}

	// XSD 1.1
	public static final ElementValue<Import> IMPORT = new ElementValue<>("import", Parser.of(Import.class));
	public static final ElementValue<Include> INCLUDE = new ElementValue<>("include", Parser.of(Include.class));
	public static final ElementValue<Redefine> REDEFINE = new ElementValue<>("redefine", Parser.of(Redefine.class));
	public static final ElementValue<Overrides> OVERRIDE = new ElementValue<>("override", Parser.of(Overrides.class));
	public static final ElementValue<ComplexType> COMPLEXTYPE = new ElementValue<>("complexType", Parser.of(ComplexType.class));
	public static final ElementValue<SimpleType> SIMPLETYPE = new ElementValue<>("simpleType", Parser.of(SimpleType.class));
	public static final ElementValue<SimpleRestriction> SIMPLE_RESTRICTION = new ElementValue<>("restriction", Parser.of(SimpleRestriction.class));
	public static final ElementValue<SimpleContentRestriction> SIMPLECONTENT_RESTRICTION = new ElementValue<>("restriction", Parser.of(SimpleContentRestriction.class));
	public static final ElementValue<ComplexDerivation> COMPLEX_RESTRICTION = new ElementValue<>("restriction", Parser.of(ComplexDerivation.class));
	public static final ElementValue<SimpleExtension> SIMPLE_EXTENSION = new ElementValue<>("extension", Parser.of(SimpleExtension.class));
	public static final ElementValue<ComplexDerivation> COMPLEX_EXTENSION = new ElementValue<>("extension", Parser.of(ComplexDerivation.class));
	public static final ElementValue<SimpleList> LIST = new ElementValue<>("list", Parser.of(SimpleList.class));
	public static final ElementValue<SimpleUnion> UNION = new ElementValue<>("union", Parser.of(SimpleUnion.class));
	public static final ElementValue<ComplexContent> COMPLEXCONTENT = new ElementValue<>("complexContent", Parser.of(ComplexContent.class));
	public static final ElementValue<OpenContent> OPENCONTENT = new ElementValue<>("openContent", Parser.of(OpenContent.class));
	public static final ElementValue<Schema.DefaultOpenContent> DEFAULTOPENCONTENT = new ElementValue<>("defaultOpenContent", Parser.of(Schema.DefaultOpenContent.class));
	public static final ElementValue<SimpleContent> SIMPLECONTENT = new ElementValue<>("simpleContent", Parser.of(SimpleContent.class));
	public static final ElementValue<AttributeGroup> ATTRIBUTEGROUP = new ElementValue<>("attributeGroup", Parser.of(AttributeGroup.class));
	public static final ElementValue<ModelGroup> GROUP_DECL = new ElementValue<>("group", new Parser<>(
			getField(ModelGroup.class, "parser"), getMethod(ModelGroup.class, "parseDecl", ModelGroup.class)));
	public static final ElementValue<Particle<ModelGroup>> GROUP = new ElementValue<>("group", new Parser<>(
			getField(ModelGroup.class, "parser"), getMethod(ModelGroup.class, "parse", Particle.class)));
	public static final ElementValue<Particle<ModelGroup>> ALL = new ElementValue<>("all", Parser.of(Particle.class));
	public static final ElementValue<Particle<ModelGroup>> CHOICE = new ElementValue<>("choice", Parser.of(Particle.class));
	public static final ElementValue<Particle<ModelGroup>> SEQUENCE = new ElementValue<>("sequence", Parser.of(Particle.class));
	public static final ElementValue<Element> ELEMENT_DECL = new ElementValue<>("element", new Parser<>(
			getField(Element.class, "parser"), getMethod(Element.class, "parseDecl", Element.class)));
	public static final ElementValue<Particle<Element>> ELEMENT = new ElementValue<>("element", new Parser<>(
			getField(Element.class, "parser"), getMethod(Element.class, "parse", Particle.class)));
	public static final ElementValue<Alternative> ALTERNATIVE = new ElementValue<>("alternative", Parser.of(Alternative.class));
	public static final ElementValue<IdentityConstraint> UNIQUE = new ElementValue<>("unique", Parser.of(IdentityConstraint.class));
	public static final ElementValue<IdentityConstraint> KEY = new ElementValue<>("key", Parser.of(IdentityConstraint.class));
	public static final ElementValue<IdentityConstraint> KEYREF = new ElementValue<>("keyref", Parser.of(IdentityConstraint.class));
	public static final ElementValue<XPathExpression> SELECTOR = new ElementValue<>("selector", Parser.of(XPathExpression.class));
	public static final ElementValue<XPathExpression> FIELD = new ElementValue<>("field", Parser.of(XPathExpression.class));
	public static final ElementValue<Attribute> ATTRIBUTE_DECL = new ElementValue<>("attribute", Parser.of(Attribute.class));
	public static final ElementValue<AttributeUse> ATTRIBUTE = new ElementValue<>("attribute", Parser.of(AttributeUse.class));
	public static final ElementValue<Particle<Wildcard>> ANY = new ElementValue<>("any", new Parser<>(
			getField(Wildcard.class, "anyParser"), getMethod(Wildcard.class, "parseAny", Particle.class)));
	public static final ElementValue<Wildcard> ANYATTRIBUTE = new ElementValue<>("anyAttribute", new Parser<>(
			getField(Wildcard.class, "anyAttributeParser"), getMethod(Wildcard.class, "parseAnyAttribute", Wildcard.class)));
	public static final ElementValue<Assertion> ASSERT = new ElementValue<>("assert", new Parser<>(
			getField(ComplexContent.Assert.class, "parser"), getMethod(Assertion.class, "parse", Assertion.class)));
	public static final ElementValue<Annotation> ANNOTATION = new ElementValue<>("annotation", Parser.of(Annotation.class));
	public static final ElementValue<Documentation> DOCUMENTATION = new ElementValue<>("documentation", Parser.of(Documentation.class));
	public static final ElementValue<Appinfo> APPINFO = new ElementValue<>("appinfo", Parser.of(Appinfo.class));
	public static final ElementValue<Notation> NOTATION = new ElementValue<>("notation", Parser.of(Notation.class));

	// XSD 1.1 Datatypes
	public static final ElementValue<ConstrainingFacet<?>> MINEXCLUSIVE = new ElementValue<>("minExclusive", Parser.of(ConstrainingFacet.class));
	public static final ElementValue<ConstrainingFacet<?>> MININCLUSIVE = new ElementValue<>("minInclusive", Parser.of(ConstrainingFacet.class));
	public static final ElementValue<ConstrainingFacet<?>> MAXEXCLUSIVE = new ElementValue<>("maxExclusive", Parser.of(ConstrainingFacet.class));
	public static final ElementValue<ConstrainingFacet<?>> MAXINCLUSIVE = new ElementValue<>("maxInclusive", Parser.of(ConstrainingFacet.class));
	public static final ElementValue<ConstrainingFacet<?>> TOTALDIGITS = new ElementValue<>("totalDigits", Parser.of(ConstrainingFacet.class));
	public static final ElementValue<ConstrainingFacet<?>> FRACTIONDIGITS = new ElementValue<>("fractionDigits", Parser.of(ConstrainingFacet.class));
	public static final ElementValue<ConstrainingFacet<?>> LENGTH = new ElementValue<>("length", Parser.of(ConstrainingFacet.class));
	public static final ElementValue<ConstrainingFacet<?>> MINLENGTH = new ElementValue<>("minLength", Parser.of(ConstrainingFacet.class));
	public static final ElementValue<ConstrainingFacet<?>> MAXLENGTH = new ElementValue<>("maxLength", Parser.of(ConstrainingFacet.class));
	public static final ElementValue<ConstrainingFacet<?>> ENUMERATION = new ElementValue<>("enumeration", Parser.of(ConstrainingFacet.class));
	public static final ElementValue<ConstrainingFacet<?>> WHITESPACE = new ElementValue<>("whiteSpace", Parser.of(ConstrainingFacet.class));
	public static final ElementValue<ConstrainingFacet<?>> PATTERN = new ElementValue<>("pattern", Parser.of(ConstrainingFacet.class));
	public static final ElementValue<ConstrainingFacet<?>> ASSERTION = new ElementValue<>("assertion", Parser.of(ConstrainingFacet.class));
	public static final ElementValue<ConstrainingFacet<?>> EXPLICITTIMEZONE = new ElementValue<>("explicitTimezone", Parser.of(ConstrainingFacet.class));

	private final QName name;
	private final Parser<T> parser;

	ElementValue(final String localName, final Parser<T> parser) {
		this.name = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, localName);
		this.parser = parser;
	}

	private static Field getField(final Class<?> cls, final String name) {
		try {
			final Field field = cls.getDeclaredField(name);
			field.setAccessible(true);
			return field;
		} catch (final ReflectiveOperationException e) {
			throw new AssertionError(e);
		}
	}

	private static Method getMethod(final Class<?> cls, final String name, final Class<?> returnType) {
		try {
			final Method method = cls.getDeclaredMethod(name, Result.class);
			if (!returnType.equals(method.getReturnType())) {
				throw new AssertionError(method.getReturnType() + " does not match expected " + returnType);
			}
			method.setAccessible(true);
			return method;
		} catch (final ReflectiveOperationException e) {
			throw new AssertionError(e);
		}
	}

	public QName getName() {
		return name;
	}

	public Parser<T> getParser() {
		return parser;
	}

	public boolean equalsName(final Node node) {
		return name.getNamespaceURI().equals(NodeHelper.namespaceUri(node)) && name.getLocalPart().equals(node.getLocalName());
	}

	@Override
	public String toString() {
		return getName().toString();
	}

}