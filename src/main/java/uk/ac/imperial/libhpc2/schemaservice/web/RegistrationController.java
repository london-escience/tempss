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

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import uk.ac.imperial.libhpc2.schemaservice.web.dao.TempssUserDao;
import uk.ac.imperial.libhpc2.schemaservice.web.db.TempssUser;

@Controller
public class RegistrationController {

	private static final Logger sLog = LoggerFactory.getLogger(RegistrationController.class.getName());

	@Autowired
	private TempssUserValidator tempssUserValidator;
	
	@Autowired
	private TempssUserDao tempssUserDao;
	
	@RequestMapping(value="/register", method=RequestMethod.GET)
	public ModelAndView registrationForm(HttpServletRequest pRequest) {
		
		String tokenKey = CsrfToken.class.getName();
		CsrfToken token = (CsrfToken)pRequest.getAttribute(tokenKey);
		
		ModelAndView mav = new ModelAndView("jsp/register");
		mav.addObject("_csrf", token);
		mav.addObject("tempssUser", new TempssUser());
        
        return mav;
	}

	@RequestMapping(value="/register", method=RequestMethod.POST)
	public ModelAndView registrationSubmit(@Valid @ModelAttribute("tempssUser") TempssUser pUser,
			BindingResult pResult, Model pModel) {
		
		sLog.debug("Received tempss user object: {}", pUser);
		
		tempssUserValidator.validate(pUser, pResult);
		
		// If we have errors then return the form with the errors
		if(pResult.hasErrors()) {
			ModelAndView errorMav = new ModelAndView("jsp/register");
			errorMav.addObject("tempssUser", pUser);
			sLog.debug("We have form submission errors...");
			return errorMav;
		}
		
		// Since we're registering a user manually and the user has agreed to 
		// register, we set them to be activated by default
		pUser.setActivated(true);
		
		// Now store the user to the DB
		tempssUserDao.add(pUser);
		
		ModelAndView mav = new ModelAndView("jsp/registered");
		mav.addObject("tempssUser", pUser);
		
        return mav;
	}
	
	@RequestMapping(value="/signin", method=RequestMethod.GET)
	public ModelAndView signinForm(HttpServletRequest pRequest) {
		
		String tokenKey = CsrfToken.class.getName();
		CsrfToken token = (CsrfToken)pRequest.getAttribute(tokenKey);
		
		ModelAndView mav = new ModelAndView("jsp/signin");
		mav.addObject("_csrf", token);
		mav.addObject("tempssUser", new TempssUser());
        
        return mav;
	}
}
