package rniesler.gphotoshare.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import rniesler.gphotoshare.domain.admin.SecurityMapping;
import rniesler.gphotoshare.security.Authorities;
import rniesler.gphotoshare.services.SecurityMappingService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AdminControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SecurityMappingService securityMappingService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(new AdminController(securityMappingService)).build();
    }

    @Test
    public void testListUsers() throws Exception {
        List<String> users = List.of("a@a", "b@b");
        when(securityMappingService.listAllowedUsers()).thenReturn(users);
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("users", users))
                .andExpect(model().attribute("newuser", new SecurityMapping()))
                .andExpect(view().name("listusers"));
    }

    @Test
    public void testAddUser() throws Exception {
        SecurityMapping command = SecurityMapping.builder().email("test@test").build();
        mockMvc.perform(post("/admin/users").flashAttr("newuser", command))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));
        ArgumentCaptor<SecurityMapping> captor = ArgumentCaptor.forClass(SecurityMapping.class);
        verify(securityMappingService).saveMapping(captor.capture());
        assertTrue(captor.getValue().getAuthorities().contains(Authorities.RNALLOWED.name()));
    }

    @Test
    public void testAddUserInvalid() throws Exception {
        SecurityMapping command = SecurityMapping.builder().build();
        mockMvc.perform(post("/admin/users").flashAttr("newuser", command))
                .andExpect(status().isOk())
                .andExpect(view().name("listusers"))
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attribute("newuser", command));
        verify(securityMappingService, never()).saveMapping(any());
    }

    @Test
    public void testDeleteUser() throws Exception {
        mockMvc.perform(post("/admin/users/test@test/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));
        verify(securityMappingService).deleteMapping("test@test");
    }
}
