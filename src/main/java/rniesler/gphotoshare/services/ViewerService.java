package rniesler.gphotoshare.services;

import rniesler.gphotoshare.domain.Album;

import java.util.List;

public interface ViewerService {
    List<Album> retrieveAccessibleAlbums();

    void joinAlbum(String albumId);
}
