package rniesler.gphotoshare.services;

import reactor.core.publisher.Mono;
import rniesler.gphotoshare.domain.Person;

public interface PersonService {
    Mono<Person> getOrPersist(Person stub);

    Mono<Person> getPersonForEmail(String email);
}
