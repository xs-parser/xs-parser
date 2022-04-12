package xs.parser;

import org.w3c.dom.*;
import xs.parser.internal.*;

public class SchemaParseException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final Node node;

	public SchemaParseException(final String message) {
		super(message);
		this.node = null;
	}

	public SchemaParseException(final Node node) {
		super(node != null ? NodeHelper.toString(node) : "");
		this.node = node;
	}

	public SchemaParseException(final Node node, final String message) {
		super((node != null ? (NodeHelper.toString(node) + System.lineSeparator()) : "") + message); // TODO
		this.node = node;
	}

	public SchemaParseException(final Node node, final Throwable cause) {
		super(node != null ? NodeHelper.toString(node) : null, cause);
		this.node = node;
	}

	public SchemaParseException(final Throwable cause) {
		this((String) null, cause);
	}

	public SchemaParseException(final String message, final Throwable cause) {
		super(message, cause);
		this.node = cause instanceof SchemaParseException
				? ((SchemaParseException) cause).node
				: null;
	}

	public SchemaParseException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		this.node = cause instanceof SchemaParseException
				? ((SchemaParseException) cause).node
				: null;
	}

	public Node node() {
		return node;
	}

}