package rniesler.gphotoshare.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;
import rniesler.gphotoshare.domain.googleapi.*;
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
    private RestTemplate restTemplate;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        service = new AlbumServiceImpl(securityService, API_URL);
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
    public void testGetAlbumFromApi() {
        String testAlbumId = "test";
        GoogleAlbum album = GoogleAlbum.builder().id(testAlbumId).build();
        String expectedUrl = API_URL + "/" + testAlbumId;
        when(restTemplate.getForObject(expectedUrl, GoogleAlbum.class)).thenReturn(album);
        assertEquals(album, service.getAlbum(testAlbumId).get());
        verify(restTemplate).getForObject(expectedUrl, GoogleAlbum.class);
    }


    @Test
    public void testCreateAndShareAlbum() {
        String testName = "test";
        String id = "testId";
        GoogleAlbum album = GoogleAlbum.builder().name(testName).id(id).build();
        when(restTemplate.postForObject(eq(API_URL), any(CreateAlbumCommand.class), eq(GoogleAlbum.class)))
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
