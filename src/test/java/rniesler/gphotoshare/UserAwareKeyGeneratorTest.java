package rniesler.gphotoshare;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cache.interceptor.KeyGenerator;
import rniesler.gphotoshare.security.SecurityService;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;


public class UserAwareKeyGeneratorTest {
    private KeyGenerator keyGenerator;

    @Mock
    private SecurityService securityService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        keyGenerator = new GphotoShareApplication().userAwareKeyGenerator(securityService);
    }

    @Test
    public void testEmailInKey() {
        String testEmail = "test@test";
        when(securityService.getAuthenticatedEmail()).thenReturn(testEmail);
        String key = (String) keyGenerator.generate(this, this.getClass().getMethods()[0]);
        assertTrue(key.contains(testEmail));
    }
}
