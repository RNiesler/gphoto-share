package rniesler.gphotoshare.security;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import rniesler.gphotoshare.domain.Person;

import java.util.Map;

public interface SecurityService {
    OAuth2AuthorizedClient getAuthorizedClient(OAuth2AuthenticationToken authentication);

    WebClient getOauth2AuthenticatedWebClient();

    WebClient getOauth2AuthenticatedWebClient(OAuth2AuthenticationToken token);

    Mono<Person> getAuthenticatedUser();

    String getAuthenticatedEmail();

    OAuth2AuthenticationToken retrieveAuthenticationToken();

    Map<String, String> retrieveUserInfo(OAuth2AuthenticationToken token);
}
