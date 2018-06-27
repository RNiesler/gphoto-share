package rniesler.gphotoshare.services;

import rniesler.gphotoshare.domain.commands.ShareAlbumCommand;
import rniesler.gphotoshare.domain.SharedAlbum;

import java.util.List;
import java.util.Optional;

public interface SharedAlbumService {
    List<SharedAlbum> getUsersSharedAlbums();

    Optional<SharedAlbum> getSharedAlbum(String id);

    void shareAlbum(ShareAlbumCommand shareCommand);

    ShareAlbumCommand getShareAlbumCommand(String id);

    List<String> getUsersForSharedAlbum(SharedAlbum sharedAlbum);
}
