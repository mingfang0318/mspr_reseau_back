package epsi.mspr.mingf.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.LdapShaPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;

import epsi.mspr.mingf.security.jwt.AuthEntryPointJwt;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.ldap.userdetails.InetOrgPersonContextMapper;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
		//securedEnabled = true,
		// jsr250Enabled = true,
		prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
	private AuthEntryPointJwt unauthorizedHandler;


	/*---------------------- AD server connexion -------------------*/
	@Bean
	public AuthenticationManager authenticationManager() {
		return new ProviderManager(Arrays.asList(activeDirectoryLdapAuthenticationProvider()));
	}
	@Bean
	public AuthenticationProvider activeDirectoryLdapAuthenticationProvider() {
		// Build the Ldap auth provider bean
		ActiveDirectoryLdapAuthenticationProvider authenticationProvider =
				new ActiveDirectoryLdapAuthenticationProvider("DOMAIN.local","ldap://192.168.108.138:389");
		authenticationProvider.setConvertSubErrorCodesToExceptions(true);
		authenticationProvider.setUseAuthenticationRequestCredentials(true);
		authenticationProvider.setSearchFilter("(userPrincipalName={0})");
		authenticationProvider.setUserDetailsContextMapper(new InetOrgPersonContextMapper());
		return authenticationProvider;
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) {
		auth.authenticationProvider(activeDirectoryLdapAuthenticationProvider());
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.cors().and().csrf().disable()
			.exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
			.authorizeRequests().antMatchers("/api/auth/**").permitAll()
			.anyRequest().authenticated();

		//http.addFilterBefore((Filter) authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
	}

}