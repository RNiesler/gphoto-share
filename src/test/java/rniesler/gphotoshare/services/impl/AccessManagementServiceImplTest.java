package rniesler.gphotoshare.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import rniesler.gphotoshare.domain.admin.AccessRequest;
import rniesler.gphotoshare.domain.admin.AccessRequestRepository;
import rniesler.gphotoshare.domain.admin.SecurityMapping;
import rniesler.gphotoshare.domain.admin.SecurityMappingRepository;
import rniesler.gphotoshare.domain.commands.RequestAccessCommand;
import rniesler.gphotoshare.security.SecurityService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class AccessManagementServiceImplTest {
    private AccessManagementServiceImpl service;

    @Mock
    private SecurityMappingRepository securityMappingRepository;
    @Mock
    private SecurityService securityService;
    @Mock
    private AccessRequestRepository accessRequestRepository;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        service = new AccessManagementServiceImpl(securityMappingRepository, securityService, accessRequestRepository);
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

    @Test
    public void testIsUserAllowed() {
        String testEmail = "test@test";
        when(securityMappingRepository.findByEmailAndAuthoritiesContaining(eq(testEmail), anyString())).thenReturn(Optional.of(new SecurityMapping()));
        assertTrue(service.isUserAllowed(testEmail));
    }

    @Test
    public void testRequestAccess() {
        String testEmail = "test@test";
        RequestAccessCommand command = RequestAccessCommand.builder().email(testEmail).build();
        service.requestAccess(command);
        ArgumentCaptor<AccessRequest> captor = ArgumentCaptor.forClass(AccessRequest.class);
        verify(accessRequestRepository).save(captor.capture());
        assertEquals(testEmail, captor.getValue().getEmail());
    }

    @Test
    public void testListPendingAccessRequests() {
        List<AccessRequest> list = List.of(AccessRequest.builder().email("test@test").build());
        when(accessRequestRepository.findAllByDenied(false)).thenReturn(list);
        assertEquals(list, service.listPendingAccessRequests());
    }

    @Test
    public void testDenyAccessRequestWhenNoRequest() {
        String testEmail = "test@test";
        when(accessRequestRepository.findByEmail(testEmail)).thenReturn(Optional.empty());
        service.denyAccessRequest(testEmail);
        verify(accessRequestRepository, never()).save(any(AccessRequest.class));
    }

    @Test
    public void testDenyAccessRequest() {
        String testEmail = "test@test";
        AccessRequest accessRequest = AccessRequest.builder().email(testEmail).build();
        when(accessRequestRepository.findByEmail(testEmail)).thenReturn(Optional.of(accessRequest));
        service.denyAccessRequest(testEmail);
        ArgumentCaptor<AccessRequest> captor = ArgumentCaptor.forClass(AccessRequest.class);
        verify(accessRequestRepository).save(captor.capture());
        assertTrue(captor.getValue().isDenied());
    }

    @Test
    public void testGrantAccessRequestWhenNoRequest() {
        String testEmail = "test@test";
        when(accessRequestRepository.findByEmail(testEmail)).thenReturn(Optional.empty());
        service.grantAccessRequest(testEmail);
        verify(securityMappingRepository, never()).save(any(SecurityMapping.class));
        verify(accessRequestRepository, never()).delete(any(AccessRequest.class));
    }

    @Test
    public void testGrantAccessRequest() {
        String testEmail = "test@test";
        AccessRequest accessRequest = AccessRequest.builder().email(testEmail).build();
        when(accessRequestRepository.findByEmail(testEmail)).thenReturn(Optional.of(accessRequest));
        service.grantAccessRequest(testEmail);
        verify(accessRequestRepository).delete(accessRequest);
        verify(securityMappingRepository).save(any(SecurityMapping.class));
    }

    @Test
    public void testGetAccessRequest() {
        String testEmail = "test@test";
        service.getAccessRequest(testEmail);
        verify(accessRequestRepository).findByEmail(testEmail);
    }
}
