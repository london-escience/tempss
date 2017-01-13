package uk.ac.imperial.libhpc2.tempss.xml;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

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
public class TemPSSXMLTemplateProcessor {

	private File _file = null;
	private Document _xml = null;
	
	private static final Logger LOG = 
			LoggerFactory.getLogger(TemPSSXMLTemplateProcessor.class);
	
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
	
		TemPSSXMLTemplateProcessor proc = new TemPSSXMLTemplateProcessor(inputFile);
		proc.parseXML();
	}
	
	private static void printUsage(String pMsg) {
		if(pMsg != null) {
			System.err.println("ERROR: " + pMsg);	
		}
		System.err.println("Usage: TemPSSXMLTemplateProcessor <XML template file>");
	}
	
	public TemPSSXMLTemplateProcessor(File inputFile) {
		this._file = inputFile;
	}

	public void parseXML() {
		// TODO: Create document builder (via factory) and parse the XML 
		// document attached to this instance of the processor.
		
	}
}
