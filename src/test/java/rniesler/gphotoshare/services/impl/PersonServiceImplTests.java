package rniesler.gphotoshare.services.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Example;
import rniesler.gphotoshare.domain.Person;
import rniesler.gphotoshare.domain.PersonRepository;

import java.util.Optional;

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
        when(personRepository.findByEmail(email)).thenReturn(Optional.of(Person.builder().email(email).build()));
        Optional<Person> returnedPerson = service.getPersonForEmail(email);
        verify(personRepository).findByEmail(email);
        Assertions.assertEquals(email, returnedPerson.get().getEmail());
    }

    @Test
    public void testGetOrPersistWhenFound() {
        Person testPerson = Person.builder().email("abc").build();
        when(personRepository.findOne(any(Example.class))).thenReturn(Optional.of(testPerson));
        Person person = service.getOrPersist(testPerson);
        verify(personRepository, never()).save(any());
        Assertions.assertEquals(testPerson, person);
    }


    @Test
    public void testGetOrPersistWhenNotFound() {
        Person testPerson = Person.builder().email("abc").build();
        when(personRepository.findOne(any(Example.class))).thenReturn(Optional.empty());
        when(personRepository.save(any(Person.class))).thenReturn(testPerson);
        Person person = service.getOrPersist(testPerson);
        Assertions.assertEquals(testPerson, person);
    }
}
