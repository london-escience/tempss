package uk.ac.imperial.libhpc2.schemaservice.api;

import java.util.Map;

import javax.servlet.ServletContext;

import uk.ac.imperial.libhpc2.schemaservice.TempssObject;
import uk.ac.imperial.libhpc2.schemaservice.UnknownTemplateException;

public class TemplateResourceUtils {

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

}
