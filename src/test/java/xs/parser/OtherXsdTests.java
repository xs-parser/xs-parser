package xs.parser;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;
import org.junit.runners.Parameterized.*;
import xs.parser.Attribute.*;
import xs.parser.Schema.*;
import xs.parser.internal.util.*;
import xs.parser.internal.*;
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

	private final Set<SchemaComponent> visited = new HashSet<>();
	private final Path schemaFile;

	public OtherXsdTests(final Path schemaFile) {
		this.schemaFile = schemaFile;
	}

	private static boolean isPrimitiveType(final SimpleType s) {
		return SimpleType.xsAnyURI().equals(s)
				|| SimpleType.xsBase64Binary().equals(s)
				|| SimpleType.xsBoolean().equals(s)
				|| SimpleType.xsDate().equals(s)
				|| SimpleType.xsDateTime().equals(s)
				|| SimpleType.xsDecimal().equals(s)
				|| SimpleType.xsDouble().equals(s)
				|| SimpleType.xsDuration().equals(s)
				|| SimpleType.xsFloat().equals(s)
				|| SimpleType.xsGDay().equals(s)
				|| SimpleType.xsGMonth().equals(s)
				|| SimpleType.xsGMonthDay().equals(s)
				|| SimpleType.xsGYear().equals(s)
				|| SimpleType.xsGYearMonth().equals(s)
				|| SimpleType.xsHexBinary().equals(s)
				|| SimpleType.xsNOTATION().equals(s)
				|| SimpleType.xsQName().equals(s)
				|| SimpleType.xsString().equals(s)
				|| SimpleType.xsTime().equals(s);
	}

	@Parameters(name = "{0}")
	public static Collection<Object[]> getSchemaFiles() throws IOException {
		return Files.walk(Paths.get("src/test/resources/xsd"))
				.filter(Files::isRegularFile)
				.map(f -> new Object[] { f })
				.collect(Collectors.toList());
	}

	private void visitAlternative(final Alternative a) {
		if (!visited.add(a)) {
			return;
		}
		if (a.test() != null) {
			a.test().defaultNamespace();
			a.test().namespaceBindings().forEach(Assert::assertNotNull);
			a.test().baseURI();
			assertNotNull(a, a.test().expression());
		}
		if (a.type() instanceof SimpleType) {
			visitSimpleType((SimpleType) a.type());
		} else if (a.type() instanceof ComplexType) {
			visitComplexType((ComplexType) a.type());
		} else {
			Assert.fail(a.type().getClass().getName());
		}
	}

	private void visitAttributeUse(final AttributeUse a) {
		if (!visited.add(a)) {
			return;
		}
		visitAnnotatedComponent(a);
		a.inheritable();
		a.required();
		if (a.valueConstraint() != null) {
			visitValueConstraint(a.valueConstraint());
		}
		visitAttribute(a.attributeDeclaration());
	}

	private void visitAttribute(final Attribute a) {
		if (!visited.add(a)) {
			return;
		}
		visitAnnotatedComponent(a);
		a.name();
		a.targetNamespace();
		visitScope(a.scope());
		if (a.valueConstraint() != null) {
			visitValueConstraint(a.valueConstraint());
		}
		assertNotNull(a, a.scope());
		visitScope(a.scope());
		if (a.valueConstraint() != null) {
			visitValueConstraint(a.valueConstraint());
		}
		a.inheritable();
	}

	private void visitScope(final Scope s) {
		Assert.assertTrue((Scope.Variety.GLOBAL.equals(s.variety()) && s.parent() == null) || (Scope.Variety.LOCAL.equals(s.variety()) && s.parent() != null));
	}

	private void visitValueConstraint(final ValueConstraint v) {
		assertNotNull(v, v.variety());
		assertNotNull(v, v.value());
		assertNotNull(v, v.lexicalForm());
	}

	private void visitSimpleType(final SimpleType s) {
		if (!visited.add(s)) {
			return;
		}
		if (SimpleType.xsAnySimpleType() == s) {
			return; // TODO: handle this separately
		} else if (SimpleType.xsAnyAtomicType() == s) {
			return; // TODO: handle this separately
		}
		visitAnnotatedComponent(s);
		s.name();
		s.targetNamespace();
		visitSimpleType((SimpleType) s.baseType());
		s.facets().forEach(this::visitConstrainingFacet);
		s.fundamentalFacets().forEach(f -> {
			visitSchemaComponent(f);
			f.value();
		});
		switch (s.variety()) {
		case ATOMIC:
			if (isPrimitiveType(s)) {
				Assert.assertEquals(s.name(), s, s.primitiveType());
			} else {
				visitSimpleType(s.primitiveType());
			}
			break;
		case LIST:
			visitSimpleType(s.itemType());
			break;
		case UNION:
			s.memberTypes().forEach(this::visitSimpleType);
			break;
		default:
			Assert.fail(s.name() + ", " + s.variety().toString());
		}
	}

	private void visitComplexType(final ComplexType c) {
		if (!visited.add(c)) {
			return;
		}
		if (ComplexType.xsAnyType() == c) {
			return; // TODO check this independently
		}
		visitAnnotatedComponent(c);
		c.name();
		c.targetNamespace();
		c.attributeUses().forEach(this::visitAttributeUse);
		if (c.attributeWildcard() != null) {
			visitWildcard(c.attributeWildcard());
		}
		c.prohibitedSubstitutions().forEach(Assert::assertNotNull);
		c.finals().forEach(Assert::assertNotNull);
		c.prohibitedSubstitutions().forEach(Assert::assertNotNull);
		c.isAbstract();
		c.derivationMethod();
		visitContentType(c.contentType());
		assertNotNull(c, c.baseType());
		Assert.assertTrue(c.name(), c.baseType() instanceof ComplexType || c.baseType() instanceof SimpleType);
	}

	private void visitGroup(final ModelGroup g) {
		if (!visited.add(g)) {
			return;
		}
		visitAnnotatedComponent(g);
		g.name();
		g.targetNamespace();
		assertNotNull(g, g.compositor());
		assertNotNull(g, g.modelGroup());
		if (g.modelGroup() != g) {
			visitGroup(g.modelGroup());
		}
		for (final Particle p : g.particles()) {
			assertNotNull(g, p);
			visitParticle(p);
		}
	}

	private void visitElement(final Element e) {
		if (!visited.add(e)) {
			return;
		}
		visitAnnotatedComponent(e);
		e.name();
		e.targetNamespace();
		if (e.typeTable() != null) {
			e.typeTable().alternatives().forEach(this::visitAlternative);
			visitAlternative(e.typeTable().defaultType());
		}
		e.valueConstraint();
		assertNotNull(e, e.scope());
		e.identityConstraints().forEach(this::visitIdentityConstraint);
		e.nillable();
		e.substitutionGroupAffiliations().forEach(Assert::assertNotNull);
		e.disallowedSubstitutions().forEach(Assert::assertNotNull);
		e.substitutionGroupExclusions().forEach(Assert::assertNotNull);
		e.isAbstract();
		assertNotNull(e, e.type());
		if (e.type() instanceof SimpleType) {
			visitSimpleType((SimpleType) e.type());
		} else if (e.type() instanceof ComplexType) {
			visitComplexType((ComplexType) e.type());
		} else {
			Assert.fail(e.type().getClass().getName());
		}
	}

	@Test
	public void testOtherXsds() throws Exception {
		final Schema schema = new Schema(resolver, NodeHelper.newDocumentBuilder().parse(schemaFile.toFile()));
		schema.visit(new Visitor() {

			final Set<SchemaComponent> visited = new HashSet<>();

			@Override
			public boolean markVisited(final SchemaComponent s) {
				return visited.add(s);
			}

			@Override
			public void onAlternative(SchemaComponent owner, Alternative alternative) {
				// TODO Auto-generated method stub

				if (alternative.test() != null) {
					alternative.test().defaultNamespace();
					alternative.test().namespaceBindings().forEach(Assert::assertNotNull);
					alternative.test().baseURI();
					Assert.assertNotNull(alternative.test().expression());
				}
			}

			@Override
			public void onAnnotation(SchemaComponent owner, Annotation annotation) {
				// TODO Auto-generated method stub
				annotation.applicationInformation().forEach(Assert::assertNotNull);
				annotation.userInformation().forEach(Assert::assertNotNull);
				annotation.attributes().forEach(Assert::assertNotNull); // TODO
			}

			@Override
			public void onAssertion(SchemaComponent owner, Assertion assertion) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAttribute(SchemaComponent owner, Attribute attribute) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAttributeGroup(SchemaComponent owner, AttributeGroup attributeGroup) {
				attributeGroup.name();
				attributeGroup.targetNamespace();
			}

			@Override
			public void onAttributeUse(SchemaComponent owner, AttributeUse attributeUse) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onComplexType(SchemaComponent owner, ComplexType complexType) {
				// TODO Auto-generated method stub
				complexType.node(); // TODO: for all schema components

				final ContentType c = complexType.contentType();
				switch (c.variety()) {
				case EMPTY:
					Assert.assertNull(c.particle());
					Assert.assertNull(c.simpleType());
					Assert.assertNull(c.openContent());
					break;
				case MIXED:
				case ELEMENT_ONLY:
					if (c.openContent() != null) {
						c.openContent().mode();
					}
					if (c.particle() != null) {
						visitParticle(c.particle());
					}
					break;
				case SIMPLE:
					Assert.assertNotNull(c.simpleType());
					visitSimpleType(c.simpleType());
					break;
				default:
					Assert.fail(c.toString());
				}

			}

			@Override
			public void onConstrainingFacet(SchemaComponent owner, ConstrainingFacet<?> constrainingFacet) {
				constrainingFacet.fixed();
				constrainingFacet.value();
			}

			@Override
			public void onElement(SchemaComponent owner, Element element) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onFundamentalFacet(SchemaComponent owner, FundamentalFacet<?> fundamentalFacet) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onIdentityConstraint(SchemaComponent owner, IdentityConstraint identityConstraint) {
				// TODO
				identityConstraint.name();
				identityConstraint.targetNamespace();
				identityConstraint.category();
				identityConstraint.referencedKey();
				identityConstraint.selector();
				identityConstraint.fields();
			}

			@Override
			public void onModelGroup(SchemaComponent owner, ModelGroup modelGroup) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onNotation(SchemaComponent owner, Notation notation) {
				notation.name();
				notation.targetNamespace();
				notation.publicIdentiifer();
				notation.systemIdentifier();
			}

			@Override
			public void onParticle(SchemaComponent owner, Particle<Term> particle) {
				particle.maxOccurs();
				particle.minOccurs();
			}

			@Override
			public void onSimpleType(SchemaComponent owner, SimpleType simpleType) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onWildcard(SchemaComponent owner, Wildcard wildcard) {
				Assert.assertNotNull(wildcard.namespaceConstraint().variety());
				wildcard.namespaceConstraint().namespaces().forEach(Assert::assertNotNull);
				wildcard.namespaceConstraint().disallowedNames().forEach(Assert::assertNotNull);
				Assert.assertNotNull(wildcard.processContents());
			}

		});
	}

}