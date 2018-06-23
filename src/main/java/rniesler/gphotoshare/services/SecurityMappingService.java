package rniesler.gphotoshare.services;

import rniesler.gphotoshare.domain.admin.SecurityMapping;

import java.util.List;

public interface SecurityMappingService {
    List<String> listAllowedUsers();

    void saveMapping(SecurityMapping newSecurityMapping);

    void deleteMapping(String email);
}
