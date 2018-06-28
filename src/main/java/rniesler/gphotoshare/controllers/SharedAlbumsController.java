package rniesler.gphotoshare.controllers;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import rniesler.gphotoshare.exceptions.AlbumNotFoundException;
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

    @GetMapping(path = "/{id}/icon.jpg", produces = MediaType.IMAGE_JPEG_VALUE)
    @ResponseBody
    public byte[] coverPhotoIcon(@PathVariable("id") String albumId) {
        return sharedAlbumService.getSharedAlbum(albumId).map(sharedAlbum -> sharedAlbum.getCoverPhotoIcon().getData())
                .orElseThrow(AlbumNotFoundException::new);
    }
}
