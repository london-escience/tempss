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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import uk.ac.imperial.libhpc2.schemaservice.SchemaProcessor;
import uk.ac.imperial.libhpc2.schemaservice.TempssObject;
import uk.ac.imperial.libhpc2.schemaservice.UnknownTemplateException;
import uk.ac.imperial.libhpc2.schemaservice.web.dao.ProfileDao;
import uk.ac.imperial.libhpc2.schemaservice.web.db.Profile;
import uk.ac.imperial.libhpc2.schemaservice.web.db.TempssUser;
import uk.ac.imperial.libhpc2.schemaservice.web.service.TempssUserDetails;

/**
 * Jersey REST class representing the profile endpoint
 */
@Component
@Path("profile")
public class ProfileRestResource {

    /**
     * Logger
     */
    private static final Logger sLog = LoggerFactory.getLogger(ProfileRestResource.class.getName());
	
    /**
     * Profile data access object for accessing the profile database
     */
    @Autowired
	ProfileDao profileDao;
    
    /**
     * ServletContext object used to access profile metadata
     * Injected via @Context annotation
     */
    ServletContext _context;
			
    @Context
    public void setServletContext(ServletContext pContext) {
        this._context = pContext;
        sLog.debug("Servlet context injected: " + pContext);
        //sLog.fine("Manuallly wiring dao bean into class...");
        //profileDao = (ProfileDao)((BeanFactory)pContext).getBean("profileDao");
        //sLog.fine("Bean <" + profileDao + "> has been injected as profileDao...");
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
        Map<String, TempssObject> components = (Map<String, TempssObject>)_context.getAttribute("components");

        JSONObject jsonResponse = new JSONObject();
		
        // If we don't have a template of this name then throw an error
        if(!components.containsKey(templateId)) {
            try {
                jsonResponse.put("status", "ERROR");
                jsonResponse.put("message", "Template with ID <" + templateId + "> does not exist.");
                return Response.status(Status.NOT_FOUND).entity(jsonResponse.toString()).build();
            } catch (JSONException e) {
                sLog.error("Error creating 404 response for template ID that is not found");
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
            sLog.error("No profile data has been provided with this request.");
            try {
                jsonResponse.put("status", "ERROR");
                jsonResponse.put("message", "No profile data stream provided with this request.");
                Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON)
                    .entity(jsonResponse.toString()).build();
            } catch (JSONException e) {
                sLog.error("Error creating 400 response profile stream that is empty");
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
            sLog.error("Error converting profile stream to string for processing.");
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("{\"status\":\"ERROR\",\"message\":\"Error converting profile stream to string for processing.\"}").build();
        }
        String completeXml = profileXml;
        // If the base XML contains filename placeholders for other
        // files that have been uploaded, then we need to get these files
        // from the multipart request and put their content into the baseXml
        // in place of the filename to create the completeXml.
        if((fileFields != null) && (fileFields.size() > 0)) {
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
                    sLog.error("Error converting file for embedding in profile to string.");
                    return Response.status(Status.INTERNAL_SERVER_ERROR).entity("{\"status\":\"ERROR\",\"message\":\"Error converting file for embedding in profile to string.\"}").build();
                }
            }
        }
		
        // Now call to the schema processor to carry out the transform
        Map<String,String> transformOutput = null;
        try {
            transformOutput = proc.convertProfileToInputData(templateId, profileXml, completeXml, session.getId());
        } catch (UnknownTemplateException e) {
            sLog.error("The template with ID <" + templateId + "> is not found: " + e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("The template with ID <" + templateId + "> is not found: " + e.getMessage()).build();
        } catch (TransformerException e) {
            sLog.error("XSLT transform error when trying to convert profile to application input file: " + e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("XSLT transform error when trying to convert profile to application input file: " + e.getMessage()).build();	
        } catch (IOException e) {
            sLog.error("IO error when trying to convert profile to application input file: " + e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("IO error when trying to convert profile to application input file: " + e.getMessage()).build();
        }
		
        // Prepare the JSON repsonse and send it back to the client
        URL servletUrl;
        try {
            servletUrl = new URL(pRequest.getScheme(), pRequest.getServerName(), pRequest.getServerPort(), pRequest.getContextPath());
        } catch (MalformedURLException e) {
            sLog.error("Unable to get servlet URL to prepare response: " + e.getMessage());
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
            sLog.error("Error preparing JSON response data: " + e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error preparing JSON response data: " + e.getMessage()).build(); 
        }
				
        return Response.ok(jsonResponse.toString(), MediaType.APPLICATION_JSON).build();
    }
    
    /**
     * Get the profile identified by "profileName" that is based
     * on the template identified by "templateId".
     * 
     * @param templateId the template ID that the requested profile is based on
     * @param profileName the name of the profile to obtain
     * @param pRequest the HTTP request object.
     * @return a JSON object containing the request status and, if successful,
     *         the profile data. If status is OK, the profile is present under the
     *         'profile' key. If an ERROR has occurred, the error can be found 
     *         under key 'code' and any additional description of the error under 
     *         the key 'message'.
     */
    @GET
    @Path("{templateId}/{profileName}")
    @Produces("application/json")
    @SuppressWarnings("unchecked")
    public Response loadProfile(
        @PathParam("templateId") String templateId,
        @PathParam("profileName") String profileName,
        @Context HttpServletRequest pRequest,
        TempssUser pUser) {
    
    	TempssUser user = getAuthenticatedUser();
    	
    	Map<String, TempssObject> components = (Map<String, TempssObject>)_context.getAttribute("components");

    	// Check the specified template exists and that it is owned by the 
    	// currently authenticated user or it is public
		TempssObject templateMetadata = components.get(templateId);
		if(templateMetadata == null) {
			String responseText = "{\"status\":\"ERROR\", \"code\":\"INVALID_TEMPLATE\", \"error\":" +
					"\"The specified template <" + templateId + "> does not exist.\"}";
			return Response.status(Status.BAD_REQUEST).entity(responseText).build();
		}
		
		// Now try and get the profile
		Profile p = profileDao.findByName(profileName, user);
		if(p == null) {
			String responseText = "{\"status\":\"ERROR\", \"code\":\"PROFILE_DOES_NOT_EXIST\", \"error\":" +
					"\"The profile with the specified name <" + profileName + "> does not exists.\"}";
			return Response.status(Status.NOT_FOUND).entity(responseText).build();
		}
		
		JSONObject jsonResponse = new JSONObject();
		try {
			jsonResponse.put("status", "OK");
			jsonResponse.put("name", p.getName());
			jsonResponse.put("templateId", p.getTemplateId());
			jsonResponse.put("profile", p.getProfileXml());
		} catch (JSONException e) {
			String responseText = "{\"status\":\"ERROR\", \"code\":\"RESPONSE_DATA\", \"error\":\"" + e.getMessage() + "\"}";
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(responseText).build();
		}
    	    	    	
    	return Response.ok(jsonResponse.toString(), MediaType.APPLICATION_JSON).build();
    }
    
    /**
     * Save the provided XML data (within a JSON wrapper with key 'profile')
     * as a profile for the specified template.
     * 
     * @param templateId the ID of the template that we're saving the profile for
     * @param profileJson the JSON data containing the XML profile as the value of the "profile" key 
     * @param pRequest the HttpServletRequest object for this request
     * @return a JSON object containing a status key. If status is OK, the request
     *         completed successfully, if it is ERROR, the error can be found under key 'code'
     *         and any additional description of the error under the key 'message'.
     */
    @POST
    @RolesAllowed("ROLE_USER")
    @Path("{templateId}/{profileName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json")
    @SuppressWarnings("unchecked")
    public Response saveProfile(
        @PathParam("templateId") String templateId,
        @PathParam("profileName") String profileName,
        @RequestBody String profileJson,
        @Context HttpServletRequest pRequest) {

    	TempssUser user = getAuthenticatedUser();
    	
    	// Check that the user is authenticated
		if(user == null) {
			String responseText = "{\"status\":\"ERROR\", "
					+ "\"code\":\"PERMISSION_DENIED\", \"error\":" +
					"\"You must be signed in to save a profile.\"}";
			return Response.status(Status.FORBIDDEN).entity(responseText).build();
		}
    	
    	Map<String, TempssObject> components = (Map<String, TempssObject>)_context.getAttribute("components");

    	// Check the specified template exists, if so, save the 
		// profile data to the database with the provided name
		TempssObject templateMetadata = components.get(templateId);
		if(templateMetadata == null) {
			String responseText = "{\"status\":\"ERROR\", \"code\":\"INVALID_TEMPLATE\", \"error\":" +
					"\"The specified template <" + templateId + "> does not exist.\"}";
			return Response.status(Status.BAD_REQUEST).entity(responseText).build();
		}
		
		// Now check if the specified profile name already exists
		try {
			if(!profileDao.profileNameAvailable(profileName, user)) {
				String responseText = "{\"status\":\"ERROR\", \"code\":\"PROFILE_NAME_EXISTS\", \"error\":" +
						"\"A profile with the specified name <" + profileName + "> aready exists.\"}";
				return Response.status(Status.CONFLICT).entity(responseText).build();
			}
		} catch(AuthenticationException e) {
			String responseText = "{\"status\":\"ERROR\", \"code\":\"AUTHENTICATION_REQUIRED\", \"error\":" +
					"\"You must be authenticated to check if a profile name exists.\"}";
			return Response.status(Status.FORBIDDEN).entity(responseText).build();
		}

    	String profileXml = "";
    	int profilePublic = 0;
    	JSONObject jsonResponse = new JSONObject();
 
    	// Get the profile XML string from the incoming request data
		try {
			JSONObject profileObj = new JSONObject(profileJson);
			profileXml = profileObj.getString("profile");
			if(profileObj.getBoolean("profilePublic")) {
				profilePublic = 1;
			}
			sLog.debug("Handling save request for profile name <" + profileName + "> for template <" 
					  + templateId + "> with public flag <" + profilePublic + ">:\n" + profileXml);
		} catch (JSONException e) {
			String responseText = "{\"status\":\"ERROR\", \"code\":\"REQUEST_DATA\", \"error\":\"" + e.getMessage() + "\"}";
			return Response.status(Status.BAD_REQUEST).entity(responseText).build();
		}
    			
		Map<String, Object> profileData = new HashMap<String,Object>();
		profileData.put("name", profileName);
		profileData.put("templateId", templateId);
		profileData.put("profileXml", profileXml);
		profileData.put("public", profilePublic);
		profileData.put("owner", user.getUsername());
		Profile profile = new Profile(profileData);
		profileDao.add(profile);
		sLog.info("The value of profileDao is: " + profileDao);
		
		try {
			jsonResponse.put("status", "OK");
			jsonResponse.put("name", profileName);
			jsonResponse.put("templateId", templateId);
		} catch (JSONException e) {
			String responseText = "{\"status\":\"ERROR\", \"code\":\"RESPONSE_DATA\", \"error\":\"" + e.getMessage() + "\"}";
			return Response.status(Status.BAD_REQUEST).entity(responseText).build();
		}
    	    	    	
    	return Response.ok(jsonResponse.toString(), MediaType.APPLICATION_JSON).build();
    }
    
    
    
    /**
     * Delete the profile identified by "profileName" that is based
     * on the template identified by "templateId".
     * 
     * @param templateId the template ID that the profile to delete is based on
     * @param profileName the name of the profile to delete
     * @param pRequest the HTTP request object.
     * @return a JSON object containing a status key. If status is OK, the request
     *         completed successfully, if it is ERROR, the error can be found under key 'code'
     *         and any additional description of the error under the key 'message'.
     */
    @DELETE
    @Path("{templateId}/{profileName}")
    @Produces("application/json")
    @SuppressWarnings("unchecked")
    public Response deleteProfile(
        @PathParam("templateId") String templateId,
        @PathParam("profileName") String profileName,
        @Context HttpServletRequest pRequest) {
    
    	TempssUser user = getAuthenticatedUser();
    	
    	// Check that the user is authenticated
		if(user == null) {
			String responseText = "{\"status\":\"ERROR\", "
					+ "\"code\":\"PERMISSION_DENIED\", \"error\":" +
					"\"You do not have permission to delete the profile with "
					+ "the specified name <" + profileName + ">.\"}";
			return Response.status(Status.FORBIDDEN).entity(responseText).build();
		}
    	
    	Map<String, TempssObject> components = (Map<String, TempssObject>)_context.getAttribute("components");

    	// Undertake sme validation checks...
    	// Check the specified template exists
		TempssObject templateMetadata = components.get(templateId);
		if(templateMetadata == null) {
			String responseText = "{\"status\":\"ERROR\", \"code\":\"INVALID_TEMPLATE\", \"error\":" +
					"\"The specified template <" + templateId + "> does not exist.\"}";
			return Response.status(Status.BAD_REQUEST).entity(responseText).build();
		}
		
		// Now check that the specified profile name exists
		if(profileDao.profileNameAvailable(profileName, user)) {
			String responseText = "{\"status\":\"ERROR\", \"code\":\"PROFILE_DOES_NOT_EXIST\", \"error\":" +
					"\"The profile with the specified name <" + profileName + "> does not exists.\"}";
			return Response.status(Status.NOT_FOUND).entity(responseText).build();
		}
		
		// Now delete the profile
		int rowsAffected = profileDao.delete(templateId, profileName, user);
		
		if(rowsAffected == 0) {
			String responseText = "{\"status\":\"ERROR\", \"code\":\"PROFILE_NOT_DELETED\", \"error\":" +
					"\"Profile <" + profileName + "> for template <" + templateId + 
					"> was not present to delete or you are not the owner.\"}";
			return Response.status(Status.BAD_REQUEST).entity(responseText).build();
		}
		
		if(rowsAffected > 1) {
			String responseText = "{\"status\":\"ERROR\", \"code\":\"MULTIPLE_PROFILES_DELETED\", \"error\":" +
					"\"Multiple profiles were deleted when trying to delete profile <" + profileName + 
					"> for template <" + templateId + "> was not present to delete.\"}";
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(responseText).build();
		}
		
		JSONObject jsonResponse = new JSONObject();
		try {
			jsonResponse.put("status", "OK");
			jsonResponse.put("name", profileName);
			jsonResponse.put("templateId", templateId);
		} catch (JSONException e) {
			String responseText = "{\"status\":\"ERROR\", \"code\":\"RESPONSE_DATA\", \"error\":\"" + e.getMessage() + "\"}";
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(responseText).build();
		}
    	    	    	
    	return Response.ok(jsonResponse.toString(), MediaType.APPLICATION_JSON).build();
    }
    
    /**
     * Get the names of the profiles registered for the template
     * with the specified ID. Returns a JSON object with the key
     * profile_names. Its value is a list of string names.
     * 
     * @param pTemplateId the ID of the template to get profile names for.
     * @return a response object containing the JSON name data or an error response.
     */
    @GET
    @Path("{templateId}/names")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json")
    public Response getProfileNamesJson(
    		@PathParam("templateId") String pTemplateId) {

		TempssUser user = getAuthenticatedUser();
		
    	List<Profile> profiles = profileDao.findByTemplateId(pTemplateId, user);
    	JSONArray profileArray = new JSONArray();
    	if(profiles != null) {
    		for(Profile p : profiles) {
    			JSONObject profileItem = new JSONObject();
    			try {
    				profileItem.put("name", p.getName());
    				profileItem.put("public", p.getPublic());
    				if(user == null) {
    					profileItem.put("owner", false);
    				}
    				else {
    					profileItem.put("owner", p.getOwner().equals(user.getUsername()));
    				}
    				profileArray.put(profileItem);
    			} catch(JSONException e) {
    				sLog.debug("Error adding profile name <{}> to JSON " +
    						"object. Ignoring this profile ", p.getName());
    			}
    			
        	}	
    	}
    	JSONObject jsonResponse = new JSONObject();
    	try {
			jsonResponse.put("profile_names", profileArray);
		} catch (JSONException e) {
			String responseText = "{\"status\", \"ERROR\", \"error\", \"" + e.getMessage() + "\"}";
			return Response.status(Status.BAD_REQUEST).entity(responseText).build();
		}
    	return Response.ok(jsonResponse.toString(), MediaType.APPLICATION_JSON).build();
    }
    
    /**
     * Get the names of the profiles registered for the template
     * with the specified ID. Returns a string with each profile
     * name on a new line.
     * 
     * @param pTemplateId the ID of the template to get profile names for.
     * @return a response object containing profile names as a string,
     *  or an error response.
     */
    @GET
    @Path("{templateId}/names")
    @Produces("text/plain")
    public Response getProfileNamesText(
    		@PathParam("templateId") String pTemplateId) {

    	TempssUser user = getAuthenticatedUser();
    	
    	List<Profile> profiles = profileDao.findByTemplateId(pTemplateId, user);
    	StringBuilder profileNames = new StringBuilder();
    	// If there are no profiles for the specified template (or the template
    	// doesn't exist)...
    	if(profiles != null) {
	    	for(Profile p : profiles) {
	    		profileNames.append(p.getName() + " (");
	    		profileNames.append(
	    				(p.getPublic() == true) ? "public" : "private");
	    		profileNames.append(")\n");
	    	}
    	}
    	return Response.ok(profileNames.toString(), MediaType.TEXT_PLAIN).build();
    }
    
    /**
     * Convert the provided profile data to an input file using
     * the transform stored in the template metadata. 
     * @param pFileId the ID of the file to convert, as obtained from the
     *               response to the convert call.
     * @return the application input file specified by the fileId
     */
    @GET
    @Path("inputFile/{fileId}")
    public Response getApplicationInputFile(
        @PathParam("fileId") String pFileId,
        @Context HttpServletRequest pRequest) {
    	
    	sLog.debug("Request to get application input file with ID: " + pFileId);
    	
    	String fileDirPath = _context.getRealPath("temp");
    	final File dataFile = new File(fileDirPath + File.separator + "output_xml_" + pFileId + ".xml");
    	if(!dataFile.exists()) {
    		return Response.status(Status.NOT_FOUND).entity("Request app input data file could not be found.").build();
    	}
    	
    	StreamingOutput so = new StreamingOutput() {
			@Override
			public void write(OutputStream pOut) throws IOException,
					WebApplicationException {

				FileInputStream in = new FileInputStream(dataFile);
				byte[] data = new byte[1024];
				int dataRead = -1;
				while((dataRead = in.read(data)) != -1) {
					pOut.write(data, 0, dataRead);
				}
				pOut.close();
				in.close();
			}
		};
		
		// Create the content disposition object for the file download
		ContentDisposition cd = ContentDisposition.type("attachment").creationDate(new Date()).fileName("tempss_input_file_" + pFileId + ".xml").build();
		
		NewCookie c = new NewCookie("fileDownload","true", "/",null, null, NewCookie.DEFAULT_MAX_AGE, false);
		return Response.status(Status.OK).
				header("Content-Disposition", cd).
				header("Content-Type", "application/xml").
				cookie(c).entity(so).build();
    }
    
    /**
     * Get the details of the currently authenticated user.
     *  
     * @return null if no user is authenticated or the TempssUser object of the
     *         authenticated user if a user is logged in.
     */
    private TempssUser getAuthenticatedUser() {
    	Authentication authToken = 
    			SecurityContextHolder.getContext().getAuthentication();
    	
    	TempssUserDetails userDetails = null;
		TempssUser user = null;
		if( (authToken != null) && !(authToken instanceof AnonymousAuthenticationToken) ) {
			userDetails = (TempssUserDetails) authToken.getPrincipal();
			user = userDetails.getUser();
		}
		
		return user;
    }
}
