package uk.ac.imperial.libhpc2.schemaservice.web.dao.impl;

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
import org.springframework.stereotype.Repository;

import uk.ac.imperial.libhpc2.schemaservice.web.dao.ProfileDao;
import uk.ac.imperial.libhpc2.schemaservice.web.db.Profile;

@Repository
public class JdbcProfileDaoImpl implements ProfileDao {

	private static final Logger sLog = LoggerFactory.getLogger(JdbcProfileDaoImpl.class.getName());
	
	private JdbcTemplate _jdbcTemplate;
	private SimpleJdbcInsert _insertProfile;
	
	@Autowired
	public void setDataSource(DataSource dataSource) {
		sLog.debug("Setting data source for profile data access object.");
		_jdbcTemplate = new JdbcTemplate(dataSource);
		_insertProfile = new SimpleJdbcInsert(_jdbcTemplate).withTableName("profile").usingGeneratedKeyColumns("id");
	}
	
	public int add(Profile pProfile) {
		Map<String,String> rowParams = new HashMap<String, String>(2);
		rowParams.put("name", pProfile.getName());
		rowParams.put("profileXml", pProfile.getProfileXml());
		Number id = _insertProfile.executeAndReturnKey(rowParams);
		return id.intValue();
	}
	
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
	
}
