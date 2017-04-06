package uk.ac.imperial.libhpc2.tempss.xml;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.DataConversionException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.Text;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


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
	
	private Map<String, Namespace> _namespaces = new HashMap<String, Namespace>();
	
	public TemPSSSchemaBuilder() {
	
	}
	
	public Document convertXMLTemplateToSchema(Document _template) {
		_schema = new Document();
		// Setup namespaces
		Namespace defaultNS = Namespace.getNamespace("http://www.libhpc.imperial.ac.uk");
		Namespace xsNS =      Namespace.getNamespace("xs", "http://www.w3.org/2001/XMLSchema");
		Namespace xsiNS =     Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		Namespace libhpcNS =  Namespace.getNamespace("libhpc", "http://www.libhpc.imperial.ac.uk/SchemaAnnotation");
		Namespace tempssNS =  Namespace.getNamespace("tempss", "http://www.libhpc.imperial.ac.uk/tempss/SchemaAnnotation");
		_namespaces.put("xs", xsNS);
		_namespaces.put("libhpc", libhpcNS);
		_namespaces.put("tempss", tempssNS);
		
		Element schemaRoot = new Element("schema", xsNS);
		schemaRoot.addNamespaceDeclaration(defaultNS);
		schemaRoot.addNamespaceDeclaration(xsiNS);
		schemaRoot.addNamespaceDeclaration(libhpcNS);
		schemaRoot.setAttribute("schemaLocation", "http://www.libhpc.imperial.ac.uk/SchemaAnnotation LibhpcSchemaAnnotation.xsd", xsiNS);
		schemaRoot.setAttribute("targetNamespace", "http://www.libhpc.imperial.ac.uk");
		schemaRoot.setAttribute("elementFormDefault", "qualified");

		// Add child element to root element to import the nektar types schema
		Element include = new Element("include", xsNS);
		include.setAttribute("schemaLocation", "NektarTypes.xsd");
		schemaRoot.addContent(include);

		// Process incoming XML and convert to schema
		processNodeTopDown(_template.getRootElement(), true, schemaRoot);
		
		_schema.setRootElement(schemaRoot);
		return _schema;
	}

	public String getDocumentAsString(Document pDocument) {
		if(pDocument == null) {
			LOG.error("Please provide a JDom document object, null provided.");
			return null;
		}
		
		Format f = Format.getPrettyFormat();
		// Set this to prevent CTRL-M characters appearing on line endings
		f.setLineSeparator(System.getProperty("line.separator"));
		XMLOutputter outputter = new XMLOutputter(f);
		StringWriter writer = new StringWriter();
		try {
			outputter.output(pDocument, writer);
		} catch (IOException e) {
			LOG.debug("Error carrying out string transform: " + e.getMessage());
			return null;
		}
		
		return writer.toString();
	}
	
	private void processNodeTopDown(Content pNode, boolean pRoot, Element pDocument) { 
		String nodeData = null;
		
		// We don't need to handle empty text nodes so if this is a text node, 
		// see if it has any data
		if(pNode.getCType() == Content.CType.Text) {
			nodeData = ((Text)pNode).getText();
			nodeData = nodeData.trim();
			nodeData = nodeData.replaceAll("\n", "");
			LOG.debug("We have a text node with content <" + nodeData + ">");
			return;
		}

		// If we have a non-text node or a text node that isn't empty...
		// We now only check for Elements - JDOM text nodes have no descendants 
		if(pNode.getCType() == Content.CType.Element) {
			Element e = null;
			Element node = (Element)pNode;
			// If the node has only a documentation child node
			// then its not the start of a new complex type (or simple type) so
			// we just process it as though it was just a leaf node...
			// However, the node will also appear as having a text node so need
			// to check for node length two where the second item is doc node
			/* BEGIN DEBUG OUTPUT */
			if(node.getContent().size() > 0) {
				LOG.debug("Node <{}> has <{}> content nodes.\n",
						node.getName(), node.getContent().size());
				String childNodes = "Child nodes for <" + node.getName() + ">: ";
				for(int i = 0; i < node.getContent().size(); i++) {
					Content n = node.getContent().get(i);
					if(n.getCType() == Content.CType.Element) {
						childNodes += ((Element)n).getName() + "  ";
					}
					else if(n.getCType() == Content.CType.Text) {
						String text = ((Text)n).getText().replace("\n", "NL");
						childNodes += "Text[" + text + "]   ";
					}
				}
				System.err.println(childNodes);
			}
			/* END DEBUG OUTPUT */
			if(node.getContent().size() > 0) {
				e = _processNode(node);
				_printNodeInfo(node, "BEGIN");
				
				Element nextIterElement = null;
				if(e != null) {
					// If the returned node (e) contains a simpleType element then
					// we don't append a complexType element to it - the child 
					// nodes that have led us to this part of the code have already
					// been processed within _processNode for simpleType blocks.
					Element simpleType = e.getChild("simpleType", _namespaces.get("xs"));
					if(simpleType == null) {
						Element complexType = new Element("complexType", _namespaces.get("xs"));
						// We now need to open a sequence or choice block. This
						// depends on whether the current element has a 
						// paramType attribute with the value of choice.
						Element elType = null;
						boolean hasParamType = node.hasAttributes() && (node.getAttribute("paramType") != null);
						String paramType = null;
						if(hasParamType) {
							paramType = node.getAttribute("paramType").getValue();
						}
						
						if( (paramType != null) && paramType.equals("choice") ) {
							elType = new Element("choice", _namespaces.get("xs"));
						}
						else {
							elType = new Element("sequence", _namespaces.get("xs"));
						}
						complexType.addContent(elType);
						e.addContent(complexType);
						nextIterElement = elType;
					}
					else {
						LOG.debug("This node has a simpleType, not appending complexType.");
						nextIterElement = e;
					}
					pDocument.addContent(e);
				}
				/*
				else {
					pDocument.addContent(complexType);
				}
				*/
				
				// If the element currently being processed has an inputType 
				// attribute with a value of "choice" then the sub elements of 
				// this element will already have been processed so we don't 
				// call processTopDown on this element.
				String inputTypeAttr = null;
				if(node.hasAttributes() && (node.getAttribute("inputType") != null)) {
					inputTypeAttr = node.getAttribute("inputType").getValue();
				}
					
				if((inputTypeAttr != null) && (inputTypeAttr.equals("choice"))) {
					_printNodeInfo(pNode, "END");
					return;
				}
				
				List<Content> nl = node.getContent();
				for(int i = 0; i < nl.size(); i++) {
					processNodeTopDown(nl.get(i), false, nextIterElement);
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
				pDocument.addContent(e);
			}
			_printNodeInfo(pNode, "END");
		}
		else {
			//System.out.println("Ignoring empty text node...");
		}
					
	}
		
	private void _printNodeInfo(Content pNode, String msg) {
		switch(pNode.getCType()) {
		case Element:
			if(msg != null) {
				System.err.println(msg + ": Handling ELEMENT node: " + ((Element)pNode).getName());
			}
			else {
				System.err.println("Handling ELEMENT node: " + ((Element)pNode).getName());
			}
			break;
		case Text:
			if(msg != null) {
				System.err.println(msg + ": Handling TEXT node: " + ((Text)pNode).getText());
			}
			else {
				System.err.println("Handling TEXT node: " + ((Text)pNode).getText());
			}
			break;
		default:
			if(msg != null) {
				System.err.println(msg + ": Handling UNKNOWN node of type <" + pNode.getCType() + ">");
			}
			else {
				System.err.println("Handling UNKNOWN node of type <" + pNode.getCType() + ">");
			}
		}
	}
	
	private Element _processNode(Content pNode) {
		Element e = null;
		String name = "";
		
		switch(pNode.getCType()) {
		case Element:
			// If the element has a name attribute, we use this name, otherwise
			// we use the tag name.
			Element node = (Element)pNode;
			name = node.getName();
			if( node.hasAttributes() && (node.getAttribute("name") != null) &&
				!node.getAttribute("name").equals("") ) {
				name = node.getAttribute("name").getValue();
			}
			e = new Element("element", _namespaces.get("xs"));
			e.setAttribute("name", name);
			
			// Now see if we have optional or repeatable tags set to true
			// If so, we add relevant minOccurs and maxOccurs attributes
			if(node.hasAttributes()) {
				Attribute optionalAttr = node.getAttribute("optional");
				Attribute repeatableAttr = node.getAttribute("repeatable");
				boolean optional = false;
				if(optionalAttr != null) {
					try {
						if(optionalAttr.getBooleanValue()) {
							optional = true;
							e.setAttribute("minOccurs", "0");
						}
					} catch (DataConversionException e1) {
						LOG.warn("Ignoring optional attribute for node <" +
							node.getName() + ">. Value couldn't be parsed.");
					}
				}
				if(repeatableAttr != null) {
					if(!optional) {
						e.setAttribute("minOccurs", "1");
					}
					String repeatable = repeatableAttr.getValue();
					int repeatCount = -1;
					try {
						repeatCount = Integer.parseInt(repeatable);
					} catch(NumberFormatException ex) {
						LOG.debug("Couldn't parse int from repeatable param.");
					}
					
					if(repeatable.equals("true")) {
						e.setAttribute("maxOccurs", "unbounded");
					}
					else if(repeatCount > 0) {
						e.setAttribute("maxOccurs", new Integer(repeatCount).toString());
					}
					else {
						LOG.debug("Unexpected value for repeatable, setting maxOccurrs to 1");
						e.setAttribute("maxOccurs", "1");
					}
				}
				else {
					// Don't need to put maxOccurs if we didn't have minOccurs
					if(optionalAttr != null) {
						e.setAttribute("maxOccurs", "1");
					}
				}
			}
			// END processing of optional/repeatable values
			
			// See if we have an inputType attribute and what its value is...
			String inputType = "";
			if(node.hasAttributes() && (node.getAttribute("inputType") != null)) {
				inputType = node.getAttribute("inputType").getValue();
			}
			
			// If the source XML node has a type attribute, we add this to the 
			// new element IF we don't have inputType="choice" set. If we've got
			// an inputType choice, its a simple type and the type attribute 
			// goes onto the restriction sub-element.
			if( node.hasAttributes() && (node.getAttribute("type") != null) &&
				!node.getAttribute("type").equals("") && !inputType.equals("choice")) {
				e.setAttribute("type", node.getAttribute("type").getValue());
			}
			
			// Now check if we have any units provided. Units and documentation
			// are added into new sub-elements libhpc:units and 
			// libhpc:documentation respectively.
			boolean haveUnits = false;
			String units = "";
			if( node.hasAttributes() && (node.getAttribute("units") != null) &&
					!node.getAttribute("units").equals("") ) {
				units = node.getAttribute("units").getValue();
				haveUnits = true;
			}
			
			// Now see if there is a <libhpc:documentation> element as the 
			// child of the element we're currently processing. If so, 
			// we'll add this into the appinfo block along with any units
			// that we need to add.
			boolean haveDoc = false;
			String docStr = "";
			Element doc = node.getChild("documentation", _namespaces.get("libhpc"));
			if(doc != null) {
				docStr = doc.getText();
				haveDoc = true;
				node.removeChild("documentation", _namespaces.get("libhpc"));
			}

			// If we have either units or documentation, add the data
			if(haveUnits || haveDoc) {
				Element annot = new Element("annotation", _namespaces.get("xs"));
				Element appinf = new Element("appinfo", _namespaces.get("xs"));
				if(haveDoc) {
					Element docEl = new Element("documentation", _namespaces.get("libhpc"));
					docEl.setText(docStr);
					appinf.addContent(docEl);
				}
				if(haveUnits) {
					Element unitsEl = new Element("units", _namespaces.get("libhpc"));
					unitsEl.setText(units);
					appinf.addContent(unitsEl);
				}
				annot.addContent(appinf);
				e.addContent(annot);
			}

			// See if we have a "choice" inputType. If we do, 
			// process a choice element and build the simpleType for it.
			if(inputType.equals("choice")) {
				Element simpleType = new Element("simpleType", _namespaces.get("xs"));
				Element restriction = new Element("restriction", _namespaces.get("xs"));
				
				String typeAttr = null;
				if( node.hasAttributes() && (node.getAttribute("type") != null)
						&& !node.getAttribute("type").equals("") ) {
					typeAttr = node.getAttribute("type").getValue();
				}
				if(typeAttr == null) {
					typeAttr = "string";
				}
				restriction.setAttribute("base", typeAttr, _namespaces.get("ns"));
				
				// Now we need to look for the <libhpc:item> child elements
				// and add the values as xs:enumeration items.
				List<Element> childNodes = node.getChildren("item", _namespaces.get("tempss"));
				for(int i = 0; i < childNodes.size(); i++) {
					Element item = childNodes.get(i);
					Element val = new Element("enumeration", _namespaces.get("xs"));
					val.setAttribute("value", item.getText());
					restriction.addContent(val);
				}
				simpleType.addContent(restriction);				
				e.addContent(simpleType);
			}

			return e;
		case Text:
			LOG.debug("Processing text node...");
			name = ((Text)pNode).getText();
			e = new Element("element", _namespaces.get("xs"));
			e.setAttribute("name", name);
			return e;
		default:
			return null;
		}
	}
}
