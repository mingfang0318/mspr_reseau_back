package epsi.mspr.mingf.payload.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class LoginRequest {
	@NotBlank
  	private String username;

	@NotBlank
	private String password;

	@NotNull
	private boolean mailConfirmed;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean getMailConfirmed() {
		return mailConfirmed;
	}

	public void setMailConfirmed(boolean mailConfirmed) {
		this.mailConfirmed = mailConfirmed;
	}
}