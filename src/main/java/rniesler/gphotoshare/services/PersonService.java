package rniesler.gphotoshare.services;

import rniesler.gphotoshare.domain.Person;

import java.util.Optional;

public interface PersonService {
    Person getOrPersist(Person stub);

    Optional<Person> getPersonForEmail(String email);
}
