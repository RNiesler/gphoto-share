package rniesler.gphotoshare.security.impl;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import rniesler.gphotoshare.domain.Person;
import rniesler.gphotoshare.security.SecurityService;
import rniesler.gphotoshare.services.impl.PersonServiceImpl;

import java.util.Collections;
import java.util.Map;

@Service
public class SecurityServiceImpl implements SecurityService {
    private OAuth2AuthorizedClientService authorizedClientService;
    private final PersonServiceImpl personService;

    public SecurityServiceImpl(OAuth2AuthorizedClientService authorizedClientService, PersonServiceImpl personService) {
        this.authorizedClientService = authorizedClientService;
        this.personService = personService;
    }

    @Override
    public OAuth2AuthorizedClient getAuthorizedClient(OAuth2AuthenticationToken authentication) {
        return this.authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(), authentication.getName());
    }

    @Override
    public WebClient getOauth2AuthenticatedWebClient() {
        return getOauth2AuthenticatedWebClient(retrieveAuthenticationToken());
    }

    @Override
    public WebClient getOauth2AuthenticatedWebClient(OAuth2AuthenticationToken token) {
        return WebClient.builder()
                .filter(oauth2Credentials(getAuthorizedClient(token)))
                .build();
    }

    @Override
    public Mono<Person> getAuthenticatedUser() {
        return personService.getPersonForEmail(this.getAuthenticatedEmail());
    }


    @Override
    public String getAuthenticatedEmail() {
        return ((OidcUser) retrieveAuthenticationToken().getPrincipal()).getEmail();
    }

    @Override
    public OAuth2AuthenticationToken retrieveAuthenticationToken() {
        //TODO error checking
        return (OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
    }

    @Override
    public Map<String, String> retrieveUserInfo(OAuth2AuthenticationToken token) {
        String userInfoEndpointUri = getAuthorizedClient(token).getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUri();
        if (!StringUtils.isEmpty(userInfoEndpointUri)) {    // userInfoEndpointUri is optional for OIDC Clients
            return getOauth2AuthenticatedWebClient(token)
                    .get()
                    .uri(userInfoEndpointUri)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } else {
            return Collections.emptyMap();
        }
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
