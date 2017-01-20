package uk.ac.imperial.libhpc2.tempss.xml;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * This class is the main entry point for handling the processing of 
 * XML-based TemPSS template descriptions. The XML format is a replacement for  
 * the original XML Schema template structure, although at present, we convert 
 * XML-based templates into the old XML Schema model to ensure backwards 
 * compatibility with existing templates and to avoid the need to rewrite 
 * transforms at this stage.
 *   
 * @author jhc02
 */
public class TemPSSXMLTemplateProcessorJAXP {

	private File _file = null;
	private Document _xml = null;
	
	private static final Logger LOG = 
			LoggerFactory.getLogger(TemPSSXMLTemplateProcessorJAXP.class);
	
	public static void main(String[] args) {
		if(args.length < 1) {
			printUsage(null);
			System.exit(0);
		}
		File inputFile = new File(args[0]);
		if(!inputFile.exists()) {
			printUsage("The specified input file doesn't exist");
			System.exit(10);
		}
		LOG.debug("XML template processor running - input file {}...",args[0]);
	
		TemPSSXMLTemplateProcessorJAXP proc = new TemPSSXMLTemplateProcessorJAXP(inputFile);
		if(!proc.parseXML()) {
			LOG.debug("Unable to parse the XML file <" + args[0] + ">");
			System.exit(0);
		}
		
		String result = proc.getConvertedResult();
		if(result != null) {
			System.out.println(result);
		}
		else {
			LOG.error("Conversion failed, result was null");
		}
	}
	
	private static void printUsage(String pMsg) {
		if(pMsg != null) {
			System.err.println("ERROR: " + pMsg);	
		}
		System.err.println("Usage: TemPSSXMLTemplateProcessor <XML template file>");
	}
	
	public TemPSSXMLTemplateProcessorJAXP(File inputFile) {
		this._file = inputFile;
	}

	public boolean parseXML() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setIgnoringElementContentWhitespace(true);
		factory.setNamespaceAware(true);
		DocumentBuilder db = null;
		try {
			db = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			LOG.debug("Error getting document builder: " + e.getMessage());
			return false;
		}
		
		try {
			LOG.info("Parsing XML input file: <" + this._file.getName() + ">");
			this._xml = db.parse(this._file);
			LOG.info("XML parsed successfully...");
		} catch (SAXException e) {
			LOG.debug("Parse error for XML file <" + this._file.getName() + 
					">: " + e.getMessage());
			return false;
		} catch (IOException e) {
			LOG.debug("Error reading the XML file <" + this._file.getName() + 
					">: " + e.getMessage());
			return false;
		}
		
		return true;		
	}
	
	public String getConvertedResult() {
		TemPSSSchemaBuilderJAXP tsb = null;
		try {
			tsb = new TemPSSSchemaBuilderJAXP();
		} catch (ParserConfigurationException e) {
			LOG.debug("Unable to create schema builder: " + e.getMessage());
			return null;
		}
		
		Document doc = tsb.convertXMLTemplateToSchema(this._xml);
		return tsb.getDocumentAsString(doc);
		
	}
}
