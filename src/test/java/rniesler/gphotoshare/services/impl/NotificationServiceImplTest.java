package rniesler.gphotoshare.services.impl;

import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jose4j.lang.JoseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import rniesler.gphotoshare.domain.*;
import rniesler.gphotoshare.exceptions.AlbumNotFoundException;
import rniesler.gphotoshare.exceptions.PersonNotFoundException;
import rniesler.gphotoshare.services.SharedAlbumService;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class NotificationServiceImplTest {

    private NotificationServiceImpl service;
    @Mock
    private SharedAlbumService sharedAlbumService;
    @Mock
    private SharedAlbumRepository sharedAlbumRepository;
    @Mock
    private PersonRepository personRepository;
    @Mock
    private PushService pushService;

    @BeforeAll
    public static void setupBcProvider() {
        Security.addProvider(new BouncyCastleProvider());
    }

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        service = new NotificationServiceImpl(sharedAlbumService, sharedAlbumRepository, personRepository, pushService);
    }

    @Test
    public void testSubscribeWhenPersonNotFound() {
        String testEmail = "test@test";
        when(personRepository.findByEmail(testEmail)).thenReturn(Optional.empty());
        assertThrows(PersonNotFoundException.class, () -> service.subscribe(testEmail, new WebPushSubscription()));
        verify(personRepository, never()).save(any());
    }

    @Test
    public void testSubscribe() {
        String testEmail = "test@test";
        Person person = Person.builder().email(testEmail).build();
        when(personRepository.findByEmail(testEmail)).thenReturn(Optional.of(person));
        service.subscribe(testEmail, new WebPushSubscription());

        ArgumentCaptor<Person> captor = ArgumentCaptor.forClass(Person.class);
        verify(personRepository).save(captor.capture());
        assertEquals(testEmail, captor.getValue().getEmail());
        assertFalse(captor.getValue().getSubscriptions().isEmpty());
    }

    @Test
    public void testNotifyWhenNoAlbum() {
        String testId = "test";
        when(sharedAlbumService.getSharedAlbum(testId)).thenReturn(Optional.empty());
        assertThrows(AlbumNotFoundException.class, () -> service.notify(testId));
        verify(sharedAlbumRepository, never()).save(any());
    }

    @Test
    public void testNotifyWhenNoSubscriptions() throws JoseException, GeneralSecurityException, IOException {
        String testId = "test";
        String email1 = "test@test";
        Person person1 = Person.builder().email(email1).build();
        SharedAlbum album = SharedAlbum.builder().id(testId).build();
        when(sharedAlbumService.getSharedAlbum(testId)).thenReturn(Optional.of(album));
        when(sharedAlbumService.getUsersForSharedAlbum(album)).thenReturn(List.of(email1));
        when(personRepository.findByEmail(email1)).thenReturn(Optional.of(person1));
        service.notify(testId);
        ArgumentCaptor<SharedAlbum> captor = ArgumentCaptor.forClass(SharedAlbum.class);
        verify(sharedAlbumRepository).save(captor.capture());
        assertTrue(captor.getValue().isNotificationSent());
        verify(pushService, never()).sendAsync(any());
    }

    @Test
    public void testNotify() throws JoseException, GeneralSecurityException, IOException {
        String testId = "test";
        SharedAlbum album = SharedAlbum.builder().id(testId).build();
        Person person = preparePersonWithSubscription();
        when(sharedAlbumService.getSharedAlbum(testId)).thenReturn(Optional.of(album));
        when(sharedAlbumService.getUsersForSharedAlbum(album)).thenReturn(List.of(person.getEmail()));
        when(personRepository.findByEmail(person.getEmail())).thenReturn(Optional.of(person));

        Future<HttpResponse> mockResponse = mockResponse(200);
        when(pushService.sendAsync(any(Notification.class))).thenReturn(mockResponse);
        service.notify(testId);

        ArgumentCaptor<SharedAlbum> captor = ArgumentCaptor.forClass(SharedAlbum.class);
        verify(sharedAlbumRepository).save(captor.capture());
        assertTrue(captor.getValue().isNotificationSent());

        ArgumentCaptor<Notification> pushServiceCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(pushService).sendAsync(pushServiceCaptor.capture());
        WebPushSubscription subscription = person.getSubscriptions().iterator().next();
        assertEquals(subscription.getEndpoint(), pushServiceCaptor.getValue().getEndpoint());
        //TODO verify payload

        verify(personRepository, never()).save(any(Person.class)); // verify the subscription was not deleted
    }

    @Test
    public void testNotifyWithSubscriptionErrors() throws JoseException, GeneralSecurityException, IOException {
        String testId = "test";
        SharedAlbum album = SharedAlbum.builder().id(testId).build();

        Person personForOk = preparePersonWithSubscription();
        Person personFor404 = preparePersonWithSubscription();
        Person personFor410 = preparePersonWithSubscription();
        Person personForException = preparePersonWithSubscription();

        when(sharedAlbumService.getSharedAlbum(testId)).thenReturn(Optional.of(album));
        List<String> emails = List.of(personForOk, personFor404, personFor410, personForException).stream()
                .map(Person::getEmail).collect(Collectors.toList());
        when(sharedAlbumService.getUsersForSharedAlbum(album)).thenReturn(emails);

        when(personRepository.findByEmail(personForOk.getEmail())).thenReturn(Optional.of(personForOk));
        when(personRepository.findByEmail(personFor404.getEmail())).thenReturn(Optional.of(personFor404));
        when(personRepository.findByEmail(personFor410.getEmail())).thenReturn(Optional.of(personFor410));
        when(personRepository.findByEmail(personForException.getEmail())).thenReturn(Optional.of(personForException));

        Future<HttpResponse> responseOk = mockResponse(200);
        Future<HttpResponse> response404 = mockResponse(404);
        Future<HttpResponse> response410 = mockResponse(410);

        when(pushService.sendAsync(any(Notification.class))).thenAnswer((Answer<Future<HttpResponse>>) invocation -> {
            String endpoint = ((Notification) invocation.getArgument(0)).getEndpoint();
            if (endpoint.equals(personForOk.getSubscriptions().iterator().next().getEndpoint())) {
                return responseOk;
            } else if (endpoint.equals(personFor404.getSubscriptions().iterator().next().getEndpoint())) {
                return response404;
            } else if (endpoint.equals(personFor410.getSubscriptions().iterator().next().getEndpoint())) {
                return response410;
            } else if (endpoint.equals(personForException.getSubscriptions().iterator().next().getEndpoint())) {
                throw new IOException();
            }
            fail("Unexpected call to push service");
            return null; // just to make compiler happy
        });

        service.notify(testId);

        ArgumentCaptor<SharedAlbum> captor = ArgumentCaptor.forClass(SharedAlbum.class);
        verify(sharedAlbumRepository).save(captor.capture());
        assertTrue(captor.getValue().isNotificationSent());

        verify(pushService, times(4)).sendAsync(any(Notification.class));

        ArgumentCaptor<Person> personArgumentCaptor = ArgumentCaptor.forClass(Person.class);
        verify(personRepository, times(2)).save(personArgumentCaptor.capture()); // verify 404 and 410 subscriptions were deleted
        assertEquals(2, personArgumentCaptor.getAllValues().size());

        // check if only the 404 and 410 person entities were removed subscriptions
        assertEquals(2, personArgumentCaptor.getAllValues().stream()
                .map(Person::getEmail)
                .filter(email -> email.equals(personFor404.getEmail()) || email.equals(personFor410.getEmail()))
                .count());
    }

    private static Future<HttpResponse> mockResponse(int status) {
        CompletableFuture<HttpResponse> future = new CompletableFuture<>();
        HttpResponse httpResponse = mock(HttpResponse.class);
        StatusLine statusLine = new StatusLine() {
            @Override
            public ProtocolVersion getProtocolVersion() {
                return null;
            }

            @Override
            public int getStatusCode() {
                return status;
            }

            @Override
            public String getReasonPhrase() {
                return null;
            }
        };
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        future.complete(httpResponse);
        return future;
    }

    private static Person preparePersonWithSubscription() {
        int randomId = new Random().nextInt();
        String email = "test@test" + randomId;
        //valid key pair
        String testPubKey = "BPEkvnVqAdHw-i_sxCrJVCwYPILT1-tJlQTEjZb9GNBpLyCwRM3Mj3f80lBZ1T1HjKzF4xa0aSGD9t3kg7weaDU=";
        String testPrivKey = "AOzFCjCigmxxPqhrTorvlIJXF-eLmxpjKEv9b9yBWxMq";
        String testEntity = "entity" + randomId;
        WebPushKeys webPushKeys = new WebPushKeys(testPrivKey, testPubKey);
        WebPushSubscription subscription = new WebPushSubscription(testEntity, webPushKeys);
        return Person.builder().email(email).subscriptions(new HashSet<>(Set.of(subscription))).build();
    }
}
