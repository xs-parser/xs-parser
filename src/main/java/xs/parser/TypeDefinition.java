package xs.parser;

import java.util.*;
import org.w3c.dom.*;

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

		public static Final getByName(final Node node) {
			return getByName(node.getNodeValue());
		}

		public static Final getByName(final String name) {
			for (final Final f : values()) {
				if (f.getName().equals(name)) {
					return f;
				}
			}
			throw new IllegalArgumentException(name.toString());
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
