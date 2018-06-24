package rniesler.gphotoshare.security.impl;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.test.context.annotation.SecurityTestExecutionListeners;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import rniesler.gphotoshare.domain.Person;
import rniesler.gphotoshare.exceptions.AuthenticationRequiredException;
import rniesler.gphotoshare.services.PersonService;
import rniesler.gphotoshare.utils.WithMockOauth2User;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringRunner.class)
@SecurityTestExecutionListeners
@WithMockOauth2User
public class SecurityServiceImplTest {
    public static final String TEST_EMAIL = "test@email";
    private SecurityServiceImpl service;

    @Mock
    private OAuth2AuthorizedClientService oAuth2AuthorizedClientService;
    @Mock
    private PersonService personService;
    @Mock
    private OAuth2AuthenticationToken token;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        service = new SecurityServiceImpl(oAuth2AuthorizedClientService, personService, new RestTemplateBuilder());
        when(token.getAuthorizedClientRegistrationId()).thenReturn("test");
        when(token.getName()).thenReturn("test");
    }

    @Test
    public void testGetAuthorizedClient() {
        OAuth2AuthorizedClient oAuth2AuthorizedClient = mock(OAuth2AuthorizedClient.class);
        when(oAuth2AuthorizedClientService.loadAuthorizedClient("test", "test")).thenReturn(oAuth2AuthorizedClient);
        assertEquals(oAuth2AuthorizedClient, service.getAuthorizedClient(token));
    }

    @Test
    public void retrieveAuthenticationToken() {
        assertNotNull(service.retrieveAuthenticationToken());
    }

    @Test
    @WithMockOauth2User(TEST_EMAIL)
    public void testGetAuthenticatedEmail() {
        assertEquals(TEST_EMAIL, service.getAuthenticatedEmail());
    }

    @Test(expected = AuthenticationRequiredException.class)
    @WithAnonymousUser
    public void testGetAuthenticatedEmailWhenNotAuthenticated() {
        service.getAuthenticatedEmail();
    }

    @Test
    @WithMockOauth2User(TEST_EMAIL)
    public void testGetAuthenticatedUser() {
        Person testPerson = Person.builder().email(TEST_EMAIL).build();
        when(personService.getPersonForEmail(TEST_EMAIL)).thenReturn(Optional.of(testPerson));
        assertEquals(testPerson, service.getAuthenticatedUser().get());
    }

    @Test
    public void testIsAuthenticated() {
        assertTrue(service.isAuthenticated());
    }

    @Test
    @WithAnonymousUser
    public void testIsNotAuthenticated() {
        assertFalse(service.isAuthenticated());
    }

    @Test(expected = AuthenticationRequiredException.class)
    @WithAnonymousUser
    public void testGetRestTemplateWhenNotAuthenticated() {
        service.getOauth2AuthenticatedRestTemplate();
    }

    @Test
    public void googleApiTestAuthorizationHeader() {
        String testUrl = "http://testurl";
        String testResponse = "test";
        String tokenValue = "testToken";
        OAuth2AccessToken oAuth2AccessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, tokenValue, Instant.now(), Instant.now().plus(1, ChronoUnit.DAYS));
        OAuth2AuthorizedClient oAuth2AuthorizedClient = mock(OAuth2AuthorizedClient.class);
        when(oAuth2AuthorizedClient.getAccessToken()).thenReturn(oAuth2AccessToken);
        when(oAuth2AuthorizedClientService.loadAuthorizedClient(anyString(), anyString())).thenReturn(oAuth2AuthorizedClient);

        RestTemplate restTemplate = service.getOauth2AuthenticatedRestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(ExpectedCount.once(), requestTo(testUrl))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, Matchers.any(String.class)))
                .andRespond(withSuccess(testResponse, null));


        assertEquals(testResponse, restTemplate.getForObject(testUrl, String.class));
        server.verify();
    }

    @Test(expected = GoogleApiException.class)
    public void googleApiTestResponseErrorHandler() {
        String testUrl = "http://testurl";
        String testResponse = "test";
        String tokenValue = "testToken";
        OAuth2AccessToken oAuth2AccessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, tokenValue, Instant.now(), Instant.now().plus(1, ChronoUnit.DAYS));
        OAuth2AuthorizedClient oAuth2AuthorizedClient = mock(OAuth2AuthorizedClient.class);
        when(oAuth2AuthorizedClient.getAccessToken()).thenReturn(oAuth2AccessToken);
        when(oAuth2AuthorizedClientService.loadAuthorizedClient(anyString(), anyString())).thenReturn(oAuth2AuthorizedClient);

        RestTemplate restTemplate = service.getOauth2AuthenticatedRestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(ExpectedCount.once(), requestTo(testUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.FORBIDDEN));


        restTemplate.getForObject(testUrl, String.class);
    }

    @Test(expected = AuthenticationRequiredException.class)
    @WithAnonymousUser
    public void getRestTemplateWhenNotAuthenticated() {
        service.getOauth2AuthenticatedRestTemplate();
    }
}
