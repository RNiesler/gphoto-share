package rniesler.gphotoshare.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import rniesler.gphotoshare.domain.Album;
import rniesler.gphotoshare.domain.AlbumsList;
import rniesler.gphotoshare.security.SecurityService;

@Service
public class AlbumService {
    private final String GPHOTOS_API_HOST;
    private final String GPHOTOS_API_PATH;

    private final int PAGE_SIZE = 2;

    private SecurityService securityService;

    public AlbumService(SecurityService securityService, @Value("${google.photos.api.host}") String apiHost,
                        @Value("${google.photos.api.albums}") String apiPath) {
        this.securityService = securityService;
        this.GPHOTOS_API_HOST = apiHost;
        this.GPHOTOS_API_PATH = apiPath;
    }

    public Flux<Album> listAlbums(OAuth2AuthenticationToken authentication) {
        WebClient webClient = securityService.getOauth2AuthenticatedWebClient(authentication);
        return webClient
                .get()
                .uri(uriBuilder ->
                        uriBuilder.scheme("https")
                                .host(GPHOTOS_API_HOST)
                                .path(GPHOTOS_API_PATH)
                                .queryParam("pageSize", PAGE_SIZE)
                                .build())
                .retrieve()
                .bodyToMono(AlbumsList.class)
                .flatMapIterable(albumsList -> albumsList.getAlbums());
    }
}
