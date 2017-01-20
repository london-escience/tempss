package uk.ac.imperial.libhpc2.tempss.xml;

import java.io.File;
import java.io.IOException;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class TemPSSXMLTemplateProcessorJDom {

	private File _file = null;
	private Document _xml = null;
	
	private static final Logger LOG = 
			LoggerFactory.getLogger(TemPSSXMLTemplateProcessorJDom.class);
	
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
	
		TemPSSXMLTemplateProcessorJDom proc = new TemPSSXMLTemplateProcessorJDom(inputFile);
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
		System.err.println("Usage: TemPSSXMLTemplateProcessorJDom <XML template file>");
	}
	
	public TemPSSXMLTemplateProcessorJDom(File inputFile) {
		this._file = inputFile;
	}

	public boolean parseXML() {
		SAXBuilder parser = new SAXBuilder();
		LOG.info("Parsing XML input file: <" + this._file.getName() + ">");
		try {
			this._xml = parser.build(this._file);
		} catch (JDOMException e) {
			LOG.debug("Parse error for XML file <" + this._file.getName() + 
					">: " + e.getMessage());
			return false;
		} catch (IOException e) {
			LOG.debug("Error reading the XML file <" + this._file.getName() + 
					">: " + e.getMessage());
			return false;
		}
		LOG.info("XML parsed successfully...");
		return true;		
	}
	
	public String getConvertedResult() {
		
		TemPSSSchemaBuilderJDom tsb = new TemPSSSchemaBuilderJDom();
		Document doc = tsb.convertXMLTemplateToSchema(this._xml);
		return tsb.getDocumentAsString(doc);
		
	}
}
