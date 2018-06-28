package rniesler.gphotoshare.services;

import rniesler.gphotoshare.domain.notifications.WebPushSubscription;

public interface NotificationService {
    void notify(String albumId);

    void subscribe(String email, WebPushSubscription subscription);
}
