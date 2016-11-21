package uk.ac.imperial.libhpc2.schemaservice.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class TempssAuthFailureHandler extends SimpleUrlAuthenticationFailureHandler {

	private static final Logger sLog = LoggerFactory.getLogger(TempssAuthFailureHandler.class.getName());
	
	@Autowired
	private UsernamePasswordAuthenticationFilter authFilter;
	
	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		
		// To support AJAX authentication, the redirect has been replaced
		// with returning a 401 response when authentication fails.
		// Setup the redirect URL
		// this.setDefaultFailureUrl("/profiles/signin");
		// super.onAuthenticationFailure(request, response, exception);
		
		// Now get the name used for the username parameter from the username/ 
		// password filter and use this to get username from the request obj.
		String username = request.getParameter(authFilter.getUsernameParameter()); 
		sLog.debug("Username from request object: {}", username);
		
		
		request.getSession(isAllowSessionCreation()
				).setAttribute("PREVIOUS_USERNAME", username);
		
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType("application/json");
		response.getWriter().write("{\"result\":\"ERROR\",\"errorMsg\":\"Invalid login credentials entered. Please try again.\"}");
		response.getWriter().flush();
		response.getWriter().close();
	}

}
