package uk.ac.imperial.libhpc2.schemaservice.security;

import java.util.Collection;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

import uk.ac.imperial.libhpc2.schemaservice.web.dao.TempssUserDao;
import uk.ac.imperial.libhpc2.schemaservice.web.db.TempssUser;
import uk.ac.imperial.libhpc2.schemaservice.web.service.TempssUserDetails;

public class TempssLdapContextMapper implements UserDetailsContextMapper {

	Logger sLog = LoggerFactory.getLogger(TempssLdapContextMapper.class.getName());
	
	@Autowired
	TempssUserDao tempssUserDao;
	
	@Override
	public UserDetails mapUserFromContext(DirContextOperations pCtx, 
			                              String pUsername,
		  Collection<? extends GrantedAuthority> pAuthorities) {
		
		String password = RandomStringUtils.random(48);
		String email = pCtx.getStringAttribute("mail");
		String firstname = pCtx.getStringAttribute("givenName");
		String lastname = pCtx.getStringAttribute("sn");
		
		// We don't want to store the user's password so we just generate a 
		// long random string and dump that in place of the password.
		
		
		TempssUser user = new TempssUser("IC/" + pUsername, password, email, 
				                         firstname, lastname, false, false);
		
		// Here we check whether a record for the user exists in the DB. If 
		// not, we create the user record here setting it to non-activated.
		TempssUser uSearch = tempssUserDao.findByName("IC/" + pUsername);
		if(uSearch == null) {
			sLog.debug("We have a login from an LDAP user who has not " +
					"previously logged in to the system, creating a new " +
					"user record and setting it inactive.");
			tempssUserDao.add(user);
		}
		else {
			sLog.debug("We already have a DB record for user <{}>", pUsername);
		}
		
		TempssUserDetails userDetails = new TempssUserDetails(user, pAuthorities);
		return userDetails;
	}

	@Override
	public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
	}

}
