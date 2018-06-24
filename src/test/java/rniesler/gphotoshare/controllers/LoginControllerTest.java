package rniesler.gphotoshare.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import rniesler.gphotoshare.security.SecurityService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class LoginControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SecurityService securityService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        InMemoryClientRegistrationRepository inMemoryClientRegistrationRepository = new InMemoryClientRegistrationRepository(CommonOAuth2Provider.GOOGLE.getBuilder("test").clientId("test").clientSecret("test").build());
        this.mockMvc = MockMvcBuilders.standaloneSetup(new LoginController(inMemoryClientRegistrationRepository, securityService)).build();
    }

    @Test
    public void testLoginAlreadyAuthenticated() throws Exception {
        when(securityService.isAuthenticated()).thenReturn(true);
        mockMvc.perform(get("/oauth_login"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void testLogin() throws Exception {
        when(securityService.isAuthenticated()).thenReturn(false);

        mockMvc.perform(get("/oauth_login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("urls"));
    }
}
