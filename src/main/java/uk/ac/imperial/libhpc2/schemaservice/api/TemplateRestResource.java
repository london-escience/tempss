/*
 * Copyright (c) 2015, Imperial College London
 * Copyright (c) 2015, The University of Edinburgh
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the names of the copyright holders nor the names of their
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * -----------------------------------------------------------------------------
 *
 * This file is part of the TemPSS - Templates and Profiles for Scientific 
 * Software - service, developed as part of the libhpc projects 
 * (http://www.imperial.ac.uk/lesc/projects/libhpc).
 *
 * We gratefully acknowledge the Engineering and Physical Sciences Research
 * Council (EPSRC) for their support of the projects:
 *   - libhpc: Intelligent Component-based Development of HPC Applications
 *     (EP/I030239/1).
 *   - libhpc Stage II: A Long-term Solution for the Usability, Maintainability
 *     and Sustainability of HPC Software (EP/K038788/1).
 */

package uk.ac.imperial.libhpc2.schemaservice.api;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.transform.TransformerException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import uk.ac.ic.prism.jhc02.csp.CSProblemDefinition;
import uk.ac.ic.prism.jhc02.csp.Constraint;
import uk.ac.imperial.libhpc2.schemaservice.ConstraintsException;
import uk.ac.imperial.libhpc2.schemaservice.SchemaProcessor;
import uk.ac.imperial.libhpc2.schemaservice.TemplateProcessorException;
import uk.ac.imperial.libhpc2.schemaservice.TempssObject;
import uk.ac.imperial.libhpc2.schemaservice.UnknownTemplateException;

/**
 * Jersey REST class representing the template endpoint
 * @author jhc02
 *
 */
@Component
@Path("template")
public class TemplateRestResource {

    /**
     * Logger
     */
    private static final Logger sLog = LoggerFactory.getLogger(TemplateRestResource.class.getName());

    /**
     * ServletContext obejct used to access template data
     * Injected via @Context annotation
     */
    ServletContext _context;

    @Context
    public void setServletContext(ServletContext pContext) {
        this._context = pContext;
        sLog.debug("Servlet context injected: " + pContext);
    }

    @GET
    @Produces("application/json")
    @SuppressWarnings("unchecked")
    public Response listTemplatesJson() {
    	Map<String, TempssObject> components = (Map<String, TempssObject>)_context.getAttribute("components");

    	JSONArray componentList = new JSONArray();
        for(TempssObject component : components.values()) {
        	if(component.ignore()) continue;
            JSONObject componentObj = new JSONObject();
            try {
                componentObj.put("id", component.getId());
                componentObj.put("name", component.getName());
                componentObj.put("schema", component.getSchema());
                componentObj.put("transform", component.getTransform());
            } catch (JSONException e) {
                sLog.error("Unable to add component data <" + component.toString() + "> to JSON object: " + e.getMessage());
                return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Unable to add component data <" + component.toString() + "> to JSON object: " + e.getMessage()).build();
            }

            componentList.put(componentObj);
        }
        JSONObject componentArray = new JSONObject();
        try {
            componentArray.put("components", componentList);
        } catch (JSONException e) {
            sLog.error("Unable to add component array to component object: " + e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Unable to add component array to component object: " + e.getMessage()).build();
        }

        return Response.ok(componentArray.toString(), MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Produces("text/plain")
    @SuppressWarnings("unchecked")
    public Response listTemplatesText() {
        Map<String, TempssObject> components = (Map<String, TempssObject>)_context.getAttribute("components");

        StringBuilder sb = new StringBuilder();
        for(TempssObject component : components.values()) {
        	if(component.ignore()) continue;
            sb.append("[" + component.getId() + ", "
                      + component.getName() + ", "
                      + component.getSchema() + ", "
                      + component.getTransform() + "]\n");
        }
        return Response.ok(sb.toString(), MediaType.TEXT_PLAIN).build();
    }

    @GET
    @Produces("text/plain")
    @Path("names")
    @SuppressWarnings("unchecked")
    public String listTemplatesNames() {
        Map<String, TempssObject> components = (Map<String, TempssObject>)_context.getAttribute("components");

        StringBuilder sb = new StringBuilder();
        for(TempssObject component : components.values()) {
        	if(component.ignore()) continue;
            sb.append(component.getName() + "\n");
        }
        return sb.toString();
    }

    @GET
    @Produces("application/json")
    @Path("names")
    @SuppressWarnings("unchecked")
    public String listTemplatesNamesJson() {
        Map<String, TempssObject> components = (Map<String, TempssObject>)_context.getAttribute("components");

        JSONArray componentNames = new JSONArray();
        for(TempssObject component : components.values()) {
        	if(component.ignore()) continue;
            componentNames.put(component.getName());
        }
        return componentNames.toString();
    }

    @GET
    @Produces("text/plain")
    @Path("ids")
    @SuppressWarnings("unchecked")
    public String listTemplatesIds() {
        Map<String, TempssObject> components = (Map<String, TempssObject>)_context.getAttribute("components");

        StringBuilder sb = new StringBuilder();
        for(TempssObject component : components.values()) {
        	if(component.ignore()) continue;
            sb.append(component.getId() + "\n");
        }
        return sb.toString();
    }

    @GET
    @Produces("application/json")
    @Path("ids")
    @SuppressWarnings("unchecked")
    public String listTemplatesIdsJson() {
        Map<String, TempssObject> components = (Map<String, TempssObject>)_context.getAttribute("components");

        JSONArray componentNames = new JSONArray();
        for(TempssObject component : components.values()) {
        	if(component.ignore()) continue;
            componentNames.put(component.getId());
        }
        return componentNames.toString();
    }

    @GET
    @Produces("text/html")
    @Path("id/{templateId}")
    public Response getTemplatesHtmlTree(@PathParam("templateId") String templateId) {
    	String templateHtml = "";
    	try {
    		// Get the template information from the metadata map
    		TempssObject metadata = TemplateResourceUtils.getTemplateMetadata(templateId, _context);
    		templateHtml = _getTemplateHtml(templateId, metadata);
    	} catch(UnknownTemplateException e) {
    		return Response.status(Status.NOT_FOUND).entity(e.getMessage()).build();
    	} catch(TemplateProcessorException e) {
    		return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    	}
    	
    	return Response.ok(templateHtml, MediaType.TEXT_HTML).build();
    }
    
    @GET
    @Produces("application/json")
    @Path("id/{templateId}")
    public Response getTemplatesHtmlJson(@PathParam("templateId") String templateId,
    		@Context HttpServletRequest request) {
    	String templateHtml = "";
    	TempssObject metadata = null;
    	try {
    		// Get the template information from the metadata map
    		metadata = TemplateResourceUtils.getTemplateMetadata(templateId, _context);
    		templateHtml = _getTemplateHtml(templateId, metadata);
    	} catch(UnknownTemplateException e) {
    		return Response.status(Status.NOT_FOUND).entity(e.getMessage()).build();
    	} catch(TemplateProcessorException e) {
    		return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    	}
    	
    	JSONObject templateObj = new JSONObject();
        try {        	
        	// Keys used for compatibility with old API
            templateObj.put("ComponentName", templateId);
            templateObj.put("TreeHtml", templateHtml);
            templateObj.put("authenticated", !(SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken));
            
            boolean constraints = false;
            // Additional keys for constraint data
            if(metadata.getConstraints() != null) {
            	constraints = true;
            	CSProblemDefinition problem = null;
            	try {
					 problem = TemplateResourceUtils.getConstraintData(templateId, this._context);
					 List<Constraint> constraintList = problem.getConstraints();
					 // Constraint map will be used to build a two way mapping between constraint variable relationships
					 Map<String, Set<String>> constraintMap = new HashMap<String, Set<String>>();
					 for(Constraint c : problem.getConstraints()) {
						 String varName = c.getVariable1FQName();
						 String var2Name = c.getVariable2FQName();
						 if(!constraintMap.containsKey(varName)) {
							 constraintMap.put(varName, new HashSet<String>());
						 }
						 constraintMap.get(varName).add(var2Name);
						 if(!constraintMap.containsKey(var2Name)) {
							 constraintMap.put(var2Name, new HashSet<String>());
						 }
						 constraintMap.get(var2Name).add(varName);
					 }
		            templateObj.put("constraintInfo", constraintMap);
				} catch (UnknownTemplateException | ConstraintsException e) {
					sLog.error("Error accessing constraint information for template <{}>", templateId);
					constraints = false;
				}
            	
            	
            }
            templateObj.put("constraints", constraints);
        } catch (JSONException e) {
            sLog.error("Unable to add template HTML for template <" 
            		+ templateId + "> to JSON object: " + e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(
            		"Unable to add template HTML for template <" + templateId +
            		"> to JSON object: " + e.getMessage()).build();
        }
    	
    	return Response.ok(templateObj.toString(), MediaType.APPLICATION_JSON).build();
    }
    
    private String _getTemplateHtml(String templateId, TempssObject metadata) 
    		throws UnknownTemplateException, TemplateProcessorException {
        // Make a call to the schema processor to transform the template 
        // schema to an HTML tree for display in a web page
        SchemaProcessor proc = new SchemaProcessor(_context);
        String htmlTree = "";
        try {
            htmlTree = proc.processComponentSelector(metadata);
        } catch (FileNotFoundException e) {
            sLog.error("File not found when trying to generate HTML tree: " + e.getMessage());
            throw new TemplateProcessorException("File not found when trying to generate HTML tree: " + e.getMessage());
        } catch (IOException e) {
            sLog.error("IO error when trying to generate HTML tree: " + e.getMessage());
            throw new TemplateProcessorException("IO error when trying to generate HTML tree: " + e.getMessage());
        } catch (ParseException e) {
            sLog.error("XML parse error when trying to generate HTML tree: " + e.getMessage());
            throw new TemplateProcessorException("XML parse error when trying to generate HTML tree: " + e.getMessage());
        } catch (TransformerException e) {
            sLog.error("XSLT transform error when trying to generate HTML tree: " + e.getMessage());
            throw new TemplateProcessorException("XSLT transform error when trying to generate HTML tree: " + e.getMessage());
        }

        return htmlTree;
    }   
}
