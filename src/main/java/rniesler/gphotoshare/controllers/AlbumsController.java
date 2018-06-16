package rniesler.gphotoshare.controllers;

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
    public String listAlbums(Model model, @RequestParam(value = "nextPageToken", required = false) String pageToken) {
        Mono<AlbumsList> albumsListMono = albumService.listAlbums(Optional.ofNullable(pageToken));
        Flux<Album> albumFlux = albumsListMono.flatMapIterable(AlbumsList::getAlbums);
        Mono<String> nextPageToken = albumsListMono.map(AlbumsList::getNextPageToken);
        model.addAttribute("albums", albumFlux.toIterable());
        model.addAttribute("nextPageToken", nextPageToken.block());
        return "albums";
    }

    @GetMapping("/{id}")
    public String getAlbum(@PathVariable("id") String id, Model model) {
        Mono<Album> albumMono = albumService.getAlbum(id).map(album -> {
            if (album.getShareInfo() == null) {
                album.setShareInfo(new ShareInfo());
            }
            return album;
        });
        Flux<Circle> allCircles = circleService.findAll();
        Mono<ShareInfo> shareInfoMono = albumMono.map(album -> album.getShareInfo());
        model.addAttribute("album", albumMono.block());
        model.addAttribute("shareInfo", shareInfoMono.block());
        model.addAttribute("circles", allCircles.toIterable());
        return "shareAlbum";
    }

    @PostMapping("/{id}/share")
    public String shareAlbum(@PathVariable("id") String albumId, @ModelAttribute ShareInfo shareInfo) {
        albumService.shareAlbum(albumId, shareInfo).block();
        return "redirect:/albums/" + albumId;
    }


    @GetMapping("/{id}/join")
    public String joinAlbum(@PathVariable("id") String albumId) {
        viewerService.joinAlbum(albumId).block();
        return "redirect:/";
    }
}
