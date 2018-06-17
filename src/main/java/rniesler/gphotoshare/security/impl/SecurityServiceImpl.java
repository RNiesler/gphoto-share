package rniesler.gphotoshare.security.impl;

import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import rniesler.gphotoshare.domain.Person;
import rniesler.gphotoshare.security.SecurityService;
import rniesler.gphotoshare.services.impl.PersonServiceImpl;

import java.util.Map;

@Service
public class SecurityServiceImpl implements SecurityService {
    private ReactiveOAuth2AuthorizedClientService authorizedClientService;
    private final PersonServiceImpl personService;

    public SecurityServiceImpl(ReactiveOAuth2AuthorizedClientService authorizedClientService, PersonServiceImpl personService) {
        this.authorizedClientService = authorizedClientService;
        this.personService = personService;
    }

    @Override
    public Mono<OAuth2AuthorizedClient> getAuthorizedClient(OAuth2AuthenticationToken authentication) {
        return this.authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(), authentication.getName());
    }

    @Override
    public Mono<WebClient> getOauth2AuthenticatedWebClient(OAuth2AuthenticationToken token) {
        return getAuthorizedClient(token)
                .map(SecurityServiceImpl::oauth2Credentials)
                .map(exchangeFilterFunction -> WebClient.builder().filter(exchangeFilterFunction).build());
    }

    @Override
    public Mono<Person> getAuthenticatedUser(OAuth2AuthenticationToken token) {
        return personService.getPersonForEmail(this.getAuthenticatedEmail(token));
    }


    @Override
    public String getAuthenticatedEmail(OAuth2AuthenticationToken token) {
        return ((OidcUser) token.getPrincipal()).getEmail();    //TODO fails
    }

    @Override
    public Mono<Map> retrieveUserInfo(OAuth2AuthenticationToken token) {
        return getAuthorizedClient(token)
                .map(oAuth2AuthorizedClient ->
                        oAuth2AuthorizedClient.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUri())
                .zipWith(getOauth2AuthenticatedWebClient(token))
                .flatMap(tuple -> tuple.getT2()
                        .get()
                        .uri(tuple.getT1())
                        .retrieve()
                        .bodyToMono(Map.class));
    }

    private static ExchangeFilterFunction oauth2Credentials(OAuth2AuthorizedClient authorizedClient) {
        return ExchangeFilterFunction.ofRequestProcessor(
                clientRequest -> {
                    ClientRequest authorizedRequest = ClientRequest.from(clientRequest)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + authorizedClient.getAccessToken().getTokenValue())
                            .build();
                    return Mono.just(authorizedRequest);
                });
    }

}
