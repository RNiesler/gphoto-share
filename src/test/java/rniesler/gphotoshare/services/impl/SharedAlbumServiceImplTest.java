package rniesler.gphotoshare.services.impl;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;
import rniesler.gphotoshare.domain.Person;
import rniesler.gphotoshare.domain.commands.ShareAlbumCommand;
import rniesler.gphotoshare.domain.SharedAlbum;
import rniesler.gphotoshare.domain.SharedAlbumRepository;
import rniesler.gphotoshare.domain.googleapi.GoogleAlbum;
import rniesler.gphotoshare.domain.googleapi.ShareInfo;
import rniesler.gphotoshare.security.SecurityService;
import rniesler.gphotoshare.services.AlbumService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SharedAlbumServiceImplTest {

    private SharedAlbumServiceImpl service;

    @Mock
    private SharedAlbumRepository sharedAlbumRepository;
    @Mock
    private SecurityService securityService;
    @Mock
    private AlbumService albumService;
    @Mock
    private RestTemplate restTemplate;

    private final static String TEST_EMAIL = "test@email";

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        service = new SharedAlbumServiceImpl(sharedAlbumRepository, securityService, albumService);
        when(securityService.getAuthenticatedEmail()).thenReturn(TEST_EMAIL);
        when(securityService.getOauth2AuthenticatedRestTemplate()).thenReturn(restTemplate);
    }

    @Test
    public void testGetUsersSharedAlbums() {
        when(sharedAlbumRepository.findAllByOwner(TEST_EMAIL)).thenReturn(Collections.emptyList());
        List<SharedAlbum> albums = service.getUsersSharedAlbums();
        verify(sharedAlbumRepository).findAllByOwner(TEST_EMAIL);
    }

    @Test
    public void testGetSharedAlbum() {
        String testId = "test";
        SharedAlbum testAlbum = new SharedAlbum();
        when(sharedAlbumRepository.findById(testId)).thenReturn(Optional.of(testAlbum));
        assertEquals(testAlbum, service.getSharedAlbum(testId).get());
    }

    @Test
    public void testGetShareCommandWhenWrongId() {
        String testId = "test";
        when(albumService.getAlbum(testId)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.getShareAlbumCommand(testId));
    }

    @Test
    public void testGetShareCommandWhenInDb() {
        String testId = "test";
        String shareToken = "token";
        String url = "url";
        List<ObjectId> sharedTo = List.of(new ObjectId(), new ObjectId());
        SharedAlbum album = SharedAlbum.builder().id(testId).name("name").shareToken(shareToken).sharedTo(sharedTo).publicUrl(url).build();
        when(sharedAlbumRepository.findById(testId)).thenReturn(Optional.of(album));
        when(albumService.getAlbum(testId)).thenReturn(Optional.of(new GoogleAlbum()));
        ShareAlbumCommand shareAlbumCommand = service.getShareAlbumCommand(testId);
        assertEquals(testId, shareAlbumCommand.getAlbumId());
        assertEquals(shareToken, shareAlbumCommand.getShareToken());
        assertEquals(url, shareAlbumCommand.getPublicUrl());
        assertEquals(sharedTo, shareAlbumCommand.getSharedTo());
    }

    @Test
    public void testGetShareCommandWhenNotInDb() {
        String testId = "test";
        String shareToken = "token";
        String url = "url";
        ShareInfo shareInfo = ShareInfo.builder().shareToken(shareToken).shareableUrl(url).build();
        GoogleAlbum googleAlbum = GoogleAlbum.builder().id(testId).shareInfo(shareInfo).build();
        when(sharedAlbumRepository.findById(testId)).thenReturn(Optional.empty());
        when(albumService.getAlbum(testId)).thenReturn(Optional.of(googleAlbum));
        ShareAlbumCommand shareAlbumCommand = service.getShareAlbumCommand(testId);
        assertEquals(testId, shareAlbumCommand.getAlbumId());
        assertEquals(shareToken, shareAlbumCommand.getShareToken());
        assertEquals(url, shareAlbumCommand.getPublicUrl());
        assertNull(shareAlbumCommand.getSharedTo());
    }

    @Test
    public void testShareDelete() {
        String testId = "test";
        ShareAlbumCommand command = ShareAlbumCommand.builder().albumId(testId).sharedTo(Collections.emptyList()).build();
        service.shareAlbum(command);
        verify(sharedAlbumRepository).deleteById(testId);
        verify(sharedAlbumRepository, never()).save(any());
    }

    @Test
    public void testShareInDb() {
        String testId = "test";
        String token = "token";
        String url = "url";
        String name = "name";
        List<ObjectId> sharedTo = List.of(new ObjectId());
        ShareAlbumCommand command = ShareAlbumCommand.builder().albumId(testId).sharedTo(sharedTo).shareToken(token)
                .publicUrl(url).build();
        SharedAlbum sharedAlbum = SharedAlbum.builder().id(testId).sharedTo(Collections.emptyList()).shareToken("wrong token")
                .publicUrl("wrong url").build();
        when(sharedAlbumRepository.findById(testId)).thenReturn(Optional.of(sharedAlbum));

        GoogleAlbum album = GoogleAlbum.builder().id(testId).coverPhotoUrl(url).name(name).build();
        when(albumService.getAlbum(testId)).thenReturn(Optional.of(album));
        when(restTemplate.getForObject(url, byte[].class)).thenReturn(new byte[] {0,0});

        service.shareAlbum(command);
        ArgumentCaptor<SharedAlbum> captor = ArgumentCaptor.forClass(SharedAlbum.class);
        verify(sharedAlbumRepository).save(captor.capture());
        assertEquals(command.getShareToken(), captor.getValue().getShareToken());
        assertEquals(command.getPublicUrl(), captor.getValue().getPublicUrl());
        assertEquals(command.getSharedTo(), captor.getValue().getSharedTo());
        assertEquals(album.getName(), captor.getValue().getName());
        assertNotNull(captor.getValue().getCoverPhoto());
        verify(restTemplate).getForObject(url, byte[].class); // called for image
    }

    @Test
    public void testShareNew() {
        String testId = "test";
        String token = "token";
        String url = "url";
        String name = "name";
        List<ObjectId> sharedTo = List.of(new ObjectId());
        ShareAlbumCommand command = ShareAlbumCommand.builder().albumId(testId).sharedTo(sharedTo).shareToken(token)
                .publicUrl(url).build();
        when(sharedAlbumRepository.findById(testId)).thenReturn(Optional.empty());

        ShareInfo shareInfo = ShareInfo.builder().shareableUrl("wrong url").shareToken("wrong token").build();
        GoogleAlbum album = GoogleAlbum.builder().id(testId).coverPhotoUrl(url).name(name).shareInfo(shareInfo).build();
        when(albumService.getAlbum(testId)).thenReturn(Optional.of(album));
        when(securityService.getAuthenticatedUser()).thenReturn(Optional.of(new Person()));
        when(restTemplate.getForObject(url, byte[].class)).thenReturn(new byte[] {0,0});

        service.shareAlbum(command);
        ArgumentCaptor<SharedAlbum> captor = ArgumentCaptor.forClass(SharedAlbum.class);
        verify(sharedAlbumRepository).save(captor.capture());
        assertEquals(command.getShareToken(), captor.getValue().getShareToken());
        assertEquals(command.getPublicUrl(), captor.getValue().getPublicUrl());
        assertEquals(command.getSharedTo(), captor.getValue().getSharedTo());
        assertEquals(album.getName(), captor.getValue().getName());
        assertNotNull(captor.getValue().getCoverPhoto());
        verify(restTemplate).getForObject(url, byte[].class); // called for image
    }

    @Test
    public void testShareNewWhenNotAuthenticated() {
        ShareAlbumCommand command = ShareAlbumCommand.builder().sharedTo(List.of(new ObjectId())).build();
        when(albumService.getAlbum(any())).thenReturn(Optional.of(new GoogleAlbum()));
        when(sharedAlbumRepository.findById(anyString())).thenReturn(Optional.empty());
        when(securityService.getAuthenticatedUser()).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.shareAlbum(command));
    }

    @Test
    public void testShareWithWrongId() {
        when(albumService.getAlbum(any())).thenReturn(Optional.empty());
        when(sharedAlbumRepository.findById(any())).thenReturn(Optional.of(new SharedAlbum()));
        when(securityService.getAuthenticatedUser()).thenReturn(Optional.empty());
        ShareAlbumCommand command = ShareAlbumCommand.builder().sharedTo(List.of(new ObjectId())).build();
        assertThrows(RuntimeException.class, () -> service.shareAlbum(command));
    }

    @Test
    public void testShareWithEmptyToken() {
        String token = "";
        String photoUrl = "url";
        List<ObjectId> sharedTo = List.of(new ObjectId());
        ShareAlbumCommand command = ShareAlbumCommand.builder().sharedTo(sharedTo).shareToken(token).build();
        SharedAlbum sharedAlbum = SharedAlbum.builder().sharedTo(Collections.emptyList()).shareToken("wrong token").build();
        when(sharedAlbumRepository.findById(any())).thenReturn(Optional.of(sharedAlbum));

        GoogleAlbum googleAlbum = GoogleAlbum.builder().coverPhotoUrl(photoUrl).build();
        when(albumService.getAlbum(any())).thenReturn(Optional.of(googleAlbum));
        when(restTemplate.getForObject(photoUrl, byte[].class)).thenReturn(new byte[] {0,0});

        service.shareAlbum(command);
        ArgumentCaptor<SharedAlbum> captor = ArgumentCaptor.forClass(SharedAlbum.class);
        verify(sharedAlbumRepository).save(captor.capture());
        assertNull(captor.getValue().getShareToken());
    }
}
