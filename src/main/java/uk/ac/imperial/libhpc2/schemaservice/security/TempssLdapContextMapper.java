package uk.ac.imperial.libhpc2.schemaservice.security;

import java.util.Collection;

import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

import uk.ac.imperial.libhpc2.schemaservice.web.db.TempssUser;
import uk.ac.imperial.libhpc2.schemaservice.web.service.TempssUserDetails;

public class TempssLdapContextMapper implements UserDetailsContextMapper {

	@Override
	public UserDetails mapUserFromContext(DirContextOperations pCtx, 
			                              String pUsername,
		  Collection<? extends GrantedAuthority> pAuthorities) {
		
		String password = "";
		String email = pCtx.getStringAttribute("mail");
		String firstname = pCtx.getStringAttribute("givenName");
		String lastname = pCtx.getStringAttribute("sn");
		
		TempssUser user = new TempssUser(pUsername, password, email, 
				                         firstname, lastname);
		TempssUserDetails userDetails = new TempssUserDetails(user, pAuthorities);
		return userDetails;
	}

	@Override
	public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
	}

}
