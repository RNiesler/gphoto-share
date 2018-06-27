package rniesler.gphotoshare.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import rniesler.gphotoshare.domain.WebPushSubscription;
import rniesler.gphotoshare.services.NotificationService;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class NotificationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NotificationService notificationService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(new NotificationController(notificationService)).build();
    }

    @Test
    public void testSubscribe() throws Exception {
        String email = "test@test";
        WebPushSubscription subscription = new WebPushSubscription();
        mockMvc.perform(post("/notifications/" + email + "/subscribe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(subscription)))
                .andExpect(status().isCreated());
        verify(notificationService).subscribe(email, subscription);
    }
}
