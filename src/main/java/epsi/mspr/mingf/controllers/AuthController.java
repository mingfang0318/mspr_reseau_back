package epsi.mspr.mingf.controllers;

import com.blueconic.browscap.Capabilities;
import com.blueconic.browscap.ParseException;
import com.blueconic.browscap.UserAgentParser;
import com.blueconic.browscap.UserAgentService;
import epsi.mspr.mingf.models.ERole;
import epsi.mspr.mingf.repository.Person;
import epsi.mspr.mingf.models.Role;
import epsi.mspr.mingf.models.User;
import epsi.mspr.mingf.payload.request.LoginRequest;
import epsi.mspr.mingf.payload.request.MailConfirmedRequest;
import epsi.mspr.mingf.payload.request.SignupRequest;
import epsi.mspr.mingf.payload.response.JwtResponse;
import epsi.mspr.mingf.payload.response.MessageResponse;
import epsi.mspr.mingf.repository.RoleRepository;
import epsi.mspr.mingf.repository.UserRepository;
import epsi.mspr.mingf.security.LDAP.PersonRepoImpl;
import epsi.mspr.mingf.security.jwt.JwtUtils;
import epsi.mspr.mingf.security.services.LoginAttemptService;
import epsi.mspr.mingf.security.services.UserDetailsImpl;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
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

	@Autowired
	private PersonRepoImpl personRepo;

	@Autowired
	private LoginAttemptService loginAttemptService;

	@PostMapping("/signin")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) throws IOException, ParseException {
		// check Anti-brute Force by Cache
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
		SecurityContextHolder.getContext().setAuthentication(authentication);
		String ipAdd = this.getClientIP(request);
		String username = loginRequest.getUsername();
		Optional<User> userOptional = userRepository.findByUsername(username);
		//check Anti-brute Force and if user is in mySql database
		if(userOptional.isPresent() && !loginAttemptService.isBlocked(ipAdd)) {
			User user = userOptional.get();
			// get user info from AD Ldap
			String dn = "uid=" +username + ",ou=people,dc=springframework,dc=org";
			Person person = personRepo.findPerson(dn);
			//-------------------- Check if mail is comfirmed and Ldap User ----  -------------
			if(user.getMailConfirmed() && (person != null && person.getPassword().equals(loginRequest.getPassword()))) {
				String jwt = jwtUtils.generateJwtToken(authentication);
				String browser = this.getClientBrowser(request);

				//-------------------- Check Login Browse --------------
				if (browser.equals(user.getBrowserLogin())) {
					String msg = "Cliquez le lien suivant pour ré-activer votre votre compte: \n ";
					this.sendConfirmedMail(jwt, msg);
					// update new user's account to database
					user.setMailConfirmed(false);
					userRepository.save(user);

					return ResponseEntity.ok(new MessageResponse("Vous avez changé de navigateur! \n Vous devrez ré-valider votre login via l'email. \n "));
				} else {

					//-------------------- Check Login IP : TODO: Blocquer compte en cas de ip étranger--------------
					if (ipAdd.equals(user.getIpLogin())) {
						String msg = "C'est un message pour vous informer que vous avez connecté avec une adresse ip inhabituel!\n ";
						this.sendConfirmedMail("", msg);
					}

					UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
					List<String> roles = userDetails.getAuthorities().stream()
							.map(GrantedAuthority::getAuthority)
							.collect(Collectors.toList());

					return ResponseEntity.ok(new JwtResponse(jwt,
							userDetails.getId(),
							userDetails.getUsername(),
							userDetails.getEmail(),
							roles));
				}
			} else {
				return ResponseEntity
						.badRequest()
						.body(new MessageResponse("Votre compte n'est pas encore validé. \n "));
			}
		} else {
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse("Votre compte n'est pas encore créé. \n "));
		}
	}

	@SneakyThrows
	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest, HttpServletRequest request) {
		//------------------ Check By LDAP --------------
		String username = "uid="+signUpRequest.getUsername();
		String dn = username + ",ou=people,dc=springframework,dc=org";
		Person person = personRepo.findPerson(dn);
		if (person != null && person.getPassword().equals(signUpRequest.getPassword())) {
			// initialiser user.mailCobfirmed = false
			signUpRequest.setMailConfirmed(false);
			//-------------------- Find client browser --------------
			String browser = this.getClientBrowser(request);
			//-------------------- Find client ip --------------
			String ipAdd = this.getClientIP(request);

			// Create new user's account to database
			User user = new User(signUpRequest.getUsername(),
					signUpRequest.getEmail(),
					encoder.encode(signUpRequest.getPassword()),
					signUpRequest.getMailConfirmed(),
					browser,
					ipAdd);
			// ----------------- Roles User -----------------------
			Set<String> strRoles = signUpRequest.getRole();
			Set<Role> roles = new HashSet<>();
			if (strRoles == null) {
				Role userRole = roleRepository.findByName(ERole.ROLE_USER).orElseThrow(() -> new RuntimeException("Error: Role is not found."));
				roles.add(userRole);
			} else {
				strRoles.forEach(role -> {
					switch (role) {
						case "admin":
							Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN).orElseThrow(() -> new RuntimeException("Error: admin Role is not found."));
							roles.add(adminRole);

							break;
						case "mod":
							Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR).orElseThrow(() -> new RuntimeException("Error: mod Role is not found."));
							roles.add(modRole);

							break;
						default:
							Role userRole = roleRepository.findByName(ERole.ROLE_USER).orElseThrow(() -> new RuntimeException("Error: user Role is not found."));
							roles.add(userRole);
					}
				});
			}

			user.setRoles(roles);
			userRepository.save(user);
			//preparer le token which will be sent by mail
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(signUpRequest.getUsername(), signUpRequest.getPassword()));
			SecurityContextHolder.getContext().setAuthentication(authentication);
			//generate a token with username+password
			String jwt = jwtUtils.generateJwtToken(authentication);

			//--------------- Send Mail -----------------
			String msg = "Clickez le lien suivant pour valider votre inscription: \n ";
			this.sendConfirmedMail(jwt, msg);
			return ResponseEntity.ok(new MessageResponse("User registered successfully! Waitting for Email Confirm!!"));
		} else {
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse("Username or password no found in LDAP "));
		}
	}

	@GetMapping("/mailConfirmed")
	public ResponseEntity<?> mailConfirmedByUser(@Valid @RequestBody MailConfirmedRequest mailConfirmedRequest) {
		String token = mailConfirmedRequest.getToken();
		String usernameFromToken = jwtUtils.getUserNameFromJwtToken(token);
		Optional<User> userOptional = userRepository.findByUsername(usernameFromToken);
		// check if user is in mySql database
		if(userOptional.isPresent()) {
			User user = userOptional.get();
			user.setMailConfirmed(true);
			try {
				userRepository.save(user);
				return ResponseEntity.ok(new MessageResponse("User Email Conformed! You can login now"));
			} catch (Exception e) {
				return ResponseEntity
						.badRequest()
						.body(new MessageResponse("Compte non validé"));
			}
		} else {
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse("Votre compte n'est pas encore créé. \n "));
		}
	}

	//-------------------- Send mail (TODO) --------------
	public void sendConfirmedMail(String jwt, String message){
		//prepare the message
		//TODOS: change URL
		String lienMail = "http://localhost:4200/mailConfirmed?token=" + jwt + "&mailConfirmed=true";
		SimpleMailMessage msg = new SimpleMailMessage();
/* 		String mailUser = signUpRequest.getEmail();
		msg.setTo(mailUser);
		String mailHost = "admin@clinique.fr";
		msg.setFrom(mailHost);
 */		// test for send mail confirm
		msg.setFrom("aaaaaa@hotmail.com");
		msg.setTo("aaaaaa@gmail.com");
		msg.setFrom("aaaaaa@hotmail.com");
		msg.setSubject("Mail de validation ");
		msg.setText(message + '\n'+ lienMail );
		//send the mail with the lien for mailConfirmed
		javaMailSender.send(msg);
	}

	//-------------------- Find client browser --------------
	public String getClientBrowser(HttpServletRequest request) throws IOException, ParseException {
		final UserAgentParser parser = new UserAgentService().loadParser();
		String userAgent = request.getHeader("user-agent");

		final Capabilities capabilities = parser.parse(userAgent);
		final String browser = capabilities.getBrowser();

		return browser;
	}

	//-------------------- Find client Ip --------------
	private String getClientIP(HttpServletRequest request) {
		String xfHeader = request.getHeader("X-Forwarded-For");
		if (xfHeader == null){
			return request.getRemoteAddr();
		}
		return xfHeader.split(",")[0];
	}
}
