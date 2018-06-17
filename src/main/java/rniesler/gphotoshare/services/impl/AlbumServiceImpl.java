package rniesler.gphotoshare.services.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import rniesler.gphotoshare.domain.Album;
import rniesler.gphotoshare.domain.AlbumsList;
import rniesler.gphotoshare.domain.AlbumsRepository;
import rniesler.gphotoshare.domain.ShareInfo;
import rniesler.gphotoshare.domain.googleapi.AlbumCommand;
import rniesler.gphotoshare.domain.googleapi.CreateAlbumCommand;
import rniesler.gphotoshare.security.SecurityService;
import rniesler.gphotoshare.services.AlbumService;

import java.util.Optional;

//TODO request only certain field when calling the API (set fields parameter) https://developers.google.com/photos/library/guides/performance-tips
@Service
public class AlbumServiceImpl implements AlbumService {
    private final String GPHOTOS_API_HOST;
    private final String GPHOTOS_API_SHARED_ALBUMS_PATH;
    private final String GPHOTOS_API_ALBUMS_PATH;

    private final int PAGE_SIZE = 2;

    private final SecurityService securityService;
    private final AlbumsRepository albumsRepository;

    public AlbumServiceImpl(SecurityService securityService,
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

    @Override
    public Mono<AlbumsList> listAlbums(OAuth2AuthenticationToken authenticationToken, Optional<String> pageToken) {
        return securityService.getOauth2AuthenticatedWebClient(authenticationToken)
                .flatMap(webClient -> webClient.get()
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
                        .bodyToMono(AlbumsList.class)
                );

    }

    @Override
    public Mono<Album> getAlbum(OAuth2AuthenticationToken authenticationToken, String albumId) {
        return albumsRepository.findById(albumId)
                .switchIfEmpty(securityService.getOauth2AuthenticatedWebClient(authenticationToken)
                        .flatMap(webClient -> webClient
                                .get()
                                .uri(uriBuilder -> uriBuilder
                                        .scheme("https")
                                        .host(GPHOTOS_API_HOST)
                                        .path(GPHOTOS_API_ALBUMS_PATH + "/" + albumId)
                                        .build())
                                .retrieve()
                                .bodyToMono(Album.class)));
    }

    @Override
    public Mono<Album> shareAlbum(OAuth2AuthenticationToken authenticationToken, final String albumId, final ShareInfo shareInfo) {
        return albumsRepository.findById(albumId)
                .switchIfEmpty(getAlbum(authenticationToken, albumId))
                .flatMap(album -> {
                    album.setShareInfo(shareInfo);
                    return albumsRepository.save(album);
                });
    }


    @Override
    public Mono<ShareInfo> createAndShareAlbum(String name) {
        AlbumCommand albumCommand = AlbumCommand.builder().title(name).build();
        CreateAlbumCommand createAlbumCommand = CreateAlbumCommand.builder().album(albumCommand).build();
        //TODO implement
        // create
//        Mono<Album> createdMono = webClientMono.flatMap(webClient ->
//                webClient.post()
//                        .uri(uriBuilder -> uriBuilder.scheme("https")
//                                .host(GPHOTOS_API_HOST)
//                                .path(GPHOTOS_API_ALBUMS_PATH)
//                                .build())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .body(Mono.just(createAlbumCommand), CreateAlbumCommand.class)
//                        .retrieve()
//                        .bodyToMono(Album.class));


//        // share
//        Mono<ShareInfo> sharedMono = createdMono.flatMap(album -> webClient
//                .post()
//                .uri(uriBuilder -> uriBuilder.scheme("https")
//                        .host(GPHOTOS_API_HOST)
//                        .path(GPHOTOS_API_ALBUMS_PATH + "/" + album.getId() + ":share")
//                        .build())
//                .retrieve()
//                .bodyToMono(ShareInfo.class));

//        return sharedMono;
        return Mono.empty();
    }
}
