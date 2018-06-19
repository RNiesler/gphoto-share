package rniesler.gphotoshare.security.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import rniesler.gphotoshare.services.PersonService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SecurityServiceImplTest {
    private SecurityServiceImpl service;

    @Mock
    private OAuth2AuthorizedClientService oAuth2AuthorizedClientService;
    @Mock
    private PersonService personService;
    @Mock
    private OAuth2AuthenticationToken token;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        service = new SecurityServiceImpl(oAuth2AuthorizedClientService, personService);
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
    public void testGetAuthenticatedUser() {

    }
}
