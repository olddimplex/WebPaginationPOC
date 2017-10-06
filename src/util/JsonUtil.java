package util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import filter.RequestEnrichmentFilter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Processor;

public final class JsonUtil {

	private static final Logger LOGGER = LogManager.getLogger(JsonUtil.class);

	/** XML root element and entity declarations */
	public static final String DOCUMENT_PREFIX = "<?xml version=\"1.0\" encoding=\"" + RequestEnrichmentFilter.CHARACTER_ENCODING + "\"?><!DOCTYPE wrapper ["
			+ "<!ENTITY nbsp \"&#160;\">"
			+ "<!ENTITY times \"&#215;\">" + "]><wrapper>";

	/** XML root element closing tag */
	public static final String DOCUMENT_SUFFIX = "</wrapper>";

	private static final byte[] DOCUMENT_PREFIX_BYTES = DOCUMENT_PREFIX.getBytes();
	private static final byte[] DOCUMENT_SUFFIX_BYTES = DOCUMENT_SUFFIX.getBytes();
	private static final Templates TEMPLATES_JSONML;

	static {
		// ServiceLoader<TransformerFactory> factoryLoader =
		// ServiceLoader.load(TransformerFactory.class);
		// for(TransformerFactory factory : factoryLoader) {
		// LOGGER.info(factory.getClass().getCanonicalName());
		// }
		Templates jsonMLTemplates = null;
		try (
			final InputStream is = 
				Thread.currentThread().getContextClassLoader().getResourceAsStream("xhtml_to_json.saxon.xsl");
			//  Thread.currentThread().getContextClassLoader().getResourceAsStream("xhtml_to_json.xalan.xsl")
		) {
			// Implementation is important because of the way Java methods are
			// referenced in the XSLT
			final TransformerFactory transformerFactory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", Thread.currentThread().getContextClassLoader());
			final Configuration configuration = ((net.sf.saxon.TransformerFactoryImpl) transformerFactory).getConfiguration();
			final Processor processor = (Processor) configuration.getProcessor();
			processor.registerExtensionFunction(new JsonUtilExtensionFunction());

			jsonMLTemplates =
				// TransformerFactory.newInstance("org.apache.xalan.processor.TransformerFactoryImpl", null)
				// Note saxon9he (free version) does not support calling external Java methods in XSLTs
				// see also: http://saxonica.com/documentation/index.html#!extensibility
				transformerFactory.newTemplates(new StreamSource(is));
		} catch (final Exception e) {
			LOGGER.error("Failed to initialize JSONML templates object", e);
		} finally {
			TEMPLATES_JSONML = jsonMLTemplates;
		}
	}

	private static final Gson GSON = new Gson();
	private static final Gson GSON_WITH_DATE_FORMAT = new GsonBuilder().setDateFormat("dd-MM-yyyy").create();
	private static final Gson GSON_NO_HTML_ESCAPING = new GsonBuilder().disableHtmlEscaping().create();

	private JsonUtil() {
	}

	public static <T> String toJson(final T toSerialize) {
		return GSON.toJson(toSerialize);
	}

	public static <T> String toJsonWithDateFormat(final T toSerialize) {
		return GSON_WITH_DATE_FORMAT.toJson(toSerialize);
	}

	public static <T> String toJsonNoHtmlEscaping(final T toSerialize) {
		return GSON_NO_HTML_ESCAPING.toJson(toSerialize);
	}

	/**
	 * Transforms arbitrary XML to the JSONML format, see:
	 * http://www.jsonml.org/<br/>
	 * A root element is added and expected entities are declared.
	 *
	 * @param is
	 * @param os
	 */
	public static void toJsonML(final InputStream is, final OutputStream os) {
		try {
			final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setIgnoringComments(true);
			dbf.setIgnoringElementContentWhitespace(true);
			dbf.setValidating(false);

			// https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Prevention_Cheat_Sheet
			dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
			dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			dbf.setXIncludeAware(false);
			
			final DocumentBuilder db = dbf.newDocumentBuilder();
			// db.setEntityResolver(new Resolver());
			final Document doc = db.parse(
				new SequenceInputStream(new InputStreamEnumeration(new ByteArrayInputStream(DOCUMENT_PREFIX_BYTES), is, new ByteArrayInputStream(DOCUMENT_SUFFIX_BYTES))));
			TEMPLATES_JSONML.newTransformer().transform(new DOMSource(doc), new StreamResult(os));

		} catch (final Exception e) {
			LOGGER.error("JSONML transformation failed", e);
		}
	}

	/**
	 * Utility method to deal with null data.
	 * 
	 * @param bytes
	 * @param os
	 * @see {@link #toJsonML(InputStream, OutputStream)}
	 */
	public static void toJsonML(final byte[] bytes, final OutputStream os) {
		if(bytes != null) {
			toJsonML(new ByteArrayInputStream(bytes), os);
		}
	}

}
