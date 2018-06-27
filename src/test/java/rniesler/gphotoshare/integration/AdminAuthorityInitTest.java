package rniesler.gphotoshare.integration;

import nl.martijndwars.webpush.PushService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import rniesler.gphotoshare.domain.admin.SecurityMapping;
import rniesler.gphotoshare.domain.admin.SecurityMappingRepository;
import rniesler.gphotoshare.security.Authorities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class AdminAuthorityInitTest {
    @Autowired
    private SecurityMappingRepository securityMappingRepository;

    @Value("${app.admin.email}")
    private String adminEmail;

    @MockBean
    private PushService pushService;

    @Test
    public void testAdminIsPresent() {
        SecurityMapping adminMapping = securityMappingRepository.findByAuthoritiesContaining(Authorities.ADMIN.name()).orElseThrow(RuntimeException::new);
        assertEquals(adminEmail, adminMapping.getEmail());
        assertTrue(adminMapping.getAuthorities().contains(Authorities.ADMIN.name()));
    }
}
