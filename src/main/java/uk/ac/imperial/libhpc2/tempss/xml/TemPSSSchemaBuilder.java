package uk.ac.imperial.libhpc2.tempss.xml;

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


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

		//processNodeBottomUp(_template.getDocumentElement(), true, schemaRoot);
		processNodeTopDown(_template.getDocumentElement(), true, schemaRoot);
		
		_schema.appendChild(schemaRoot);
		return _schema;
	}
	
	public String getDocumentAsString(Document pDocument) {
		if(pDocument == null) {
			LOG.error("Please provide a document object, null provided.");
			return null;
		}
		
		DOMSource src = new DOMSource(pDocument);
		StringWriter writer = new StringWriter();
		StreamResult res = new StreamResult(writer);
		
		TransformerFactory tf = TransformerFactory.newInstance();
		tf.setAttribute("indent-number", 2);
		try {
			Transformer t = tf.newTransformer();
			t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.transform(src, res);
		} catch (TransformerConfigurationException e) {
			LOG.debug("Error setting up transformer for String conversion: "
					+ e.getMessage());
			return null;
		} catch (TransformerException e) {
			LOG.debug("Error carrying out string transform: " + e.getMessage());
			return null;
		}
		
		return writer.toString();
	}
	
	/*
	private void processNodeBottomUp(Node pNode, boolean pRoot, Node pDocument) {
		if(pNode.hasChildNodes()) {
			// Make a check to see if this node has a single empty child text  
			// node. If it does then we ignore the child node.
			NodeList nl = pNode.getChildNodes();
			for(int i = 0; i < nl.getLength(); i++) {
				processNodeBottomUp(nl.item(i), false, pDocument);
			}
			_printNodeInfo(pNode);
		}
		else {
			short nodeType = pNode.getNodeType();
			String nodeData = null;
			if(nodeType == Node.TEXT_NODE) {
				nodeData = ((Text)pNode).getData();
				nodeData = nodeData.trim();
				nodeData = nodeData.replaceAll("\n", "");
			}
			if(nodeData == null || nodeData.length() != 0) {
				_printNodeInfo(pNode);
			}
			else {
				// System.out.println("Ignoring empty text node...");
			}
		}
	}
	*/
	
	private void processNodeTopDown(Node pNode, boolean pRoot, Node pDocument) {
		short nodeType = pNode.getNodeType();
		String nodeData = null;
		
		// We don't need to handle empty text nodes so if this is a text node, 
		// see if it has any data
		if(nodeType == Node.TEXT_NODE) {
			nodeData = ((Text)pNode).getData();
			nodeData = nodeData.trim();
			nodeData = nodeData.replaceAll("\n", "");
		}
		// If we have a non-text node or a text node that isn't empty...
		if(nodeData == null || nodeData.length() != 0) {
			Element e = null;
			if(pNode.hasChildNodes()) {
				e = _processNode(pNode);
				_printNodeInfo(pNode, "BEGIN");
				
				Element nextIterElement = null;
				if(e != null) {
					// If the returned node (e) contains a simpleType element then
					// we don't append a complexType element to it - the child 
					// nodes that have led us to this part of the code have already
					// been processed within _processNode for simpleType blocks.
					if(e.getElementsByTagName("xs:simpleType").getLength() == 0) {
						Element complexType = _schema.createElement("xs:complexType");
						Element seq = _schema.createElement("xs:sequence");
						complexType.appendChild(seq);
						e.appendChild(complexType);
						nextIterElement = seq;
					}
					else {
						LOG.debug("This node has a simpleType, not appending complexType.");
						nextIterElement = e;
					}
					pDocument.appendChild(e);
				}
				/*
				else {
					pDocument.appendChild(complexType);
				}
				*/
				
				// If the element currently being processed has an inputType 
				// attribute with a value of "choice" then the sub elements of 
				// this element will already have been processed so we don't 
				// call processTopDown on this element.
				String inputTypeAttr = ((Element)pNode).getAttribute("inputType");
				if((inputTypeAttr != null) && (inputTypeAttr.equals("choice"))) {
					_printNodeInfo(pNode, "END");
					return;
				}
				
				NodeList nl = pNode.getChildNodes();
				for(int i = 0; i < nl.getLength(); i++) {
					processNodeTopDown(nl.item(i), false, nextIterElement);
					/*
					if(e != null) {
						processNodeTopDown(nl.item(i), false, e);
					}
					else {
						processNodeTopDown(nl.item(i), false, pDocument);
					}
					*/
				}
			}
			else {
				e = _processNode(pNode);
				_printNodeInfo(pNode, "BEGIN");
								
				pDocument.appendChild(e);
			}
			_printNodeInfo(pNode, "END");
		}
		else {
			//System.out.println("Ignoring empty text node...");
		}
					
	}
		
	private void _printNodeInfo(Node pNode, String msg) {
		switch(pNode.getNodeType()) {
		case Node.ELEMENT_NODE:
			if(msg != null) {
				System.out.println(msg + ": Handling ELEMENT node: " + ((Element)pNode).getTagName());
			}
			else {
				System.out.println("Handling ELEMENT node: " + ((Element)pNode).getTagName());
			}
			break;
		case Node.TEXT_NODE:
			if(msg != null) {
				System.out.println(msg + ": Handling TEXT node: " + ((Text)pNode).getData());
			}
			else {
				System.out.println("Handling TEXT node: " + ((Text)pNode).getData());
			}
			break;
		default:
			if(msg != null) {
				System.out.println(msg + ": Handling UNKNOWN node of type <" + pNode.getNodeType() + ">");
			}
			else {
				System.out.println("Handling UNKNOWN node of type <" + pNode.getNodeType() + ">");
			}
		}
	}
	
	private Element _processNode(Node pNode) {
		Element e = null;
		String name = "";
		
		switch(pNode.getNodeType()) {
		case Node.ELEMENT_NODE:
			// If the element has a name attribute, we use this name, otherwise
			// we use the tag name.
			name = ((Element)pNode).getTagName();
			if( (((Element)pNode).getAttribute("name") != null) &&
				!((Element)pNode).getAttribute("name").equals("") ) {
				name = ((Element)pNode).getAttribute("name");
			}
			e = _schema.createElement("xs:element");
			e.setAttribute("name", name);
			
			// If the source XML node has a type attribute, we add this to the 
			// new element
			if( (((Element)pNode).getAttribute("type") != null) &&
				!((Element)pNode).getAttribute("type").equals("") ) {
				e.setAttribute("type", ((Element)pNode).getAttribute("type"));
			}
			
			// Now check if we have any units provided. Units and documentation
			// are added into new sub-elements libhpc:units and 
			// libhpc:documentation respectively.
			if( (((Element)pNode).getAttribute("units") != null) &&
					!((Element)pNode).getAttribute("units").equals("") ) {
				
				String units = ((Element)pNode).getAttribute("units");
				Element annot = _schema.createElement("xs:annotation");
				Element appinf = _schema.createElement("xs:appinfo");
				Element unitsEl = _schema.createElement("libhpc:units");
				Text unitsText = _schema.createTextNode(units);
				e.appendChild(annot).appendChild(appinf).appendChild(unitsEl).appendChild(unitsText);
			}
									
			String inputTypeAttr = ((Element)pNode).getAttribute("inputType");
			// Process a choice element and build the simpleType for it.
			if((inputTypeAttr != null) && (inputTypeAttr.equals("choice"))) {
				Element simpleType = _schema.createElement("xs:simpleType");
				Element restriction = _schema.createElement("xs:restriction");
				
				String typeAttr = ((Element)pNode).getAttribute("type");
				if( (typeAttr == null) || (typeAttr.equals("")) ) {
					typeAttr = "xs:string";
				}
				restriction.setAttribute("base", typeAttr);
				
				// Now we need to look for the <libhpc:item> child elements
				// and add the values as xs:enumeration items.
				NodeList childNodes = pNode.getChildNodes();
				for(int i = 0; i < childNodes.getLength(); i++) {
					if(childNodes.item(i).getNodeType() != Node.ELEMENT_NODE) {
						continue;
					}
					Element item = (Element)childNodes.item(i);
					if(item.getTagName() == "tempss:item") {
						String data = ((Text)item.getFirstChild()).getData();
						Element val = _schema.createElement("xs:enumeration");
						val.setAttribute("value", data);
						restriction.appendChild(val);
					}
				}
				
				simpleType.appendChild(restriction);				
				e.appendChild(simpleType);
			}
			
			return e;
		case Node.TEXT_NODE:
			LOG.debug("Processing text node...");
			name = ((Text)pNode).getData();
			e = _schema.createElement("xs:element");
			e.setAttribute("name", name);
			return e;
		default:
			return null;
		}
	}
	
	private void _printNodeInfo(Node pNode) {
		switch(pNode.getNodeType()) {
		case Node.ELEMENT_NODE:
			System.out.println("Handling ELEMENT node: " + ((Element)pNode).getTagName());
			break;
		case Node.TEXT_NODE:
			System.out.println("Handling TEXT node: " + ((Text)pNode).getData());
			break;
		default:
			System.out.println("Handling UNKNOWN node of type <" + pNode.getNodeType() + ">");
		}
	}
}
