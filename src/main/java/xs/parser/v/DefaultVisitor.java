package xs.parser.v;

import java.util.*;
import java.util.concurrent.*;
import org.w3c.dom.*;
import org.w3c.dom.Element;
import xs.parser.*;
import xs.parser.Notation;

/**
 * A visitor with a default {@link #visit(AnnotatedComponent, Node, SchemaComponent)} implementation that allows only one instance of a distinct Schema component with its associated context to be visited per instance of this class.
 */
public class DefaultVisitor implements Visitor {

	private static class V {

		final AnnotatedComponent context;
		final Node node;
		final SchemaComponent s;

		V(final AnnotatedComponent context, final Node node, final SchemaComponent s) {
			this.context = context;
			this.node = node;
			this.s = s;
		}

		@Override
		public boolean equals(final Object other) {
			if (this == other) {
				return true;
			} else if (other instanceof V) {
				final V v = (V) other;
				return Objects.equals(v.context, context)
						&& Objects.equals(v.node, node)
						&& Objects.equals(v.s, s);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return Objects.hash(context, node, s);
		}

	}

	private final Set<V> visited = ConcurrentHashMap.newKeySet();

	@Override
	public boolean visit(final AnnotatedComponent context, final Node node, final SchemaComponent s) {
		return visited.add(new V(context, node, s));
	}

	@Override
	public void onAlternative(final AnnotatedComponent context, final Element element, final Alternative alternative) {
		// Do nothing for default implementation
	}

	@Override
	public void onAnnotation(final AnnotatedComponent context, final Element element, final Annotation annotation) {
		// Do nothing for default implementation
	}

	@Override
	public void onAssertion(final AnnotatedComponent context, final Element element, final Assertion assertion) {
		// Do nothing for default implementation
	}

	@Override
	public void onAttribute(final AnnotatedComponent context, final Element element, final Attribute attribute) {
		// Do nothing for default implementation
	}

	@Override
	public void onAttributeGroup(final AnnotatedComponent context, final Element element, final AttributeGroup attributeGroup) {
		// Do nothing for default implementation
	}

	@Override
	public void onAttributeUse(final AnnotatedComponent context, final Element element, final AttributeUse attributeUse) {
		// Do nothing for default implementation
	}

	@Override
	public void onComplexType(final AnnotatedComponent context, final Element element, final ComplexType complexType) {
		// Do nothing for default implementation
	}

	@Override
	public void onConstrainingFacet(final SimpleType context, final Element element, final ConstrainingFacet constrainingFacet) {
		// Do nothing for default implementation
	}

	@Override
	public void onElement(final AnnotatedComponent context, final Element element, final xs.parser.Element xsElement) {
		// Do nothing for default implementation
	}

	@Override
	public void onFundamentalFacet(final SimpleType context, final Element element, final FundamentalFacet fundamentalFacet) {
		// Do nothing for default implementation
	}

	@Override
	public void onIdentityConstraint(final AnnotatedComponent context, final Element element, final IdentityConstraint identityConstraint) {
		// Do nothing for default implementation
	}

	@Override
	public void onModelGroup(final AnnotatedComponent context, final Element element, final ModelGroup modelGroup) {
		// Do nothing for default implementation
	}

	@Override
	public void onNotation(final AnnotatedComponent context, final Element element, final Notation notation) {
		// Do nothing for default implementation
	}

	@Override
	public void onParticle(final AnnotatedComponent context, final Element element, final Particle particle) {
		// Do nothing for default implementation
	}

	@Override
	public void onSchema(final Document document, final Schema schema) {
		// Do nothing for default implementation
	}

	@Override
	public void onSimpleType(final AnnotatedComponent context, final Element element, final SimpleType simpleType) {
		// Do nothing for default implementation
	}

	@Override
	public void onWildcard(final AnnotatedComponent context, final Element element, final Wildcard wildcard) {
		// Do nothing for default implementation
	}

}
