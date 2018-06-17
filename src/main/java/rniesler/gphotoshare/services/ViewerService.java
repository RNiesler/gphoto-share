package rniesler.gphotoshare.services;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rniesler.gphotoshare.domain.Album;

public interface ViewerService {
    Flux<Album> retrieveAccessibleAlbums(OAuth2AuthenticationToken authenticationToken);

    Mono<Void> joinAlbum(OAuth2AuthenticationToken authenticationToken, String albumId);
}
