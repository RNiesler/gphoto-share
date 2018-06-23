package rniesler.gphotoshare.services.impl;

import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import rniesler.gphotoshare.domain.Person;
import rniesler.gphotoshare.domain.PersonRepository;
import rniesler.gphotoshare.domain.admin.SecurityMapping;
import rniesler.gphotoshare.domain.admin.SecurityMappingRepository;
import rniesler.gphotoshare.services.PersonService;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@Service
public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;
    private final SecurityMappingRepository securityMappingRepository;

    public PersonServiceImpl(PersonRepository personRepository, SecurityMappingRepository securityMappingRepository) {
        this.personRepository = personRepository;
        this.securityMappingRepository = securityMappingRepository;
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

    @Override
    public Set<String> getPersonAuthorities(String email) {
        return securityMappingRepository.findById(email).map(SecurityMapping::getAuthorities).orElseGet(() -> Collections.emptySet());
    }
}
