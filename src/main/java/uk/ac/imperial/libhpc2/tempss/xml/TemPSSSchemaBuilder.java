package uk.ac.imperial.libhpc2.tempss.xml;

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * This class builds a TemPSS-compatible schema, for conversion to an HTML 
 * template tree by TemPSS' existing API, from an XML description of a template 
 * using the new TemPSS template definition format. It takes a parsed XML 
 * document and outputs an XML schema as a new Document object.
 *  
 * @author jhc02
 */
public class TemPSSSchemaBuilder {

	private static final Logger LOG = LoggerFactory.getLogger(
										TemPSSSchemaBuilder.class.getName());
	private Document _schema = null;
	
	public TemPSSSchemaBuilder() throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		_schema = builder.newDocument();
	}
	
	public Document convertXMLTemplateToSchema(Document _template) {
		Element schemaRoot = _schema.createElement("xs:schema");
		schemaRoot.setAttribute("xmlns", "http://www.libhpc.imperial.ac.uk");
		schemaRoot.setAttribute("targetNamespace", "http://www.libhpc.imperial.ac.uk");
		schemaRoot.setAttribute("elementFormDefault", "qualified");
		schemaRoot.setAttribute("xmlns:xs", "http://www.w3.org/2001/XMLSchema");
		schemaRoot.setAttribute("xmlns:libhpc", "http://www.libhpc.imperial.ac.uk/SchemaAnnotation");
		schemaRoot.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		schemaRoot.setAttribute("xsi:schemaLocation", "http://www.libhpc.imperial.ac.uk/SchemaAnnotation LibhpcSchemaAnnotation.xsd");

		_schema.appendChild(schemaRoot);
		return _schema;
	}
	
	public String getDocumentAsString(Document pDocument) {
		if(pDocument == null) {
			LOG.error("Please provide a document object, null provided.");
			return null;
		}
		
		DOMSource src = new DOMSource(pDocument);
		StreamResult res = new StreamResult();
		
		TransformerFactory tf = TransformerFactory.newInstance();
		try {
			Transformer t = tf.newTransformer();
			t.transform(src, res);
		} catch (TransformerConfigurationException e) {
			LOG.debug("Error setting up transformer for String conversion: "
					+ e.getMessage());
			return null;
		} catch (TransformerException e) {
			LOG.debug("Error carrying out string transform: " + e.getMessage());
			return null;
		}
		
		StringWriter writer = new StringWriter();
		res.setWriter(writer);
		writer.flush();
		
		return writer.toString();
	}
}
