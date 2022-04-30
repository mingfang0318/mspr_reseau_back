package epsi.mspr.mingf.models;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


@Entity
@Table(	name = "users", 
		uniqueConstraints = { 
			@UniqueConstraint(columnNames = "username"),
			@UniqueConstraint(columnNames = "email") 
		})
@Getter
@Setter
public class Person {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false, updatable = false)
	private Long id;

	@NotBlank
	@Size(max = 20)
	private String username;

	@NotBlank
	@Size(max = 50)
	@Email
	private String email;

	@NotBlank
	@Size(max = 20)
	private String ipLogin;

	@NotBlank
	@Size(max = 20)
	private String browserLogin;

	@NotNull
	private boolean mailConfirmed;

	@NotNull
	private boolean isBlocked;

	public Person() {
	}
	public Person(String username, String email, boolean mailConfirmed, String ipLogin, String browserLogin) {
		this.username = username;
		this.email =email;
		this.mailConfirmed =mailConfirmed;
		this.ipLogin =ipLogin;
		this.browserLogin = browserLogin;
	}

}

