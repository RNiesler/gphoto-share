package rniesler.gphotoshare.domain.admin;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface AccessRequestRepository extends MongoRepository<AccessRequest, String> {
    Optional<AccessRequest> findByEmail(String email);

    List<AccessRequest> findAllByDenied(boolean denied);
}
