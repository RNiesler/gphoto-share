package rniesler.gphotoshare.services;

import reactor.core.publisher.Mono;
import rniesler.gphotoshare.domain.Album;
import rniesler.gphotoshare.domain.AlbumsList;
import rniesler.gphotoshare.domain.ShareInfo;

import java.util.Optional;

public interface AlbumService {
    Mono<AlbumsList> listAlbums(Optional<String> pageToken);

    Mono<Album> getAlbum(String albumId);

    Mono<Album> shareAlbum(String albumId, ShareInfo shareInfo);

    Mono<ShareInfo> createAndShareAlbum(String name);
}
