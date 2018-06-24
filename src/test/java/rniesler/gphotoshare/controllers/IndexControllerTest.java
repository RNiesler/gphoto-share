package rniesler.gphotoshare.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.MethodParameter;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import rniesler.gphotoshare.domain.SharedAlbum;
import rniesler.gphotoshare.security.SecurityService;
import rniesler.gphotoshare.services.ViewerService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class IndexControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SecurityService securityService;
    @Mock
    private ViewerService viewerService;

    static class WithOAuth2TokenArgumentResolver implements HandlerMethodArgumentResolver {

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.getParameterType().isAssignableFrom(OAuth2AuthenticationToken.class);
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
            return new OAuth2AuthenticationToken(new OAuth2User() {
                @Override
                public Collection<? extends GrantedAuthority> getAuthorities() {
                    return null;
                }

                @Override
                public Map<String, Object> getAttributes() {
                    return null;
                }

                @Override
                public String getName() {
                    return "test";
                }
            }, Collections.emptyList(), "test");
        }
    }

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);

        this.mockMvc = MockMvcBuilders.standaloneSetup(new IndexController(securityService, viewerService))
                .setCustomArgumentResolvers(new WithOAuth2TokenArgumentResolver()).build();
    }

    @Test
    public void testIndex() throws Exception {
        List<SharedAlbum> albumList = List.of(SharedAlbum.builder().name("test").build());
        when(viewerService.retrieveAccessibleAlbums()).thenReturn(albumList);

        OAuth2AuthenticationToken token = mock(OAuth2AuthenticationToken.class);
        when(securityService.retrieveAuthenticationToken()).thenReturn(token);
        String testUserName = "test";
        when(token.getName()).thenReturn(testUserName);

        OAuth2AuthorizedClient oauth2Client = mock(OAuth2AuthorizedClient.class);
        ClientRegistration clientRegistration = CommonOAuth2Provider.GOOGLE.getBuilder("google")
                .clientId("test").clientSecret("test").build();
        when(oauth2Client.getClientRegistration()).thenReturn(clientRegistration);

        when(securityService.getAuthorizedClient(any())).thenReturn(oauth2Client);

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("albums", albumList))
                .andExpect(model().attribute("clientName", clientRegistration.getClientName()))
                .andExpect(model().attribute("userName", testUserName));
    }

}
