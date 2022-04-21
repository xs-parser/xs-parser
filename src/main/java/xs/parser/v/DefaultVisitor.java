package xs.parser.v;

import java.util.*;
import java.util.concurrent.*;
import xs.parser.*;

public abstract class DefaultVisitor implements Visitor {

	private final Set<SchemaComponent> visited = new ConcurrentSkipListSet<>();

	@Override
	public boolean markVisited(final SchemaComponent s) {
		return visited.add(s);
	}

	@Override
	public void onAlternative(SchemaComponent owner, Alternative alternative) { }

	@Override
	public void onAnnotation(SchemaComponent owner, Annotation annotation) { }

	@Override
	public void onAssertion(SchemaComponent owner, Assertion assertion) { }

	@Override
	public void onAttribute(SchemaComponent owner, Attribute attribute) { }

	@Override
	public void onAttributeGroup(SchemaComponent owner, AttributeGroup attributeGroup) { }

	@Override
	public void onAttributeUse(SchemaComponent owner, AttributeUse attributeUse) { }

	@Override
	public void onComplexType(SchemaComponent owner, ComplexType complexType) { }

	@Override
	public void onConstrainingFacet(SchemaComponent owner, ConstrainingFacet constrainingFacet) { }

	@Override
	public void onElement(SchemaComponent owner, Element element) { }

	@Override
	public void onFundamentalFacet(SchemaComponent owner, FundamentalFacet fundamentalFacet) { }

	@Override
	public void onIdentityConstraint(SchemaComponent owner, IdentityConstraint identityConstraint) { }

	@Override
	public void onModelGroup(SchemaComponent owner, ModelGroup modelGroup) { }

	@Override
	public void onNotation(SchemaComponent owner, Notation notation) { }

	@Override
	public void onParticle(SchemaComponent owner, Particle particle) { }

	@Override
	public void onSimpleType(SchemaComponent owner, SimpleType simpleType) { }

	@Override
	public void onWildcard(SchemaComponent owner, Wildcard wildcard) { }

}
