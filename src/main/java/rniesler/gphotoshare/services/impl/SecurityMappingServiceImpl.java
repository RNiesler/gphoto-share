package rniesler.gphotoshare.services.impl;

import org.springframework.stereotype.Service;
import rniesler.gphotoshare.domain.admin.SecurityMapping;
import rniesler.gphotoshare.domain.admin.SecurityMappingRepository;
import rniesler.gphotoshare.security.Authorities;
import rniesler.gphotoshare.security.SecurityService;
import rniesler.gphotoshare.services.SecurityMappingService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SecurityMappingServiceImpl implements SecurityMappingService {
    private final SecurityMappingRepository securityMappingRepository;
    private final SecurityService securityService;

    public SecurityMappingServiceImpl(SecurityMappingRepository securityMappingRepository, SecurityService securityService) {
        this.securityMappingRepository = securityMappingRepository;
        this.securityService = securityService;
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
}
