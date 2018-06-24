package rniesler.gphotoshare.services.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import rniesler.gphotoshare.domain.SharedAlbum;
import rniesler.gphotoshare.domain.SharedAlbumRepository;
import rniesler.gphotoshare.domain.googleapi.JoinCommand;
import rniesler.gphotoshare.exceptions.AlbumNotFoundException;
import rniesler.gphotoshare.security.SecurityService;
import rniesler.gphotoshare.services.CircleService;
import rniesler.gphotoshare.services.SharedAlbumService;
import rniesler.gphotoshare.services.ViewerService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ViewerServiceImpl implements ViewerService {
    private final String GPHOTOS_API_SHARED_ALBUMS_PATH;
    private final SharedAlbumRepository sharedAlbumRepository;
    private final CircleService circleService;
    private final SecurityService securityService;
    private final SharedAlbumService sharedAlbumService;

    public ViewerServiceImpl(CircleService circleService, SecurityService securityService,
                             @Value("${google.photos.api.sharedAlbums}") String sharedAlbumsApiPath,
                             SharedAlbumRepository sharedAlbumRepository, SharedAlbumService sharedAlbumService) {
        this.circleService = circleService;
        this.securityService = securityService;
        this.GPHOTOS_API_SHARED_ALBUMS_PATH = sharedAlbumsApiPath;
        this.sharedAlbumRepository = sharedAlbumRepository;
        this.sharedAlbumService = sharedAlbumService;
    }

    @Override
    public List<SharedAlbum> retrieveAccessibleAlbums() {
        return circleService
                .findAllByMember(securityService.getAuthenticatedEmail())
                .stream()
                .flatMap(circle -> sharedAlbumRepository.findBySharedToContaining(circle.getId()).stream())
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public void joinAlbum(String albumId) {
        sharedAlbumService.getSharedAlbum(albumId)
                .filter(album -> album.getShareToken() != null)
                .ifPresentOrElse(album -> {
                    RestTemplate restTemplate = securityService.getOauth2AuthenticatedRestTemplate();
                    restTemplate
                            .postForLocation(GPHOTOS_API_SHARED_ALBUMS_PATH + ":join", JoinCommand.builder().shareToken(album.getShareToken()).build());
                }, () -> {
                    throw new AlbumNotFoundException();
                });
    }
}