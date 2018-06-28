package rniesler.gphotoshare.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import rniesler.gphotoshare.domain.notifications.WebPushSubscription;
import rniesler.gphotoshare.services.NotificationService;

@RestController
@Slf4j
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping(value = "/{email}/subscribe", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void subscribe(@PathVariable("email") String email, @RequestBody WebPushSubscription subscription) {
        notificationService.subscribe(email, subscription);
    }
}
