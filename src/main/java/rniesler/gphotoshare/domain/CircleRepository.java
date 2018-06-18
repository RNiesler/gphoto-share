package rniesler.gphotoshare.domain;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CircleRepository extends MongoRepository<Circle, ObjectId> {
    List<Circle> findAllByOwner(String owner);

    List<Circle> findAllByMembersIsContaining(String memberEmail);
}
