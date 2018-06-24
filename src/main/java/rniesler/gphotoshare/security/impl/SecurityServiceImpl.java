package rniesler.gphotoshare.security.impl;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import rniesler.gphotoshare.domain.Person;
import rniesler.gphotoshare.exceptions.AuthenticationRequiredException;
import rniesler.gphotoshare.security.SecurityService;
import rniesler.gphotoshare.services.PersonService;

import java.util.Optional;

@Service
public class SecurityServiceImpl implements SecurityService {
    private OAuth2AuthorizedClientService authorizedClientService;
    private final PersonService personService;
    private final RestTemplateBuilder restTemplateBuilder;

    public SecurityServiceImpl(OAuth2AuthorizedClientService authorizedClientService, PersonService personService, RestTemplateBuilder restTemplateBuilder) {
        this.authorizedClientService = authorizedClientService;
        this.personService = personService;
        this.restTemplateBuilder = restTemplateBuilder;
    }

    @Override
    public OAuth2AuthorizedClient getAuthorizedClient(OAuth2AuthenticationToken authentication) {
        return this.authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(), authentication.getName());
    }

    @Override
    public Optional<Person> getAuthenticatedUser() {
        return personService.getPersonForEmail(this.getAuthenticatedEmail());
    }

    @Override
    public String getAuthenticatedEmail() {
        OAuth2AuthenticationToken token = retrieveAuthenticationToken();
        if (token == null) {
            throw new AuthenticationRequiredException();
        }
        return ((OidcUser) token.getPrincipal()).getEmail();
    }

    @Override
    public OAuth2AuthenticationToken retrieveAuthenticationToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication instanceof OAuth2AuthenticationToken ? (OAuth2AuthenticationToken) authentication : null;
    }

    @Override
    public Boolean isAuthenticated() {
        return !(SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken);
    }

    @Override
    public RestTemplate getOauth2AuthenticatedRestTemplate() {
        OAuth2AuthenticationToken token = retrieveAuthenticationToken();
        if (token == null) {
            throw new AuthenticationRequiredException();
        }
        OAuth2AuthorizedClient authorizedClient = getAuthorizedClient(token);
        return restTemplateBuilder
                .errorHandler(new GoogleApiResponseErrorHandler())
                .interceptors((ClientHttpRequestInterceptor) (request, body, execution) -> {
                    request.getHeaders().set(HttpHeaders.AUTHORIZATION, "Bearer " + authorizedClient.getAccessToken().getTokenValue());
                    return execution.execute(request, body);
                })
                .build();
    }
}
