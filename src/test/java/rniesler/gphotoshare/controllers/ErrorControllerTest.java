package rniesler.gphotoshare.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import rniesler.gphotoshare.domain.admin.AccessRequest;
import rniesler.gphotoshare.domain.commands.RequestAccessCommand;
import rniesler.gphotoshare.security.SecurityService;
import rniesler.gphotoshare.services.AccessManagementService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ErrorControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AccessManagementService accessManagementService;
    @Mock
    private SecurityService securityService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(new ErrorController(securityService, accessManagementService)).build();
    }

    @Test
    public void testAccessDeniedMappingWhenNotAuthenticated() throws Exception {
        when(securityService.isAuthenticated()).thenReturn(false);
        mockMvc.perform(get("/errors/accessDenied"))
                .andExpect(status().isForbidden())
                .andExpect(view().name("errors/accessDenied"));
        verify(accessManagementService, never()).getAccessRequest(any());
    }

    @Test
    public void testAccessDeniedWithNoRequest() throws Exception {
        String testEmail = "test@test";
        when(securityService.isAuthenticated()).thenReturn(true);
        when(accessManagementService.isUserAllowed(testEmail)).thenReturn(false);
        when(securityService.getAuthenticatedEmail()).thenReturn(testEmail);
        when(accessManagementService.getAccessRequest(testEmail)).thenReturn(Optional.empty());
        mockMvc.perform(get("/errors/accessDenied"))
                .andExpect(status().isForbidden())
                .andExpect(view().name("errors/accessDenied"))
                .andExpect(model().attributeExists("request"))
                .andExpect(model().attributeDoesNotExist("existingRequest"));
    }

    @Test
    public void testAccessDeniedWithExistingRequest() throws Exception {
        String testEmail = "test@test";
        when(securityService.isAuthenticated()).thenReturn(true);
        when(accessManagementService.isUserAllowed(testEmail)).thenReturn(false);
        when(securityService.getAuthenticatedEmail()).thenReturn(testEmail);
        when(accessManagementService.getAccessRequest(testEmail)).thenReturn(Optional.of(new AccessRequest()));
        mockMvc.perform(get("/errors/accessDenied"))
                .andExpect(status().isForbidden())
                .andExpect(view().name("errors/accessDenied"))
                .andExpect(model().attributeDoesNotExist("request"))
                .andExpect(model().attributeExists("existingRequest"));
    }

    @Test
    public void testRequestAccessWhenInvalid() throws Exception {
        RequestAccessCommand command = new RequestAccessCommand();
        mockMvc.perform(post("/errors/requestAccess").flashAttr("request", command))
                .andExpect(status().isOk())
                .andExpect(view().name("errors/accessDenied"))
                .andExpect(model().attributeExists("request"))
                .andExpect(model().attributeDoesNotExist("existingRequest"));
        verify(accessManagementService, never()).requestAccess(any());
    }

    @Test
    public void testRequestAccess() throws Exception {
        String testEmail = "test@test";
        RequestAccessCommand command = RequestAccessCommand.builder().name("test").email(testEmail).build();
        mockMvc.perform(post("/errors/requestAccess").flashAttr("request", command))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/errors/accessDenied"));
        verify(accessManagementService).requestAccess(command);
    }
}
