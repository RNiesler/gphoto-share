package rniesler.gphotoshare.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
@Slf4j
public class OAuth2LoginSecurityConfig extends WebSecurityConfigurerAdapter {
    private final UserService userService;

    public OAuth2LoginSecurityConfig(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/oauth_login/**").permitAll()
                .antMatchers("/webjars/**").permitAll()
                .antMatchers("/admin/**").hasAuthority(Authorities.ADMIN.name())
                .anyRequest().authenticated()
                .anyRequest().hasAuthority(Authorities.RNALLOWED.name())
                .and()
                .requiresChannel()
                .requestMatchers(r -> r.getHeader("X-Forwarded-Proto") != null)
                .requiresSecure()
                .and()
                .oauth2Login()
                .userInfoEndpoint().oidcUserService(userService)
                .and()
                .loginPage("/oauth_login");
    }
}