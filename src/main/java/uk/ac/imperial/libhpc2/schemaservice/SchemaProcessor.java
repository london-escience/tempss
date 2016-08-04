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

package uk.ac.imperial.libhpc2.schemaservice;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.dom4j.DocumentException;

public class SchemaProcessor {
    /**
     * Logger
     */
    private static final Logger sLog = Logger.getLogger(SchemaProcessor.class.getName());

    /**
     * Servlet context passed in on class creation
     */
    ServletContext _context;

    public SchemaProcessor(ServletContext pContext) {
        this._context = pContext;
    }

    /**
     * Method to process which component has been selected and return the HTML
     * representation of the parameter tree built from the underlying XML schema
     * file which has been transformed.
     *
     * @param fileItems List of files posted to server
     * @param resp HttpServletResponse
     * @param session Current session object
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ParseException
     * @throws TransformerException
     */
    public String processComponentSelector(TempssObject pComponentMetadata)
        throws FileNotFoundException, IOException, ParseException, TransformerException {

        sLog.fine("ServletContext: " + _context);
        String schemaPath = _context.getRealPath("/WEB-INF/classes") + File.separator;
        //String verboseName = pComponentMetadata.getName();
        String schemaName = pComponentMetadata.getSchema();

        // Construct full path to file
        String schemaPathAndName = schemaPath + schemaName;

        // Get the contents of the schema.
        File schemaFile = new File(schemaPathAndName);
        if (!schemaFile.exists()) {
            throw new FileNotFoundException("Could not find component schema file on server.");
        }

        // I am going to hack around with the schema to include any included
        // schemas.  Therefore need to get the schema as a string rather than
        // work directly with the StreamSource.

        //Source schemaSource = new StreamSource(schemaFile);
        String schemaString = null;
        try {
            schemaString = FileUtils.readFileToString(schemaFile, "utf-8");
        } catch (IOException e) {
            throw new IOException("Unable to read the schema file as a string...", e);
        }

        // Use regex to find and include files. First find the identifier
        // assigned to the schema namespace. Usually it's xs or xsd Need to
        // match pattern like: xmlns:xs="http://www.w3.org/2001/XMLSchema"
        String namespaceIdentifier = "";
        Pattern pattern0 = Pattern.compile("xmlns:(.*?)=\"http://www.w3.org/2001/XMLSchema\"");
        Matcher matcher0 = pattern0.matcher(schemaString);

        // There should be only one, but use while loop anyway
        while (matcher0.find()) {
            namespaceIdentifier = matcher0.group(1);
        }

        if (namespaceIdentifier.length()==0) {
            throw new ParseException("Could not find schema namespace identifier.", 0);
        }

        // Want to find filename.xsd in schemaLocation="LibhpcCommon.xsd"
        Pattern pattern = Pattern.compile("include schemaLocation=\"(.*?)\"");
        Matcher matcher = pattern.matcher(schemaString);

        // For each included file, process it:
        while (matcher.find()) {
            String includedSchemaName = matcher.group(1);
            String includedSchemaPathAndName = schemaPath + includedSchemaName;
            File includedSchemaFile = new File(includedSchemaPathAndName);

            if (!includedSchemaFile.exists()) {
                throw new FileNotFoundException("Could not find included component schema file on server: " + includedSchemaName);
            }

            String includedSchemaString = FileUtils.readFileToString(includedSchemaFile, "utf-8");

            // Need to find the part of the included schema that's within the
            // <schema> <\schema> tabs. Another regex...
            String internalSchema = "";

            // NB [\\S\\s] matches anything that's a space or not a space. Use
            // this instead of . because . excludes new lines
            String regex = "<"+namespaceIdentifier+":schema([\\S\\s]*?)>([\\S\\s]*?)</"+namespaceIdentifier+":schema>";
            Pattern pattern2 = Pattern.compile(regex);
            Matcher matcher2 = pattern2.matcher(includedSchemaString);

            // The result should be in the second group. The first group will
            // contain all the attributes in the xs:schema tag.  Not sure what
            // happens if there are no attributes. If you are seeing an error
            // thrown here, check that...
            while (matcher2.find()) {
                //String test1 = matcher2.group(1);
                String test2 = matcher2.group(2);
                internalSchema = test2;
            }

            // Next we need to replace the include statement with the included
            // text.  We need to find the text to replace. Another regex
            String textToReplace = "";
            String regex3 = "<"+namespaceIdentifier+":include(.*?)"+includedSchemaName+"(.*?)>";
            Pattern pattern3 = Pattern.compile(regex3);
            Matcher matcher3 = pattern3.matcher(schemaString);

            // The result should be in the second group. The first group will
            // contain all the attributes in the xs:schema tag.  Not sure what
            // happens if there are no attributes. If you are seeing an error
            // thrown here, check that...
            while (matcher3.find()) {
                String test1 = matcher3.group(1);
                String test2 = matcher3.group(2);
                textToReplace = "<"+namespaceIdentifier+":include" + test1 + includedSchemaName+test2+">";
            }

            // Make the replacement
            schemaString = schemaString.replace(textToReplace, internalSchema);
        }

        // Get the schema -> HTML transform
        String transformPath = _context.getRealPath("/WEB-INF/classes") + File.separator + "XsdToHtmlTransform.xsl";
        File xslFile = new File(transformPath);
        Source xsl = new StreamSource(xslFile);

        //String xslName = xslFile.toString();
        //String xslContent = new Scanner( xslFile ).useDelimiter("\\A").next();

        // Convert the schema into an HTML form
        String outputHTML = "";
        Transformer transformer = null;
        try {
            transformer = TransformerFactory.newInstance().newTransformer(xsl);
        } catch (TransformerConfigurationException e) {
            throw new TransformerException("Error creating transformer for the specified XSLT document <" + transformPath + ">", e);
        } catch (TransformerFactoryConfigurationError e) {
            throw new TransformerException("Configuration error creating transformer for the specified XSLT document <" + transformPath + ">", e);
        }

        LibhpcErrorListener errorHandler = new LibhpcErrorListener();
        transformer.setErrorListener(errorHandler);

        try {
            StringReader reader = new StringReader(schemaString);
            Source schemaSource = new StreamSource(reader);
            StreamResult htmlOutput = new StreamResult(new StringWriter());
            transformer.transform(schemaSource, htmlOutput);
            outputHTML = htmlOutput.getWriter().toString();
        } catch (TransformerException e) {
            throw new TransformerException("Error carrying out XSLT transform: " + errorHandler.getErrorMessages().toString(), e);
        }

        // The outputHtml is perfectly formed, complete HTML as provided by the
        // transform.  I do not want to change this as when developers may like
        // to test the transform directly on their schemas However, I want to
        // add a hidden field with the component name.  Therefore do this now:
        String htmlToReturn = outputHTML + "<input type=\"hidden\" name=\"componentname\" value = \"" + pComponentMetadata.getId() + "\"\\>";

        return htmlToReturn;
    }

    @SuppressWarnings("unchecked")
    public Map<String,String> convertProfileToInputData(
        String pComponentName,
        String pBasicXml,
        String pXml,
        String pSessionId)
        throws UnknownTemplateException, TransformerException, IOException
    {
        // Now we need to convert the completed xml profile into form that is
        // expected as input by the component. Look up the component metadata to
        // get the path to the XSLT transform for this component.
        Map<String, TempssObject> components = (Map<String, TempssObject>)_context.getAttribute("components");
        String transformPath = "";
        if(components.containsKey(pComponentName)) {
            TempssObject componentMetadata = components.get(pComponentName);
            transformPath = _context.getRealPath("/WEB-INF/classes") + File.separator + componentMetadata.getTransform();
        } else {
            throw new UnknownTemplateException("Unhandled component name while transforming xml:" + pComponentName);
        }

        // Now we use the transform to get the result.
        File xslFile = new File(transformPath);
        Source xsl = new StreamSource(xslFile);
        StringReader reader = new StringReader(pXml);
        Source xmlInput = new StreamSource(reader);
        StreamResult xmlOutput = new StreamResult(new StringWriter());
        String outputXml = "";
        Transformer transformer;

        try {
            transformer = TransformerFactory.newInstance().newTransformer(xsl);
        } catch (TransformerConfigurationException e) {
            throw new TransformerException("Error creating transformer for the specified XSLT document <" + xslFile + ">", e);
        } catch (TransformerFactoryConfigurationError e) {
            throw new TransformerException("Configuration error creating transformer for the specified XSLT document <" + xslFile + ">", e);
        }

        LibhpcErrorListener errorHandler = new LibhpcErrorListener();
        transformer.setErrorListener(errorHandler);

        try {
            transformer.transform(xmlInput, xmlOutput);
        } catch (TransformerException e) {
            throw new TransformerException("Error carrying out XSLT transform: " + errorHandler.getErrorMessages().toString(), e);
        }
        outputXml = xmlOutput.getWriter().toString();
        try {
            outputXml = SchemaProcessorUtils.prettyPrintXml(outputXml);
        } catch (DocumentException e) {
            throw new IOException("Document error formatting XML output data: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new IOException("IO error formatting XML output data: " + e.getMessage(), e);
        }

        // Save the xml as files
        String basicXmlFileName = "basic_input_xml_" + pSessionId + ".xml";
        String fullXmlFileName = "full_input_xml_" + pSessionId + ".xml";
        String outputXmlFileName = "output_xml_" + pSessionId + ".xml";

        // TODO: Fix this:
        // Get temporary directory for storing output files
        // File tempDir = (File) getServletContext().getAttribute("javax.servlet.context.tmpdir");

        // Get path to web-inf folder
        String filePath = _context.getRealPath("/temp");
        File tempDir = new File(filePath);
        //boolean isSuccess = tempDir.mkdirs();

        try {
            File file = new File(tempDir, basicXmlFileName);
            BufferedWriter output;
            output = new BufferedWriter(new FileWriter(file));
            output.write(pBasicXml);
            output.close();

            File file2 = new File(tempDir, fullXmlFileName);
            BufferedWriter output2 = new BufferedWriter(new FileWriter(file2));
            output2.write(pXml);
            output2.close();

            File file3 = new File(tempDir, outputXmlFileName);
            BufferedWriter output3 = new BufferedWriter(new FileWriter(file3));
            output3.write(outputXml);
            output3.close();
        } catch (IOException e) {
            throw new IOException("IO error when writing temporary output files: " + e.getMessage(), e);
        }

        Map<String,String> transformOutputMap = new HashMap<String, String>();

        transformOutputMap.put("BasicXmlFile", basicXmlFileName);
        transformOutputMap.put("FullXmlFile", fullXmlFileName);
        transformOutputMap.put("TransformStatus", (errorHandler.errorsEncounteredDuringTransform() == true) ? "true" : "false");
        transformOutputMap.put("TransformErrors", errorHandler.getErrorMessages().toString());
        transformOutputMap.put("TransformedDataFile", outputXmlFileName);

        return transformOutputMap;
    }
}
