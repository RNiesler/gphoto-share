package rniesler.gphotoshare.services;

import rniesler.gphotoshare.domain.Person;

import java.util.Optional;
import java.util.Set;

public interface PersonService {
    Person getOrPersist(Person stub);

    Optional<Person> getPersonForEmail(String email);

    Set<String> getPersonAuthorities(String email);
}
