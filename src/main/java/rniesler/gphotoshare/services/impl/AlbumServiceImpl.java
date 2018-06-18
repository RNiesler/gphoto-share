package rniesler.gphotoshare.services.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import rniesler.gphotoshare.domain.Album;
import rniesler.gphotoshare.domain.googleapi.AlbumsList;
import rniesler.gphotoshare.domain.AlbumsRepository;
import rniesler.gphotoshare.domain.ShareInfo;
import rniesler.gphotoshare.domain.googleapi.AlbumCommand;
import rniesler.gphotoshare.domain.googleapi.CreateAlbumCommand;
import rniesler.gphotoshare.security.SecurityService;
import rniesler.gphotoshare.services.AlbumService;

import java.util.Optional;

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
    public AlbumsList listAlbums(Optional<String> pageToken) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("https://" + GPHOTOS_API_HOST + "/" + GPHOTOS_API_ALBUMS_PATH);
        if (pageToken.isPresent()) {
            uriBuilder.queryParam("pageToken", pageToken.get());
        }
        uriBuilder.queryParam("pageSize", PAGE_SIZE);
        return securityService.getOauth2AuthenticatedRestTemplate()
                .getForObject(uriBuilder.build().toUriString(), AlbumsList.class);
    }

    @Override
    public Optional<Album> getAlbum(String albumId) {
        Album album = albumsRepository.findById(albumId)
                .orElseGet(() -> {
                    String uri = "https://" + GPHOTOS_API_HOST + "/" + GPHOTOS_API_ALBUMS_PATH + "/" + albumId;
                    return securityService.getOauth2AuthenticatedRestTemplate()
                            .getForObject(uri, Album.class);
                });
        return Optional.ofNullable(album);
    }

    @Override
    public Optional<Album> shareAlbum(final String albumId, final ShareInfo shareInfo) {
        return getAlbum(albumId)
                .map(album -> {
                    album.setShareInfo(shareInfo);
                    return albumsRepository.save(album);
                });
    }


    @Override
    public ShareInfo createAndShareAlbum(String name) {
        AlbumCommand albumCommand = AlbumCommand.builder().title(name).build();
        CreateAlbumCommand createAlbumCommand = CreateAlbumCommand.builder().album(albumCommand).build();
        RestTemplate restTemplate = securityService.getOauth2AuthenticatedRestTemplate();
        String uri = "https://" + GPHOTOS_API_HOST + "/" + GPHOTOS_API_ALBUMS_PATH;
        Album album = restTemplate
                .postForObject(uri, createAlbumCommand, Album.class);
        ShareInfo shareInfo = restTemplate.postForObject(uri + "/" + album.getId() + ":share", null, ShareInfo.class);
        return shareInfo;
    }
}