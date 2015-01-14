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
 * This file is part of the TemPro template and profile service, developed as
 * part of the libhpc projects (http://www.imperial.ac.uk/lesc/projects/libhpc).
 *
 * We gratefully acknowledge the Engineering and Physical Sciences Research
 * Council (EPSRC) for their support of the projects:
 *   - libhpc: Intelligent Component-based Development of HPC Applications
 *     (EP/I030239/1).
 *   - libhpc Stage II: A Long-term Solution for the Usability, Maintainability
 *     and Sustainability of HPC Software (EP/K038788/1).
 */

package uk.ac.imperial.libhpc2.schemaservice.api;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
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

import uk.ac.imperial.libhpc2.schemaservice.SchemaProcessor;
import uk.ac.imperial.libhpc2.schemaservice.TemproObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Jersey REST class representing the template endpoint
 * @author jhc02
 *
 */
@Path("template")
public class TemplateRestResource {

    /**
     * Logger
     */
    private static final Logger sLog = Logger.getLogger(TemplateRestResource.class.getName());

    /**
     * ServletContext obejct used to access template data
     * Injected via @Context annotation
     */
    ServletContext _context;



    @Context
    public void setServletContext(ServletContext pContext) {
        this._context = pContext;
        sLog.fine("Servlet context injected: " + pContext);
    }

    @GET
    @Produces("application/json")
    @SuppressWarnings("unchecked")
    public Response listTemplatesJson() {
        Map<String, TemproObject> components = (Map<String, TemproObject>)_context.getAttribute("components");

        JSONArray componentList = new JSONArray();
        for(TemproObject component : components.values()) {
            JSONObject componentObj = new JSONObject();
            try {
                componentObj.put("id", component.getId());
                componentObj.put("name", component.getName());
                componentObj.put("schema", component.getSchema());
                componentObj.put("transform", component.getTransform());
            } catch (JSONException e) {
                sLog.severe("Unable to add component data <" + component.toString() + "> to JSON object: " + e.getMessage());
                return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Unable to add component data <" + component.toString() + "> to JSON object: " + e.getMessage()).build();
            }

            componentList.put(componentObj);
        }
        JSONObject componentArray = new JSONObject();
        try {
            componentArray.put("components", componentList);
        } catch (JSONException e) {
            sLog.severe("Unable to add component array to component object: " + e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Unable to add component array to component object: " + e.getMessage()).build();
        }

        return Response.ok(componentArray.toString(), MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Produces("text/plain")
    @SuppressWarnings("unchecked")
    public Response listTemplatesText() {
        Map<String, TemproObject> components = (Map<String, TemproObject>)_context.getAttribute("components");

        StringBuilder sb = new StringBuilder();
        for(TemproObject component : components.values()) {
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
        Map<String, TemproObject> components = (Map<String, TemproObject>)_context.getAttribute("components");

        StringBuilder sb = new StringBuilder();
        for(TemproObject component : components.values()) {
            sb.append(component.getName() + "\n");
        }
        return sb.toString();
    }

    @GET
    @Produces("application/json")
    @Path("names")
    @SuppressWarnings("unchecked")
    public String listTemplatesNamesJson() {
        Map<String, TemproObject> components = (Map<String, TemproObject>)_context.getAttribute("components");

        JSONArray componentNames = new JSONArray();
        for(TemproObject component : components.values()) {
            componentNames.put(component.getName());
        }
        return componentNames.toString();
    }

    @GET
    @Produces("text/plain")
    @Path("ids")
    @SuppressWarnings("unchecked")
    public String listTemplatesIds() {
        Map<String, TemproObject> components = (Map<String, TemproObject>)_context.getAttribute("components");

        StringBuilder sb = new StringBuilder();
        for(TemproObject component : components.values()) {
            sb.append(component.getId() + "\n");
        }
        return sb.toString();
    }

    @GET
    @Produces("application/json")
    @Path("ids")
    @SuppressWarnings("unchecked")
    public String listTemplatesIdsJson() {
        Map<String, TemproObject> components = (Map<String, TemproObject>)_context.getAttribute("components");

        JSONArray componentNames = new JSONArray();
        for(TemproObject component : components.values()) {
            componentNames.put(component.getId());
        }
        return componentNames.toString();
    }

    @GET
    @Produces("text/html")
    @Path("id/{templateId}")
    @SuppressWarnings("unchecked")
    public Response getTemplatesHtmlTree(@PathParam("templateId") String templateId) {
        // Get the component metadata from the servletcontext and check the name is valid
        Map<String, TemproObject> components = (Map<String, TemproObject>)_context.getAttribute("components");

        // If we don't have a template of this name then throw an error
        if(!components.containsKey(templateId)) {
            return Response.status(Status.NOT_FOUND).entity("Template with ID <" + templateId + "> does not exist.").build();
        }

        // Get the template information from the metadata map
        // and make a call to the schema processor to transform
        // the template schema to an HTML tree for display in
        // a web page
        SchemaProcessor proc = new SchemaProcessor(_context);
        TemproObject metadata = components.get(templateId);
        String htmlTree = "";
        try {
            htmlTree = proc.processComponentSelector(metadata);
        } catch (FileNotFoundException e) {
            sLog.severe("File not found when trying to generate HTML tree: " + e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("File not found when trying to generate HTML tree: " + e.getMessage()).build();
        } catch (IOException e) {
            sLog.severe("IO error when trying to generate HTML tree: " + e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("IO error when trying to generate HTML tree: " + e.getMessage()).build();
        } catch (ParseException e) {
            sLog.severe("XML parse error when trying to generate HTML tree: " + e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("XML parse error when trying to generate HTML tree: " + e.getMessage()).build();
        } catch (TransformerException e) {
            sLog.severe("XSLT transform error when trying to generate HTML tree: " + e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("XSLT transform error when trying to generate HTML tree: " + e.getMessage()).build();
        }

        return Response.ok(htmlTree, MediaType.TEXT_HTML).build();
    }
}
