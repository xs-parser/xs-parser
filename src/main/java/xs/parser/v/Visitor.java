package xs.parser.v;

import xs.parser.*;

public interface Visitor {

	public boolean markVisited(SchemaComponent s);

	public void onAlternative(SchemaComponent owner, Alternative alternative);

	public void onAnnotation(SchemaComponent owner, Annotation annotation);

	public void onAssertion(SchemaComponent owner, Assertion assertion);

	public void onAttribute(SchemaComponent owner, Attribute attribute);

	public void onAttributeGroup(SchemaComponent owner, AttributeGroup attributeGroup);

	public void onAttributeUse(SchemaComponent owner, AttributeUse attributeUse);

	public void onComplexType(SchemaComponent owner, ComplexType complexType);

	public void onConstrainingFacet(SchemaComponent owner, ConstrainingFacet constrainingFacet);

	public void onElement(SchemaComponent owner, Element element);

	public void onFundamentalFacet(SchemaComponent owner, FundamentalFacet fundamentalFacet);

	public void onIdentityConstraint(SchemaComponent owner, IdentityConstraint identityConstraint);

	public void onModelGroup(SchemaComponent owner, ModelGroup modelGroup);

	public void onNotation(SchemaComponent owner, Notation notation);

	public void onParticle(SchemaComponent owner, Particle particle);

	public void onSimpleType(SchemaComponent owner, SimpleType simpleType);

	public void onWildcard(SchemaComponent owner, Wildcard wildcard);

}
