package rniesler.gphotoshare.services;

import rniesler.gphotoshare.domain.Album;
import rniesler.gphotoshare.domain.googleapi.AlbumsList;
import rniesler.gphotoshare.domain.ShareInfo;

import java.util.Optional;

public interface AlbumService {
    AlbumsList listAlbums(Optional<String> pageToken);

    Optional<Album> getAlbum(String albumId);

    Optional<Album> shareAlbum(String albumId, ShareInfo shareInfo);

    ShareInfo createAndShareAlbum(String name);
}
