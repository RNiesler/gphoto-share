package rniesler.gphotoshare.domain;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface CircleRepository extends ReactiveMongoRepository<Circle, ObjectId> {
    Flux<Circle> findAllByOwner(String owner);

    Flux<Circle> findAllByMembersIsContaining(String memberEmail);
}
