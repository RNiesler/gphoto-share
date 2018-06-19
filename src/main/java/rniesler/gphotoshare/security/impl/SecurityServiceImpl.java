package rniesler.gphotoshare.security.impl;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import rniesler.gphotoshare.domain.Person;
import rniesler.gphotoshare.security.SecurityService;
import rniesler.gphotoshare.services.impl.PersonServiceImpl;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

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
    public Optional<Person> getAuthenticatedUser() {
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
            return getOauth2AuthenticatedRestTemplate()
                    .getForObject(userInfoEndpointUri, Map.class);
        } else {
            return Collections.emptyMap();
        }
    }

    @Override
    public Boolean isAuthenticated() {
        return !(SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken);
    }


    public RestTemplate getOauth2AuthenticatedRestTemplate() {
        OAuth2AuthorizedClient authorizedClient = getAuthorizedClient(retrieveAuthenticationToken());
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
        restTemplateBuilder = restTemplateBuilder.interceptors((ClientHttpRequestInterceptor) (request, body, execution) -> {
            request.getHeaders().set(HttpHeaders.AUTHORIZATION, "Bearer " + authorizedClient.getAccessToken().getTokenValue());
            return execution.execute(request, body);
        });
        return restTemplateBuilder.build();
    }
}
