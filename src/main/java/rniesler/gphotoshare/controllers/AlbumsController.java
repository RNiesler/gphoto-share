package rniesler.gphotoshare.controllers;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rniesler.gphotoshare.domain.Album;
import rniesler.gphotoshare.domain.AlbumsList;
import rniesler.gphotoshare.domain.Circle;
import rniesler.gphotoshare.domain.ShareInfo;
import rniesler.gphotoshare.services.AlbumService;
import rniesler.gphotoshare.services.CircleService;
import rniesler.gphotoshare.services.ViewerService;

import java.util.Optional;

@Controller
@RequestMapping("/albums")
public class AlbumsController {
    private final AlbumService albumService;
    private final CircleService circleService;
    private final ViewerService viewerService;

    public AlbumsController(AlbumService albumService, CircleService circleService, ViewerService viewerService) {
        this.albumService = albumService;
        this.circleService = circleService;
        this.viewerService = viewerService;
    }

    @GetMapping({"", "/"})
    public String listAlbums(Model model, @RequestParam(value = "nextPageToken", required = false) String pageToken, OAuth2AuthenticationToken authentication) {
        Mono<AlbumsList> albumsListMono = albumService.listAlbums(authentication, Optional.ofNullable(pageToken));
        Flux<Album> albumFlux = albumsListMono.flatMapIterable(AlbumsList::getAlbums);
        Mono<String> nextPageToken = albumsListMono.map(AlbumsList::getNextPageToken);
        model.addAttribute("albums", albumFlux);
        model.addAttribute("nextPageToken", nextPageToken);
        return "albums";
    }

    @GetMapping("/{id}")
    public String getAlbum(@PathVariable("id") String id, Model model, OAuth2AuthenticationToken authentication) {
        Mono<Album> albumMono = albumService.getAlbum(authentication, id).map(album -> {
            if (album.getShareInfo() == null) {
                album.setShareInfo(new ShareInfo());
            }
            return album;
        });
        Flux<Circle> allCircles = circleService.findAll(authentication);
        Mono<ShareInfo> shareInfoMono = albumMono.map(album -> album.getShareInfo());
        model.addAttribute("album", albumMono);
        model.addAttribute("shareInfo", shareInfoMono);
        model.addAttribute("circles", allCircles);
        return "shareAlbum";
    }

    @PostMapping("/{id}/share")
    public String shareAlbum(@PathVariable("id") String albumId, @ModelAttribute ShareInfo shareInfo, OAuth2AuthenticationToken authentication) {
        albumService.shareAlbum(authentication, albumId, shareInfo).block();
        return "redirect:/albums/" + albumId;
    }


    @GetMapping("/{id}/join")
    public String joinAlbum(@PathVariable("id") String albumId, OAuth2AuthenticationToken authentication) {
        viewerService.joinAlbum(authentication, albumId).block();
        return "redirect:/";
    }
}
