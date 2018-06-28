package rniesler.gphotoshare.domain.notifications;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NewAlbumNotification {
    private String url;
    private String title;
    private String iconUrl;
    private String description;
}
