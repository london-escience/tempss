package uk.ac.imperial.libhpc2.schemaservice.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfToken;

import uk.ac.imperial.libhpc2.schemaservice.web.service.TempssUserDetails;

public class TempssAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private static final Logger sLog = LoggerFactory.getLogger(TempssAuthSuccessHandler.class.getName());
	
	@Autowired
	private UsernamePasswordAuthenticationFilter authFilter;
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, 
										HttpServletResponse response,
										Authentication auth) 
										throws IOException, ServletException {
		
		String tokenKey = CsrfToken.class.getName();
		CsrfToken token = (CsrfToken)request.getAttribute(tokenKey);
		sLog.debug("CSRF token: <{}>", token);
		
		String user = request.getParameter(authFilter.getUsernameParameter());
		sLog.debug("Handling successful auth request for user <{}>...", user);
		
		TempssUserDetails details = (TempssUserDetails) auth.getPrincipal();
		String firstname = details.getUser().getFirstname();
		String lastname = details.getUser().getLastname();
		
		// We're not redirecting since we want to send back a JSON response so
		// instead we prepare some JSON to send back and clear an auth data
		// left over in the session.
		clearAuthenticationAttributes(request);
		
		response.setHeader("X-CSRF-TOKEN", token.getToken());
		
		response.setContentType("application/json");
		response.getWriter().write("{\"result\":\"OK\",\"firstname\":\"" + 
				firstname + "\", \"lastname\":\"" + lastname + "\"}");
		response.getWriter().flush();
		response.getWriter().close();
	}

}
