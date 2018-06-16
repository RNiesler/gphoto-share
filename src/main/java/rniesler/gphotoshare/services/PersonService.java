package rniesler.gphotoshare.services;

import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import rniesler.gphotoshare.domain.Person;
import rniesler.gphotoshare.domain.PersonRepository;

@Service
public class PersonService {

    private final PersonRepository personRepository;

    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public Mono<Person> getOrPersist(Person stub) {
        return personRepository.findOne(Example.of(stub))
                .switchIfEmpty(personRepository.save(stub));
    }

    public Mono<Person> getPersonForEmail(String email) {
        return personRepository.findByEmail(email);
    }
}
