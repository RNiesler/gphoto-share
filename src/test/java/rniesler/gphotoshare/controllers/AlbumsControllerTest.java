package rniesler.gphotoshare.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.NestedServletException;
import org.springframework.web.util.UriComponentsBuilder;
import rniesler.gphotoshare.domain.Circle;
import rniesler.gphotoshare.domain.commands.ShareAlbumCommand;
import rniesler.gphotoshare.domain.googleapi.AlbumsList;
import rniesler.gphotoshare.domain.googleapi.GoogleAlbum;
import rniesler.gphotoshare.exceptions.AlbumNotFoundException;
import rniesler.gphotoshare.services.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AlbumsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AlbumService albumService;
    @Mock
    private CircleService circleService;
    @Mock
    private ViewerService viewerService;
    @Mock
    private SharedAlbumService sharedAlbumService;
    @Mock
    private NotificationService notificationService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(new AlbumsController(albumService, circleService, viewerService, sharedAlbumService, notificationService)).build();
    }

    @Test
    public void testListAlbums() throws Exception {
        String pageToken = "current";
        String nextPageToken = "next";
        AlbumsList albumList = new AlbumsList();
        List<GoogleAlbum> albums = List.of(new GoogleAlbum(), new GoogleAlbum());
        albumList.setAlbums(albums);
        albumList.setNextPageToken(nextPageToken);
        when(albumService.listAlbums(Optional.of(pageToken))).thenReturn(albumList);

        mockMvc.perform(get(UriComponentsBuilder.fromPath("/albums").queryParam("nextPageToken", pageToken).build().toUriString()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("albums", albums))
                .andExpect(model().attribute("pageToken", pageToken))
                .andExpect(model().attribute("nextPageToken", albumList.getNextPageToken()))
                .andExpect(view().name("albumlist"));
    }

    @Test
    public void testGetAlbumNotExisting() throws Exception {
        String testId = "wrong";
        when(albumService.getAlbum(testId)).thenReturn(Optional.empty());
        try {
            mockMvc.perform(get("/albums/" + testId))
                    .andExpect(status().isNotFound())
                    .andExpect(view().name("errors/404"));
        } catch (NestedServletException servletException) {
            assertTrue(servletException.getCause() instanceof AlbumNotFoundException);
        } finally {
            verify(sharedAlbumService, never()).getShareAlbumCommand(any());
            verify(circleService, never()).findAll();
        }
    }

    @Test
    public void testGetAlbum() throws Exception {
        String testId = "test";
        GoogleAlbum album = GoogleAlbum.builder().id(testId).build();
        ShareAlbumCommand command = ShareAlbumCommand.builder().albumId(testId).build();
        when(albumService.getAlbum(testId)).thenReturn(Optional.of(album));
        when(sharedAlbumService.getShareAlbumCommand(testId)).thenReturn(command);
        List<Circle> circles = List.of(Circle.builder().name("test").build());
        when(circleService.findAll()).thenReturn(circles);
        mockMvc.perform(get("/albums/" + testId))
                .andExpect(status().isOk())
                .andExpect(model().attribute("album", album))
                .andExpect(model().attribute("shareCommand", command))
                .andExpect(model().attribute("circles", circles))
                .andExpect(view().name("shareAlbum"));
    }

    @Test
    public void testShareAlbum() throws Exception {
        String testId = "test";
        ShareAlbumCommand command = ShareAlbumCommand.builder().albumId(testId).publicUrl("test").build();
        mockMvc.perform(post("/albums/" + testId).flashAttr("shareCommand", command))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/albums"));
        verify(sharedAlbumService).shareAlbum(command);
    }

    @Test
    public void testShareAlbumInvalid() throws Exception {
        String testId = "test";
        ShareAlbumCommand command = ShareAlbumCommand.builder().albumId(testId).build();
        GoogleAlbum album = GoogleAlbum.builder().id(testId).build();
        when(albumService.getAlbum(testId)).thenReturn(Optional.of(album));
        mockMvc
                .perform(post("/albums/" + testId).flashAttr("shareCommand", command))
                .andExpect(status().isOk())
                .andExpect(view().name("shareAlbum"))
                .andExpect(model().attribute("shareCommand", command))
                .andExpect(model().attribute("album", album));
    }

    @Test
    public void testJoinAlbum() throws Exception {
        String testId = "test";
        mockMvc.perform(get("/albums/" + testId + "/join"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
        verify(viewerService).joinAlbum(testId);
    }

    @Test
    public void testNotify() throws Exception {
        String testId = "test";
        mockMvc.perform(post("/albums/" + testId + "/notify"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/albums/" + testId));
        verify(notificationService).notify(testId);
    }
}
