package rniesler.gphotoshare.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import rniesler.gphotoshare.domain.Person;
import rniesler.gphotoshare.services.PersonService;

import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
public class UserService extends OidcUserService {
    private final PersonService personService;

    public UserService(PersonService personService) {
        this.personService = personService;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        Person stub = Person.builder().name(oidcUser.getUserInfo().getFullName()).email(oidcUser.getUserInfo().getEmail()).build();
        Person person = personService.getOrPersist(stub);
        Set<GrantedAuthority> authorities;
        Set<String> additionalAuthorities = personService.getPersonAuthorities(person.getEmail());
        if (additionalAuthorities.isEmpty()) {
            authorities = (Set<GrantedAuthority>) oidcUser.getAuthorities();
        } else {
            authorities = new HashSet<>(oidcUser.getAuthorities());
            additionalAuthorities.stream().map(authorityStr -> (GrantedAuthority) () -> authorityStr)
                    .forEach(authority -> authorities.add(authority));
        }
        log.info("Authenticated person with id " + person.getId());
        User user;
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        if (StringUtils.hasText(userNameAttributeName)) {
            user = new User(person, authorities, oidcUser, userNameAttributeName);
        } else {
            user = new User(person, authorities, oidcUser);
        }

        return user;
    }
}
