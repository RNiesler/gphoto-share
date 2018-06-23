package rniesler.gphotoshare.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import rniesler.gphotoshare.domain.admin.SecurityMapping;
import rniesler.gphotoshare.domain.admin.SecurityMappingRepository;

import java.util.Set;

@Component
public class InitAdminRole implements ApplicationListener<ContextRefreshedEvent> {
    private final SecurityMappingRepository securityMappingRepository;

    @Value("${app.admin.email}")
    private String adminEmail;

    public InitAdminRole(SecurityMappingRepository securityMappingRepository) {
        this.securityMappingRepository = securityMappingRepository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        securityMappingRepository.findByAuthoritiesContaining(Authorities.ADMIN.name()).orElseGet(() -> {
            SecurityMapping securityMapping = new SecurityMapping();
            securityMapping.setEmail(adminEmail);
            securityMapping.setAuthorities(Set.of(Authorities.ADMIN.name(),Authorities.RNALLOWED.name()));
            return securityMappingRepository.save(securityMapping);
        });
    }
}
