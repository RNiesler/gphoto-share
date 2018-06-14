package rniesler.gphotoshare.controllers;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import rniesler.gphotoshare.services.AlbumService;

@Controller
public class AlbumsController {
    private final AlbumService albumService;

    public AlbumsController(AlbumService albumService) {
        this.albumService = albumService;
    }

    @GetMapping("/albums")
    public String listAlbums(Model model, OAuth2AuthenticationToken authentication) {
        model.addAttribute("albums", albumService.listAlbums(authentication).toIterable());
        return "albums";
    }
}
