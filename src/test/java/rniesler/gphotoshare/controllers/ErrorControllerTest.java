package rniesler.gphotoshare.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

public class ErrorControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(new ErrorController()).build();
    }

    @Test
    public void testAccessDeniedMapping() throws Exception {
        mockMvc.perform(get("/errors/accessDenied"))
                .andExpect(status().isForbidden())
                .andExpect(view().name("errors/accessDenied"));
    }

}
