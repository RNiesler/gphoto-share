package rniesler.gphotoshare.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import rniesler.gphotoshare.domain.SharedAlbum;
import rniesler.gphotoshare.services.SharedAlbumService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

public class SharedAlbumsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SharedAlbumService sharedAlbumService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(new SharedAlbumsController(sharedAlbumService)).build();
    }

    @Test
    public void testGetSharedAlbums() throws Exception {
        List<SharedAlbum> albums = List.of(SharedAlbum.builder().name("test").build());
        when(sharedAlbumService.getUsersSharedAlbums()).thenReturn(albums);
        mockMvc.perform(get("/shared"))
                .andExpect(status().isOk())
                .andExpect(view().name("mysharedalbums"))
                .andExpect(model().attribute("albums", albums));
    }
}
