package rniesler.gphotoshare.services.impl;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import rniesler.gphotoshare.domain.Circle;
import rniesler.gphotoshare.domain.CircleRepository;
import rniesler.gphotoshare.security.SecurityService;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CircleServiceImplTest {
    private CircleServiceImpl service;

    @Mock
    private CircleRepository circleRepository;
    @Mock
    private SecurityService securityService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        service = new CircleServiceImpl(circleRepository, securityService);
    }

    @Test
    public void testFindAll() {
        String testEmail = "email";
        when(securityService.getAuthenticatedEmail()).thenReturn(testEmail);
        when(circleRepository.findAllByOwner(testEmail)).thenReturn(Collections.emptyList());
        assertEquals(Collections.emptyList(), service.findAll());
        verify(circleRepository).findAllByOwner(testEmail);
        verify(securityService).getAuthenticatedEmail();
    }

    @Test
    public void testFindAllByMember() {
        String testMember = "email";
        when(circleRepository.findAllByMembersIsContaining(testMember)).thenReturn(Collections.emptyList());
        assertEquals(Collections.emptyList(), service.findAllByMember(testMember));
        verify(circleRepository).findAllByMembersIsContaining(testMember);
    }

    @Test
    public void testGet() {
        ObjectId testCircleId = ObjectId.get();
        Circle testCircle = Circle.builder().id(testCircleId).build();
        when(circleRepository.findById(testCircleId)).thenReturn(Optional.of(testCircle));
        assertEquals(testCircle, service.get(testCircleId.toHexString()).get());
        verify(circleRepository).findById(testCircleId);
    }

    @Test
    public void testPersist() {
        Circle testCircle = Circle.builder().name("test").build();
        when(circleRepository.save(testCircle)).thenReturn(testCircle);
        assertEquals(testCircle, service.persist(testCircle));
        verify(circleRepository).save(testCircle);
    }

    @Test
    public void testDelete() {
        ObjectId testId = ObjectId.get();
        service.deleteCircle(testId.toHexString());
        verify(circleRepository).deleteById(testId);
    }
}
