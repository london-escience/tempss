package uk.ac.imperial.libhpc2.schemaservice.web.dao;

import java.util.List;

import uk.ac.imperial.libhpc2.schemaservice.web.db.TempssUser;

public interface TempssUserDao {
	
	public int add(TempssUser pUser);
	
	public int delete(String pUsername);
	
	public List<TempssUser> findAll();
	
	public TempssUser findByName(String pUsername);
	
	public int activateUser(TempssUser pUser);
	
	public int deactivateUser(TempssUser pUser);

}
