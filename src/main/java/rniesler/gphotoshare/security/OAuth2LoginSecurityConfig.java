package rniesler.gphotoshare.security;

//@EnableWebSecurity
//@Slf4j
//public class OAuth2LoginSecurityConfig extends WebSecurityConfigurerAdapter {
//    @Autowired
//    private PersonServiceImpl personService;
//
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http
//                .authorizeRequests()
//                .anyRequest().authenticated()
//                .and()
//                .oauth2Login();
//    }
//
//    @EventListener({AuthenticationSuccessEvent.class, InteractiveAuthenticationSuccessEvent.class})
//    public void onSuccessfulAuthentication(AbstractAuthenticationEvent event) {
//        OidcUserInfo userInfo = ((OidcUser) event.getAuthentication().getPrincipal()).getUserInfo();
//        Person stub = Person.builder().name(userInfo.getFullName()).email(userInfo.getEmail()).build();
//        Person person = personService.getOrPersist(stub).block();
//        log.info("Person id " + person.getId());
//    }
//
//}

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.server.SecurityWebFilterChain;
import rniesler.gphotoshare.domain.Person;
import rniesler.gphotoshare.services.impl.PersonServiceImpl;

@EnableWebFluxSecurity
@Slf4j
public class OAuth2LoginSecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange()
                .anyExchange().authenticated()
                .and()
                .oauth2Login();
        return http.build();
    }

    @Autowired
    private PersonServiceImpl personService;

    @EventListener({AuthenticationSuccessEvent.class, InteractiveAuthenticationSuccessEvent.class})
    public void onSuccessfulAuthentication(AbstractAuthenticationEvent event) {
        //TODO not firing
        OidcUserInfo userInfo = ((OidcUser) event.getAuthentication().getPrincipal()).getUserInfo();
        Person stub = Person.builder().name(userInfo.getFullName()).email(userInfo.getEmail()).build();
        Person person = personService.getOrPersist(stub).block();
        log.info("Person id " + person.getId());
    }
}