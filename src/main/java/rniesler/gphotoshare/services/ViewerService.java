package rniesler.gphotoshare.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rniesler.gphotoshare.domain.Album;
import rniesler.gphotoshare.domain.AlbumsRepository;
import rniesler.gphotoshare.domain.ShareInfo;
import rniesler.gphotoshare.domain.googleapi.JoinCommand;
import rniesler.gphotoshare.security.SecurityService;

@Service
public class ViewerService {
    private final String GPHOTOS_API_HOST;
    private final String GPHOTOS_API_SHARED_ALBUMS_PATH;
    private final AlbumsRepository albumsRepository;
    private final CircleService circleService;
    private final SecurityService securityService;
    private final AlbumService albumService;

    public ViewerService(AlbumsRepository albumsRepository, CircleService circleService,
                         SecurityService securityService, AlbumService albumService,
                         @Value("${google.photos.api.host}") String apiHost,
                         @Value("${google.photos.api.sharedAlbums}") String sharedAlbumsApiPath) {
        this.albumsRepository = albumsRepository;
        this.circleService = circleService;
        this.securityService = securityService;
        this.albumService = albumService;
        this.GPHOTOS_API_HOST = apiHost;
        this.GPHOTOS_API_SHARED_ALBUMS_PATH = sharedAlbumsApiPath;
    }

    public Flux<Album> retrieveAccessibleAlbums() {
        return circleService
                .findAllByMember(securityService.getAuthenticatedEmail())
                .flatMap(circle -> albumsRepository.findAllSharedToCircle(circle.getId()));
    }

    public Mono<Void> joinAlbum(String albumId) {
        ShareInfo newShared = albumService.createAndShareAlbum("test").block();
        WebClient webClient = securityService.getOauth2AuthenticatedWebClient();
        return albumService.getAlbum(albumId)
                .flatMap(album ->
                        webClient.post()
                                .uri(uriBuilder -> uriBuilder.scheme("https")
                                        .host(GPHOTOS_API_HOST)
                                        .path(GPHOTOS_API_SHARED_ALBUMS_PATH + ":join")
                                        .build())
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(Mono.just(JoinCommand.builder().shareToken(newShared.getShareToken()).build()), JoinCommand.class)
                                .exchange())
                .then();
    }

}