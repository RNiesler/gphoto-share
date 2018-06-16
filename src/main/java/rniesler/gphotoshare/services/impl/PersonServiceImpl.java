package rniesler.gphotoshare.services.impl;

import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import rniesler.gphotoshare.domain.Person;
import rniesler.gphotoshare.domain.PersonRepository;
import rniesler.gphotoshare.services.PersonService;

@Service
public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;

    public PersonServiceImpl(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    public Mono<Person> getOrPersist(Person stub) {
        return personRepository.findOne(Example.of(stub))
                .switchIfEmpty(personRepository.save(stub));
    }

    @Override
    public Mono<Person> getPersonForEmail(String email) {
        return personRepository.findByEmail(email);
    }
}
