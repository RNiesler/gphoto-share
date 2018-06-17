package org.springframework.boot.autoconfigure.security.oauth2.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableConfigurationProperties(OAuth2ClientProperties.class)
@Conditional(OAuth2ClientRegistrationRepositoryConfiguration.ClientsConfiguredCondition.class)
class ReactiveOAuth2ClientRegistrationRepositoryConfiguration {

    private final OAuth2ClientProperties properties;

    ReactiveOAuth2ClientRegistrationRepositoryConfiguration(OAuth2ClientProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean(ReactiveClientRegistrationRepository.class)
    public InMemoryReactiveClientRegistrationRepository clientRegistrationRepository() {
        List<ClientRegistration> registrations = new ArrayList<>(
                OAuth2ClientPropertiesRegistrationAdapter
                        .getClientRegistrations(this.properties).values());
        return new InMemoryReactiveClientRegistrationRepository(registrations);
    }
}
