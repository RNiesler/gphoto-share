package rniesler.gphotoshare.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import rniesler.gphotoshare.domain.Album;
import rniesler.gphotoshare.domain.Circle;
import rniesler.gphotoshare.domain.ShareInfo;
import rniesler.gphotoshare.domain.googleapi.AlbumsList;
import rniesler.gphotoshare.services.AlbumService;
import rniesler.gphotoshare.services.CircleService;
import rniesler.gphotoshare.services.ViewerService;

import java.util.List;
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
        AlbumsList albumsList = albumService.listAlbums(Optional.ofNullable(pageToken));
        model.addAttribute("albums", albumsList.getAlbums());
        model.addAttribute("nextPageToken", albumsList.getNextPageToken());
        return "albums";
    }

    @GetMapping("/{id}")
    public String getAlbum(@PathVariable("id") String id, Model model) {
        Optional<Album> albumOptional = albumService.getAlbum(id)
                .map(album -> {
                    if (album.getShareInfo() == null) {
                        album.setShareInfo(new ShareInfo());
                    }
                    return album;
                });
        Album album = albumOptional.orElseThrow(RuntimeException::new); //TODO proper exception
        List<Circle> allCircles = circleService.findAll();
        model.addAttribute("album", album);
        model.addAttribute("shareInfo", album.getShareInfo());
        model.addAttribute("circles", allCircles);
        return "shareAlbum";
    }

    @PostMapping("/{id}/share")
    public String shareAlbum(@PathVariable("id") String albumId, @ModelAttribute ShareInfo shareInfo) {
        albumService.shareAlbum(albumId, shareInfo);
        return "redirect:/albums/" + albumId;
    }


    @GetMapping("/{id}/join")
    public String joinAlbum(@PathVariable("id") String albumId) {
        viewerService.joinAlbum(albumId);
        return "redirect:/";
    }
}
