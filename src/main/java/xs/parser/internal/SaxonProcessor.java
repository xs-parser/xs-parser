package xs.parser.internal;

import java.util.*;
import java.util.AbstractMap.*;
import java.util.Map.*;
import java.util.stream.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import org.w3c.dom.*;

/**
 * Processor for XPath and XSLT.
 */
public class SaxonProcessor {

	private static final boolean isSaxonLoaded;

	private static final class SaxonHolder {

		private static final net.sf.saxon.s9api.Processor processor = new net.sf.saxon.s9api.Processor(false);
		private static final net.sf.saxon.s9api.XsltCompiler xsltCompiler = processor.newXsltCompiler();

		@SuppressWarnings("unchecked")
		private static <X extends Exception> net.sf.saxon.s9api.XsltExecutable compileTemplate(final Source source) throws X {
			try {
				return xsltCompiler.compile(source);
			} catch (final net.sf.saxon.s9api.SaxonApiException e) {
				throw (X) e;
			} catch (final NoClassDefFoundError e) {
				return null;
			}
		}

		@SuppressWarnings("unchecked")
		private static <X extends Exception> Document transform(final Object template, final Node node, final Map<String, ?> params, final String templateName) throws X {
			try {
				final net.sf.saxon.s9api.Xslt30Transformer transformer = ((net.sf.saxon.s9api.XsltExecutable) template).load30();
				final net.sf.saxon.s9api.DocumentBuilder builder = processor.newDocumentBuilder();
				final Document out = NodeHelper.newDocument();
				final Map<net.sf.saxon.s9api.QName, ? extends net.sf.saxon.s9api.XdmValue> parameters = params.entrySet().stream()
						.map(e -> {
							final net.sf.saxon.s9api.XdmValue v = e.getValue() instanceof Node
									? net.sf.saxon.s9api.XdmValue.makeValue(builder.wrap(e.getValue()))
									: net.sf.saxon.s9api.XdmAtomicValue.makeAtomicValue(e.getValue());
							return new SimpleImmutableEntry<>(new net.sf.saxon.s9api.QName(e.getKey()), v);
						})
						.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
				if (templateName != null) {
					transformer.setInitialTemplateParameters(parameters, false);
					transformer.callTemplate(new net.sf.saxon.s9api.QName(templateName), new net.sf.saxon.s9api.DOMDestination(out));
				} else {
					transformer.setStylesheetParameters(parameters);
					transformer.transform(new DOMSource(node), new net.sf.saxon.s9api.DOMDestination(out));
				}
				return out;
			} catch (final Exception e) {
				throw (X) e;
			}
		}

	}

	static {
		boolean saxonLoaded = true;
		try {
			Class.forName("net.sf.saxon.s9api.Processor");
		} catch (final ClassNotFoundException e) {
			saxonLoaded = false;
		}
		isSaxonLoaded = saxonLoaded;
	}

	private SaxonProcessor() { }

	public static boolean isSaxonLoaded() {
		return isSaxonLoaded;
	}

	public static Object processor() {
		return SaxonHolder.processor;
	}

	public static Object compileTemplate(final Source source) {
		return isSaxonLoaded ? SaxonHolder.compileTemplate(source) : null;
	}

	public static Document transform(final Object template, final Node node, final Map<String, ?> params, final String templateName) {
		if (isSaxonLoaded) {
			return SaxonHolder.transform(template, node, params, templateName);
		} else {
			throw new UnsupportedOperationException("net.sf.saxon.s9api must be on the classpath to resolve xs:redefine or xs:override");
		}
	}

}