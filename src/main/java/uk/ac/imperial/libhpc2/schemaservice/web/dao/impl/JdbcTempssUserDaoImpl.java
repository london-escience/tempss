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

package uk.ac.imperial.libhpc2.schemaservice.web.dao.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.security.crypto.password.PasswordEncoder;

import uk.ac.imperial.libhpc2.schemaservice.web.dao.TempssUserDao;
import uk.ac.imperial.libhpc2.schemaservice.web.db.TempssUser;

public class JdbcTempssUserDaoImpl implements TempssUserDao {

	private static final Logger sLog = LoggerFactory.getLogger(JdbcTempssUserDaoImpl.class.getName());
	
	private JdbcTemplate _jdbcTemplate;
	private SimpleJdbcInsert _insertProfile;

	@Autowired
	private PasswordEncoder passwordEncoder;
	
	public void setDataSource(DataSource dataSource) {
		sLog.debug("Setting data source <" + dataSource + "> for tempssuser data access object.");
		_jdbcTemplate = new JdbcTemplate(dataSource);
		_insertProfile = new SimpleJdbcInsert(_jdbcTemplate).withTableName("user").usingGeneratedKeyColumns("id");
	}
	
	@Override
	public int add(TempssUser pUser) {
		// Hash the password to store it in the DB
		String hashedPassword = passwordEncoder.encode(pUser.getPassword());
		
		Map<String,Object> rowParams = new HashMap<String, Object>(8);
		rowParams.put("username", pUser.getUsername());
		rowParams.put("password", hashedPassword);
		rowParams.put("email", pUser.getEmail());
		rowParams.put("firstname", pUser.getFirstname());
		rowParams.put("lastname", pUser.getLastname());
		rowParams.put("locked", (pUser.getLocked() == true) ? "1" : "0");
		rowParams.put("activated", (pUser.getActivated() == true) ? "1" : "0");
		if(pUser.getActivationTime() != null) {
			// Don't need to call usingColumns since we're using all cols
			// Set reg time to same as activation time it may be unset
			rowParams.put("regtime", pUser.getActivationTime());
			rowParams.put("acttime", pUser.getActivationTime());
		}
		else {
			_insertProfile.usingColumns("username", "password", "email", 
					"firstname", "lastname", "locked", "activated");
		}
		Number id = _insertProfile.executeAndReturnKey(rowParams);
		return id.intValue();
	}
	
	@Override
	public int delete(String pUsername) {
		int rowsAffected = _jdbcTemplate.update("DELETE FROM user WHERE username = ?", pUsername);
		return rowsAffected;
	}
	
	@Override
	public List<TempssUser> findAll() {
		List<Map<String,Object>> userList = _jdbcTemplate.queryForList("select * from user");
		List<TempssUser> users = new ArrayList<TempssUser>();
		
		for(Map<String,Object> data : userList) {
			TempssUser u = new TempssUser((String)data.get("username"), 
					                      (String)data.get("password"),
					                      (String)data.get("email"),
					                      (String)data.get("firstname"),
					                      (String)data.get("lastname"),
					                      Boolean.parseBoolean((String)data.get("locked")),
					                      Boolean.parseBoolean((String)data.get("activated")),
					                      (Timestamp)data.get("registrationTime"),
					                      (Timestamp)data.get("activationTime"));
			users.add(u);
		}
		
		sLog.debug("Found <{}> users", users.size());
		for(TempssUser u : users) {
			sLog.debug("User <{}>: Email: {}, Firstname: {}, Lastname: {}, "
					+ "Locked <{}>, Activated: <{}>", 
					u.getUsername(), u.getEmail(), u.getFirstname(), 
					u.getLastname(), u.getLocked(), u.getActivated());
		}
		
		return users;
	}
	
	@Override
	public TempssUser findByName(String pUsername) {
		TempssUser user = null;
		List<Map<String,Object>> users = 
				_jdbcTemplate.queryForList(
						"select * from user where username = ?", pUsername);
		
		if(users.size() == 1) {
			Map<String,Object> userData = users.get(0);
			boolean locked = true;
			boolean activated = false;
			Object lockedObj = userData.get("locked");
			Object activatedObj = userData.get("activated");
			if( (lockedObj != null) && (((Integer)lockedObj) == 0)) {
				locked = false;
			}
			if( (activatedObj != null) && (((Integer)activatedObj) == 1)) {
				activated = true;
			}
			user = new TempssUser((String)userData.get("username"),
					(String)userData.get("password"),
					(String)userData.get("email"),
					(String)userData.get("firstname"),
					(String)userData.get("lastname"),
					locked,
                    activated,
                    (Timestamp)userData.get("registrationTime"),
                    (Timestamp)userData.get("activationTime"));
		}
		else if(users.size() > 1) {
			sLog.error("ERROR: More than 1 user with name <{}> found.", 
					pUsername);
			return null;
		}
		
		if(user == null) {
			sLog.debug("User with name <{}> not found.", pUsername);
			return null;
		}
			
		
		sLog.debug("Found user with name <{}> and email <{}>.", 
				user.getUsername(), user.getEmail());
		
		return user;
	}

}
