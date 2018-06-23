package rniesler.gphotoshare.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import rniesler.gphotoshare.domain.Person;

import java.util.Set;

@Getter
public class User extends DefaultOidcUser {
    private Person person;

    public User(Person person, Set<GrantedAuthority> authorities, OidcUser oidcUser) {
        super(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
        this.person = person;
    }

    public User(Person person, Set<GrantedAuthority> authorities, OidcUser oidcUser, String nameAttributeKey) {
        super(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo(), nameAttributeKey);
        this.person = person;
    }

}