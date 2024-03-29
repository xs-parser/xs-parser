package xs.parser;

import java.io.*;
import java.net.*;
import java.nio.charset.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import xs.parser.Schema.*;
import xs.parser.internal.util.*;

public final class Utilities {

	public static final String PROLOG_UTF8 = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";

	private Utilities() { }

	public static DocumentResolver stringResolver(final String content) {
		return new DefaultDocumentResolver() {

			@Override
			public Document resolve(final URI resourceUri) throws Exception {
				final StringReader reader = new StringReader(content);
				final InputSource source = new InputSource(reader);
				source.setSystemId(resourceUri.toString());
				return NodeHelper.newDocumentBuilder().parse(source);
			}

		};
	}

	public static Schema stringToSchema(final String content) throws IOException, SAXException {
		try (final ByteArrayInputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
			return new Schema(NodeHelper.newDocumentBuilder().parse(new InputSource(stream)));
		}
	}

	public static Document stringToDocument(final String content) throws IOException, SAXException {
		return NodeHelper.newDocumentBuilder().parse(new InputSource(new StringReader(content)));
	}

}
