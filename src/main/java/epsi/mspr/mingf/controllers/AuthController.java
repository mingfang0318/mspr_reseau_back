package epsi.mspr.mingf.controllers;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CountryResponse;
import epsi.mspr.mingf.models.Person;
import epsi.mspr.mingf.payload.request.LoginRequest;
import epsi.mspr.mingf.payload.request.MailConfirmedRequest;
import epsi.mspr.mingf.payload.request.OtpValideRequest;
import epsi.mspr.mingf.payload.response.JwtResponse;
import epsi.mspr.mingf.payload.response.MessageResponse;
import epsi.mspr.mingf.repository.PersonRepository;
import epsi.mspr.mingf.security.jwt.JwtUtils;
import epsi.mspr.mingf.security.services.LoginAttemptService;
import epsi.mspr.mingf.security.services.OtpService;
import epsi.mspr.mingf.security.services.SMSServiceTwilio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.ldap.userdetails.InetOrgPerson;
import org.springframework.web.bind.annotation.*;
import ua_parser.Parser;
import ua_parser.Client;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Objects;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	public PersonRepository personRepository;

	@Autowired
	PasswordEncoder encoder;

	@Autowired
	JwtUtils jwtUtils;

	@Autowired
	private JavaMailSender javaMailSender;

	@Autowired
	private LoginAttemptService loginAttemptService;

	@Autowired
	private OtpService otpService;

	@PostMapping("/signin")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) throws IOException, GeoIp2Exception {
		String ipAdd = this.getClientIP(request);
		if(!loginAttemptService.isBlocked(ipAdd)) {
			// Auth with LDAP
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
			// get data from request
			String username = authentication.getName();
			Optional<Person> personOpt = personRepository.findByUsername(username);//person in MySql
			String browser = this.getClientBrowser(request);
			//check Anti-brute Force
			// if person is in mySql database and his account is not blocked
			if (personOpt.isPresent() && !personOpt.get().isBlocked()) {
				String jwt = jwtUtils.generateJwtToken(authentication);
				String brwoserUser = personOpt.get().getBrowserLogin();
				//-------------------- Check Login Browse --------------
				if (browser.equals(brwoserUser) && personOpt.get().isMailConfirmed()) {
					//-------------------- Check Login IP TODO : change the condition --------------
					if (!ipAdd.equals(personOpt.get().getIpLogin())) {
						String msg = "C'est un message pour vous informer que vous avez connecté avec une adresse ip inhabituel!\n ";
						this.sendConfirmedMail("", msg);
						// if ip is not french
						//String ipCountry= this.givenIP_ReturnsCityData(ipAdd);
						if(!ipAdd.equals("127.0.0.1")) {//ipCountry != "France"
							personOpt.get().setBlocked(true);
							personRepository.save(personOpt.get());
							String msg2 = "C'est un message pour vous informer que vous avez connecté avec une adresse ip étrangère. Votre compte est bloqué. ";
							this.sendConfirmedMail("", msg2);
							return ResponseEntity
									.status(HttpStatus.LOCKED)
									.body("vous avez connecté avec une adresse ip étrangère. Votre compte est bloqué. ");
						}
					}
					// send code by SMS
					final InetOrgPerson user = (InetOrgPerson) authentication.getPrincipal();
					String userPhone = user.getMobile();//get user mobile from LDAP
					int otp = otpService.generateOTP(authentication.getName());
					SMSServiceTwilio smsServiceTwilio = new SMSServiceTwilio(userPhone,otp);
					smsServiceTwilio.sendSMS();
					return ResponseEntity.ok(new JwtResponse(jwt,
							username));
				} else {
					String msg = " Ceci est mail pour vous informer que vous avez changé de navigateur! \n ";
					this.sendConfirmedMail(jwt, msg);
					// update new user's account to database
					personOpt.get().setMailConfirmed(false);
					personRepository.save(personOpt.get());
					return ResponseEntity
							.badRequest()
							.body(new MessageResponse("Vous avez changé de navigateur! Vous devrez ré-valider votre login via votre email. "));
				}
			} // 1ère connexion
			else if (personOpt.isEmpty()) {
				// if person is not in mysql, Create a new user's account to database
				String email = loginRequest.getUsername() + "@springframework.org";//TODO: initialiser le mail
				Person newUser = new Person(loginRequest.getUsername(),
						email,
						true,//initialiser mailConfirmed=true
						ipAdd,
						browser);

				personRepository.save(newUser);
				//generate a token with username+password
				String jwt = jwtUtils.generateJwtToken(authentication);

				// send code by SMS
				final InetOrgPerson user = (InetOrgPerson) authentication.getPrincipal();
				String userPhone = user.getMobile();//get user mobile from LDAP
				int otp = otpService.generateOTP(authentication.getName());
				SMSServiceTwilio smsServiceTwilio = new SMSServiceTwilio(userPhone,otp);
				smsServiceTwilio.sendSMS();
				return ResponseEntity.ok(new JwtResponse(jwt,
						username));
			} else {
				return ResponseEntity
						.badRequest()
						.body(new MessageResponse("Username or password no found in LDAP "));
			}
		} else {
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse("Votre IP est bloqué. "));
		}
	}

	@PostMapping("/otpValidation")
	public ResponseEntity<?> otpValidate(@Valid @RequestBody OtpValideRequest otpValideRequest) {
		String token = otpValideRequest.getToken();
		String username = jwtUtils.getUserNameFromJwtToken(token);
		// get otp by request
		int otpRequest = otpValideRequest.getCodeOtp();
		if(otpRequest > 0) {
			// get code by username from Cache
			int otpInCache = otpService.getOtp(username);
			if(otpInCache > 0 && otpRequest == otpInCache){
				otpService.clearOTP(username);
				return ResponseEntity.ok(new MessageResponse(" Connexion succèss"));
			} else {
				return ResponseEntity
						.status(HttpStatus.LOCKED)
						.body(new MessageResponse(" Votre code n'est pas valide. "));
			}
		} else {
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse(" Votre code n'est pas valide. "));
		}
	}

	@PutMapping("/mailConfirmed")
	public ResponseEntity<?> mailConfirmedByUser(@Valid @RequestBody MailConfirmedRequest mailConfirmedRequest) {
		String token = mailConfirmedRequest.getToken();
		String usernameFromToken = jwtUtils.getUserNameFromJwtToken(token);
		Optional<Person> personOptional = personRepository.findByUsername(usernameFromToken);
		// check if user is in mySql database
		if(personOptional.isPresent()) {
			Person user = personOptional.get();
			user.setMailConfirmed(true);
			try {
				personRepository.save(user);
				return ResponseEntity.ok(new MessageResponse("User Email Conformed! You can login now. "));
			} catch (Exception e) {
				return ResponseEntity
						.badRequest()
						.body(new MessageResponse("Compte non validé. "));
			}
		} else {
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse("Votre compte n'est pas encore créé. "));
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
		msg.setFrom("mingfang0318@hotmail.com");
		msg.setTo("mingfang0318@gmail.com");
		msg.setFrom("mingfang0318@hotmail.com");
		msg.setSubject("Mail de validation ");
		msg.setText(message + '\n'+ lienMail );
		//send the mail with the lien for mailConfirmed
		javaMailSender.send(msg);
	}

	//-------------------- Find client browser --------------
	public String getClientBrowser(HttpServletRequest request) throws IOException {
		String userAgent = request.getHeader("user-agent");
		String browser = null;
		Parser parser = new Parser();
		Client client = parser.parse(userAgent);
		if (Objects.nonNull(client)) {
			browser = client.userAgent.family;
		}
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

	//-------------------- Get country name from Ip --------------
	public String givenIP_ReturnsCityData(String ip)
			throws IOException, GeoIp2Exception {
		File database = new ClassPathResource("GeoLite2-Country.mmdb").getFile();// get file in ./resources folder
		DatabaseReader dbReader = new DatabaseReader.Builder(database)
				.build();

		InetAddress ipAddress = InetAddress.getByName(ip);
		CountryResponse response = dbReader.country(ipAddress);

		return response.getCountry().getName();
	}
}
