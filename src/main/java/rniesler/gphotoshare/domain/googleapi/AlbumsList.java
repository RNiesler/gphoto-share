package rniesler.gphotoshare.domain.googleapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class AlbumsList implements Serializable {
    @JsonProperty("albums") // should be sharedAlbums when using the sharedAlbums endpoint
    private List<GoogleAlbum> albums;
    private String nextPageToken;
}
