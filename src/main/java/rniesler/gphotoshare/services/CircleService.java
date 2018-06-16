package rniesler.gphotoshare.services;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rniesler.gphotoshare.domain.Circle;

public interface CircleService {
    Flux<Circle> findAll();

    Flux<Circle> findAllByMember(String memberEmail);

    Mono<Circle> get(String id);

    Mono<Circle> persist(Circle circle);

    Mono<Void> importFromGooglePlus();
}
