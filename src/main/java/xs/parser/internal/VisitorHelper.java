package xs.parser.internal;

import xs.parser.*;
import xs.parser.v.*;

public class VisitorHelper {

	public static void visitAlternative(final SchemaComponent s, final Visitor visitor, final Alternative a) {
		visitor.onAlternative(s, a);
		a.visit(visitor);
	}

	public static void visitAnnotation(final SchemaComponent s, final Visitor visitor, final Annotation a) {
		visitor.onAnnotation(s, a);
		a.visit(visitor);
	}

	public static void visitAnnotations(final AnnotatedComponent a, final Visitor visitor) {
		for (final Annotation annotation : a.annotations()) {
			visitAnnotation(a, visitor, annotation);
		}
	}

	public static void visitAssertion(final SchemaComponent s, final Visitor visitor, final Assertion a) {
		visitor.onAssertion(s, a);
		a.visit(visitor);
	}

	public static void visitAttribute(final SchemaComponent s, final Visitor visitor, final Attribute a) {
		visitor.onAttribute(s, a);
		a.visit(visitor);
	}

	public static void visitAttributeGroup(final SchemaComponent s, final Visitor visitor, final AttributeGroup a) {
		visitor.onAttributeGroup(s, a);
		a.visit(visitor);
	}

	public static void visitAttributeUse(final SchemaComponent s, final Visitor visitor, final AttributeUse a) {
		visitor.onAttributeUse(s, a);
		a.visit(visitor);
	}

	public static void visitConstrainingFacet(final SchemaComponent s, final Visitor visitor, final ConstrainingFacet c) {
		visitor.onConstrainingFacet(s, c);
		c.visit(visitor);
	}

	public static void visitElement(final SchemaComponent s, final Visitor visitor, final Element e) {
		visitor.onElement(s, e);
		e.visit(visitor);
	}

	public static void visitFundamentalFacet(final SchemaComponent s, final Visitor visitor, final FundamentalFacet f) {
		visitor.onFundamentalFacet(s, f);
		f.visit(visitor);
	}

	public static void visitIdentityConstraint(final SchemaComponent s, final Visitor visitor, final IdentityConstraint i) {
		visitor.onIdentityConstraint(s, i);
		i.visit(visitor);
	}

	public static void visitModelGroup(final SchemaComponent s, final Visitor visitor, final ModelGroup modelGroup) {
		visitor.onModelGroup(s, modelGroup);
		modelGroup.visit(visitor);
	}

	public static void visitNotation(final SchemaComponent s, final Visitor visitor, final Notation notation) {
		visitor.onNotation(s, notation);
		notation.visit(visitor);
	}

	public static void visitParticle(final SchemaComponent s, final Visitor visitor, final Particle p) {
		visitor.onParticle(s, p);
		p.visit(visitor);
	}

	public static void visitSimpleType(final SchemaComponent s, final Visitor visitor, final SimpleType simpleType) {
		visitor.onSimpleType(s, simpleType);
		simpleType.visit(visitor);
	}

	public static void visitTerm(final SchemaComponent s, final Visitor visitor, final Term term) {
		if (term instanceof Element) {
			visitor.onElement(s, (Element) term);
		} else if (term instanceof ModelGroup) {
			visitor.onModelGroup(s, (ModelGroup) term);
		} else {
			visitor.onWildcard(s, (Wildcard) term);
		}
		term.visit(visitor);
	}

	public static void visitTypeDefinition(final SchemaComponent s, final Visitor visitor, final TypeDefinition type) {
		if (type instanceof ComplexType) {
			visitor.onComplexType(s, (ComplexType) type);
		} else {
			visitor.onSimpleType(s, (SimpleType) type);
		}
		type.visit(visitor);
	}

	public static void visitWildcard(final SchemaComponent s, final Visitor visitor, final Wildcard wildcard) {
		visitor.onWildcard(s, wildcard);
		wildcard.visit(visitor);
	}

}
