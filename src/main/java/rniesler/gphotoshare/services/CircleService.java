package rniesler.gphotoshare.services;

import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rniesler.gphotoshare.domain.Circle;
import rniesler.gphotoshare.domain.CircleRepository;
import rniesler.gphotoshare.domain.googleapi.GoogleCirclesResponse;
import rniesler.gphotoshare.security.SecurityService;

@Service
@Slf4j
public class CircleService {
    private final CircleRepository circleRepository;
    private final SecurityService securityService;
    private final String apiHost;
    private final String apiPath;

    public CircleService(CircleRepository circleRepository, SecurityService securityService,
                         @Value("${google.plus.api.host}") String apiHost, @Value("${google.plus.api.circles}") String apiPath) {
        this.circleRepository = circleRepository;
        this.securityService = securityService;
        this.apiHost = apiHost;
        this.apiPath = apiPath;
    }

    public Flux<Circle> findAll() {
        return circleRepository.findAllByOwner(securityService.getAuthenticatedEmail());
    }

    public Flux<Circle> findAllByMember(String memberEmail) {
        return circleRepository.findAllByMembersIsContaining(memberEmail);
    }

    public Mono<Circle> get(String id) {
        return circleRepository.findById(new ObjectId(id));
    }

    public Mono<Circle> persist(Circle circle) {
        return circleRepository.save(circle);
    }

    public Mono<Void> importFromGooglePlus() {
        WebClient webClient = securityService.getOauth2AuthenticatedWebClient();

        Mono<GoogleCirclesResponse> responseMono = webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host(apiHost)
                        .path(apiPath)
                        .build())
                .retrieve()
                .bodyToMono(GoogleCirclesResponse.class);
        //TODO fix the 403 with Google
        return responseMono
                .flatMapIterable(GoogleCirclesResponse::getCircleList).then();
    }
}
