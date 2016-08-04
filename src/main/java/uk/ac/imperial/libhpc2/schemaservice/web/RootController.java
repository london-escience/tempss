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

package uk.ac.imperial.libhpc2.schemaservice.web;

import java.security.Principal;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.mitchellbosecke.pebble.PebbleEngine;

import uk.ac.imperial.libhpc2.schemaservice.web.dao.ProfileDao;
import uk.ac.imperial.libhpc2.schemaservice.web.db.Profile;
import uk.ac.imperial.libhpc2.schemaservice.web.db.TempssUser;
import uk.ac.imperial.libhpc2.schemaservice.web.service.TempssUserDetails;

@Controller
public class RootController {

	private static final Logger sLog = LoggerFactory.getLogger(RootController.class.getName());
	
	@Autowired
	private ProfileDao profileDao;
	
	// For debugging purposes, we get access to the pebble engine and
	// empty the template cache on each call so that we don't have to
	// restart the tomcat server to rebuild a changed template on a reload
	@Autowired
	private PebbleEngine pebbleEngine;
	
	@RequestMapping("/")
    public ModelAndView index(Model pModel, 
    						  @AuthenticationPrincipal Principal principal,
    						  HttpServletRequest pRequest) {
		
		sLog.debug("Processing root controller request for access to /");
				
		TempssUserDetails userDetails = null;
		TempssUser user = null;
		if(principal != null) {
			userDetails = (TempssUserDetails) ((Authentication) principal).getPrincipal();
			user = userDetails.getUser();
		}
		
		
		sLog.debug("Value of user principal: {}", user);
		//sLog.debug("Value of auth: {}", auth);
		//sLog.debug("Value of auth.isAuthenticated(): {}", auth.isAuthenticated());
		
//		if(auth != null && auth.isAuthenticated() && 
//				!(auth instanceof AnonymousAuthenticationToken)) {
//			activeUser = (User)auth.getPrincipal();
//		}
//		sLog.debug("Value of activeUser: {}", activeUser);
		
		if(user != null) {
			sLog.debug("We have a user with username: {}", user.getUsername()); 
		}
		
		String tokenKey = CsrfToken.class.getName();
		CsrfToken token = (CsrfToken)pRequest.getAttribute(tokenKey);
		
		pebbleEngine.getTemplateCache().invalidateAll();
		
        ModelAndView mav = new ModelAndView("index");
        
        mav.addObject("_csrf", token);
        
        List<Profile> profiles = profileDao.findAll(user);
        
        mav.addObject("firstname", "TemPSS");
        mav.addObject("surname", "Team");
        mav.addObject("profiles", profiles);
        mav.addObject("user", userDetails);
        return mav;
    }
	
	@RequestMapping("/logout")
	public String logout(HttpServletRequest req, HttpServletResponse resp) {
		sLog.debug("Request to log user out...");
	    Authentication auth = 
	    		SecurityContextHolder.getContext().getAuthentication();
	    if (auth != null) {
	    	sLog.debug("The user is logged in, logging user out.");
	        new SecurityContextLogoutHandler().logout(req, resp, auth);
	        return "redirect:/login?logout";
	    }
	    else{
	    	sLog.debug("The user was not logged in.");
	    }
	    return "redirect:/tempss/profiles";
	}
	
	@RequestMapping("/about")
	public ModelAndView about(Model pModel) {
	
		ModelAndView mav = new ModelAndView("about");
		return mav;
	}
	
	@RequestMapping("/docs")
	public ModelAndView docs(Model pModel) {
	
		ModelAndView mav = new ModelAndView("docs");
		return mav;
	}
	
	@RequestMapping("/contact")
	public ModelAndView contact(Model pModel) {
	
		ModelAndView mav = new ModelAndView("contact");
		return mav;
	}

	@RequestMapping("/login-test")
	public ModelAndView loginTest(Model pModel,
								  @AuthenticationPrincipal Principal principal){
		
		sLog.debug("Processing root controller request for login-test page");
		
		User activeUser = (User) ((Authentication) principal).getPrincipal();
		
		pebbleEngine.getTemplateCache().invalidateAll();
		
        ModelAndView mav = new ModelAndView("login-test");
        
        mav.addObject("firstname", "TemPSS");
        mav.addObject("surname", "Team");
        mav.addObject("user", activeUser);
        return mav;
    }
	
	@RequestMapping(value="/*")
    public ModelAndView redirectHome() {
        return new ModelAndView("redirect:/profiles/");
    }

}
