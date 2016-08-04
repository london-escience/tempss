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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.security.core.AuthenticationException;

import uk.ac.imperial.libhpc2.schemaservice.web.dao.ProfileDao;
import uk.ac.imperial.libhpc2.schemaservice.web.db.Profile;
import uk.ac.imperial.libhpc2.schemaservice.web.db.TempssUser;

public class JdbcProfileDaoImpl implements ProfileDao {

	private static final Logger sLog = LoggerFactory.getLogger(JdbcProfileDaoImpl.class.getName());
	
	private JdbcTemplate _jdbcTemplate;
	private SimpleJdbcInsert _insertProfile;
	
	public void setDataSource(DataSource dataSource) {
		sLog.debug("Setting data source <" + dataSource + "> for profile data access object.");
		_jdbcTemplate = new JdbcTemplate(dataSource);
		_insertProfile = new SimpleJdbcInsert(_jdbcTemplate).withTableName("profile").usingGeneratedKeyColumns("id");
	}
	
	@Override
	public int add(Profile pProfile) {
		boolean pub = new Boolean(pProfile.getPublic());
		Map<String,String> rowParams = new HashMap<String, String>(5);
		rowParams.put("name", pProfile.getName());
		rowParams.put("templateId", pProfile.getTemplateId());
		rowParams.put("profileXml", pProfile.getProfileXml());
		rowParams.put("public", (pub) ? "1" : "0");
		rowParams.put("owner", pProfile.getOwner());
		Number id = _insertProfile.executeAndReturnKey(rowParams);
		return id.intValue();
	}
	
	@Override
	public int delete(String pTemplateId, String pProfileName, 
			          TempssUser pUser) {

		int rowsAffected = _jdbcTemplate.update("DELETE FROM profile WHERE "
				+ "templateId = ? AND name = ? and owner = ?", 
				new Object[] {pTemplateId, pProfileName, pUser.getUsername()});
		return rowsAffected;
	}
	
	/**
	 * Given the user name, we can findAll profiles that the user either owns, 
	 * or that are publicly accessible.
	 */
	@Override
	public List<Profile> findAll(TempssUser pUser) {
		String sql = "select * from profile WHERE public = ?";
		Object[] params = new Object[] {"1"};
		if(pUser != null) {
			sql += " OR owner = ?";
			params = new Object[] {"1", pUser.getUsername()};
		}
		
		List<Map<String,Object>> profileList = _jdbcTemplate.queryForList(
				sql, params); 
		
		List<Profile> profiles = new ArrayList<Profile>();
		
		for(Map<String,Object> data : profileList) {
			Profile p = new Profile(data);
			profiles.add(p);
		}
		
		sLog.debug("Found <{}> profiles", profiles.size());
		for(Profile p : profiles) {
			sLog.debug("Profile <{}>: {}", p.getName(), p.getProfileXml());
		}
		sLog.debug("Found <{}> profiles", profiles.size());
		
		return profiles;
	}
	
	@Override
	public Profile findByName(String pName, TempssUser pUser) {
		String sql = "select * from profile where name = ? and public = ?";
		Object[] params = new Object[] {pName, "1"};
		if(pUser != null) {
			sql = "select * from profile where name = ? and (public = ? "
					+ "or owner = ?)";
			params = new Object[] {pName, "1", pUser.getUsername()};
		}
		
		List<Map<String,Object>> profiles = _jdbcTemplate.queryForList(
				sql, params);
		
		Profile profile = null;
		if(profiles.size() > 0) {
			Map<String,Object> profileData = profiles.get(0);
			profile = new Profile(profileData);
		}
		if(profiles.size() > 1) {
			sLog.error("More than 1 profile with specified name <{}> found. "
					+ "Returning first instance.", pName);
		}
		
		if(profile == null) {
			sLog.debug("Profile with name <{}> not found.", pName);
			return null;
		}
		
		sLog.debug("Found profile with name <{}> and XML <{}>.", 
				profile.getName(), profile.getProfileXml());
		
		return profile;
	}

	@Override
	public List<Profile> findByTemplateId(String pTemplateId, 
			                              TempssUser pUser) {
		
		String sql = "select * from profile where templateId = ? and public = ?";
		Object[] params = new Object[] {pTemplateId, "1"};
		if(pUser != null) {
			sql = "select * from profile where templateId = ? and (public = ?"
					+ "or owner = ?)";
			params = new Object[] {pTemplateId, "1", pUser.getUsername()};
		}
		
		List<Map<String,Object>> profileList = _jdbcTemplate.queryForList(
				sql, params);
		
		if(profileList.size() == 0) {
			sLog.debug("Profiles for templateId <{}> and user <{}> not found.",
					pTemplateId, (pUser != null) ? pUser.getUsername() : "NONE");
			return null;
		}
		
		sLog.debug("Found <{}> profiles for template id <{}> and user <{}>.", 
				profileList.size(), pTemplateId, 
				(pUser != null) ? pUser.getUsername() : "NONE");
		
		List<Profile> profileResult = new ArrayList<Profile>();
		for(Map<String,Object> dbItem : profileList) {
			Profile p = new Profile(dbItem);
			profileResult.add(p);
		}
		
		return profileResult;
	} 
	
	/**
	 * Check if a profile name exists. This will check all registered profiles 
	 * to see if the specified name is available, regardless of whether the 
	 * currently authenticated user is able to access the profile of the 
	 * specified name or not. It is necessary to be authenticated to call this
	 * method.
	 */
	@Override
	public boolean profileNameAvailable(String pName, TempssUser pUser) 
		throws AuthenticationException {
		if(pUser == null) {
			throw new AuthenticationException("User must be authenticated to "
					+ "call this method.") {
			};
		}
		
		// Lookup the profile name to see if it exists.
		String sql = "select * from profile WHERE name = ?";
		Object[] param = new Object[] {pName};
		List<Map<String,Object>> profileList = _jdbcTemplate.queryForList(
				sql, param);
		
		if(profileList.size() == 0) {
			return true;
		}
		return false;
	}
}
