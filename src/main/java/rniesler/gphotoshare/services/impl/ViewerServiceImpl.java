package rniesler.gphotoshare.services.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import rniesler.gphotoshare.domain.Album;
import rniesler.gphotoshare.domain.AlbumsRepository;
import rniesler.gphotoshare.domain.ShareInfo;
import rniesler.gphotoshare.domain.googleapi.JoinCommand;
import rniesler.gphotoshare.security.SecurityService;
import rniesler.gphotoshare.services.AlbumService;
import rniesler.gphotoshare.services.CircleService;
import rniesler.gphotoshare.services.ViewerService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ViewerServiceImpl implements ViewerService {
    private final String GPHOTOS_API_SHARED_ALBUMS_PATH;
    private final AlbumsRepository albumsRepository;
    private final CircleService circleService;
    private final SecurityService securityService;
    private final AlbumService albumService;

    public ViewerServiceImpl(AlbumsRepository albumsRepository, CircleService circleService,
                             SecurityService securityService, AlbumService albumService,
                             @Value("${google.photos.api.sharedAlbums}") String sharedAlbumsApiPath) {
        this.albumsRepository = albumsRepository;
        this.circleService = circleService;
        this.securityService = securityService;
        this.albumService = albumService;
        this.GPHOTOS_API_SHARED_ALBUMS_PATH = sharedAlbumsApiPath;
    }

    @Override
    public List<Album> retrieveAccessibleAlbums() {
        return circleService
                .findAllByMember(securityService.getAuthenticatedEmail())
                .stream()
                .flatMap(circle -> albumsRepository.findAllSharedToCircle(circle.getId()).stream())
                .collect(Collectors.toList());
    }

    @Override
    public void joinAlbum(String albumId) {
        ShareInfo newShared = albumService.createAndShareAlbum("test");
        albumService.getAlbum(albumId)
                .ifPresent(album -> {
                    RestTemplate restTemplate = securityService.getOauth2AuthenticatedRestTemplate();
                    restTemplate
                            .postForLocation(GPHOTOS_API_SHARED_ALBUMS_PATH + ":join", JoinCommand.builder().shareToken(newShared.getShareToken()).build());
                });
    }
}