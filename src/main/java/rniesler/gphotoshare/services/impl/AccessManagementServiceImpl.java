package rniesler.gphotoshare.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rniesler.gphotoshare.domain.admin.AccessRequest;
import rniesler.gphotoshare.domain.admin.AccessRequestRepository;
import rniesler.gphotoshare.domain.admin.SecurityMapping;
import rniesler.gphotoshare.domain.admin.SecurityMappingRepository;
import rniesler.gphotoshare.domain.commands.RequestAccessCommand;
import rniesler.gphotoshare.security.Authorities;
import rniesler.gphotoshare.security.SecurityService;
import rniesler.gphotoshare.services.AccessManagementService;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AccessManagementServiceImpl implements AccessManagementService {
    private final SecurityMappingRepository securityMappingRepository;
    private final SecurityService securityService;
    private final AccessRequestRepository accessRequestRepository;

    public AccessManagementServiceImpl(SecurityMappingRepository securityMappingRepository, SecurityService securityService, AccessRequestRepository accessRequestRepository) {
        this.securityMappingRepository = securityMappingRepository;
        this.securityService = securityService;
        this.accessRequestRepository = accessRequestRepository;
    }

    @Override
    public List<String> listAllowedUsers() {
        return securityMappingRepository.findAllByAuthoritiesContaining(Authorities.RNALLOWED.name()).stream()
                .filter(securityMapping -> !securityMapping.getEmail().equals(securityService.getAuthenticatedEmail()))
                .map(SecurityMapping::getEmail)
                .collect(Collectors.toList());
    }

    @Override
    public void saveMapping(SecurityMapping newSecurityMapping) {
        securityMappingRepository.save(newSecurityMapping);
    }

    @Override
    public void deleteMapping(String email) {
        securityMappingRepository.deleteById(email);
    }

    @Override
    public boolean isUserAllowed(String email) {
        return securityMappingRepository.findByEmailAndAuthoritiesContaining(email, Authorities.RNALLOWED.name()).isPresent();
    }

    @Override
    public Optional<AccessRequest> getAccessRequest(String email) {
        return accessRequestRepository.findByEmail(email);
    }

    @Override
    public AccessRequest requestAccess(RequestAccessCommand command) {
        AccessRequest accessRequest = new AccessRequest(command.getEmail(), command.getName(), false);
        return accessRequestRepository.save(accessRequest);
    }

    @Override
    public List<AccessRequest> listPendingAccessRequests() {
        return accessRequestRepository.findAllByDenied(false);
    }

    @Override
    public void denyAccessRequest(String email) {
        accessRequestRepository.findByEmail(email).ifPresentOrElse(accessRequest -> {
            accessRequest.setDenied(true);
            accessRequestRepository.save(accessRequest);
        }, () -> log.error("Access request not found for " + email));
    }

    @Override
    public void grantAccessRequest(String email) {
        accessRequestRepository.findByEmail(email)
                .filter(accessRequest -> !accessRequest.isDenied())
                .ifPresentOrElse(accessRequest -> {
                    securityMappingRepository.save(new SecurityMapping(accessRequest.getEmail(), Set.of(Authorities.RNALLOWED.name())));
                    accessRequestRepository.delete(accessRequest);
                }, () -> log.error("Access request not found for " + email));
    }
}
