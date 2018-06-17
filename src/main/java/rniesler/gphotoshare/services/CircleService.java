package rniesler.gphotoshare.services;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rniesler.gphotoshare.domain.Circle;

public interface CircleService {
    Flux<Circle> findAll(OAuth2AuthenticationToken authenticationToken);

    Flux<Circle> findAllByMember(String memberEmail);

    Mono<Circle> get(String id);

    Mono<Circle> persist(Circle circle);
}
