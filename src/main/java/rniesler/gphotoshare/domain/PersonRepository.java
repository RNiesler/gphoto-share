package rniesler.gphotoshare.domain;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PersonRepository extends MongoRepository<Person, ObjectId> {
    Optional<Person> findByEmail(String email);
}
