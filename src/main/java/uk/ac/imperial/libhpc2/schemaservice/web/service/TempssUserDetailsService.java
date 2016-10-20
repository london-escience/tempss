package uk.ac.imperial.libhpc2.schemaservice.web.service;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import uk.ac.imperial.libhpc2.schemaservice.web.dao.TempssUserDao;
import uk.ac.imperial.libhpc2.schemaservice.web.db.TempssUser;

@Component
public class TempssUserDetailsService implements UserDetailsService {

	@Autowired
	private TempssUserDao tempssUserDao;
	
	@Override
	public UserDetails loadUserByUsername(String pUsername) 
			throws UsernameNotFoundException, DataAccessException {
		// Lookup the user here and then return a custom user details
		// object containing all our required user data.
		TempssUser u = tempssUserDao.findByName(pUsername);
		if(u == null) {
			throw new UsernameNotFoundException("User " + pUsername +
					" does not exist.");
		}
		
		// TODO: Add functionality to store and access user roles. For now just
		// set this to NO_ROLE.
		SimpleGrantedAuthority no_role = new SimpleGrantedAuthority("NO_ROLE");
		Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		authorities.add(no_role);

		return new TempssUserDetails(u, authorities);
	}

}
