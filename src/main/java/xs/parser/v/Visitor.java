package xs.parser.v;

import org.w3c.dom.*;
import xs.parser.*;
import xs.parser.Element;
import xs.parser.Notation;

/**
 * Interface for visiting all descendants of a Schema component within a schema document.
 *
 * <pre>
 * Visitors.visit(schemaComponent, new Visitor() { ... });
 * </pre>
 *
 * If not all schema types need to be handled, it is recommended to use the {@link DefaultVisitor} implementation instead to reduce the number of method overrides.
 */
public interface Visitor {

	/**
	 * Invoked before a Schema component is visited by any of the {@code on*} methods.
	 * If this method returns {@code false}, then it and all of its descendants will not be visited by any subsequent methods.
	 * @param context The ancestor Schema component of {@code s}
	 * @param node The underlying XDM node that {@code s} was constructed from
	 * @param s The Schema component
	 * @return {@code true} if this Schema component should be visited by this {@link Visitor}.
	 */
	public boolean visit(final AnnotatedComponent context, final Node node, SchemaComponent s);

	/**
	 * Invoked when an {@link Alternative} is visited.
	 * @param context The ancestor Schema component of this {@link Alternative}
	 * @param node The underlying XDM node that this {@link Alternative} was constructed from
	 * @param alternative The {@link Alternative}
	 */
	public void onAlternative(AnnotatedComponent context, Node node, Alternative alternative);

	/**
	 * Invoked when an {@link Annotation} is visited.
	 * @param context The ancestor Schema component of this {@link Annotation}
	 * @param node The underlying XDM node that this {@link Annotation} was constructed from
	 * @param annotation The {@link Annotation}
	 */
	public void onAnnotation(AnnotatedComponent context, Node node, Annotation annotation);

	/**
	 * Invoked when an {@link Assertion} is visited.
	 * @param context The ancestor Schema component of this {@link Assertion}
	 * @param node The underlying XDM node that this {@link Assertion} was constructed from
	 * @param assertion The {@link Assertion}
	 */
	public void onAssertion(AnnotatedComponent context, Node node, Assertion assertion);

	/**
	 * Invoked when an {@link Attribute} is visited.
	 * @param context The ancestor Schema component of this {@link Attribute}
	 * @param node The underlying XDM node that this {@link Attribute} was constructed from
	 * @param attribute The {@link Attribute}
	 */
	public void onAttribute(AnnotatedComponent context, Node node, Attribute attribute);

	/**
	 * Invoked when an {@link AttributeGroup} is visited.
	 * @param context The ancestor Schema component of this {@link AttributeGroup}
	 * @param node The underlying XDM node that this {@link AttributeGroup} was constructed from
	 * @param attributeGroup The {@link AttributeGroup}
	 */
	public void onAttributeGroup(AnnotatedComponent context, Node node, AttributeGroup attributeGroup);

	/**
	 * Invoked when an {@link AttributeUse} is visited.
	 * @param context The ancestor Schema component of this {@link AttributeUse}
	 * @param node The underlying XDM node that this {@link AttributeUse} was constructed from
	 * @param attributeUse The {@link AttributeUse}
	 */
	public void onAttributeUse(AnnotatedComponent context, Node node, AttributeUse attributeUse);

	/**
	 * Invoked when a {@link ComplexType} is visited.
	 * @param context The ancestor Schema component of this {@link ComplexType}
	 * @param node The underlying XDM node that this {@link ComplexType} was constructed from
	 * @param complexType The {@link ComplexType}
	 */
	public void onComplexType(AnnotatedComponent context, Node node, ComplexType complexType);

	/**
	 * Invoked when a {@link ConstrainingFacet} is visited.
	 * @param context The ancestor Schema component of this {@link ConstrainingFacet}
	 * @param node The underlying XDM node that this {@link ConstrainingFacet} was constructed from
	 * @param constrainingFacet The {@link ConstrainingFacet}
	 */
	public void onConstrainingFacet(SimpleType context, Node node, ConstrainingFacet constrainingFacet);

	/**
	 * Invoked when an {@link Element} is visited.
	 * @param context The ancestor Schema component of this {@link Element}
	 * @param node The underlying XDM node that this {@link Element} was constructed from
	 * @param element The {@link Element}
	 */
	public void onElement(AnnotatedComponent context, Node node, Element element);

	/**
	 * Invoked when a {@link FundamentalFacet} is visited.
	 * @param context The ancestor Schema component of this {@link FundamentalFacet}
	 * @param node The underlying XDM node that this {@link FundamentalFacet} was constructed from
	 * @param fundamentalFacet The {@link FundamentalFacet}
	 */
	public void onFundamentalFacet(SimpleType context, Node node, FundamentalFacet fundamentalFacet);

	/**
	 * Invoked when an {@link IdentityConstraint} is visited.
	 * @param context The ancestor Schema component of this {@link IdentityConstraint}
	 * @param node The underlying XDM node that this {@link IdentityConstraint} was constructed from
	 * @param identityConstraint The {@link IdentityConstraint}
	 */
	public void onIdentityConstraint(AnnotatedComponent context, Node node, IdentityConstraint identityConstraint);

	/**
	 * Invoked when a {@link ModelGroup} is visited.
	 * @param context The ancestor Schema component of this {@link ModelGroup}
	 * @param node The underlying XDM node that this {@link ModelGroup} was constructed from
	 * @param modelGroup The {@link ModelGroup}
	 */
	public void onModelGroup(AnnotatedComponent context, Node node, ModelGroup modelGroup);

	/**
	 * Invoked when a {@link Notation} is visited.
	 * @param context The ancestor Schema component of this {@link Notation}
	 * @param node The underlying XDM node that this {@link Notation} was constructed from
	 * @param notation The {@link Notation}
	 */
	public void onNotation(AnnotatedComponent context, Node node, Notation notation);

	/**
	 * Invoked when a {@link Particle} is visited.
	 * @param context The ancestor Schema component of this {@link Particle}
	 * @param node The underlying XDM node that this {@link Particle} was constructed from
	 * @param particle The {@link Particle}
	 */
	public void onParticle(AnnotatedComponent context, Node node, Particle particle);

	/**
	 * Invoked when a {@link Schema} is visited.
	 * @param document The underlying document that this {@link Schema} was constructed from
	 * @param schema The {@link Schema}
	 */
	public void onSchema(Document document, Schema schema);

	/**
	 * Invoked when a {@link SimpleType} is visited.
	 * @param context The ancestor Schema component of this {@link SimpleType}
	 * @param node The underlying XDM node that this {@link SimpleType} was constructed from
	 * @param simpleType The {@link SimpleType}
	 */
	public void onSimpleType(AnnotatedComponent context, Node node, SimpleType simpleType);

	/**
	 * Invoked when a {@link Wildcard} is visited.
	 * @param context The ancestor Schema component of this {@link Wildcard}
	 * @param node The underlying XDM node that this {@link Wildcard} was constructed from
	 * @param wildcard The {@link Wildcard}
	 */
	public void onWildcard(AnnotatedComponent context, Node node, Wildcard wildcard);

}
