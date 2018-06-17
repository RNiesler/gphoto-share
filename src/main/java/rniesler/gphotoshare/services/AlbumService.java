package rniesler.gphotoshare.services;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import reactor.core.publisher.Mono;
import rniesler.gphotoshare.domain.Album;
import rniesler.gphotoshare.domain.AlbumsList;
import rniesler.gphotoshare.domain.ShareInfo;

import java.util.Optional;

public interface AlbumService {
    Mono<AlbumsList> listAlbums(OAuth2AuthenticationToken authenticationToken, Optional<String> pageToken);

    Mono<Album> getAlbum(OAuth2AuthenticationToken authenticationToken, String albumId);

    Mono<Album> shareAlbum(OAuth2AuthenticationToken authenticationToken, String albumId, ShareInfo shareInfo);

    Mono<ShareInfo> createAndShareAlbum(String name);
}
