package rniesler.gphotoshare.services;

import rniesler.gphotoshare.domain.googleapi.GoogleAlbum;
import rniesler.gphotoshare.domain.googleapi.ShareInfo;
import rniesler.gphotoshare.domain.googleapi.AlbumsList;

import java.util.Optional;

public interface AlbumService {
    AlbumsList listAlbums(Optional<String> pageToken);

    Optional<GoogleAlbum> getAlbum(String albumId);

    ShareInfo createAndShareAlbum(String name);
}
