package uk.ac.imperial.libhpc2.schemaservice.security;

import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * A request matcher to control which endpoints have CSRF protection applied to them.
 * This switches off CSRF protection for a subset of the REST API endpoints.
 * @author jhc02
 */
public class TempssCsrfRequestMatcher implements RequestMatcher {

	// Retain default allowed methods - CSRF protection is not applied to these
	private Pattern unprotectedMethods = Pattern.compile("^(GET|TRACE|HEAD|OPTIONS)$");
	// Set up the unprotected path(s) for which we want to disable CSRF protection
	//private RegexRequestMatcher requestMatcher = new RegexRequestMatcher("/tempss/api/profile/*/convert", null);
	private AntPathRequestMatcher requestMatcher = new AntPathRequestMatcher("/api/profile/*/convert", null);
	
	/**
	 * Return false if CSRF protection is not to be applied, true if it is.
	 * First check if the request method is one of the allowed methods, 
	 * then check if the path of the request matches the unprotected path(s) 
	 */
	@Override
	public boolean matches(HttpServletRequest pRequest) {
		if(unprotectedMethods.matcher(pRequest.getMethod()).matches()) {
			return false;
		}
		
		if(requestMatcher.matches(pRequest)) {
			return false;
		}
		
		return true;
	}

}
