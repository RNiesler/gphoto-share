package rniesler.gphotoshare.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import rniesler.gphotoshare.domain.Circle;
import rniesler.gphotoshare.domain.CircleRepository;
import rniesler.gphotoshare.security.SecurityService;
import rniesler.gphotoshare.services.CircleService;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class CircleServiceImpl implements CircleService {
    private final CircleRepository circleRepository;
    private final SecurityService securityService;

    public CircleServiceImpl(CircleRepository circleRepository, SecurityService securityService) {
        this.circleRepository = circleRepository;
        this.securityService = securityService;
    }

    @Override
    public List<Circle> findAll() {
        return circleRepository.findAllByOwner(securityService.getAuthenticatedEmail());
    }

    @Override
    public List<Circle> findAllByMember(String memberEmail) {
        return circleRepository.findAllByMembersIsContaining(memberEmail);
    }

    @Override
    public Optional<Circle> get(String id) {
        return circleRepository.findById(new ObjectId(id));
    }

    @Override
    public Circle persist(Circle circle) {
        return circleRepository.save(circle);
    }

    @Override
    public void deleteCircle(String id) {
        circleRepository.deleteById(new ObjectId(id));
    }
}
