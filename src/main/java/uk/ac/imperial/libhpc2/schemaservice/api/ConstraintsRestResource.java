/*
 * Copyright (c) 2017, Imperial College London
 * Copyright (c) 2017, The University of Edinburgh
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

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.template.PebbleTemplate;

import uk.ac.ic.prism.jhc02.csp.AssignedVariable;
import uk.ac.ic.prism.jhc02.csp.BacktrackingSolver;
import uk.ac.ic.prism.jhc02.csp.CSPSolver;
import uk.ac.ic.prism.jhc02.csp.CSProblem;
import uk.ac.ic.prism.jhc02.csp.CSProblemDefinition;
import uk.ac.ic.prism.jhc02.csp.Constraint;
import uk.ac.ic.prism.jhc02.csp.Solution;
import uk.ac.ic.prism.jhc02.csp.Variable;
import uk.ac.imperial.libhpc2.schemaservice.ConstraintsException;
import uk.ac.imperial.libhpc2.schemaservice.UnknownTemplateException;

/**
 * Jersey REST class representing the template endpoint
 * @author jhc02
 *
 */
@Component
@Path("constraints")
public class ConstraintsRestResource {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(ConstraintsRestResource.class.getName());

    /**
     * ServletContext obejct used to access template data
     * Injected via @Context annotation
     */
    ServletContext _context;

    @Context
    public void setServletContext(ServletContext pContext) {
        this._context = pContext;
        LOG.debug("Servlet context injected: " + pContext);
    }

    // Use the pebble engine to render the HTML constraint info
	@Autowired
    private PebbleEngine _pebbleEngine;
	
	/**
	 * Renders some information about the specified templateId detailing the 
	 * variables and their domains, and describing the set of constraints 
	 * defined for this template. This information is returned as a JSON response
	 * with any HTML content provided under the HTML key of the JSON. 
	 *  
	 * @param templateId the ID of the template to get constraint information for.
	 * @return a JSON response containing details of variables and constraints. 
	 */
    @GET
    @Produces("application/json")
    @Path("{templateId}")
    public Response getConstraintInfoJSON(@PathParam("templateId")  String templateId) {
    	CSProblemDefinition problem = null;
    	try {
    		problem = TemplateResourceUtils.getConstraintData(templateId, this._context);
    	} catch (UnknownTemplateException e) {
    		LOG.error("Specified template ID <" + templateId + "> doesn't exist: " + e.getMessage());
    		return Response.status(Response.Status.NOT_FOUND).build();
    	} catch(ConstraintsException e) {
    		LOG.error("Error getting constraint info for template <" + templateId + 
    				">: " + e.getMessage());
    		return Response.status(Response.Status.NOT_FOUND).build();
    	}
    	
    	JSONObject responseJson = new JSONObject();
    	// Process the constraint satisfaction problem data to be returned to the caller as JSON.
    	List<Variable> vars = problem.getVariables();
    	List<Constraint> constraints = problem.getConstraints();
    	
    	// Define a map that we'll populate when iterating through vars for use when 
    	// preparing constraint data
    	Map<String, Variable> varMap = new HashMap<String, Variable>(vars.size());
    	// Build variables JSON object
    	JSONArray varArray = new JSONArray();
    	for(Variable v : vars) {
    		varMap.put(v.getName(), v);
    		JSONArray valArray = new JSONArray(v.getValues());
    		JSONObject varObject = new JSONObject();
    		try {
				varObject.put("name", v.getName());
				varObject.put("domain", valArray);
			} catch (JSONException e) {
				LOG.error("ERROR adding variable data to JSON object: " + e.getMessage());
			}
    		varArray.put(varObject);
    	}
    	
    	JSONArray constraintArray = new JSONArray();
    	for(Constraint c : constraints) {
    		try {
    			JSONObject constraintObject = new JSONObject();
	    		constraintObject.put("variable1", c.getVariable1FQName());
	    		constraintObject.put("variable2", c.getVariable2FQName());
	    		
	    		Variable v1 = varMap.get(c.getVariable1FQName());
	    		JSONArray mappingArray = new JSONArray();
				// For each value in v1, get all the valid mappings
	    		for(String value : v1.getValues()) {
	    			JSONObject mappingItem = new JSONObject();
	    			mappingItem.put("sourceVar", v1.getName());
	    			mappingItem.put("sourceValue", value);
	    			List<String> targetVals = c.getValidValues(v1.getName(), value);
	    			mappingItem.put("targetValues", new JSONArray(targetVals));
	    			mappingArray.put(mappingItem);
	    		}
	    		
	    		constraintObject.put("mappings", mappingArray);
	    		constraintArray.put(constraintObject);
    		} catch (JSONException e) {
				LOG.error("ERROR converting constraint <{}> JSON object: {}",
						 c.getName(), e.getMessage());
			}
    	}
    	
    	try {
    		responseJson.put("variables", varArray);
    		responseJson.put("constraints", constraintArray);
		} catch (JSONException e) {
			LOG.error("ERROR adding variable array to response JSON object: " + e.getMessage());
		}
    	
    	return Response.ok(responseJson.toString(), MediaType.APPLICATION_JSON).build();
    }
    
	/**
	 * Renders some information about the specified templateId detailing the 
	 * variables and their domains, and describing the set of constraints 
	 * defined for this template. This is rendered as plain HTML.
	 *  
	 * @param templateId the ID of the template to get constraint information for.
	 * @return a JSON response containing details of variables and constraints. 
	 */
    @GET
    @Produces("text/html")
    @Path("{templateId}")
    public Response getConstraintInfoHTML(@PathParam("templateId")  String templateId) {
    	CSProblemDefinition problem = null;
    	try {
    		problem = TemplateResourceUtils.getConstraintData(templateId, this._context);
    	} catch (UnknownTemplateException e) {
    		LOG.error("Specified template ID <" + templateId + "> doesn't exist: " + e.getMessage());
    		return Response.status(Response.Status.NOT_FOUND).build();
    	} catch(ConstraintsException e) {
    		LOG.error("Error getting constraint info for template <" + templateId + 
    				">: " + e.getMessage());
    		return Response.status(Response.Status.NOT_FOUND).build();
    	}
    	
    	String templateName = problem.getSolverName();
  
      	// Process the constraint satisfaction problem data to be returned to the caller as HTML.
    	List<Variable> vars = problem.getVariables();
    	List<Constraint> constraints = problem.getConstraints();
    	
    	// Define a map that we'll populate when iterating through vars for use when 
    	// preparing constraint data
    	Map<String, Variable> varMap = new HashMap<String, Variable>(vars.size());
    	List<String> varNames = new ArrayList<String>(vars.size());
    	Map<String,String> varFQNameLocalNameMap = new HashMap<String,String>(vars.size());
    	Map<String,List<String>> varDomains = new HashMap<String, List<String>>(vars.size());
    	
    	// FIXME: StringBuffer in which the HTML content will be built
    	// Rather than building a StringBuffer here, we need instead to built a context dict
    	// that is then passed to the template in order to carry out the proper rendering process.
    	
    	
    	StringBuffer htmlBuffer = new StringBuffer("<h3>Variables</h3>\n");
    	
    	for(Variable v : vars) {
    		varMap.put(v.getName(), v);
    		varNames.add(v.getName());
    		varFQNameLocalNameMap.put(v.getName(), v.getLocalName());
    		varDomains.put(v.getName(), v.getValues());
    		htmlBuffer.append("<div>Variable: <b>" + v.getName() + "</b>\n<div>");
    		for(String val : v.getValues()) {
    			htmlBuffer.append("<span>" + val + "</span><br/>");
    		}
    	}
    	
    	Map<String,String[]> constraintData = new HashMap<String, String[]>(constraints.size());
    	Map<String, Map<String, List<String>>> constraintMappings = new HashMap<String, Map<String, List<String>>>(constraints.size());
    	for(Constraint c : constraints) {
    		String var1 = c.getVariable1FQName();
    		String var2 = c.getVariable2FQName();
    		constraintData.put(var1 + "<->" + var2, new String[] {var1, var2});
    		List<String> varValues = varMap.get(var1).getValues();
    		Map<String, List<String>> valueMap = new HashMap<String, List<String>>(varValues.size());
    		for(String varValue : varValues) {
    			valueMap.put(varValue, new ArrayList<String>(c.getValidValues(var1, varValue)));
    		}
    		constraintMappings.put(var1 + "<->" + var2, valueMap);
    	}
    	
    	// Render the template
    	Map<String, Object> templateContext = new HashMap<String, Object>();
    	templateContext.put("templateId", templateId);
    	templateContext.put("templateName", templateName);
    	templateContext.put("variables", varNames);
    	templateContext.put("variableNameMap", varFQNameLocalNameMap);
    	templateContext.put("variableDomains", varDomains);
    	templateContext.put("constraints", constraintData);
    	templateContext.put("constraintMappings", constraintMappings);
    	
    	StringWriter sw = new StringWriter();
    	try {
    		PebbleTemplate tpl = this._pebbleEngine.getTemplate("constraint_info");
    		tpl.evaluate(sw, templateContext);
    	} catch (PebbleException e) {
			LOG.error("Unable to get constraint info template for rendering: " + e.getMessage());
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		} catch (IOException e) {
			LOG.error("IO error during template rendering: " + e.getMessage());
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
		
    	String responseStr = sw.toString();
    	
    	return Response.ok(responseStr, MediaType.TEXT_HTML).build();
    }
    
    @POST
    @Produces("application/json")
    @Path("{templateId}/solver")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response solveConstraintValues(@PathParam("templateId") String templateId,
    		MultivaluedMap<String, String> dataParams) {
    	StringBuffer sb = new StringBuffer();
    	for(String key : dataParams.keySet()) {
    		sb.append("Key: " + key + "    Value: " + dataParams.getFirst(key) + "\n");
    	}
    	LOG.debug("Request to solve constraint values for template <{}>\n{}", templateId, sb.toString());
    	
    	// Now process the retrieved data and call the solver
    	CSProblemDefinition definition = null;
    	try {
    		definition = TemplateResourceUtils.getConstraintData(templateId, this._context);
		} catch (UnknownTemplateException e) {
			LOG.error("The template with ID <{}> could not be found: " + e.getMessage());
			return Response.status(Status.NOT_FOUND).build();
		} catch(ConstraintsException e) {
			LOG.error("Error getting constraint problem definition: " + e.getMessage());
			return Response.serverError().build();
		}
    	
    	List<AssignedVariable> avList = new ArrayList<AssignedVariable>();
    	// Now go through the variables 
    	for(String key : dataParams.keySet()) {
    		if(!dataParams.getFirst(key).equals("NONE")) {
    			LOG.debug("Handling initial value for variable <{}>", key);
    			Variable v = definition.getVariable(key);
    			avList.add(new AssignedVariable(v, dataParams.getFirst(key)));
    		}
    	}
    	CSProblem problem = new CSProblem(definition, avList);
    	CSPSolver solver = new BacktrackingSolver(problem);
    	List<Solution> solutions = solver.solve();
    	
    	// Process the list of solutions from the solver
    	Map<String, Set<String>> results = processSolutions(solutions);
    	JSONObject responseJson = new JSONObject();
    	try {
    		JSONArray varItemArray = new JSONArray();
	    	for(String varName : results.keySet()) {
	    		JSONObject varItem = new JSONObject();
	    		varItem.put("variable", varName);
	    		JSONArray varValues = new JSONArray(results.get(varName));
	    		varItem.put("values", varValues);
	    		varItemArray.put(varItem);
	    	}
	    	responseJson.put("solutions", varItemArray);
	    	responseJson.put("result", "OK");
    	} catch(JSONException e) {
    		LOG.error("Unable to create JSON object for variable results...");
    		return Response.serverError().build();
    	}

    	return Response.ok(responseJson.toString(), MediaType.APPLICATION_JSON).build();
    }
    
    private Map<String, Set<String>> processSolutions(List<Solution> pSolutions) {
    	Map<String, Set<String>> varMappings = new HashMap<String,Set<String>>();
    	for(Solution s : pSolutions) {
    		for(AssignedVariable av : s.getSolution()) {
    			String avName = av.getName();
    			if(!varMappings.containsKey(avName)) {
    				varMappings.put(avName, new HashSet<String>());
    			}
    			varMappings.get(avName).add(av.getValue());
    		}
    	}
    	return varMappings;
    }

}