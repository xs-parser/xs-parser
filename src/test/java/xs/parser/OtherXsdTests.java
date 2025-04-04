package xs.parser;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import javax.xml.*;
import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;
import org.junit.runners.Parameterized.*;
import org.w3c.dom.Element;
import xs.parser.Assertion.*;
import xs.parser.Schema.*;
import xs.parser.Wildcard.*;
import xs.parser.internal.util.*;
import xs.parser.v.*;

@RunWith(Parameterized.class)
public class OtherXsdTests {

	private static final DocumentResolver resolver = new DefaultDocumentResolver() {

		@Override
		public URI resolveUri(final String baseUri, final String namespace, final String schemaLocation) {
			final String path;
			if ("http://www.w3.org/XML/1998/namespace".equals(namespace) && "http://www.w3.org/2001/xml.xsd".equals(schemaLocation)) {
				path = "src/test/resources/w3c/xml.xsd";
			} else if ("http://www.w3.org/XML/1998/namespace".equals(namespace) && "http://www.w3.org/2001/03/xml.xsd".equals(schemaLocation)) {
				path = "src/test/resources/w3c/xml.xsd";
			} else if ("http://www.w3.org/1999/xlink".equals(namespace)) {
				path = "src/test/resources/w3c/xlink.xsd";
			} else if ("http://rest.immobilienscout24.de/schema/common/1.0".equals(namespace)) {
				path = "src/test/resources/xsd/is24/common/common-1.0.xsd";
			} else {
				try {
					return new URI(baseUri).resolve(schemaLocation);
				} catch (final URISyntaxException e) {
					return null;
				}
			}
			return Paths.get(path).toUri();
		}

	};

	static {
		System.setProperty("XS_PARSER_VERBOSE", Boolean.TRUE.toString());
	}

	private final Path schemaFile;

	public OtherXsdTests(final Path schemaFile) {
		this.schemaFile = schemaFile;
	}

	@Parameters(name = "{0}")
	public static Collection<Object[]> getSchemaFiles() throws IOException {
		return Files.walk(Paths.get("src/test/resources/xsd"))
				.filter(Files::isRegularFile)
				.map(f -> new Object[] { f })
				.collect(Collectors.toList());
	}

	@Test
	public void testOtherXsds() throws Exception {
		final Schema schema = new Schema(resolver, NodeHelper.newDocumentBuilder().parse(schemaFile.toFile()));
		Visitors.visit(schema, new DefaultVisitor() {

			void checkTargetNamespace(final String targetNamespace) {
				Assert.assertNotEquals(XMLConstants.NULL_NS_URI, targetNamespace);
			}

			void checkValueConstraint(final Attribute.ValueConstraint valueConstraint) {
				Assert.assertNotNull(valueConstraint);
				Assert.assertNotNull(valueConstraint.variety());
				Assert.assertNotNull(valueConstraint.value());
				Assert.assertNotNull(valueConstraint.lexicalForm());
			}

			void checkXPathExpression(final XPathExpression test) {
				Assert.assertNotNull(test);
				test.defaultNamespace();
				test.namespaceBindings().forEach(namespaceBinding -> {
					Assert.assertNotNull(namespaceBinding);
					Assert.assertNotNull(namespaceBinding.prefix());
					Assert.assertNotNull(namespaceBinding.namespace());
				});
				test.baseURI();
				Assert.assertNotNull(test.expression());
			}

			@Override
			public void onAlternative(final AnnotatedComponent context, final Element element, final Alternative alternative) {
				Assert.assertNotNull(element);
				final XPathExpression test = alternative.test();
				if (test != null) {
					checkXPathExpression(test);
				}
			}

			@Override
			public void onAnnotation(final AnnotatedComponent context, final Element element, final Annotation annotation) {
				Assert.assertNotNull(element);
				annotation.applicationInformation().forEach(Assert::assertNotNull);
				annotation.userInformation().forEach(Assert::assertNotNull);
				annotation.attributes().forEach(Assert::assertNotNull);
			}

			@Override
			public void onAssertion(final AnnotatedComponent context, final Element element, final Assertion assertion) {
				Assert.assertNotNull(element);
				final XPathExpression test = assertion.test();
				if (test != null) {
					checkXPathExpression(test);
				}
			}

			@Override
			public void onAttribute(final AnnotatedComponent context, final Element element, final Attribute attribute) {
				Assert.assertNotNull(element);
				attribute.name();
				checkTargetNamespace(attribute.targetNamespace());
				attribute.inheritable();
				Assert.assertNotNull(attribute.scope());
				if (Attribute.Scope.Variety.LOCAL.equals(attribute.scope().variety())) {
					Assert.assertNotNull(attribute.scope().parent());
				} else {
					Assert.assertNull(attribute.scope().parent());
				}
				if (attribute.valueConstraint() != null) {
					checkValueConstraint(attribute.valueConstraint());
				}
			}

			@Override
			public void onAttributeGroup(final AnnotatedComponent context, final Element element, final AttributeGroup attributeGroup) {
				Assert.assertNotNull(element);
				attributeGroup.name();
				checkTargetNamespace(attributeGroup.targetNamespace());
			}

			@Override
			public void onAttributeUse(final AnnotatedComponent context, final Element element, final AttributeUse attributeUse) {
				Assert.assertNotNull(element);
				attributeUse.inheritable();
				attributeUse.required();
				if (attributeUse.valueConstraint() != null) {
					checkValueConstraint(attributeUse.valueConstraint());
				}
			}

			@Override
			public void onComplexType(final AnnotatedComponent context, final Element element, final ComplexType complexType) {
				Assert.assertNotNull(element);
				complexType.name();
				checkTargetNamespace(complexType.targetNamespace());
				complexType.prohibitedSubstitutions().forEach(Assert::assertNotNull);
				complexType.finals().forEach(Assert::assertNotNull);
				complexType.isAbstract();
				Assert.assertNotNull(complexType.derivationMethod());
				Assert.assertNotNull(complexType.contentType());
				if (complexType.contentType().openContent() != null) {
					complexType.contentType().openContent().mode();
				}
				complexType.prohibitedSubstitutions().forEach(Assert::assertNotNull);
				if (complexType.name() == null) {
					Assert.assertNotNull(complexType.context());
				} else {
					Assert.assertNull(complexType.context());
				}
			}

			@Override
			public void onConstrainingFacet(final SimpleType context, final Element element, final ConstrainingFacet constrainingFacet) {
				Assert.assertNotNull(element);
				constrainingFacet.fixed();
				Assert.assertNotNull(constrainingFacet.value());
			}

			@Override
			public void onElement(final AnnotatedComponent context, final Element element, final xs.parser.Element xsElement) {
				Assert.assertNotNull(element);
				xsElement.name();
				checkTargetNamespace(xsElement.targetNamespace());
				xsElement.typeTable();
				Assert.assertNotNull(xsElement.scope());
				if (xs.parser.Element.Scope.Variety.LOCAL.equals(xsElement.scope().variety())) {
					Assert.assertNotNull(xsElement.scope().parent());
				} else {
					Assert.assertNull(xsElement.scope().parent());
				}
				xsElement.nillable();
				if (xsElement.valueConstraint() != null) {
					checkValueConstraint(xsElement.valueConstraint());
				}
				xsElement.disallowedSubstitutions().forEach(Assert::assertNotNull);
				xsElement.substitutionGroupExclusions().forEach(Assert::assertNotNull);
				xsElement.isAbstract();
			}

			@Override
			public void onFundamentalFacet(final SimpleType context, final Element element, final FundamentalFacet fundamentalFacet) {
				Assert.assertNotNull(element);
				Assert.assertNotNull(fundamentalFacet.value());
			}

			@Override
			public void onIdentityConstraint(final AnnotatedComponent context, final Element element, final IdentityConstraint identityConstraint) {
				Assert.assertNotNull(element);
				identityConstraint.name();
				checkTargetNamespace(identityConstraint.targetNamespace());
				Assert.assertNotNull(identityConstraint.category());
				checkXPathExpression(identityConstraint.selector());
				identityConstraint.fields().forEach(field -> checkXPathExpression(field));
			}

			@Override
			public void onModelGroup(final AnnotatedComponent context, final Element element, final ModelGroup modelGroup) {
				Assert.assertNotNull(element);
				modelGroup.name();
				checkTargetNamespace(modelGroup.targetNamespace());
				Assert.assertNotNull(modelGroup.compositor());
			}

			@Override
			public void onNotation(final AnnotatedComponent context, final Element element, final Notation notation) {
				Assert.assertNotNull(element);
				notation.name();
				checkTargetNamespace(notation.targetNamespace());
				notation.publicIdentiifer();
				notation.systemIdentifier();
			}

			@Override
			public void onParticle(final AnnotatedComponent context, final Element element, final Particle particle) {
				Assert.assertNotNull(element);
				Assert.assertNotNull(particle.maxOccurs());
				Assert.assertNotNull(particle.minOccurs());
			}

			@Override
			public void onSimpleType(final AnnotatedComponent context, final Element element, final SimpleType simpleType) {
				Assert.assertNotNull(element);
				simpleType.name();
				checkTargetNamespace(simpleType.targetNamespace());
				simpleType.finals().forEach(Assert::assertNotNull);
				if (simpleType.name() == null) {
					Assert.assertNotNull(simpleType.context());
				} else {
					Assert.assertNull(simpleType.context());
				}
				Assert.assertTrue(simpleType.variety() != null || simpleType == SimpleType.xsAnySimpleType());
			}

			@Override
			public void onWildcard(final AnnotatedComponent context, final Element element, final Wildcard wildcard) {
				Assert.assertNotNull(element);
				final NamespaceConstraint namespaceConstraint = wildcard.namespaceConstraint();
				Assert.assertNotNull(namespaceConstraint);
				Assert.assertNotNull(namespaceConstraint.variety());
				namespaceConstraint.namespaces().forEach(Assert::assertNotNull);
				namespaceConstraint.disallowedNames().forEach(Assert::assertNotNull);
				Assert.assertNotNull(wildcard.processContents());
			}

		});
	}

}
