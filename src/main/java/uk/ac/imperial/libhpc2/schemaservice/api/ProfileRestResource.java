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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.FormDataParam;

import uk.ac.imperial.libhpc2.schemaservice.SchemaProcessor;
import uk.ac.imperial.libhpc2.schemaservice.TemproObject;
import uk.ac.imperial.libhpc2.schemaservice.UnknownTemplateException;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Jersey REST class representing the profile endpoint
 */
@Path("profile")
public class ProfileRestResource {

    /**
     * Logger
     */
    private static final Logger sLog = Logger.getLogger(ProfileRestResource.class.getName());
	
    /**
     * ServletContext obejct used to access profile metadata
     * Injected via @Context annotation
     */
    ServletContext _context;
	
	
	
    @Context
    public void setServletContext(ServletContext pContext) {
        this._context = pContext;
        sLog.fine("Servlet context injected: " + pContext);
    }
	
    /**
     * Convert the provided profile data to an input file using
     * the transform stored in the template metadata. 
     * @param templateId
     * @return
     */
    @POST
    @Path("{templateId}/convert")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json")
    @SuppressWarnings("unchecked")
    public Response convertProfileToInputData(
        @PathParam("templateId") String templateId,
        @Context HttpServletRequest pRequest,
        FormDataMultiPart multipartData) {
		
        HttpSession session = pRequest.getSession();
        SchemaProcessor proc = new SchemaProcessor(_context);
		
        // Get the component metadata from the servletcontext and check the name is valid
        Map<String, TemproObject> components = (Map<String, TemproObject>)_context.getAttribute("components");

        JSONObject jsonResponse = new JSONObject();
		
        // If we don't have a template of this name then throw an error
        if(!components.containsKey(templateId)) {
            try {
                jsonResponse.put("status", "ERROR");
                jsonResponse.put("message", "Template with ID <" + templateId + "> does not exist.");
                return Response.status(Status.NOT_FOUND).entity(jsonResponse.toString()).build();
            } catch (JSONException e) {
                sLog.severe("Error creating 404 response for template ID that is not found");
                return Response.status(Status.INTERNAL_SERVER_ERROR).entity("{\"status\":\"ERROR\"}").build();
            }
        }

        // Get the profile file data and list of all additional files provided as input
        List<FormDataBodyPart> profileField = multipartData.getFields("xmlupload");
        List<FormDataBodyPart> fileFields = multipartData.getFields("xmlupload_file");
		
        sLog.info("<" + profileField.size() + "> profile elements have been uploaded " +
                  "to the profile converter, only the first will be used (additional files " +
                  "should be uploaded with xmlupload_file tag).");
        if(profileField.size() == 0) {
            sLog.severe("No profile data has been provided with this request.");
            try {
                jsonResponse.put("status", "ERROR");
                jsonResponse.put("message", "No profile data stream provided with this request.");
                Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON)
                    .entity(jsonResponse.toString()).build();
            } catch (JSONException e) {
                sLog.severe("Error creating 400 response profile stream that is empty");
                return Response.status(Status.INTERNAL_SERVER_ERROR).entity("{\"status\":\"ERROR\"}").build();
            }
        }
		
        // Now process any files to be inserted into the profile.
        // The profile XML is the data that we're going to transform to 
        // an input file based on the template specified by template ID.
        // Get the profile as a String and then insert any additional XML
        // content into it.
        sLog.info("Handling uploaded profile from file with name: " + profileField.get(0).getContentDisposition().getFileName());
        InputStream profileXmlStream = profileField.get(0).getValueAs(InputStream.class);
        String profileXml = "";
        try {
            profileXml = IOUtils.toString(profileXmlStream);
        } catch (IOException e) {
            sLog.severe("Error converting profile stream to string for processing.");
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("{\"status\":\"ERROR\",\"message\":\"Error converting profile stream to string for processing.\"}").build();
        }
        String completeXml = profileXml;
        // If the base XML contains filename placeholders for other
        // files that have been uploaded, then we need to get these files
        // from the multipart request and put their content into the baseXml
        // in place of the filename to create the completeXml.
        if(fileFields.size() > 0) {
            for(int i = 0; i < fileFields.size(); i++) {
                try {
                    FormDataBodyPart fileData = fileFields.get(i);
                    sLog.info("Handling additional uploaded file with name: " + fileData.getContentDisposition().getFileName());
                    String fileName = fileData.getContentDisposition().getFileName();
                    InputStream fileXmlStream = fileFields.get(i).getValueAs(InputStream.class);
                    String fileXml = IOUtils.toString(fileXmlStream);

                    // Embed file in profile. File name appears in lower case 
                    // so do case-insensitive string replace using (?i)
                    String tempXml = completeXml.replaceAll("(?i)" + fileName, fileXml);
                    completeXml = tempXml;
                } catch (IOException e) {
                    sLog.severe("Error converting file for embedding in profile to string.");
                    return Response.status(Status.INTERNAL_SERVER_ERROR).entity("{\"status\":\"ERROR\",\"message\":\"Error converting file for embedding in profile to string.\"}").build();
                }
            }
        }
		
        // Now call to the schema processor to carry out the transform
        Map<String,String> transformOutput = null;
        try {
            transformOutput = proc.convertProfileToInputData(templateId, profileXml, completeXml, session.getId());
        } catch (UnknownTemplateException e) {
            sLog.severe("The template with ID <" + templateId + "> is not found: " + e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("The template with ID <" + templateId + "> is not found: " + e.getMessage()).build();
        } catch (TransformerException e) {
            sLog.severe("XSLT transform error when trying to convert profile to application input file: " + e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("XSLT transform error when trying to convert profile to application input file: " + e.getMessage()).build();	
        } catch (IOException e) {
            sLog.severe("IO error when trying to convert profile to application input file: " + e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("IO error when trying to convert profile to application input file: " + e.getMessage()).build();
        }
		
        // Prepare the JSON repsonse and send it back to the client
        URL servletUrl;
        try {
            servletUrl = new URL(pRequest.getScheme(), pRequest.getServerName(), pRequest.getServerPort(), pRequest.getContextPath());
        } catch (MalformedURLException e) {
            sLog.severe("Unable to get servlet URL to prepare response: " + e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Unable to get servlet URL to prepare response: " + e.getMessage()).build();
        }
        String servletBaseURL = servletUrl.toString();
        // TODO: Change to separate streaming servlet. This assumes temp is temporary
        // servlet directory used above
        try {
        	
            jsonResponse.put("BasicXmlInputs", servletBaseURL + "/temp/" + transformOutput.get("BasicXmlFile"));
            jsonResponse.put("FullXmlInputs", servletBaseURL + "/temp/" + transformOutput.get("FullXmlFile"));
            jsonResponse.put("TransformFailed", transformOutput.get("TransformStatus"));
            jsonResponse.put("TransformErrorMessages", transformOutput.get("TransformErrors"));
            jsonResponse.put("TransformedXml", servletBaseURL + "/temp/" + transformOutput.get("TransformedDataFile"));
            jsonResponse.put("status","OK");
        } catch (JSONException e) {
            sLog.severe("Error preparing JSON response data: " + e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error preparing JSON response data: " + e.getMessage()).build(); 
        }
				
        return Response.ok(jsonResponse.toString(), MediaType.APPLICATION_JSON).build();
    }
}
