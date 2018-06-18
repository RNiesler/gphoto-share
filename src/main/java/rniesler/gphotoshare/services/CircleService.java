package rniesler.gphotoshare.services;

import rniesler.gphotoshare.domain.Circle;

import java.util.List;
import java.util.Optional;

public interface CircleService {
    List<Circle> findAll();

    List<Circle> findAllByMember(String memberEmail);

    Optional<Circle> get(String id);

    Circle persist(Circle circle);
}
