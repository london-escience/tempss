package uk.ac.imperial.libhpc2.tempss.xml;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public class TemPSSXMLTemplateProcessor {

	private File _file = null;
	private String _inputStr = null;
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
		
		// Run a test to see if the provided file is a schema file or not...
		/*
		String templateContent = null;
		FileInputStream fis;
		try {
			fis = new FileInputStream(inputFile);
			InputStreamReader isr = new InputStreamReader(fis);
			char[] buffer = new char[65535];
			int charsRead = isr.read(buffer);
			LOG.debug("Read <" + charsRead + "> characters.");
			StringWriter sw = new StringWriter();
			sw.write(buffer, 0, charsRead);
			sw.flush();
			templateContent = sw.toString();
			LOG.debug("Length of template content string: <" + templateContent.length() + ">");
		} catch (FileNotFoundException e) {
			LOG.debug("File was not found: {}", e.getMessage());
		} catch (IOException e) {
			LOG.debug("IO error reading template data from file: {}", e.getMessage());		
		}
		
		boolean isXMLTemplate = isXMLTemplate(templateContent);
		LOG.debug("Result of isXMLTemplate: <" + new Boolean(isXMLTemplate).toString() + ">");
		*/

		LOG.debug("XML template processor running - input file {}...",args[0]);
	
		TemPSSXMLTemplateProcessor proc = new TemPSSXMLTemplateProcessor(inputFile);
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
	
	/**
	 * Given a string representing a TemPSS schema definition, this method 
	 * does some checks to try and determine whether the provided data is a 
	 * schema in the new XML format.
	 * 
	 * @param pTemplateContent The template definition data to check
	 * @return <code>true</code> if the provided data is likely to be an XML 
	 *         template definition, <code>false</code> otherwise.
	 */
	public static boolean isXMLTemplate(String pTemplateContent) {
		// Look for an XML header, all template files, schema or standard xml
		// should have this.
		Pattern xml_header = Pattern.compile("<?xml .*?>");
		Matcher matcher = xml_header.matcher(pTemplateContent);
		int numMatches = 0;
		while(matcher.find()) {
			numMatches++;
		}
		if(numMatches == 0) return false;
		
		LOG.debug("Checking provided template data: We have an XML header...");
		
		// Now see if we have an xs:schema tag which tells us we have a schema
		// rather than an XML template. If we find this, return false.
		Pattern schema_header = Pattern.compile("<x[a-zA-Z0-9]*:schema .*");
		matcher = schema_header.matcher(pTemplateContent);
		numMatches = 0;
		while(matcher.find()) {
			numMatches++;
		}
		if(numMatches == 0) {
			// There's no schema header but there was an XML header so assume 
			// that this is an XML template.
			return true;
		}
	
		return false;
	}
	
	public TemPSSXMLTemplateProcessor(File inputFile) {
		this._file = inputFile;
	}
	
	public TemPSSXMLTemplateProcessor(String inputString) {
		this._inputStr = inputString;
	}

	public boolean parseXML() {
		SAXBuilder parser = new SAXBuilder();
		try {
			// If the class instance was created with a String rather than file
			if(this._inputStr != null) {
				LOG.info("Parsing XML input as string...");
				StringReader sr = new StringReader(this._inputStr);
				this._xml = parser.build(sr);
			}
			else {
				LOG.info("Parsing XML input file: <" + this._file.getName() + ">");
				this._xml = parser.build(this._file);
			}
		} catch (JDOMException e) {
			if(this._inputStr != null) {
				LOG.debug("Parse error for XML file: " + e.getMessage());	
			}
			else {
				LOG.debug("Parse error for XML file <" + this._file.getName() +
						">: " + e.getMessage());
			}
			return false;
		} catch (IOException e) {
			if(this._inputStr != null) {
				LOG.debug("Error reading the XML file: " + e.getMessage()); 	
			}
			else {
				LOG.debug("Error reading the XML file <" + this._file.getName() + 
						">: " + e.getMessage());
			}
			
			return false;
		}
		LOG.info("XML parsed successfully...");
		return true;		
	}
	
	public String getConvertedResult() {
		
		TemPSSSchemaBuilder tsb = new TemPSSSchemaBuilder();
		Document doc = tsb.convertXMLTemplateToSchema(this._xml);
		return tsb.getDocumentAsString(doc);
		
	}
}
