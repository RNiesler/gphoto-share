package rniesler.gphotoshare.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.apache.http.HttpResponse;
import org.jose4j.lang.JoseException;
import org.springframework.stereotype.Service;
import rniesler.gphotoshare.domain.*;
import rniesler.gphotoshare.domain.notifications.NewAlbumNotification;
import rniesler.gphotoshare.domain.notifications.WebPushSubscription;
import rniesler.gphotoshare.exceptions.AlbumNotFoundException;
import rniesler.gphotoshare.exceptions.PersonNotFoundException;
import rniesler.gphotoshare.services.NotificationService;
import rniesler.gphotoshare.services.SharedAlbumService;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {
    private final SharedAlbumService sharedAlbumService;
    private final SharedAlbumRepository sharedAlbumRepository;
    private final PersonRepository personRepository;
    private final PushService pushService;

    public NotificationServiceImpl(SharedAlbumService sharedAlbumService, SharedAlbumRepository sharedAlbumRepository,
                                   PersonRepository personRepository, PushService pushService) {
        this.sharedAlbumService = sharedAlbumService;
        this.sharedAlbumRepository = sharedAlbumRepository;
        this.personRepository = personRepository;
        this.pushService = pushService;
    }

    @Override
    public void notify(String albumId) {
        sharedAlbumService.getSharedAlbum(albumId).filter(sharedAlbum -> !sharedAlbum.isNotificationSent()).ifPresentOrElse(sharedAlbum -> {
            sendNotificationsForAlbum(sharedAlbum);
            sharedAlbum.setNotificationSent(true);
            sharedAlbumRepository.save(sharedAlbum);
        }, () -> {
            log.error("Could not send notification. No album found for id " + albumId);
            throw new AlbumNotFoundException();
        });
    }

    private void sendNotificationsForAlbum(SharedAlbum sharedAlbum) {
        Map<PersonSubscription, Future<HttpResponse>> futureMap = sharedAlbumService.getUsersForSharedAlbum(sharedAlbum).stream()
                .map(email -> personRepository.findByEmail(email))
                .flatMap(Optional::stream)
                .filter(person -> person.getSubscriptions() != null)
                .flatMap(person -> person.getSubscriptions().stream().map(subscription -> new PersonSubscription(person, subscription)))
                .collect(HashMap::new, (map, personSubscription) -> map.put(personSubscription, sendNotification(personSubscription, sharedAlbum)), HashMap::putAll);
        for (Map.Entry<PersonSubscription, Future<HttpResponse>> entry : futureMap.entrySet()) {
            if (entry.getValue() != null) {
                try {
                    HttpResponse response = entry.getValue().get();
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode == 404 || statusCode == 410) { // 404 and 410 mean that there's no subscription is the PushService
                        removeSubscription(entry.getKey());
                    }
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Sending notification failed for " + entry.getKey().person);
                }
            }
        }
    }

    /**
     * @return Future of PushService response or null if exception thrown by the PushService.
     */
    private Future<HttpResponse> sendNotification(PersonSubscription personSubscription, SharedAlbum sharedAlbum) {
        NewAlbumNotification notification = NewAlbumNotification.builder()
                .title(sharedAlbum.getName())
                .url(sharedAlbum.getPublicUrl())
                .description("Album has been shared with you")
//                .iconUrl("test") //TODO icon - photo url
                .build();
        try {
            return pushService.sendAsync(new Notification(convertSubscription(personSubscription.subscription),
                    new ObjectMapper().writeValueAsString(notification)));
        } catch (IOException | GeneralSecurityException | JoseException e) {
            log.error("Could not send notification. " + e.getClass().getName() + ": " + e.getMessage());
            return null;
        }
    }

    private static Subscription convertSubscription(WebPushSubscription webPushSubscription) {
        Subscription subscription = new Subscription();
        subscription.keys = subscription.new Keys(webPushSubscription.getKeys().getP256dh(),
                webPushSubscription.getKeys().getAuth());
        subscription.endpoint = webPushSubscription.getEndpoint();
        return subscription;
    }

    private void removeSubscription(PersonSubscription personSubscription) {
        personRepository.findByEmail(personSubscription.person.getEmail())
                .ifPresent(person -> {
                    person.getSubscriptions().remove(personSubscription.subscription);
                    personRepository.save(person);
                });

    }

    /**
     * Persist WebPush subscription for the user
     *
     * @param email        User's email
     * @param subscription Web Push subscription entity
     */
    @Override
    public void subscribe(String email, WebPushSubscription subscription) {
        personRepository.findByEmail(email).ifPresentOrElse(person -> {
            if (person.getSubscriptions() == null) {
                person.setSubscriptions(new HashSet<>());
            }
            person.getSubscriptions().add(subscription);
            personRepository.save(person);
        }, () -> {
            throw new PersonNotFoundException();
        });
    }

    /**
     * Just a Person - WebPushSubscription tuple
     */
    @AllArgsConstructor
    private class PersonSubscription {
        Person person;
        WebPushSubscription subscription;
    }
}
