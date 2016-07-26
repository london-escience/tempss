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

package uk.ac.imperial.libhpc2.schemaservice.web.db;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class TempssUser {
	
	@NotBlank
	private String _username;
	
	@NotNull
	@Size(min = 8, max = 48)
	private String _password;
	
	@NotNull
	@Email
	private String _email;
	
	@NotBlank
	private String _firstname;
	
	private String _lastname;
	
	public TempssUser() { } 
	
	public TempssUser(String pUsername, String pPassword, String pEmail, 
                      String pFirstname, String pLastname) { 
		this._username = pUsername;
		this._password = pPassword;
		this._email = pEmail;
		this._firstname = pFirstname;
		this._lastname = pLastname;
		
	}

	public String getUsername() {
		return _username;
	}

	public void setUsername(String pUsername) {
		this._username = pUsername;
	}
	
	public String getPassword() {
		return _password;
	}

	public void setPassword(String pPassword) {
		// Hash the incoming password to generate the hash that we'll store
		PasswordEncoder encoder = new BCryptPasswordEncoder();
		String hashedPassword = encoder.encode(pPassword);
		this._password = hashedPassword;
	}
	
	public String getEmail() {
		return _email;
	}

	public void setEmail(String pEmail) {
		this._email = pEmail;
	}

	public String getFirstname() {
		return _firstname;
	}

	public void setFirstname(String pFirstname) {
		this._firstname = pFirstname;
	}

	public String getLastname() {
		return _lastname;
	}

	public void setLastname(String pLastname) {
		this._lastname = pLastname;
	}
}
