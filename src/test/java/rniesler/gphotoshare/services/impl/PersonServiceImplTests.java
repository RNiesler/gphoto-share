package rniesler.gphotoshare.services.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Example;
import reactor.core.publisher.Mono;
import rniesler.gphotoshare.domain.Person;
import rniesler.gphotoshare.domain.PersonRepository;

import static org.mockito.Mockito.*;

public class PersonServiceImplTests {
    @Mock
    private PersonRepository personRepository;

    private PersonServiceImpl service;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        service = new PersonServiceImpl(personRepository);
    }

    @Test
    public void testGetPersonForEmail() {
        String email = "abc";
        when(personRepository.findByEmail(email)).thenReturn(Mono.just(Person.builder().email(email).build()));
        Mono<Person> returnedMono = service.getPersonForEmail(email);
        verify(personRepository).findByEmail(email);
        Assertions.assertEquals(email, returnedMono.block().getEmail());
    }

    @Test
    public void testGetOrPersistWhenFound() {
        Person testPerson = Person.builder().email("abc").build();
        when(personRepository.findOne(any(Example.class))).thenReturn(Mono.just(testPerson));
        when(personRepository.save(any(Person.class))).thenReturn(Mono.empty()); // it's still called to prepare the Mono in defaultIfEmpty
        Mono<Person> monoPerson = service.getOrPersist(testPerson);
        Assertions.assertEquals(testPerson, monoPerson.block());
    }


    @Test
    public void testGetOrPersistWhenNotFound() {
        Person testPerson = Person.builder().email("abc").build();
        when(personRepository.findOne(any(Example.class))).thenReturn(Mono.empty());
        when(personRepository.save(any(Person.class))).thenReturn(Mono.just(testPerson));
        Mono<Person> monoPerson = service.getOrPersist(testPerson);
        Assertions.assertEquals(testPerson, monoPerson.block());
    }
}
