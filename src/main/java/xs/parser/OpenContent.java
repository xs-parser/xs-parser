package xs.parser;

import java.util.*;
import org.w3c.dom.*;
import xs.parser.internal.*;
import xs.parser.internal.SequenceParser.*;

/**
 * <pre>
 * &lt;openContent
 *   id = ID
 *   mode = (none | interleave | suffix) : interleave
 *   {any attributes with non-schema namespace . . .}&gt;
 *   Content: (annotation?, any?)
 * &lt;/openContent&gt;
 * </pre>
 *
 * <table>
 *   <caption style="font-size: large; text-align: left">Property Record: Open Content</caption>
 *   <thead>
 *     <tr>
 *       <th style="text-align: left">Method</th>
 *       <th style="text-align: left">Property</th>
 *       <th style="text-align: left">Representation</th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <td>{@link OpenContent#mode()}</td>
 *       <td>{mode}</td>
 *       <td>One of {interleave, suffix}. Required.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link OpenContent#wildcard()}</td>
 *       <td>{wildcard}</td>
 *       <td>A Wildcard component. Required.</td>
 *     </tr>
 *   </tbody>
 * </table>
 */
public class OpenContent {

	public enum Mode {

		NONE("none"),
		INTERLEAVE("interleave"),
		SUFFIX("suffix");

		private final String name;

		private Mode(final String value) {
			this.name = value;
		}

		public static Mode getByName(final Node node) {
			final String name = node.getNodeValue();
			for (final Mode m : values()) {
				if (m.getName().equals(name)) {
					return m;
				}
			}
			throw new IllegalArgumentException(name);
		}

		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return getName();
		}

	}

	protected static final SequenceParser parser = new SequenceParser()
			.optionalAttributes(AttributeValue.ID, AttributeValue.MODE)
			.elements(0, 1, ElementValue.ANNOTATION)
			.elements(0, 1, ElementValue.ANY);

	private final Deque<Annotation> annotations;
	private final Mode mode;
	private final Particle<Wildcard> wildcard;

	OpenContent(final Deque<Annotation> annotations, final Mode mode, final Particle<Wildcard> wildcard) {
		this.annotations = Objects.requireNonNull(annotations);
		this.mode = mode;
		this.wildcard = wildcard;
	}

	protected static OpenContent parse(final Result result) {
		final Mode mode = result.value(AttributeValue.MODE);
		final Particle<Wildcard> wildcard = result.parse(ElementValue.ANY);
		return new OpenContent(result.annotations(), mode, wildcard);
	}

	protected Deque<Annotation> annotations() {
		return annotations;
	}

	/** @return The ·actual value· of the mode [attribute] of the ·wildcard element·, if present, otherwise interleave. */
	public Mode mode() {
		return mode;
	}

	/** @return Let W be the wildcard corresponding to the &lt;any&gt; [child] of the ·wildcard element·. If the {open content} of the ·explicit content type· is ·absent·, then W; otherwise a wildcard whose {process contents} and {annotations} are those of W, and whose {namespace constraint} is the wildcard union of the {namespace constraint} of W and of {open content}.{wildcard} of the ·explicit content type·, as defined in Attribute Wildcard Union (§3.10.6.3). */
	public Particle<Wildcard> wildcard() {
		return wildcard;
	}

}
