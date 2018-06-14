package rniesler.gphotoshare.security;

import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfiguration {
    private SecurityService securityService;

    public SecurityConfiguration(SecurityService securityService) {
        this.securityService = securityService;
    }
}
