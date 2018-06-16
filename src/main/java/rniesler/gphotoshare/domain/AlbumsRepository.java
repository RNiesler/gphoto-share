package rniesler.gphotoshare.domain;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface AlbumsRepository extends ReactiveMongoRepository<Album, String> {

    @Query("{'shareInfo.sharedTo': ?0}")
    Flux<Album> findAllSharedToCircle(ObjectId circleId);
}
