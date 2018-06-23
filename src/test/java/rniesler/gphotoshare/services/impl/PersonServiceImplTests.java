package rniesler.gphotoshare.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Example;
import rniesler.gphotoshare.domain.Person;
import rniesler.gphotoshare.domain.PersonRepository;
import rniesler.gphotoshare.domain.admin.SecurityMapping;
import rniesler.gphotoshare.domain.admin.SecurityMappingRepository;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class PersonServiceImplTests {
    @Mock
    private PersonRepository personRepository;
    @Mock
    private SecurityMappingRepository securityMappingRepository;

    private PersonServiceImpl service;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        service = new PersonServiceImpl(personRepository, securityMappingRepository);
    }

    @Test
    public void testGetPersonForEmail() {
        String email = "abc";
        when(personRepository.findByEmail(email)).thenReturn(Optional.of(Person.builder().email(email).build()));
        Optional<Person> returnedPerson = service.getPersonForEmail(email);
        verify(personRepository).findByEmail(email);
        assertEquals(email, returnedPerson.get().getEmail());
    }

    @Test
    public void testGetOrPersistWhenFound() {
        Person testPerson = Person.builder().email("abc").build();
        when(personRepository.findOne(any(Example.class))).thenReturn(Optional.of(testPerson));
        Person person = service.getOrPersist(testPerson);
        verify(personRepository, never()).save(any());
        assertEquals(testPerson, person);
    }


    @Test
    public void testGetOrPersistWhenNotFound() {
        Person testPerson = Person.builder().email("abc").build();
        when(personRepository.findOne(any(Example.class))).thenReturn(Optional.empty());
        when(personRepository.save(any(Person.class))).thenReturn(testPerson);
        Person person = service.getOrPersist(testPerson);
        assertEquals(testPerson, person);
    }

    @Test
    public void testGetPersonAuthorities() {
        String email = "test@test";
        SecurityMapping testSecurityMapping = new SecurityMapping();
        Set<String> testAuthorities = Set.of("A", "B");
        testSecurityMapping.setAuthorities(testAuthorities);

        when(securityMappingRepository.findById(email)).thenReturn(Optional.of(testSecurityMapping));
        assertEquals(testAuthorities, service.getPersonAuthorities(email));
    }
}
