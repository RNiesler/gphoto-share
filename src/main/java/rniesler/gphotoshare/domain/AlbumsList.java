package rniesler.gphotoshare.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class AlbumsList {
    private List<Album> albums;
}
