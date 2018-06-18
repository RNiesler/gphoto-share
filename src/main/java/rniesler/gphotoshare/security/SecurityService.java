package rniesler.gphotoshare.security;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.client.RestTemplate;
import rniesler.gphotoshare.domain.Person;

import java.util.Map;
import java.util.Optional;

public interface SecurityService {
    OAuth2AuthorizedClient getAuthorizedClient(OAuth2AuthenticationToken authentication);

    RestTemplate getOauth2AuthenticatedRestTemplate();

    Optional<Person> getAuthenticatedUser();

    String getAuthenticatedEmail();

    OAuth2AuthenticationToken retrieveAuthenticationToken();

    Map<String, String> retrieveUserInfo(OAuth2AuthenticationToken token);
}
