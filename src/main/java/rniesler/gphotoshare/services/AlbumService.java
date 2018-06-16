package rniesler.gphotoshare.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import rniesler.gphotoshare.domain.Album;
import rniesler.gphotoshare.domain.AlbumsList;
import rniesler.gphotoshare.domain.AlbumsRepository;
import rniesler.gphotoshare.domain.ShareInfo;
import rniesler.gphotoshare.domain.googleapi.AlbumCommand;
import rniesler.gphotoshare.domain.googleapi.CreateAlbumCommand;
import rniesler.gphotoshare.security.SecurityService;

import java.util.Optional;

@Service
public class AlbumService {
    private final String GPHOTOS_API_HOST;
    private final String GPHOTOS_API_SHARED_ALBUMS_PATH;
    private final String GPHOTOS_API_ALBUMS_PATH;

    private final int PAGE_SIZE = 2;

    private final SecurityService securityService;
    private final AlbumsRepository albumsRepository;

    public AlbumService(SecurityService securityService,
                        @Value("${google.photos.api.host}") String apiHost,
                        @Value("${google.photos.api.albums}") String albumsApiPath,
                        @Value("${google.photos.api.sharedAlbums}") String sharedAlbumsApiPath,
                        AlbumsRepository albumsRepository) {
        this.securityService = securityService;
        this.GPHOTOS_API_HOST = apiHost;
        this.GPHOTOS_API_SHARED_ALBUMS_PATH = sharedAlbumsApiPath;
        this.GPHOTOS_API_ALBUMS_PATH = albumsApiPath;
        this.albumsRepository = albumsRepository;
    }

    public Mono<AlbumsList> listAlbums(Optional<String> pageToken) {
        WebClient webClient = securityService.getOauth2AuthenticatedWebClient();
        return webClient
                .get()
                .uri(uriBuilder -> {
                    uriBuilder.scheme("https")
                            .host(GPHOTOS_API_HOST)
                            .path(GPHOTOS_API_SHARED_ALBUMS_PATH);
                    if (pageToken.isPresent()) {
                        uriBuilder.queryParam("pageToken", pageToken.get());
                    }
                    return uriBuilder
                            .queryParam("pageSize", PAGE_SIZE)
                            .build();
                })
                .retrieve()
                .bodyToMono(AlbumsList.class);
    }

    public Mono<Album> getAlbum(String albumId) {
        return albumsRepository.findById(albumId)
                .switchIfEmpty(securityService.getOauth2AuthenticatedWebClient()
                        .get()
                        .uri(uriBuilder -> uriBuilder
                                .scheme("https")
                                .host(GPHOTOS_API_HOST)
                                .path(GPHOTOS_API_ALBUMS_PATH + "/" + albumId)
                                .build())
                        .retrieve()
                        .bodyToMono(Album.class));
    }

    public Mono<Album> shareAlbum(final String albumId, final ShareInfo shareInfo) {
        return albumsRepository.findById(albumId)
                .switchIfEmpty(getAlbum(albumId))
                .flatMap(album -> {
                    album.setShareInfo(shareInfo);
                    return albumsRepository.save(album);
                });
    }


    public Mono<ShareInfo> createAndShareAlbum(String name) {
        AlbumCommand albumCommand = AlbumCommand.builder().title(name).build();
        CreateAlbumCommand createAlbumCommand = CreateAlbumCommand.builder().album(albumCommand).build();
        WebClient webClient = securityService.getOauth2AuthenticatedWebClient();
        // create
        Mono<Album> createdMono = webClient
                .post()
                .uri(uriBuilder -> uriBuilder.scheme("https")
                        .host(GPHOTOS_API_HOST)
                        .path(GPHOTOS_API_ALBUMS_PATH)
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(createAlbumCommand), CreateAlbumCommand.class)
                .retrieve()
                .bodyToMono(Album.class);
        // share
        Mono<ShareInfo> sharedMono = createdMono.flatMap(album -> webClient
                .post()
                .uri(uriBuilder -> uriBuilder.scheme("https")
                        .host(GPHOTOS_API_HOST)
                        .path(GPHOTOS_API_ALBUMS_PATH + "/" + album.getId() + ":share")
                        .build())
                .retrieve()
                .bodyToMono(ShareInfo.class));
        ;
        return sharedMono;
    }
}
