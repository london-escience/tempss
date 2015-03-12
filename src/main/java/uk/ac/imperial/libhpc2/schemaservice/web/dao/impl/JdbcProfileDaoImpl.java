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
import java.util.Set;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import uk.ac.imperial.libhpc2.schemaservice.web.dao.ProfileDao;
import uk.ac.imperial.libhpc2.schemaservice.web.db.Profile;

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
		Map<String,String> rowParams = new HashMap<String, String>(2);
		rowParams.put("name", pProfile.getName());
		rowParams.put("templateId", pProfile.getTemplateId());
		rowParams.put("profileXml", pProfile.getProfileXml());
		Number id = _insertProfile.executeAndReturnKey(rowParams);
		return id.intValue();
	}
	
	@Override
	public int delete(String pTemplateId, String pProfileName) {
		int rowsAffected = _jdbcTemplate.update("DELETE FROM profile WHERE templateId = ? AND name = ?", new Object[] {pTemplateId, pProfileName});
		return rowsAffected;
	}
	
	@Override
	public List<Profile> findAll() {
		//List<Profile> profiles = _jdbcTemplate.queryForList("select * from profile", Profile.class);
		List<Map<String,Object>> profileDataList = _jdbcTemplate.queryForList("select * from profile");
		List<Profile> profiles = new ArrayList<Profile>();
		
		for(Map<String,Object> data : profileDataList) {
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
	public Profile findByName(String pName) {
		List<Map<String,Object>> profiles = _jdbcTemplate.queryForList("select * from profile where name = ?", pName);	
		Profile profile = null;
		if(profiles.size() > 0) {
			Map<String,Object> profileData = profiles.get(0);
			profile = new Profile(profileData);
		}
		if(profiles.size() > 1) {
			sLog.error("More than 1 profile with specified name <{}> found. Returning first instance.", pName);
		}
		
		if(profile == null) {
			sLog.debug("Profile with name <{}> not found.", pName);
			return null;
		}
		
		sLog.debug("Found profile with name <{}> and XML <{}>.", profile.getName(), profile.getProfileXml());
		
		return profile;
	}

	@Override
	public List<Profile> findByTemplateId(String pTemplateId) {
		List<Map<String,Object>> profileList = _jdbcTemplate.queryForList(
				"select * from profile where templateId = ?", pTemplateId);	
		
		if(profileList.size() == 0) {
			sLog.debug("Profiles for templateId <{}> not found.", pTemplateId);
			return null;
		}
		
		sLog.debug("Found <{}> profiles for template id <{}>.", profileList.size(), pTemplateId);
		
		List<Profile> profileResult = new ArrayList<Profile>();
		for(Map<String,Object> dbItem : profileList) {
			Profile p = new Profile(dbItem);
			profileResult.add(p);
		}
		
		return profileResult;
	}
	
}
