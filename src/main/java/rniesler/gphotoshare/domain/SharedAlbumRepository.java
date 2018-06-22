package rniesler.gphotoshare.domain;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SharedAlbumRepository extends MongoRepository<SharedAlbum, String> {
    List<SharedAlbum> findBySharedToContaining(ObjectId circleId);

    List<SharedAlbum> findAllByOwner(String ownersEmail);
}
