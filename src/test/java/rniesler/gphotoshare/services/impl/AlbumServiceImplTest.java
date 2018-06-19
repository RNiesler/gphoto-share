package rniesler.gphotoshare.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;
import rniesler.gphotoshare.domain.Album;
import rniesler.gphotoshare.domain.AlbumsRepository;
import rniesler.gphotoshare.domain.Person;
import rniesler.gphotoshare.domain.ShareInfo;
import rniesler.gphotoshare.domain.googleapi.AlbumsList;
import rniesler.gphotoshare.domain.googleapi.CreateAlbumCommand;
import rniesler.gphotoshare.domain.googleapi.ShareResult;
import rniesler.gphotoshare.security.SecurityService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AlbumServiceImplTest {
    private final static String API_URL = "API_URL";

    private AlbumServiceImpl service;

    @Mock
    private SecurityService securityService;
    @Mock
    private AlbumsRepository albumsRepository;
    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        service = new AlbumServiceImpl(securityService, API_URL, albumsRepository);
        when(securityService.getOauth2AuthenticatedRestTemplate()).thenReturn(restTemplate);
    }

    @Test
    public void testListAlbums() {
        AlbumsList testAlbumsList = new AlbumsList();
        String expectedUrl = API_URL + "?pageSize=10";
        when(restTemplate.getForObject(expectedUrl, AlbumsList.class)).thenReturn(testAlbumsList);
        assertEquals(testAlbumsList, service.listAlbums(Optional.empty()));
        verify(restTemplate).getForObject(expectedUrl, AlbumsList.class);
    }

    @Test
    public void testListAlbumsWithPageToken() {
        AlbumsList testAlbumsList = new AlbumsList();
        String testPageToken = "test";
        String expectedUrl = API_URL + "?pageToken=" + testPageToken + "&pageSize=10";
        when(restTemplate.getForObject(expectedUrl, AlbumsList.class)).thenReturn(testAlbumsList);
        assertEquals(testAlbumsList, service.listAlbums(Optional.of(testPageToken)));
        verify(restTemplate).getForObject(expectedUrl, AlbumsList.class);
    }

    @Test
    public void testGetAlbumInDb() {
        String testAlbumId = "test";
        Album album = Album.builder().id(testAlbumId).build();
        when(albumsRepository.findById(testAlbumId)).thenReturn(Optional.of(album));
        assertEquals(album, service.getAlbum(testAlbumId).get());
    }

    @Test
    public void testGetAlbumFromApi() {
        String testAlbumId = "test";
        Album album = Album.builder().id(testAlbumId).build();
        when(albumsRepository.findById(testAlbumId)).thenReturn(Optional.empty());
        String expectedUrl = API_URL + "/" + testAlbumId;
        when(restTemplate.getForObject(expectedUrl, Album.class)).thenReturn(album);
        assertEquals(album, service.getAlbum(testAlbumId).get());
        verify(restTemplate).getForObject(expectedUrl, Album.class);
    }

    @Test
    public void testShareAlbum() {
        String testId = "test";
        Album album = Album.builder().id(testId).build();
        ShareInfo shareInfo = new ShareInfo();
        when(albumsRepository.findById(testId)).thenReturn(Optional.of(album));
        when(albumsRepository.save(any(Album.class))).thenReturn(album);
        when(securityService.getAuthenticatedUser()).thenReturn(Optional.of(new Person()));
        Album retAlbum = service.shareAlbum(testId, shareInfo).get();
        assertEquals(shareInfo, retAlbum.getShareInfo());
        ArgumentCaptor<Album> albumArgumentCaptor = ArgumentCaptor.forClass(Album.class);
        verify(albumsRepository).save(albumArgumentCaptor.capture());
        assertEquals(shareInfo, albumArgumentCaptor.getValue().getShareInfo());
    }

    @Test
    public void testCreateAndShareAlbum() {
        String testName = "test";
        String id = "testId";
        Album album = Album.builder().name(testName).id(id).build();
        when(restTemplate.postForObject(eq(API_URL), any(CreateAlbumCommand.class), eq(Album.class)))
                .thenReturn(album);
        String expectedUrl = API_URL + "/" + id + ":share";
        ShareResult shareResult = new ShareResult();
        ShareInfo shareInfo = new ShareInfo();
        when(restTemplate.postForObject(expectedUrl, null, ShareResult.class))
                .thenReturn(shareResult);
        shareResult.setShareInfo(shareInfo);
        assertEquals(shareInfo, service.createAndShareAlbum(testName));
    }

}
