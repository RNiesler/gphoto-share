package rniesler.gphotoshare.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import rniesler.gphotoshare.services.SharedAlbumService;

@Controller
@RequestMapping("/shared")
public class SharedAlbumsController {
    private final SharedAlbumService sharedAlbumService;

    public SharedAlbumsController(SharedAlbumService sharedAlbumService) {
        this.sharedAlbumService = sharedAlbumService;
    }

    @GetMapping({"", "/"})
    public String getSharedAlbums(Model model) {
        model.addAttribute("albums", sharedAlbumService.getUsersSharedAlbums());
        return "mysharedalbums";
    }
}
