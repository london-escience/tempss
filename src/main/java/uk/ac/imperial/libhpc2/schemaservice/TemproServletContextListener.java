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

package uk.ac.imperial.libhpc2.schemaservice;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * This class handles the initialisation of the tempro servlet,
 * reading in details of all the available schemas and creating
 * a map of them that is stored in the application context.
 *
 * This map is used to check that subsequent requests to the service
 * refer to valid components.
 *
 * @author jhc02
 */
public class TemproServletContextListener implements ServletContextListener {

    private static final Logger sLog = Logger.getLogger(TemproServletContextListener.class.getName());

    /**
     * Search for all the properties files in the "Template" directory
     * and read in the details from them, creating a TemproObject instance
     * for each and storing this in the component map.
     */
    public void contextInitialized(ServletContextEvent pContext) {
        Map<String, TemproObject> componentMap = new HashMap<String, TemproObject>();

        // Now search for the available properties files describing components
        // These are placed in the META-INF/Template directory in the classpath

        // We can't simply get a list of all files in the classpath from the
        // classloader so we instead get access to the location of the current
        // class by accessing its URL and then construct the path to
        // META-INF/Template where we can search for our files.
        Class<?> clazz = this.getClass();
        String className = clazz.getSimpleName() + ".class";
        sLog.info("Class name: " + className);
        URL path = null;
        try {
            sLog.info("Class URL: " + clazz.getResource(className).toString());
            path = new URL(clazz.getResource(className).toString());
        } catch (MalformedURLException e1) {
            sLog.severe("Unable to get class URL to search for component property files.");
            pContext.getServletContext().setAttribute("components", componentMap);
            return;
        }

        String templatePath = path.toString().substring(0, path.toString().indexOf("WEB-INF/classes/")+16) + "META-INF/Template";
        sLog.info("templatePath: " + templatePath);

        URI templatePathURI = null;
        try {
            templatePathURI = new URI(templatePath);
        } catch (URISyntaxException e1) {
            sLog.severe("Unable to construct URI for template path to search for property files.");
            pContext.getServletContext().setAttribute("components", componentMap);
            return;
        }

        File[] templateMetadataFiles = new File(templatePathURI).listFiles(new FilenameFilter() {
            public boolean accept(File f, String name) {
                return name.endsWith(".properties");
            }
        });

        // Now process the template metadata files to generate instances
        // of TemplateObject that can be stored in the application context
        for(File f : templateMetadataFiles) {
            Properties props = new Properties();
            String resourcePath = f.getAbsolutePath().substring(f.getAbsolutePath().indexOf("META-INF/Template"));
            sLog.info("Template file: " + f.getAbsolutePath() + "\nGetting resource: " + resourcePath);
            InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(resourcePath);
            if(resourceStream != null) {
                try {
                    props.load(resourceStream);
                } catch (IOException e) {
                    sLog.severe("Unable to load resource metadata for <" + resourcePath + ">");
                    continue;
                }
            }
            else {
                sLog.severe("Input stream for resource <" + resourcePath + "> is null.");
                continue;
            }

            String[] components = props.getProperty("component.id").split(",");
            for(String comp : components) {
                comp = comp.trim();
                String name = props.getProperty(comp+".name");
                String schema = props.getProperty(comp+".schema");
                String transform = props.getProperty(comp+".transform");
                TemproObject obj = new TemproObject(comp, name, schema, transform);
                sLog.info("Found and registered new template object: \n" + obj.toString());
                componentMap.put(comp, obj);
            }
        }

        pContext.getServletContext().setAttribute("components", componentMap);
    }

    public void contextDestroyed(ServletContextEvent pContext) {
        pContext.getServletContext().setAttribute("components", null);
    }
}
