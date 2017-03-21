package uk.ac.imperial.libhpc2.schemaservice.api;

import java.io.InputStream;
import java.util.Map;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ic.prism.jhc02.csp.CSPInitException;
import uk.ac.ic.prism.jhc02.csp.CSPParseException;
import uk.ac.ic.prism.jhc02.csp.CSProblemDefinition;
import uk.ac.imperial.libhpc2.schemaservice.ConstraintsException;
import uk.ac.imperial.libhpc2.schemaservice.TempssObject;
import uk.ac.imperial.libhpc2.schemaservice.UnknownTemplateException;

public class TemplateResourceUtils {

	private static final Logger LOG = LoggerFactory.getLogger(TemplateResourceUtils.class.getName());
	
	@SuppressWarnings("unchecked")
	public static TempssObject getTemplateMetadata(String pTemplateId, ServletContext pContext) 
			throws UnknownTemplateException {
    	// Get the component metadata from the servletcontext and check the name is valid
        Map<String, TempssObject> components = 
        		(Map<String, TempssObject>)pContext.getAttribute("components");

        // If we don't have a template of this name then throw an error, otherwise
        // get the tempss metadata object from the component map and return it.
        if(!components.containsKey(pTemplateId)) {
        	throw new UnknownTemplateException("Template with ID <" + pTemplateId + 
        			"> does not exist.");
        }
        TempssObject metadata = components.get(pTemplateId);
        return metadata;
    }

	public static CSProblemDefinition getConstraintData(String templateId, ServletContext pContext) 
    		throws UnknownTemplateException, ConstraintsException {
		CSProblemDefinition definition = null;
		definition = (CSProblemDefinition)pContext.getAttribute("csproblem-" + templateId);
		if(definition != null) {
			return definition;
		}
		
		TempssObject metadata = getTemplateMetadata(templateId, pContext);
		String constraintFile = metadata.getConstraints();
		if(constraintFile == null) throw new ConstraintsException("There is no constraint file " +
				"configured for this template <" + templateId + ">.");
		
		// Now that we have the name of the constraint file we can create an 
		// instance of a constraint satisfaction problem definition based on this file.
		// The file is loaded as a resource from the jar file.
		InputStream xmlResource = TemplateResourceUtils.class.getClassLoader().getResourceAsStream("META-INF/Constraints/" + constraintFile);
		if(xmlResource == null) {
			LOG.error("Unable to access constraint file <" + constraintFile + "> as resource.");
			throw new ConstraintsException("The constraint XML file could not be accessed.");
		}
		try {
			definition = CSProblemDefinition.fromXML(xmlResource);
			pContext.setAttribute("csproblem-" + templateId, definition);
		} catch (CSPInitException e) {
			LOG.error("Error setting up constraint definition object: " + e.getMessage());
			throw new ConstraintsException("Error setting up constraint definition object: " + e.getMessage(), e);
		} catch (CSPParseException e) {
			LOG.error("Error parsing constraints XML data: " + e.getMessage());
			throw new ConstraintsException("Error parsing constraints XML data.", e);
		}

		return definition;
    }
}
