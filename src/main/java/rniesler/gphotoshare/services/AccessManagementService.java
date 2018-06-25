package rniesler.gphotoshare.services;

import rniesler.gphotoshare.domain.admin.AccessRequest;
import rniesler.gphotoshare.domain.admin.SecurityMapping;
import rniesler.gphotoshare.domain.commands.RequestAccessCommand;

import java.util.List;
import java.util.Optional;

public interface AccessManagementService {
    List<String> listAllowedUsers();

    void saveMapping(SecurityMapping newSecurityMapping);

    void deleteMapping(String email);

    boolean isUserAllowed(String email);

    Optional<AccessRequest> getAccessRequest(String email);

    AccessRequest requestAccess(RequestAccessCommand command);

    List<AccessRequest> listPendingAccessRequests();

    void denyAccessRequest(String email);

    void grantAccessRequest(String email);
}
