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
import org.w3c.dom.*;
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
			public void onAlternative(final AnnotatedComponent context, final Node node,final Alternative alternative) {
				Assert.assertNotNull(node);
				final XPathExpression test = alternative.test();
				if (test != null) {
					checkXPathExpression(test);
				}
			}

			@Override
			public void onAnnotation(final AnnotatedComponent context, final Node node,final Annotation annotation) {
				Assert.assertNotNull(node);
				annotation.applicationInformation().forEach(Assert::assertNotNull);
				annotation.userInformation().forEach(Assert::assertNotNull);
				annotation.attributes().forEach(Assert::assertNotNull);
			}

			@Override
			public void onAssertion(final AnnotatedComponent context, final Node node,final Assertion assertion) {
				Assert.assertNotNull(node);
				final XPathExpression test = assertion.test();
				if (test != null) {
					checkXPathExpression(test);
				}
			}

			@Override
			public void onAttribute(final AnnotatedComponent context, final Node node,final Attribute attribute) {
				Assert.assertNotNull(node);
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
			public void onAttributeGroup(final AnnotatedComponent context, final Node node,final AttributeGroup attributeGroup) {
				Assert.assertNotNull(node);
				attributeGroup.name();
				checkTargetNamespace(attributeGroup.targetNamespace());
			}

			@Override
			public void onAttributeUse(final AnnotatedComponent context, final Node node,final AttributeUse attributeUse) {
				Assert.assertNotNull(node);
				attributeUse.inheritable();
				attributeUse.required();
				if (attributeUse.valueConstraint() != null) {
					checkValueConstraint(attributeUse.valueConstraint());
				}
			}

			@Override
			public void onComplexType(final AnnotatedComponent context, final Node node,final ComplexType complexType) {
				Assert.assertNotNull(node);
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
			public void onConstrainingFacet(final SimpleType context, final Node node,final ConstrainingFacet constrainingFacet) {
				Assert.assertNotNull(node);
				constrainingFacet.fixed();
				Assert.assertNotNull(constrainingFacet.value());
			}

			@Override
			public void onElement(final AnnotatedComponent context, final Node node,final Element element) {
				Assert.assertNotNull(node);
				element.name();
				checkTargetNamespace(element.targetNamespace());
				element.typeTable();
				Assert.assertNotNull(element.scope());
				if (Element.Scope.Variety.LOCAL.equals(element.scope().variety())) {
					Assert.assertNotNull(element.scope().parent());
				} else {
					Assert.assertNull(element.scope().parent());
				}
				element.nillable();
				if (element.valueConstraint() != null) {
					checkValueConstraint(element.valueConstraint());
				}
				element.disallowedSubstitutions().forEach(Assert::assertNotNull);
				element.substitutionGroupExclusions().forEach(Assert::assertNotNull);
				element.isAbstract();
			}

			@Override
			public void onFundamentalFacet(final SimpleType context, final Node node,final FundamentalFacet fundamentalFacet) {
				Assert.assertNotNull(node);
				Assert.assertNotNull(fundamentalFacet.value());
			}

			@Override
			public void onIdentityConstraint(final AnnotatedComponent context, final Node node,final IdentityConstraint identityConstraint) {
				Assert.assertNotNull(node);
				identityConstraint.name();
				checkTargetNamespace(identityConstraint.targetNamespace());
				Assert.assertNotNull(identityConstraint.category());
				checkXPathExpression(identityConstraint.selector());
				identityConstraint.fields().forEach(field -> checkXPathExpression(field));
			}

			@Override
			public void onModelGroup(final AnnotatedComponent context, final Node node,final ModelGroup modelGroup) {
				Assert.assertNotNull(node);
				modelGroup.name();
				checkTargetNamespace(modelGroup.targetNamespace());
				Assert.assertNotNull(modelGroup.compositor());
			}

			@Override
			public void onNotation(final AnnotatedComponent context, final Node node,final Notation notation) {
				Assert.assertNotNull(node);
				notation.name();
				checkTargetNamespace(notation.targetNamespace());
				notation.publicIdentiifer();
				notation.systemIdentifier();
			}

			@Override
			public void onParticle(final AnnotatedComponent context, final Node node,final Particle particle) {
				Assert.assertNotNull(node);
				Assert.assertNotNull(particle.maxOccurs());
				Assert.assertNotNull(particle.minOccurs());
			}

			@Override
			public void onSimpleType(final AnnotatedComponent context, final Node node,final SimpleType simpleType) {
				Assert.assertNotNull(node);
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
			public void onWildcard(final AnnotatedComponent context, final Node node,final Wildcard wildcard) {
				Assert.assertNotNull(node);
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
