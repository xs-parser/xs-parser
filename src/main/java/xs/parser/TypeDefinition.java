package xs.parser;

import java.util.*;
import org.w3c.dom.*;
import xs.parser.internal.util.*;

/**
 * The abstract model provides two kinds of type definition component: simple and complex.
 * <p>
 * This specification uses the phrase type definition in cases where no distinction need be made between simple and complex types.
 * <p>
 * Type definitions form a hierarchy with a single root.
 */
public interface TypeDefinition extends AnnotatedComponent {

	/** Type definition final */
	public enum Final {

		/** Type definition final default */
		DEFAULT(""),
		/** Type definition final extension */
		EXTENSION("extension"),
		/** Type definition final restriction */
		RESTRICTION("restriction"),
		/** Type definition final list */
		LIST("list"),
		/** Type definition final union */
		UNION("union"),
		/** Type definition final all */
		ALL("#all");

		private final String name;

		private Final(final String name) {
			this.name = name;
		}

		static Deque<Final> getAttrValueAsFinals(final Attr attr) {
			final String value = NodeHelper.collapseWhitespace(attr.getValue());
			if ("#all".equals(value)) {
				return Deques.singletonDeque(Final.ALL);
			}
			final String[] values = value.split(NodeHelper.LIST_SEP);
			final Deque<Final> ls = new ArrayDeque<>();
			for (final String v : values) {
				final Final b = Final.getByName(v);
				if (Final.ALL.equals(b)) {
					throw NodeHelper.newParseException(attr, Final.ALL + " cannot be present in List");
				}
				ls.add(b);
			}
			return Deques.unmodifiableDeque(ls);
		}

		static Final getAttrValueAsFinal(final Attr attr) {
			return getByName(NodeHelper.collapseWhitespace(attr.getValue()));
		}

		static Final getByName(final String name) {
			for (final Final f : values()) {
				if (f.name.equals(name)) {
					return f;
				}
			}
			throw new IllegalArgumentException(name);
		}

		/** @return The name of this type definition final */
		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return name;
		}

	}

	/** @return An xs:NCName value. Optional. */
	public String name();

	/** @return An xs:anyURI value. Optional. */
	public String targetNamespace();

	/** @return A type definition component. Required. */
	public TypeDefinition baseTypeDefinition();

	/** @return A subset of {extension, restriction}. */
	public Deque<Final> finals();

	/**
	 * Required if {name} is ·absent·, otherwise must be ·absent·.
	 * @return Either an Element Declaration or a Complex Type Definition.
	 */
	public AnnotatedComponent context();

}
