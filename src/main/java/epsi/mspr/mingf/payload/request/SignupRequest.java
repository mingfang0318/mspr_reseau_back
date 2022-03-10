package epsi.mspr.mingf.payload.request;

import org.springframework.stereotype.Service;

import java.util.Set;

import javax.validation.constraints.*;

@Service
public class SignupRequest {
  @NotBlank
  @Size(min = 3, max = 20)
  private String username;

  @NotBlank
  @Size(max = 50)
  @Email
  private String email;

  private Set<String> role;

  private boolean mailConfirmed;

  @NotBlank
  @Size(min = 6, max = 60)
  private String password;

  @NotNull
  private int failedLoginAttempts;

  @NotNull
  private boolean loginDisabled;

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

  public Set<String> getRole() {
    return this.role;
  }

  public void setRole(Set<String> role) {
    this.role = role;
  }

  public boolean getMailConfirmed() {
    return this.mailConfirmed;
  }

  public void setMailConfirmed(boolean mailConfirmed) {
    this.mailConfirmed = mailConfirmed;
  }

}