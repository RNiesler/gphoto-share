package rniesler.gphotoshare.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class AlbumsList {
    @JsonProperty("sharedAlbums")
    private List<Album> albums;
    private String nextPageToken;
}
