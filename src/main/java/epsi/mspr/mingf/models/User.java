package epsi.mspr.mingf.models;

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
public class User {
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
	@Size(max = 120)
	private String password;

	@NotBlank
	@Size(max = 20)
	private String ipLogin;

	@NotBlank
	@Size(max = 20)
	private String browserLogin;

	@NotNull
	private boolean mailConfirmed;

	@ElementCollection
	@JoinTable(	name = "user_roles",
				joinColumns = @JoinColumn(name = "user_id"), 
				inverseJoinColumns = @JoinColumn(name = "role_id"))
	private Set<Role> roles = new HashSet<>();

	public User() {
	}
	public User(String username, String email, String password, boolean mailConfirmed, String ipLogin, String browserLogin) {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	public boolean getMailConfirmed() {
		return mailConfirmed;
	}

	public void setMailConfirmed(boolean mailConfirmed) {
		this.mailConfirmed = mailConfirmed;
	}

	public String getBrowserLogin() {
		return browserLogin;
	}

	public void setBrowserLogin(String browserLogin) {
		this.browserLogin = browserLogin;
	}

	public String getIpLogin() {
		return ipLogin;
	}

	public void setIpLogin(String ipLogin) {
		this.ipLogin = ipLogin;
	}

}

