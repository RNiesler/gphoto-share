package rniesler.gphotoshare.domain.admin;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SecurityMappingRepository extends MongoRepository<SecurityMapping, String> {
    Optional<SecurityMapping> findByAuthoritiesContaining(String authority);

    List<SecurityMapping> findAllByAuthoritiesContaining(String authority);

    Optional<SecurityMapping> findByEmailAndAuthoritiesContaining(String email, String authority);
}
