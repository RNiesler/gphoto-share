package rniesler.gphotoshare.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import rniesler.gphotoshare.domain.commands.ShareAlbumCommand;
import rniesler.gphotoshare.domain.googleapi.AlbumsList;
import rniesler.gphotoshare.exceptions.AlbumNotFoundException;
import rniesler.gphotoshare.services.AlbumService;
import rniesler.gphotoshare.services.CircleService;
import rniesler.gphotoshare.services.SharedAlbumService;
import rniesler.gphotoshare.services.ViewerService;

import java.util.Optional;

@Controller
@RequestMapping("/albums")
public class AlbumsController {
    private final AlbumService albumService;
    private final CircleService circleService;
    private final ViewerService viewerService;
    private final SharedAlbumService sharedAlbumService;

    public AlbumsController(AlbumService albumService, CircleService circleService, ViewerService viewerService, SharedAlbumService sharedAlbumService) {
        this.albumService = albumService;
        this.circleService = circleService;
        this.viewerService = viewerService;
        this.sharedAlbumService = sharedAlbumService;
    }

    @GetMapping({"", "/"})
    public String listAlbums(Model model, @RequestParam(value = "nextPageToken", required = false) Optional<String> pageToken) {
        AlbumsList albumsList = albumService.listAlbums(pageToken);
        model.addAttribute("albums", albumsList.getAlbums());
        pageToken.ifPresent(token -> model.addAttribute("pageToken", pageToken));
        model.addAttribute("nextPageToken", albumsList.getNextPageToken());
        return "albums";
    }

    @GetMapping("/{id}")
    public String getAlbum(@PathVariable("id") String id, Model model) {
        model.addAttribute("album", albumService.getAlbum(id).orElseThrow(AlbumNotFoundException::new));
        model.addAttribute("shareCommand", sharedAlbumService.getShareAlbumCommand(id));
        model.addAttribute("circles", circleService.findAll());
        return "shareAlbum";
    }

    @PostMapping("/{id}/share")
    public String shareAlbum(@PathVariable("id") String albumId, @ModelAttribute ShareAlbumCommand shareAlbumCommand) {
        sharedAlbumService.shareAlbum(shareAlbumCommand);
        return "redirect:/albums";
    }


    @GetMapping("/{id}/join")
    public String joinAlbum(@PathVariable("id") String albumId) {
        viewerService.joinAlbum(albumId);
        return "redirect:/";
    }
}
