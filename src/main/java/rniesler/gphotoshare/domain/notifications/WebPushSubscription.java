package rniesler.gphotoshare.domain.notifications;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebPushSubscription {
    private String endpoint;
    private WebPushKeys keys;
}
