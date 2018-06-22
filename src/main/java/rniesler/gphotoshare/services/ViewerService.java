package rniesler.gphotoshare.services;

import rniesler.gphotoshare.domain.SharedAlbum;

import java.util.List;

public interface ViewerService {
    List<SharedAlbum> retrieveAccessibleAlbums();

    void joinAlbum(String albumId);
}
