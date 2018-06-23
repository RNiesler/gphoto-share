package rniesler.gphotoshare.services.impl;

import org.bson.types.Binary;
import org.springframework.stereotype.Service;
import rniesler.gphotoshare.domain.ShareAlbumCommand;
import rniesler.gphotoshare.domain.SharedAlbum;
import rniesler.gphotoshare.domain.SharedAlbumRepository;
import rniesler.gphotoshare.domain.googleapi.GoogleAlbum;
import rniesler.gphotoshare.security.SecurityService;
import rniesler.gphotoshare.services.AlbumService;
import rniesler.gphotoshare.services.SharedAlbumService;

import java.util.List;
import java.util.Optional;

@Service
public class SharedAlbumServiceImpl implements SharedAlbumService {
    private final SharedAlbumRepository sharedAlbumRepository;
    private final SecurityService securityService;
    private final AlbumService albumService;

    public SharedAlbumServiceImpl(SharedAlbumRepository sharedAlbumRepository, SecurityService securityService, AlbumService albumService) {
        this.sharedAlbumRepository = sharedAlbumRepository;
        this.securityService = securityService;
        this.albumService = albumService;
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
        //TODO validation: id not null
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
                sharedAlbum.setCoverPhoto(new Binary(getContents(googleAlbum.getCoverPhotoUrl())));
            }, () -> {
                throw new RuntimeException();
            });
            sharedAlbumRepository.save(sharedAlbum);
        }
    }

    protected byte[] getContents(String url) {
        return securityService.getOauth2AuthenticatedRestTemplate().getForObject(url, byte[].class);
    }

    @Override
    public ShareAlbumCommand getShareAlbumCommand(String id) {
        GoogleAlbum album = albumService.getAlbum(id).orElseThrow(RuntimeException::new); //TODO exception
        Optional<SharedAlbum> sharedAlbumOptional = getSharedAlbum(id);
        ShareAlbumCommand shareAlbumCommand = new ShareAlbumCommand();
        shareAlbumCommand.setAlbumId(id);
        if (sharedAlbumOptional.isPresent()) {
            SharedAlbum sharedAlbum = sharedAlbumOptional.get();
            shareAlbumCommand.setPublicUrl(sharedAlbum.getPublicUrl());
            shareAlbumCommand.setShareToken(sharedAlbum.getShareToken());
            shareAlbumCommand.setSharedTo(sharedAlbum.getSharedTo());
        } else if (album.getShareInfo() != null) {
            shareAlbumCommand.setPublicUrl(album.getShareInfo().getShareableUrl());
            shareAlbumCommand.setShareToken(album.getShareInfo().getShareToken());
        }
        return shareAlbumCommand;
    }
}
