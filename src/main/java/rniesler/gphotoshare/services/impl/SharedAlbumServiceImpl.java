package rniesler.gphotoshare.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.bson.types.Binary;
import org.springframework.stereotype.Service;
import rniesler.gphotoshare.domain.CircleRepository;
import rniesler.gphotoshare.domain.SharedAlbum;
import rniesler.gphotoshare.domain.SharedAlbumRepository;
import rniesler.gphotoshare.domain.commands.ShareAlbumCommand;
import rniesler.gphotoshare.domain.googleapi.GoogleAlbum;
import rniesler.gphotoshare.exceptions.AlbumNotFoundException;
import rniesler.gphotoshare.security.SecurityService;
import rniesler.gphotoshare.services.AlbumService;
import rniesler.gphotoshare.services.ImageService;
import rniesler.gphotoshare.services.SharedAlbumService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SharedAlbumServiceImpl implements SharedAlbumService {
    private final SharedAlbumRepository sharedAlbumRepository;
    private final SecurityService securityService;
    private final AlbumService albumService;
    private final CircleRepository circleRepository;
    private final ImageService imageService;

    public SharedAlbumServiceImpl(SharedAlbumRepository sharedAlbumRepository, SecurityService securityService,
                                  AlbumService albumService, CircleRepository circleRepository, ImageService imageService) {
        this.sharedAlbumRepository = sharedAlbumRepository;
        this.securityService = securityService;
        this.albumService = albumService;
        this.circleRepository = circleRepository;
        this.imageService = imageService;
    }

    @Override
    public List<SharedAlbum> getUsersSharedAlbums() {
        return sharedAlbumRepository.findAllByOwner(securityService.getAuthenticatedEmail());
    }

    @Override
    public Optional<SharedAlbum> getSharedAlbum(String id) {
        return sharedAlbumRepository.findById(id);
    }

    @Override
    public void shareAlbum(ShareAlbumCommand shareCommand) {
        if (shareCommand.getAlbumId() == null) {
            throw new IllegalArgumentException("Album ID cannot be null");
        }
        if (shareCommand.getSharedTo().isEmpty()) {
            sharedAlbumRepository.deleteById(shareCommand.getAlbumId());
        } else {
            SharedAlbum sharedAlbum = getSharedAlbum(shareCommand.getAlbumId()).orElseGet(() -> {
                SharedAlbum newSharedAlbum = new SharedAlbum();
                newSharedAlbum.setId(shareCommand.getAlbumId());
                newSharedAlbum.setOwner(securityService.getAuthenticatedEmail());
                return newSharedAlbum;
            });
            sharedAlbum.setPublicUrl(shareCommand.getPublicUrl());
            sharedAlbum.setSharedTo(shareCommand.getSharedTo());
            if (shareCommand.getShareToken() == null || "".equals(shareCommand.getShareToken())) {
                sharedAlbum.setShareToken(null);
            } else {
                sharedAlbum.setShareToken(shareCommand.getShareToken());
            }
            albumService.getAlbum(shareCommand.getAlbumId()).ifPresentOrElse((googleAlbum) -> {
                sharedAlbum.setName(googleAlbum.getName());
                byte[] coverPhoto = getContents(googleAlbum.getCoverPhotoUrl());
                sharedAlbum.setCoverPhoto(new Binary(coverPhoto));
                sharedAlbum.setCoverPhotoIcon(new Binary(imageService.resizeToIcon(coverPhoto)));
            }, () -> {
                throw new AlbumNotFoundException();
            });
            sharedAlbumRepository.save(sharedAlbum);
        }
    }

    protected byte[] getContents(String url) {
        return securityService.getOauth2AuthenticatedRestTemplate().getForObject(url, byte[].class);
    }

    @Override
    public ShareAlbumCommand getShareAlbumCommand(String id) {
        GoogleAlbum album = albumService.getAlbum(id).orElseThrow(AlbumNotFoundException::new);
        Optional<SharedAlbum> sharedAlbumOptional = getSharedAlbum(id);
        ShareAlbumCommand shareAlbumCommand = new ShareAlbumCommand();
        shareAlbumCommand.setAlbumId(id);
        if (sharedAlbumOptional.isPresent()) {
            SharedAlbum sharedAlbum = sharedAlbumOptional.get();
            shareAlbumCommand.setPublicUrl(sharedAlbum.getPublicUrl());
            shareAlbumCommand.setShareToken(sharedAlbum.getShareToken());
            shareAlbumCommand.setSharedTo(sharedAlbum.getSharedTo());
            shareAlbumCommand.setNotificationSent(sharedAlbum.isNotificationSent());
        } else if (album.getShareInfo() != null) {
            shareAlbumCommand.setPublicUrl(album.getShareInfo().getShareableUrl());
            shareAlbumCommand.setShareToken(album.getShareInfo().getShareToken());
            shareAlbumCommand.setNotificationSent(false);
        }
        return shareAlbumCommand;
    }

    @Override
    public List<String> getUsersForSharedAlbum(SharedAlbum sharedAlbum) {
        return sharedAlbum.getSharedTo().stream()
                .map(circleId -> circleRepository.findById(circleId))
                .flatMap(Optional::stream)
                .flatMap(circle -> circle.getMembers().stream())
                .distinct()
                .collect(Collectors.toList());
    }
}
