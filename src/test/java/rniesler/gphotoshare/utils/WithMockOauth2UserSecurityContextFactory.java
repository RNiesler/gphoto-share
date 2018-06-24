package rniesler.gphotoshare.utils;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import rniesler.gphotoshare.domain.Person;
import rniesler.gphotoshare.security.Authorities;
import rniesler.gphotoshare.security.User;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;

public class WithMockOauth2UserSecurityContextFactory implements WithSecurityContextFactory<WithMockOauth2User> {
    @Override
    public SecurityContext createSecurityContext(WithMockOauth2User annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Person person = Person.builder().email(annotation.value()).build();
        Set<GrantedAuthority> authorities = Set.of((GrantedAuthority) () -> Authorities.RNALLOWED.name(),
                (GrantedAuthority) () -> "USER");
        OidcIdToken idToken = new OidcIdToken("tokenValue", Instant.now(),
                Instant.now().plus(1, ChronoUnit.DAYS),
                Map.of("sub", "123", "email", annotation.value()));
        OidcUser oidcUser = new DefaultOidcUser(authorities, idToken);
        User user = new User(person, authorities, oidcUser);
        OAuth2AuthenticationToken token = new OAuth2AuthenticationToken(user, authorities, "google");
        context.setAuthentication(token);
        return context;
    }
}
