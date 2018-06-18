package rniesler.gphotoshare.services.impl;

import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import rniesler.gphotoshare.domain.Person;
import rniesler.gphotoshare.domain.PersonRepository;
import rniesler.gphotoshare.services.PersonService;

import java.util.Optional;

@Service
public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;

    public PersonServiceImpl(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    public Person getOrPersist(Person stub) {
        return personRepository.findOne(Example.of(stub))
                .orElseGet(() -> personRepository.save(stub));
    }

    @Override
    public Optional<Person> getPersonForEmail(String email) {
        return personRepository.findByEmail(email);
    }
}
