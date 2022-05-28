package xs.parser;

import java.util.*;
import org.w3c.dom.*;
import xs.parser.internal.util.*;

public interface TypeDefinition extends AnnotatedComponent {

	public enum Final {

		DEFAULT(""),
		EXTENSION("extension"),
		RESTRICTION("restriction"),
		LIST("list"),
		UNION("union"),
		ALL("#all");

		private final String name;

		private Final(final String name) {
			this.name = name;
		}

		static Deque<Final> getNodeValueAsFinals(final Node node) {
			final String value = NodeHelper.requireNodeValue(node);
			if ("#all".equals(value)) {
				return Deques.singletonDeque(Final.ALL);
			}
			final String[] values = value.split(NodeHelper.LIST_SEP);
			final Deque<Final> ls = new ArrayDeque<>();
			for (final String v : values) {
				final Final b = Final.getByName(v);
				if (Final.ALL.equals(b)) {
					throw NodeHelper.newParseException(node, Final.ALL + " cannot be present in List");
				}
				ls.add(b);
			}
			return Deques.unmodifiableDeque(ls);
		}

		static Final getNodeValueAsFinal(final Node node) {
			return getByName(NodeHelper.requireNodeValue(node));
		}

		public static Final getByName(final String name) {
			for (final Final f : values()) {
				if (f.getName().equals(name)) {
					return f;
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

	public String name();

	public String targetNamespace();

	public TypeDefinition baseType();

	public Deque<Final> finals();

	public Node context();

}
