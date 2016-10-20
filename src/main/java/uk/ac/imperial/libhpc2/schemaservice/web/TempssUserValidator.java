package uk.ac.imperial.libhpc2.schemaservice.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import uk.ac.imperial.libhpc2.schemaservice.web.dao.TempssUserDao;
import uk.ac.imperial.libhpc2.schemaservice.web.db.TempssUser;

/**
 * This is a custom validator for the TempssUser object which checks whether
 * the specified username already exists and whether the password and password 
 * confirmation match.
 *  
 * @author jhc02
 */
@Component
public class TempssUserValidator implements Validator {

	Logger sLog = LoggerFactory.getLogger(TempssUserValidator.class.getName());
	
	@Autowired
	TempssUserDao tempssUserDao;
	
	private static final String[] _ldapPrefixes = {"IC/"};
	
	@Override
	public boolean supports(Class<?> pClazz) {
		if(TempssUser.class.equals(pClazz)) {
			return true;
		}
		return false;
	}

	@Override
	public void validate(Object pObject, Errors pErrors) {
		TempssUser u = (TempssUser)pObject;
		// Do a username lookup to see if this user exists
		sLog.debug("Validating registration form: Doing lookup for existing "
				+ "user with name <{}>", u.getUsername());
		TempssUser uSearch = tempssUserDao.findByName(u.getUsername());
		if(! (uSearch == null) ) {
			pErrors.rejectValue("username", "USER_EXISTS", "A user with this "
					+ "username already exists. Please select a different "
					+ "username.");
		}
		
		for(String prefix : _ldapPrefixes) {
			if(u.getUsername().startsWith(prefix)) {
				pErrors.rejectValue("username", "INVALID_PREFIX", "A username "
						+ "cannot begin with the prefix " + prefix + ".");
				break;
			}
		}
		
		if(u.getUsername().contains("/")) {
			pErrors.rejectValue("username", "INVALID_CHARACTER", "A username "
					+ "cannot contain a '/' character.");
		}
		
		if(!u.getPassword().equals(u.getPassword2())) {
			if( (u.getPassword().length() >= 8)   && 
				(u.getPassword().length() <= 48)  && 
				(u.getPassword2().length() >= 8)  &&
			    (u.getPassword2().length() <= 48) ) {
				pErrors.rejectValue("password", "PASSWORD_MISMATCH",
                                "The two passwords entered did not match.");
			}
		}
	}

	
}
