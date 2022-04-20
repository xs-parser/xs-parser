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
import xs.parser.ComplexType.*;
import xs.parser.Schema.*;
import xs.parser.internal.*;

@RunWith(Parameterized.class)
public class OtherXsdTests {

	private static final DocumentResolver resolver = new DocumentResolver() {

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

	private void assertNotNull(final Object o, final Object value) {
		if (value == null) {
			throw new AssertionError(Objects.toString(o));
		}
	}

	private void assertNotNull(final SchemaComponent s, final Object value) {
		if (value == null) {
			throw new AssertionError(NodeHelper.toString(s.node()));
		}
	}

	private void visitSchemaComponent(final SchemaComponent s) {
		assertNotNull((Object) s, s.node());
	}

	private void visitAnnotatedComponent(final AnnotatedComponent a) {
		visitSchemaComponent(a);
		a.annotations().forEach(this::visitAnnotation);
	}

	private void visitAnnotation(final Annotation a) {
		if (!visited.add(a)) {
			return;
		}
		visitSchemaComponent(a);
		a.applicationInformation().forEach(Assert::assertNotNull);
		a.userInformation().forEach(Assert::assertNotNull);
		a.attributes().forEach(Assert::assertNotNull); // TODO
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

	private void visitAttributeGroup(final AttributeGroup a) {
		if (!visited.add(a)) {
			return;
		}
		visitAnnotatedComponent(a);
		a.name();
		a.targetNamespace();
		a.attributeWildcard();
		a.attributeUses().forEach(this::visitAttributeUse);
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

	private void visitConstrainingFacet(final ConstrainingFacet<?> f) {
		visitAnnotatedComponent(f);
		f.fixed();
		f.value();
	}

	private void visitWildcard(final Wildcard w) {
		visitAnnotatedComponent(w);
		assertNotNull(w, w.namespaceConstraint().variety());
		w.namespaceConstraint().namespaces().forEach(Assert::assertNotNull);
		w.namespaceConstraint().disallowedNames().forEach(Assert::assertNotNull);
		assertNotNull(w, w.processContents());
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

	private void visitContentType(final ContentType c) {
		switch (c.variety()) {
		case EMPTY:
			Assert.assertNull(c.particle());
			Assert.assertNull(c.simpleType());
			Assert.assertNull(c.openContent());
			break;
		case MIXED:
		case ELEMENT_ONLY:
			if (c.openContent() != null) {
				visitOpenContent(c.openContent());
			}
			if (c.particle() != null) {
				visitParticle(c.particle());
			}
			break;
		case SIMPLE:
			assertNotNull(c, c.simpleType());
			visitSimpleType(c.simpleType());
			break;
		default:
			Assert.fail(c.toString());
		}
	}

	private void visitOpenContent(final OpenContent o) {
		assertNotNull(o, o.mode());
		visitParticle(o.wildcard());
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
		for (final Particle<Term> p : g.particles()) {
			assertNotNull(g, p);
			visitParticle(p);
		}
	}

	private void visitParticle(final Particle<?> p) {
		if (!visited.add(p)) {
			return;
		}
		visitAnnotatedComponent(p);
		p.maxOccurs();
		p.minOccurs();
		assertNotNull(p, p.term());
		if (p.term() instanceof Element) {
			visitElement((Element) p.term());
		} else if (p.term() instanceof ModelGroup) {
			visitGroup((ModelGroup) p.term());
		} else if (p.term() instanceof Wildcard) {
			visitWildcard((Wildcard) p.term());
		} else {
			Assert.fail(p.term().getClass().getName());
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

	private void visitIdentityConstraint(final IdentityConstraint i) {
		if (!visited.add(i)) {
			return;
		}
		visitAnnotatedComponent(i);
		// TODO
		i.name();
		i.targetNamespace();
		i.category();
		i.referencedKey();
		i.selector();
		i.fields();
	}

	private void visitNotation(final Notation n) {
		if (!visited.add(n)) {
			return;
		}
		visitAnnotatedComponent(n);
		n.name();
		n.targetNamespace();
		n.publicIdentiifer();
		n.systemIdentifier();
	}

	@Test
	public void testOtherXsds() throws Exception {
		final Schema schema = new Schema(resolver, NodeHelper.newDocumentBuilder().parse(schemaFile.toFile()));
		visitAnnotatedComponent(schema);
		schema.attributeDeclarations().forEach(this::visitAttribute);
		schema.attributeGroupDefinitions().forEach(this::visitAttributeGroup);
		schema.typeDefinitions().stream().filter(SimpleType.class::isInstance).map(SimpleType.class::cast).forEach(this::visitSimpleType);
		schema.typeDefinitions().stream().filter(ComplexType.class::isInstance).map(ComplexType.class::cast).forEach(this::visitComplexType);
		schema.elementDeclarations().forEach(this::visitElement);
		schema.notationDeclarations().forEach(this::visitNotation);
		schema.identityConstraintDefinitions().forEach(this::visitIdentityConstraint);
	}

}