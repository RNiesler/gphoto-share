package rniesler.gphotoshare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import rniesler.gphotoshare.security.SecurityService;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

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
            keyBuilder.append(Arrays.stream(params).map(obj -> {
                if (obj == null) {
                    return "null";
                } else if (obj instanceof Optional) {
                    return ((Optional) obj).isPresent() ? ((Optional) obj).get().toString() : "empty";
                } else {
                    return obj.toString();
                }
            }).collect(Collectors.joining(",")));
            return keyBuilder.toString();
        };
    }
}
