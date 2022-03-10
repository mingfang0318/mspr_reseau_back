package epsi.mspr.mingf.controllers;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import epsi.mspr.mingf.models.User;
import epsi.mspr.mingf.payload.response.MessageResponse;
import epsi.mspr.mingf.repository.RoleRepository;
import epsi.mspr.mingf.repository.UserRepository;
import epsi.mspr.mingf.security.jwt.JwtUtils;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/test")
public class TestController {

	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	UserRepository userRepository;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	PasswordEncoder encoder;

	@Autowired
	JwtUtils jwtUtils;

	@Autowired
	private JavaMailSender javaMailSender;

	@GetMapping("/all")
	@ResponseBody
	public ResponseEntity<Collection<User>> findAll() {
		List<User> users = userRepository.findAll();
		return ResponseEntity.ok(users);
	}
	
	@GetMapping("/user")
	@PreAuthorize("hasRole('ROLE_USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
	public String userAccess() {
		return "User Content.";
	}

	@GetMapping("/mod")
	@PreAuthorize("hasRole('ROLE_MODERATOR')")
	public String moderatorAccess() {
		return "Moderator Board.";
	}

	//----------- TODO : Read, update and Delete of users by Admin
	@DeleteMapping("/admin/{idUser}")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<?> deleteUser(@PathVariable Long idUser) {
		try {
			userRepository.deleteById(idUser);
			return ResponseEntity.ok(new MessageResponse(" User supprimé. "));
		} catch (Exception e) {
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse(" User non supprimé. "));
		}
	}

	@GetMapping("/admin/{idUser}")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<?> getUserById(@PathVariable Long idUser) {
		Optional<User> userOptional = userRepository.findById(idUser);
		// check if user is in mySql database
		if(userOptional.isPresent()) {
			User user = userOptional.get();
			return ResponseEntity.ok(user);
		} else {
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse(" User non found. "));
		}
	}
}