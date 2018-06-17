package rniesler.gphotoshare.security;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import rniesler.gphotoshare.domain.Person;

import java.util.Map;

public interface SecurityService {
    Mono<OAuth2AuthorizedClient> getAuthorizedClient(OAuth2AuthenticationToken authentication);

    Mono<WebClient> getOauth2AuthenticatedWebClient(OAuth2AuthenticationToken token);

    Mono<Person> getAuthenticatedUser(OAuth2AuthenticationToken token);

    String getAuthenticatedEmail(OAuth2AuthenticationToken token);

    Mono<Map> retrieveUserInfo(OAuth2AuthenticationToken token);
}
