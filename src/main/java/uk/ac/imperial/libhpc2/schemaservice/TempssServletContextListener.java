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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles the initialisation of the tempss servlet,
 * reading in details of all the available schemas and constraint
 * data and creating a map of this information that is stored in 
 * the application context.
 *
 * This map is used to check that subsequent requests to the service 
 * refer to valid components and to identify when constraint data is 
 * available.
 * 
 * Specification of a constraints file is optional.
 *
 * @author jhc02
 */
public class TempssServletContextListener implements ServletContextListener {

    private static final Logger sLog = LoggerFactory.getLogger(TempssServletContextListener.class.getName());

    /**
     * Search for all the properties files in the "Template" directory
     * and read in the details from them, creating a TempssObject instance
     * for each and storing this in the component map.
     * 
     * Also read in constraint information and add this to the TempssObjects 
     * for templates that also have constraints information stored.
     */
    public void contextInitialized(ServletContextEvent pContext) {
        Map<String, TempssObject> componentMap = new HashMap<String, TempssObject>();

        // Now search for the available properties files describing components
        // These are placed in the META-INF/Template directory in the classpath
        File[] templateMetadataFiles = null;
        File[] constraintMetadataFiles = null;
        try {
        	templateMetadataFiles = getResourceFiles(pContext, "META-INF/Template", ".properties", new String[]{});
        	constraintMetadataFiles = getResourceFiles(pContext, "META-INF/Constraints", ".xml", new String[]{});
        } catch (MalformedURLException e) {
        	sLog.error(e.getMessage());
            pContext.getServletContext().setAttribute("components", componentMap);
            return;
        } catch (URISyntaxException e) {
            sLog.error(e.getMessage());
            pContext.getServletContext().setAttribute("components", componentMap);
            return;
        }
        
        // We now need to check if there's a configuration file present that
        // specifies some templates that are to be ignored
        TempssConfig config = TempssConfig.getInstance();
        List<String> ignorePatterns = config.getIgnorePatterns();
        
        // Now process the template metadata files to generate instances
        // of TemplateObject that can be stored in the application context
        for (File f : templateMetadataFiles) {
            Properties props = new Properties();
            String absolutePath = f.getAbsolutePath();
            sLog.debug("Template absolute path: " + absolutePath);
            String resourcePath = absolutePath.substring(absolutePath.indexOf("META-INF" + File.separator + "Template"));
            sLog.debug("Template file: " + absolutePath + "\nGetting resource: " + resourcePath);
            InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(resourcePath);
            if(resourceStream != null) {
                try {
                    props.load(resourceStream);
                } catch (IOException e) {
                    sLog.error("Unable to load resource metadata for <" + resourcePath + ">");
                    continue;
                }
            }
            else {
                sLog.error("Input stream for resource <" + resourcePath + "> is null.");
                continue;
            }

            String[] components = props.getProperty("component.id").split(",");            
            for(String comp : components) {
                comp = comp.trim();
                String name = props.getProperty(comp+".name");
                String schema = props.getProperty(comp+".schema");
                String transform = props.getProperty(comp+".transform");
                String constraints = props.getProperty(comp+".constraints");
                // Check that the constraints file was found and is present in the constraintMetadataFiles 
                // list. If it is not, then set constraints to null and log an error
                if(constraints != null) {
	                boolean constraintFileFound = false;
	                for(File cf : constraintMetadataFiles) {
	                	if(cf.getName().equals(constraints)) {
	                		constraintFileFound = true;
	                		break;
	                	}
	                }
	                if(!constraintFileFound) {
	                	sLog.error("The specified constraints file <{}> was not found.", constraints);
	                	constraints = null;
	                	
	                }
                }
                
                // For some templates, it may be the case that they're used by third-party tools
                // and are not designed to be shown in the TemPSS template UI. To support this, 
                // a template properties file can contain a <template-name>.ignore key and the 
                // template will be added to the ignore list
                if(props.containsKey(comp+".ignore")) {
                	TempssConfig.getInstance().getIgnorePatterns().add(comp);
                }
                
                TempssObject obj = new TempssObject(comp, name, schema, transform, constraints);
                sLog.info("Found and registered new template object: \n" + obj.toString());
                componentMap.put(comp, obj);
            }
        }
        
        // Now compare the IDs to the ignore patterns obtained from the 
        // tempss configuration and remove any components to be ignored.
        // UPDATE Apr 17: This updating of the component map has been modified to
        // set the ignore flag on a tempss object rather than removing it from the
        // componentMap altogether.
		_updateComponentMap(componentMap.keySet(), ignorePatterns, componentMap);
        pContext.getServletContext().setAttribute("components", componentMap);
        
    }

    public void contextDestroyed(ServletContextEvent pContext) {
        pContext.getServletContext().setAttribute("components", null);
    }
    
    private void _updateComponentMap(
    			Set<String> pComponents, List<String> pIgnorePatterns,
    			Map<String, TempssObject> pComponentMap) {
    	
    	Set<String> removeSet = new HashSet<String>();
    	for(String pattern : pIgnorePatterns) {
    		sLog.debug("Processing ignore pattern: <{}>", pattern);
    		if(pattern.endsWith("*")) {
    			String searchValue = pattern.substring(0, pattern.length()-1);
    			sLog.debug("Ignoring components beginning with <{}>", searchValue);
    			for(String id : pComponents) {
    				if(id.startsWith(searchValue)) {
    					removeSet.add(id);
    				}
    			}
    		}
    		else {
    			removeSet.add(pattern);
    		}
    	}
    	// The component IDs are a keySet obtained from the component Map. They
    	// maintain a two-way binding with the component map so calling
    	// removeAll on the keySet removes the associated items from the map
    	// pComponents.removeAll(removeSet);
    	// UPDATE Apr 17: Instead of removing items from component map we now lookup
    	// each item in the removeSet and set its ignore flag to true.
    	for(String id : removeSet) {
    		pComponentMap.get(id).setIgnore(true);
    	}
    }
    
    private File[] getResourceFiles(ServletContextEvent pContext, String pPath, 
    								final String pExtension, final String[] pIgnore)
    		throws MalformedURLException, URISyntaxException {
        // We can't simply get a list of all files in the classpath from the
        // classloader so we instead get access to the location of the current
        // class by accessing its URL and then construct the path to the file search 
        // location (e.g. META-INF/Template) where we can search for our files.
        Class<?> clazz = this.getClass();
        String className = clazz.getSimpleName() + ".class";
        sLog.debug("Class name: " + className);
        URL path = null;
        try {
            sLog.debug("Class URL: " + clazz.getResource(className).toString());
            path = new URL(clazz.getResource(className).toString());
        } catch (MalformedURLException e1) {
            sLog.error("Unable to get class URL to search for component property files.");
            throw e1;
        }

        String resourcePath = path.toString().substring(0,path.toString().indexOf("WEB-INF/classes/") + 16) + pPath;
        sLog.debug("resourcePath: " + resourcePath);

        URI resourcePathURI = null;
        try {
            resourcePathURI = new URI(resourcePath);
        } catch (URISyntaxException e1) {
            sLog.error("Unable to construct URI for template path to search for property files.");
            throw e1;
        }

        File[] resourceFiles = new File(resourcePathURI).listFiles(new FilenameFilter() {
            public boolean accept(File f, String name) {
                // Check if the file is in the ignore list
            	for(String filename : pIgnore) {
            		if(filename.equals(name)) return false;
            	}
            	return name.endsWith(pExtension);
            }
        });
        
        return resourceFiles;
    }
}
