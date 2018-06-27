package rniesler.gphotoshare.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebPushKeys {
    private String auth;
    private String p256dh;
}
