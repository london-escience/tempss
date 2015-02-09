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
		Profile profile = _jdbcTemplate.queryForObject("select * from profile where name = ?", 
				Profile.class, pName);	
		
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
