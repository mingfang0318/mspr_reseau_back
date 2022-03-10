package epsi.mspr.mingf.security.jwt;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import epsi.mspr.mingf.security.services.LoginAttemptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

	private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);
	@Autowired
	private LoginAttemptService loginAttemptService;
	@Autowired
	private HttpServletRequest request;

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		String ip = getClientIP();
		logger.error("Unauthorized error: {}", authException.getMessage());
		if (loginAttemptService.isBlocked(ip)) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Error: compte bloqué");//403
		} else {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Unauthorized");//401
		}
	}
	private String getClientIP() {
		String xfHeader = request.getHeader("X-Forwarded-For");
		if (xfHeader == null){
			return request.getRemoteAddr();
		}
		return xfHeader.split(",")[0];
	}

}