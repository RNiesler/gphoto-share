package rniesler.gphotoshare.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class WebPushControllerAdvice {
    private final String webpushPubKey;

    public WebPushControllerAdvice(@Value("${webpush.keys.public}") String webpushPubKey) {
        this.webpushPubKey = webpushPubKey;
    }

    /**
     * Inject Web Push public key to the model
     */
    @ModelAttribute("_webpush_pubkey")
    public String webpushPubKey() {
        return webpushPubKey;
    }
}
