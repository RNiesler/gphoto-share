package rniesler.gphotoshare.services.impl;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;
import rniesler.gphotoshare.domain.Album;
import rniesler.gphotoshare.domain.AlbumsRepository;
import rniesler.gphotoshare.domain.Circle;
import rniesler.gphotoshare.domain.ShareInfo;
import rniesler.gphotoshare.domain.googleapi.JoinCommand;
import rniesler.gphotoshare.security.SecurityService;
import rniesler.gphotoshare.services.AlbumService;
import rniesler.gphotoshare.services.CircleService;
import rniesler.gphotoshare.services.ViewerService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ViewerServiceImplTest {
    private final static String apiUrl = "TEST_URL";
    private ViewerService service;

    @Mock
    private AlbumsRepository albumsRepository;
    @Mock
    private CircleService circleService;
    @Mock
    private SecurityService securityService;
    @Mock
    private AlbumService albumService;


    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        service = new ViewerServiceImpl(albumsRepository, circleService, securityService, albumService, apiUrl);
    }

    @Test
    public void testRetrieveAccessibleAlbums() {
        ObjectId id1 = ObjectId.get();
        ObjectId id2 = ObjectId.get();
        Album album1 = Album.builder().name("test1").build();
        Album album2 = Album.builder().name("test2").build();
        Album album3 = Album.builder().name("test3").build();
        List<Circle> circles = List.of(
                Circle.builder().id(id1).build(),
                Circle.builder().id(id2).build()
        );
        when(circleService.findAllByMember(any())).thenReturn(circles);
        when(albumsRepository.findAllSharedToCircle(id1)).thenReturn(List.of(album1, album2));
        when(albumsRepository.findAllSharedToCircle(id2)).thenReturn(List.of(album2, album3));

        List<Album> result = service.retrieveAccessibleAlbums();
        Assertions.assertEquals(List.of(album1, album2, album3), result);
        verify(securityService).getAuthenticatedEmail();
        verify(circleService).findAllByMember(any());
        verify(albumsRepository, times(2)).findAllSharedToCircle(any());
    }

    @Test
    public void testJoinAlbum() {
        String id = "test";
        String testToken = "testToken";
        ShareInfo testShareInfo = ShareInfo.builder().shareToken(testToken).build();
        Album testAlbum = Album.builder().name("test").id(id).shareInfo(testShareInfo).build();
        when(albumService.getAlbum(id)).thenReturn(Optional.of(testAlbum));
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        when(securityService.getOauth2AuthenticatedRestTemplate()).thenReturn(mockRestTemplate);
        service.joinAlbum("test");
        verify(mockRestTemplate).postForLocation(eq(apiUrl + ":join"), any(JoinCommand.class));
    }
}
