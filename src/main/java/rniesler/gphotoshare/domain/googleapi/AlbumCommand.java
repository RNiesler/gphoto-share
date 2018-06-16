package rniesler.gphotoshare.domain.googleapi;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AlbumCommand {
    private String title;
}
