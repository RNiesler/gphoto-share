package rniesler.gphotoshare.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import rniesler.gphotoshare.domain.commands.ShareAlbumCommand;
import rniesler.gphotoshare.domain.googleapi.AlbumsList;
import rniesler.gphotoshare.exceptions.AlbumNotFoundException;
import rniesler.gphotoshare.services.*;

import javax.validation.Valid;
import java.util.Optional;

@Controller
@RequestMapping("/albums")
public class AlbumsController {
    private final AlbumService albumService;
    private final CircleService circleService;
    private final ViewerService viewerService;
    private final SharedAlbumService sharedAlbumService;
    private final NotificationService notificationService;

    public AlbumsController(AlbumService albumService, CircleService circleService, ViewerService viewerService, SharedAlbumService sharedAlbumService, NotificationService notificationService) {
        this.albumService = albumService;
        this.circleService = circleService;
        this.viewerService = viewerService;
        this.sharedAlbumService = sharedAlbumService;
        this.notificationService = notificationService;
    }

    @GetMapping({"", "/"})
    public String listAlbums(Model model, @RequestParam(value = "nextPageToken", required = false) Optional<String> pageToken) {
        AlbumsList albumsList = albumService.listAlbums(pageToken);
        model.addAttribute("albums", albumsList.getAlbums());
        pageToken.ifPresent(token -> model.addAttribute("pageToken", token));
        model.addAttribute("nextPageToken", albumsList.getNextPageToken());
        return "albumlist";
    }

    @GetMapping("/{id}")
    public String getAlbum(@PathVariable("id") String id, Model model) {
        String view = setAlbumModel(id, model);
        model.addAttribute("shareCommand", sharedAlbumService.getShareAlbumCommand(id));
        return view;
    }

    private String setAlbumModel(String id, Model model) {
        model.addAttribute("album", albumService.getAlbum(id).orElseThrow(AlbumNotFoundException::new));
        model.addAttribute("circles", circleService.findAll());
        return "shareAlbum";

    }

    @PostMapping("/{id}")
    public String shareAlbum(@PathVariable("id") String albumId, @Valid @ModelAttribute("shareCommand") ShareAlbumCommand shareAlbumCommand, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return setAlbumModel(albumId, model);
        } else {
            sharedAlbumService.shareAlbum(shareAlbumCommand);
            return "redirect:/albums";
        }
    }


    @GetMapping("/{id}/join")
    public String joinAlbum(@PathVariable("id") String albumId) {
        viewerService.joinAlbum(albumId);
        return "redirect:/";
    }

    @PostMapping("/{id}/notify")
    public String notify(@PathVariable("id") String id) {
        notificationService.notify(id);
        return "redirect:/albums/" + id;
    }
}
