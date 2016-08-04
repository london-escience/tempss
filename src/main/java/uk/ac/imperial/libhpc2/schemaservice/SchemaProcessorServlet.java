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

/**
 * This is the main servlet of the libhpc TemPSS template and profile service.
 * This servlet handles requests related to the processing and use of templates
 * and profiles.
 */

package uk.ac.imperial.libhpc2.schemaservice;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.dom4j.DocumentException;
import org.json.JSONException;
import org.json.JSONObject;

// This can be used so that xsl transforms can include other xsl transforms. We
// will need this, but it is not properly implemented yet; we need to check the
// project's properties and verify that Java Compiler -> Compiler compliance
// level is set to 1.6 for this to work. See:
//
// http://stackoverflow.com/questions/8697513/why-do-i-get-must-override-a-super
// class-method-with-override

/*
class XsltURIResolver implements URIResolver {
    @Override
    public Source resolve(String href, String base) throws TransformerException {
        try{
              InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("xslts/" + href);
              return new StreamSource(inputStream);
        }
        catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }
}
*/

/**
 * Servlet for carrying out tasks related to processing of libhpc
 * component schemas.
 */
public class SchemaProcessorServlet extends HttpServlet {

    /**
     * UID representing servlet version.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logger
     */
    private static final Logger sLog = Logger.getLogger(SchemaProcessorServlet.class.getName());

    /**
     * Enumeration of possible actions that can be performed by
     * this servlet.
     */
    public enum ServerAction {
        COMPONENTSELECTOR,
        SCHEMAUPLOAD,
        XMLUPLOAD
    }

    /**
     * Method to handle get requests.
     *
     * At the moment this is essentially an empty method.
     *
     * @param req HttpServletRequest object.
     * @param resp HttpServletResponse object.
     * @throws ServletException if problem
     * @throws IOException if problem
     */
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/plain");
        PrintWriter writer = resp.getWriter();
        writer.println("Libhpc Schema Processor");
    }

    /**
     * Method for dealing with post requests to the servlet.
     * This passes the request to the appropriate handler.
     *
     * @param req HttpServletRequest object.
     * @param resp HttpServletResponse object.
     * @throws ServletException if problem
     * @throws IOException if problem
     */
    @SuppressWarnings("unchecked")
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Get the component info from the application context
        Map<String, TempssObject> components = (Map<String, TempssObject>)getServletContext().getAttribute("components");

        try {
            // Setup session to hold state.
            HttpSession session = req.getSession(true);

            // Ensure we are dealing with a file upload request
            boolean isMultipart = ServletFileUpload.isMultipartContent(req);

            if (isMultipart) {
                // Create a factory for disk-based file items
                DiskFileItemFactory factory = new DiskFileItemFactory();

                // Configure a repository (to ensure a secure temp location is used)
                ServletContext servletContext = this.getServletConfig().getServletContext();
                File repository = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
                factory.setRepository(repository);

                // Create a new file upload handler
                ServletFileUpload upload = new ServletFileUpload(factory);

                // Parse the request
                List<FileItem> items = upload.parseRequest(req);

                // Need to check which handler to use, set using a form field
                ServerAction serverAction = null;
                for (FileItem item : items) {
                    if (item.isFormField()) {
                        String fieldName = item.getFieldName();
                        if (fieldName.equals("serveraction")) {
                            serverAction =
                                    ServerAction.valueOf(item.getString().toUpperCase());
                            break;
                        }
                    }
                }

                // Call appropriate handler
                switch (serverAction) {
                    case COMPONENTSELECTOR:
                        processComponentSelector(items, resp, session, components);
                        break;
                    case SCHEMAUPLOAD:
                        processSchemaUpload(items, resp, session, components);
                        break;
                    case XMLUPLOAD:
                        processXMLUpload(items, req, resp, session, components);
                        break;
                    default:
                        // Servlet does nothing if it does not recognise the request
                }
            } else {
                // Servlet does nothing if it does not recognise the request
            }
        } catch (FileUploadException e) {
            throw new ServletException("Cannot parse multipart request.", e);
        } catch(Exception e){
            throw new ServletException("Unhandled error.", e);
        }
    }

    /**
     * Delivers an html form in tree form upon upload of a schema.  (Not the
     * main entry point at present.)
     *
     * @param fileItems
     * @param resp
     * @param session
     * @throws Exception
     */
    private void processSchemaUpload(List<FileItem> fileItems,
            HttpServletResponse resp, HttpSession session, Map<String, TempssObject> components)
                    throws Exception {

        for (FileItem item : fileItems) {
            String fieldName = item.getFieldName();
            if (fieldName.equals("schemaupload")) {
                // This item is the uploaded schema file, so process
                InputStream fileStream = item.getInputStream();
                String fileString = SchemaProcessorUtils.convertStreamToString(fileStream);

                // Save the schema string to the user's session
                session.setAttribute("SchemaString", fileString);
                fileStream.close();

                // Setup the source for the XSD->HTML transform
                InputStream filestream2 = item.getInputStream();
                Source xsdInput = new StreamSource(filestream2);

                // Get the XSL file
                String xslFilePath =
                        getServletContext().getRealPath("WEB-INF/classes")
                            + File.separator
                            + "XsdToHtmlTransform.xsl";
                File xslFile = new File(xslFilePath);
                Source xslSource = new StreamSource(xslFile);

                // Setup the outputs
                String htmlOutputString = "";
                Transformer transformer =
                        TransformerFactory.newInstance().newTransformer(xslSource);
                LibhpcErrorListener errorHandler = new LibhpcErrorListener();
                transformer.setErrorListener(errorHandler);
                try {
                    StreamResult htmlOutputStreamResult =
                            new StreamResult(new StringWriter());
                    transformer.transform(xsdInput, htmlOutputStreamResult);
                    htmlOutputString = htmlOutputStreamResult.getWriter().toString();
                } catch (TransformerException e) {
                    // TODO: Handle properly
                    throw new Exception(errorHandler.getErrorMessages().toString());
                }

                resp.setContentType("text/html");
                PrintWriter writer = resp.getWriter();
                writer.println(htmlOutputString);
            }
        }
    }

    /**
     * Process an XML tree and convert to the actual input form required by
     * a libhpc component.
     *
     * @param fileItems The list of FileItems uploaded to the servlet.
     * @param req HttpServletRequest object.
     * @param resp HttpServletResponse object.
     * @param session HttpSession object
     * @throws Exception if problem processing tree.
     */
    private void processXMLUpload(List<FileItem> fileItems,
                                  HttpServletRequest req,
                                  HttpServletResponse resp,
                                  HttpSession session,
                                  Map<String, TempssObject> components)
            throws UnknownTemplateException, IOException {

        // The list of file items should contain the main xml string plus
        // xml files to substitute in.
        // Get the main xml string, and put the files in a map.

        String sBasicXml = "";
        String sComponentName = "";
        Map<String, FileItem> filesMap = new HashMap<String, FileItem>();
        for (FileItem item : fileItems) {
            String sFieldName = item.getFieldName();
            if (sFieldName.equals("xmlupload")) {
                sBasicXml = item.getString();
            } else if (sFieldName.equals("xmlupload_file")) {
                String fileName = item.getName();
                // Only if the user provided a file
                if (fileName != null) {
                    filesMap.put(fileName, item);
                }
            } else if (sFieldName.equals("componentname")) {
                sComponentName = item.getString();
                if(!components.containsKey(sComponentName)) {
                        throw new UnknownTemplateException("Unhandled component name while transforming xml:" + sComponentName);
                }
            }
        }

        // Substitute any provided xml files into the basic xml.
        // Example: user may have provided the geometry for Nektar++ as an xml file.
        String sFullXml = sBasicXml;

        if (sComponentName.equalsIgnoreCase("electrocardiology") ||
            sComponentName.equalsIgnoreCase("incompressiblenavierstokes") ||
            sComponentName.equalsIgnoreCase("compressibleflowsolver") ||
            sComponentName.equalsIgnoreCase("advectiondiffusion")) {
            for (Map.Entry<String, FileItem> entry : filesMap.entrySet()) {
                String fileName = entry.getKey();
                FileItem fileItem = entry.getValue();
                InputStream filestream = fileItem.getInputStream();
                String fileString = SchemaProcessorUtils.convertStreamToString(filestream);

                // For some reason the fileName loses its case.
                // So do case-insensitive string replace using (?i)
                String sTemp = sFullXml.replaceAll("(?i)" + fileName, fileString);
                sFullXml = sTemp;
            }
        }
        // Format the xml nicely
        try {
            sFullXml = SchemaProcessorUtils.prettyPrintXml(sFullXml);
        } catch (DocumentException e) {
            throw new IOException("Document error when formatting complete XML prior to transform: " + e.getMessage(), e);
        }

        String sessionId = session.getId();

        SchemaProcessor proc = new SchemaProcessor(getServletContext());
        Map<String, String> transformOutput;
        try {
            transformOutput = proc.convertProfileToInputData(sComponentName, sBasicXml, sFullXml, sessionId);
        } catch (TransformerException e) {
            throw new IOException("Error transforming profile to input data file: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new IOException("IO error transforming profile to input data file: " + e.getMessage(), e);
        }

        URL servletUrl = new URL(req.getScheme(), req.getServerName(), req.getServerPort(), req.getContextPath());
        String servletBaseURL = servletUrl.toString();
        JSONObject json = new JSONObject();
        // TODO: Change to separate streaming servlet. This assumes temp is temporary
        // servlet directory used above
        try {
            json.put("BasicXmlInputs", servletBaseURL + "/temp/" + transformOutput.get("BasicXmlFile"));
            json.put("FullXmlInputs", servletBaseURL + "/temp/" + transformOutput.get("FullXmlFile"));
            json.put("TransformFailed", transformOutput.get("TransformStatus"));
            json.put("TransformErrorMessages", transformOutput.get("TransformErrors"));
            json.put("TransformedXml", servletBaseURL + "/temp/" + transformOutput.get("TransformedDataFile"));
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        resp.setContentType("application/json");
        PrintWriter writer = resp.getWriter();
        writer.print(json);
        //writer.flush();
    }

    /**
     * Method to process which component has been selected and return
     * the HTML representation of the parameter tree built from the
     * underlying XML schema file which has been transformed.
     *
     * @param fileItems List of files posted to server
     * @param resp HttpServletResponse
     * @param session Current session object
     * @throws Exception
     */
    private void processComponentSelector(List<FileItem> fileItems,
            HttpServletResponse resp, HttpSession session,
            Map<String, TempssObject> components) throws IOException {

        // Need to check which component has been set using a form field
        String componentName = "";
        for (FileItem item : fileItems) {
            if (item.isFormField()) {
                String fieldName = item.getFieldName();
                if (fieldName.equals("componentselector")) {
                    componentName = item.getString();
                    break;
                }
            }
        }

        // Save the component name string to the  user's session
        session.setAttribute("ComponentName", componentName);

        // We now get the full name and schema filename for the component
        // from the template metadata object
        //String schemaPath = getServletContext().getRealPath("WEB-INF/classes") + File.separator;
        String verboseName = "";
        //String schemaName = "";
        TempssObject componentMetadata = null;
        if(components.containsKey(componentName)) {
            componentMetadata = components.get(componentName);
        } else {
            try {
                resp.sendError(404, "Received an unrecognised component name: " + componentName);
            } catch (IOException e) {
                sLog.severe("Unable to send 404 error response to client: " + e.getMessage());
                throw e;
            }
        }

        // Now call to the SchemaProcessor to get the HTML
        SchemaProcessor proc = new SchemaProcessor(getServletContext());

        String htmlToReturn = null;
        String htmlTransformError = null;
        try {
            htmlToReturn = proc.processComponentSelector(componentMetadata);
        } catch (FileNotFoundException e) {
            htmlTransformError = "File not found when processing transform for template: " + e.getMessage();
        } catch (IOException e) {
            htmlTransformError = "IO error when processing transform for template: " + e.getMessage();
        } catch (ParseException e) {
            htmlTransformError = "Parse error when processing transform for template: " + e.getMessage();
        } catch (TransformerException e) {
            htmlTransformError = "Error transforming schema into HTML template: " + e.getMessage();
        }

        PrintWriter writer = resp.getWriter();
        resp.setContentType("application/json");

        // If we have no tree to return, throw an error
        if(htmlToReturn == null) {
            sLog.severe(htmlTransformError);
            resp.setStatus(500);
            writer.print("{\"status\":\"ERROR\",\"message\":\"" + htmlTransformError + "\"}");
        }
        else {
            JSONObject json = new JSONObject();
            try {
                json.put("ComponentName", verboseName);
                json.put("TreeHtml", htmlToReturn);
                writer.print(json);
            } catch (JSONException e) {
                String errorMessage = "Error writing tree data to JSON return object: " + e.getMessage();
                sLog.severe(errorMessage);
                resp.setStatus(500);
                writer.print("{\"status\":\"ERROR\",\"message\":\"" + errorMessage + "\"}");
            }
        }
    }
}
