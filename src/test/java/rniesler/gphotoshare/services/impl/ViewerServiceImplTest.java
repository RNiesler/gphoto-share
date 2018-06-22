package rniesler.gphotoshare.services.impl;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;
import rniesler.gphotoshare.domain.Circle;
import rniesler.gphotoshare.domain.SharedAlbum;
import rniesler.gphotoshare.domain.SharedAlbumRepository;
import rniesler.gphotoshare.domain.googleapi.JoinCommand;
import rniesler.gphotoshare.security.SecurityService;
import rniesler.gphotoshare.services.CircleService;
import rniesler.gphotoshare.services.SharedAlbumService;
import rniesler.gphotoshare.services.ViewerService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ViewerServiceImplTest {
    private final static String apiUrl = "TEST_URL";
    private ViewerService service;

    @Mock
    private CircleService circleService;
    @Mock
    private SecurityService securityService;
    @Mock
    private SharedAlbumRepository sharedAlbumRepository;
    @Mock
    private SharedAlbumService sharedAlbumService;


    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        service = new ViewerServiceImpl(circleService, securityService, apiUrl, sharedAlbumRepository, sharedAlbumService);
    }

    @Test
    public void testRetrieveAccessibleAlbums() {
        ObjectId id1 = ObjectId.get();
        ObjectId id2 = ObjectId.get();
        SharedAlbum album1 = SharedAlbum.builder().name("test1").build();
        SharedAlbum album2 = SharedAlbum.builder().name("test2").build();
        SharedAlbum album3 = SharedAlbum.builder().name("test3").build();
        List<Circle> circles = List.of(
                Circle.builder().id(id1).build(),
                Circle.builder().id(id2).build()
        );
        when(circleService.findAllByMember(any())).thenReturn(circles);
        when(sharedAlbumRepository.findBySharedToContaining(id1)).thenReturn(List.of(album1, album2));
        when(sharedAlbumRepository.findBySharedToContaining(id2)).thenReturn(List.of(album2, album3));

        List<SharedAlbum> result = service.retrieveAccessibleAlbums();
        Assertions.assertEquals(List.of(album1, album2, album3), result);
        verify(securityService).getAuthenticatedEmail();
        verify(circleService).findAllByMember(any());
        verify(sharedAlbumRepository, times(2)).findBySharedToContaining(any());
    }

    @Test
    public void testJoinAlbum() {
        String id = "test";
        String testToken = "testToken";
        SharedAlbum testAlbum = SharedAlbum.builder().name("test").id(id).shareToken(testToken).build();
        when(sharedAlbumService.getSharedAlbum(id)).thenReturn(Optional.of(testAlbum));
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        when(securityService.getOauth2AuthenticatedRestTemplate()).thenReturn(mockRestTemplate);
        service.joinAlbum("test");
        verify(mockRestTemplate).postForLocation(eq(apiUrl + ":join"), any(JoinCommand.class));
    }
}
