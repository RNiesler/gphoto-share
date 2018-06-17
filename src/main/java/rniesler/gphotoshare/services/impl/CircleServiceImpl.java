package rniesler.gphotoshare.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rniesler.gphotoshare.domain.Circle;
import rniesler.gphotoshare.domain.CircleRepository;
import rniesler.gphotoshare.security.SecurityService;
import rniesler.gphotoshare.services.CircleService;

@Service
@Slf4j
public class CircleServiceImpl implements CircleService {
    private final CircleRepository circleRepository;
    private final SecurityService securityService;
    private final String apiHost;
    private final String apiPath;

    public CircleServiceImpl(CircleRepository circleRepository, SecurityService securityService,
                             @Value("${google.plus.api.host}") String apiHost, @Value("${google.plus.api.circles}") String apiPath) {
        this.circleRepository = circleRepository;
        this.securityService = securityService;
        this.apiHost = apiHost;
        this.apiPath = apiPath;
    }

    @Override
    public Flux<Circle> findAll() {
        return circleRepository.findAllByOwner(securityService.getAuthenticatedEmail());
    }

    @Override
    public Flux<Circle> findAllByMember(String memberEmail) {
        return circleRepository.findAllByMembersIsContaining(memberEmail);
    }

    @Override
    public Mono<Circle> get(String id) {
        return circleRepository.findById(new ObjectId(id));
    }

    @Override
    public Mono<Circle> persist(Circle circle) {
        return circleRepository.save(circle);
    }
}
