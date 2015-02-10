package uk.ac.imperial.libhpc2.schemaservice.web.dao;

import java.util.List;

import uk.ac.imperial.libhpc2.schemaservice.web.db.Profile;

public interface ProfileDao {

	public int add(Profile pProfile);
	
	public int delete(String pTemplateId, String pProfileName);
	
	public List<Profile> findAll();
	
	public Profile findByName(String pName);
	
	public List<Profile> findByTemplateId(String pTemplateId);

}
