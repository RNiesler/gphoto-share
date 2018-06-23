package rniesler.gphotoshare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.annotation.Bean;
import rniesler.gphotoshare.security.SecurityService;

@EnableCaching
@SpringBootApplication
public class GphotoShareApplication {

    public static void main(String[] args) {
        SpringApplication.run(GphotoShareApplication.class, args);
    }

    @Bean("userAwareKeyGenerator")
    public KeyGenerator userAwareKeyGenerator(SecurityService securityService) {
        return (target, method, params) -> {
            StringBuilder keyBuilder = new StringBuilder();
            keyBuilder.append(method.getName());
            keyBuilder.append("-");
            keyBuilder.append(securityService.getAuthenticatedEmail());
            keyBuilder.append("-");
            keyBuilder.append(SimpleKeyGenerator.generateKey(params));
            return keyBuilder.toString();
        };
    }
}
