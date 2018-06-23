package rniesler.gphotoshare.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import rniesler.gphotoshare.domain.admin.SecurityMapping;
import rniesler.gphotoshare.domain.admin.SecurityMappingRepository;
import rniesler.gphotoshare.security.SecurityService;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SecurityMappingServiceImplTest {
    private SecurityMappingServiceImpl service;

    @Mock
    private SecurityMappingRepository securityMappingRepository;
    @Mock
    private SecurityService securityService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        service = new SecurityMappingServiceImpl(securityMappingRepository, securityService);
    }

    @Test
    public void testSaveMapping() {
        SecurityMapping testSecurityMapping = new SecurityMapping();
        service.saveMapping(testSecurityMapping);
        verify(securityMappingRepository).save(testSecurityMapping);
    }

    @Test
    public void deleteMapping() {
        String email = "test@test";
        service.deleteMapping(email);
        verify(securityMappingRepository).deleteById(email);
    }

    @Test
    public void testListAllowedUsersWhenEmpty() {
        when(securityMappingRepository.findAllByAuthoritiesContaining(any())).thenReturn(Collections.emptyList());
        assertEquals(Collections.emptyList(), service.listAllowedUsers());
    }

    @Test
    public void testListAllowedUsers() {
        String testEmail1 = "test@test";
        String testEmail2 = "test2@test";
        String testUsersEmail = "user@test";
        SecurityMapping testMapping1 = SecurityMapping.builder().email(testEmail1).build();
        SecurityMapping testMapping2 = SecurityMapping.builder().email(testEmail2).build();
        SecurityMapping testMapping3 = SecurityMapping.builder().email(testUsersEmail).build();
        when(securityService.getAuthenticatedEmail()).thenReturn(testUsersEmail);
        when(securityMappingRepository.findAllByAuthoritiesContaining(any())).thenReturn(List.of(testMapping1, testMapping2, testMapping3));
        assertEquals(List.of(testEmail1, testEmail2), service.listAllowedUsers());
    }
}
