package xs.parser.internal;

import java.util.*;
import java.util.AbstractMap.*;
import java.util.Map.*;
import java.util.stream.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import net.sf.saxon.s9api.*;
import org.w3c.dom.*;
import xs.parser.internal.util.*;

/**
 * Processor for XPath and XSLT
 */
public final class SaxonProcessor {

	private static final class SaxonHolder {

		private static final Processor processor = new Processor(false);
		private static final XsltCompiler xsltCompiler = processor.newXsltCompiler();

		private SaxonHolder() { }

		private static XsltExecutable compileTemplate(final Source source, final String name) {
			try {
				return xsltCompiler.compile(source);
			} catch (final SaxonApiException e) {
				Reporting.report("Failed to compile template " + name, e);
			} catch (final NoClassDefFoundError e) {
				Reporting.report(SAXON_S9API_PACKAGE + " must be on the classpath to compile template " + name, e);
			}
			return null;
		}

		private static Document transform(final Object template, final Node node, final Map<String, ?> params, final String templateName) {
			try {
				final Xslt30Transformer transformer = ((XsltExecutable) template).load30();
				final DocumentBuilder builder = processor.newDocumentBuilder();
				final Document out = NodeHelper.newDocument();
				final Map<QName, ? extends XdmValue> parameters = params.entrySet().stream()
						.map(e -> {
							final XdmValue v = e.getValue() instanceof Node
									? XdmValue.makeValue(builder.wrap(e.getValue()))
									: XdmAtomicValue.makeAtomicValue(e.getValue());
							return new SimpleImmutableEntry<>(new QName(e.getKey()), v);
						})
						.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
				if (templateName != null) {
					transformer.setInitialTemplateParameters(parameters, false);
					transformer.callTemplate(new QName(templateName), new DOMDestination(out));
				} else {
					transformer.setStylesheetParameters(parameters);
					transformer.transform(new DOMSource(node), new DOMDestination(out));
				}
				return out;
			} catch (final Exception e) {
				throw new SaxonApiUncheckedException(e);
			}
		}

	}

	private static final String SAXON_S9API_PACKAGE = "net.sf.saxon.s9api";
	public static final boolean IS_SAXON_LOADED;

	static {
		boolean isSaxonLoaded = true;
		try {
			Class.forName(SAXON_S9API_PACKAGE + ".Processor");
		} catch (final ClassNotFoundException e) {
			isSaxonLoaded = false;
		}
		IS_SAXON_LOADED = isSaxonLoaded;
	}

	private SaxonProcessor() { }

	public static Object processor() {
		return SaxonHolder.processor;
	}

	public static Object compileTemplate(final Source source, final String name) {
		return IS_SAXON_LOADED ? SaxonHolder.compileTemplate(source, name) : null;
	}

	public static Document transform(final Object template, final Node node, final Map<String, ?> params, final String templateName, final String operation) {
		if (template != null) {
			return SaxonHolder.transform(template, node, params, templateName);
		} else {
			throw new UnsupportedOperationException("cannot perform " + operation + " due to a previous template compilation failure");
		}
	}

}
