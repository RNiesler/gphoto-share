package rniesler.gphotoshare.domain;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface AlbumsRepository extends MongoRepository<Album, String> {

    @Query("{'shareInfo.sharedTo': ?0}")
    List<Album> findAllSharedToCircle(ObjectId circleId);
}
