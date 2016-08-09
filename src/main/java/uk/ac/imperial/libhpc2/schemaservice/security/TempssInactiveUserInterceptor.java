package uk.ac.imperial.libhpc2.schemaservice.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import uk.ac.imperial.libhpc2.schemaservice.web.db.TempssUser;
import uk.ac.imperial.libhpc2.schemaservice.web.service.TempssUserDetails;

public class TempssInactiveUserInterceptor extends HandlerInterceptorAdapter {

	private static final Logger sLog = LoggerFactory.getLogger(TempssInactiveUserInterceptor.class.getName());
	
	@Override
	public void postHandle(HttpServletRequest request, 
			               HttpServletResponse response, 
			               Object handler,
			               ModelAndView modelAndView) throws Exception {
		
		// In this interceptor handler method we check if there is an 
		// authenticated user and whether that user is activated. If the user
		// is logged in but not activated, we return the activation page view
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		sLog.debug("InactiveUserInterceptor: We have the following auth object: {}", auth);
		if(auth instanceof UsernamePasswordAuthenticationToken) {
			TempssUserDetails userDetails = (TempssUserDetails)auth.getPrincipal();
			TempssUser user = userDetails.getUser();
			if(!user.getActivated()) {
				String tokenKey = CsrfToken.class.getName();
				CsrfToken token = (CsrfToken)request.getAttribute(tokenKey);
				
				sLog.debug("Interceptor handling request from user <{}> who " +
						"is not activated. Returning activation page view.",
						user.getUsername());
				modelAndView.addObject("_csrf", token);
				modelAndView.setViewName("activation");
			}
		}
	}
}
