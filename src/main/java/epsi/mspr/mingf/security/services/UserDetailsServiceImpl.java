package epsi.mspr.mingf.security.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import epsi.mspr.mingf.models.User;
import epsi.mspr.mingf.repository.UserRepository;

import javax.servlet.http.HttpServletRequest;


@Service
public class UserDetailsServiceImpl implements UserDetailsService {
	@Autowired
	UserRepository userRepository;

	@Autowired
	private LoginAttemptService loginAttemptService;

	@Autowired
	private HttpServletRequest request;

	@Override
	@Transactional
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		String ip = getClientIP();
		if (loginAttemptService.isBlocked(ip)) {
			throw new UsernameNotFoundException(" User compte blocked");
		}
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

		return UserDetailsImpl.build(user);
	}

	private String getClientIP() {
		String xfHeader = request.getHeader("X-Forwarded-For");
		if (xfHeader == null){
			return request.getRemoteAddr();
		}
		return xfHeader.split(",")[0];
	}

}