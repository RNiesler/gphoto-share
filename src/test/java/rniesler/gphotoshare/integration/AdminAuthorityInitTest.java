package rniesler.gphotoshare.integration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import rniesler.gphotoshare.domain.admin.SecurityMapping;
import rniesler.gphotoshare.domain.admin.SecurityMappingRepository;
import rniesler.gphotoshare.security.Authorities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AdminAuthorityInitTest {
    @Autowired
    private SecurityMappingRepository securityMappingRepository;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Test
    public void testAdminIsPresent() {
        SecurityMapping adminMapping = securityMappingRepository.findByAuthoritiesContaining(Authorities.ADMIN.name()).orElseThrow(RuntimeException::new);
        assertEquals(adminEmail, adminMapping.getEmail());
        assertTrue(adminMapping.getAuthorities().contains(Authorities.ADMIN.name()));
    }
}
