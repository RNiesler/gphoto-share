package rniesler.gphotoshare.services;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rniesler.gphotoshare.domain.Album;

public interface ViewerService {
    Flux<Album> retrieveAccessibleAlbums();

    Mono<Void> joinAlbum(String albumId);
}
