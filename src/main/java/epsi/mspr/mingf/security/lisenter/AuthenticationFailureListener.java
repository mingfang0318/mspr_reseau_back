package epsi.mspr.mingf.security.lisenter;

import epsi.mspr.mingf.security.services.LoginAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.ExecutionException;

@Configuration
@WebListener
public class AuthenticationFailureListener implements
        ApplicationListener<AuthenticationFailureBadCredentialsEvent> {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Override
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent e) {
        final String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            try {
                loginAttemptService.loginFailed(request.getRemoteAddr());
            } catch (ExecutionException ex) {
                ex.printStackTrace();
            }
        } else {
            try {
                loginAttemptService.loginFailed(xfHeader.split(",")[0]);
            } catch (ExecutionException ex) {
                ex.printStackTrace();
            }
        }
    }

}
