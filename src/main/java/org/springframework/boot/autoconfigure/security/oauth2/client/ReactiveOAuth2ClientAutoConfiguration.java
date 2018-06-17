package org.springframework.boot.autoconfigure.security.oauth2.client;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

@Configuration
@AutoConfigureBefore(name = "org.springframework.boot.autoconfigure.security.reactive.WebFluxSecurityConfiguration")
@ConditionalOnClass({ EnableWebFluxSecurity.class, ClientRegistration.class })
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@Import({ ReactiveOAuth2ClientRegistrationRepositoryConfiguration.class,
        ReactiveOAuth2WebSecurityConfiguration.class })
public class ReactiveOAuth2ClientAutoConfiguration {

}