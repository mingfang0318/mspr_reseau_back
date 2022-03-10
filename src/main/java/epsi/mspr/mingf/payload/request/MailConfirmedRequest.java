package epsi.mspr.mingf.payload.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class MailConfirmedRequest {
	@NotBlank
  	private String token;

	@NotNull
	private boolean mailConfirmed;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public boolean getMailConfirmed() {
		return mailConfirmed;
	}

	public void setMailConfirmed(boolean mailConfirmed) {
		this.mailConfirmed = mailConfirmed;
	}
}
