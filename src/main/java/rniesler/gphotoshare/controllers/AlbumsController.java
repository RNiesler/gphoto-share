package rniesler.gphotoshare.controllers;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rniesler.gphotoshare.domain.Album;
import rniesler.gphotoshare.domain.AlbumsList;
import rniesler.gphotoshare.services.AlbumService;

import java.util.Optional;

@Controller
public class AlbumsController {
    private final AlbumService albumService;

    public AlbumsController(AlbumService albumService) {
        this.albumService = albumService;
    }

    @GetMapping("/albums")
    public String listAlbums(Model model, OAuth2AuthenticationToken authentication,
                             @RequestParam(value = "nextPageToken", required = false) String pageToken) {
        Mono<AlbumsList> albumsListMono = albumService.listAlbums(authentication, Optional.ofNullable(pageToken));
        Flux<Album> albumFlux = albumsListMono.flatMapIterable(AlbumsList::getAlbums);
        Mono<String> nextPageToken = albumsListMono.map(AlbumsList::getNextPageToken);
        model.addAttribute("albums", albumFlux.toIterable());
        model.addAttribute("nextPageToken", nextPageToken.block());
        return "albums";
    }
}
