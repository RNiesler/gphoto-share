package rniesler.gphotoshare.services.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import rniesler.gphotoshare.domain.googleapi.*;
import rniesler.gphotoshare.security.SecurityService;
import rniesler.gphotoshare.services.AlbumService;

import java.util.Optional;

@Service
public class AlbumServiceImpl implements AlbumService {
    private final String GPHOTOS_API_ALBUMS_PATH;

    private final int PAGE_SIZE = 10;

    private final SecurityService securityService;

    public AlbumServiceImpl(SecurityService securityService,
                            @Value("${google.photos.api.albums}") String albumsApiPath) {
        this.securityService = securityService;
        this.GPHOTOS_API_ALBUMS_PATH = albumsApiPath;
    }

    @Cacheable(cacheNames = "albums", keyGenerator = "userAwareKeyGenerator")
    @Override
    public AlbumsList listAlbums(Optional<String> pageToken) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(GPHOTOS_API_ALBUMS_PATH);
        if (pageToken.isPresent()) {
            uriBuilder.queryParam("pageToken", pageToken.get());
        }
        uriBuilder.queryParam("pageSize", PAGE_SIZE);
        return securityService.getOauth2AuthenticatedRestTemplate()
                .getForObject(uriBuilder.build().toUriString(), AlbumsList.class);
    }

    @Cacheable("album")
    @Override
    public Optional<GoogleAlbum> getAlbum(String albumId) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(GPHOTOS_API_ALBUMS_PATH + "/{albumId}");
        GoogleAlbum album = securityService.getOauth2AuthenticatedRestTemplate()
                .getForObject(uriComponentsBuilder.buildAndExpand(albumId).toString(), GoogleAlbum.class);
        return Optional.ofNullable(album);
    }


    @Override
    public ShareInfo createAndShareAlbum(String name) {
        AlbumCommand albumCommand = AlbumCommand.builder().title(name).build();
        CreateAlbumCommand createAlbumCommand = CreateAlbumCommand.builder().album(albumCommand).build();
        RestTemplate restTemplate = securityService.getOauth2AuthenticatedRestTemplate();
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(GPHOTOS_API_ALBUMS_PATH + "/{albumId}:share");
        GoogleAlbum album = restTemplate
                .postForObject(GPHOTOS_API_ALBUMS_PATH, createAlbumCommand, GoogleAlbum.class);
        ShareResult shareResult = restTemplate.postForObject(uriComponentsBuilder.buildAndExpand(album.getId()).toString(), null, ShareResult.class);
        return shareResult.getShareInfo();
    }
}
