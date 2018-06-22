package rniesler.gphotoshare.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import rniesler.gphotoshare.domain.Person;
import rniesler.gphotoshare.services.impl.PersonServiceImpl;

@EnableWebSecurity
@Slf4j
@Order(100)
public class OAuth2LoginSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private PersonServiceImpl personService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/oauth_login/**").permitAll()
                .antMatchers("/webjars/**").permitAll()
                .antMatchers("/admin/**").hasRole("ADMIN") //TODO
                .anyRequest().authenticated()
                .and()
                .oauth2Login().loginPage("/oauth_login");
    }

    @EventListener({AuthenticationSuccessEvent.class, InteractiveAuthenticationSuccessEvent.class})
    public void onSuccessfulAuthentication(AbstractAuthenticationEvent event) {
        OidcUserInfo userInfo = ((OidcUser) event.getAuthentication().getPrincipal()).getUserInfo();
        Person stub = Person.builder().name(userInfo.getFullName()).email(userInfo.getEmail()).build();
        Person person = personService.getOrPersist(stub);
        log.info("Person id " + person.getId());
    }

}