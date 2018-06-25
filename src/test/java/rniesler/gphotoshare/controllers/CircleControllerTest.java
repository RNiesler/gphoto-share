package rniesler.gphotoshare.controllers;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.NestedServletException;
import rniesler.gphotoshare.domain.Circle;
import rniesler.gphotoshare.domain.Person;
import rniesler.gphotoshare.exceptions.CircleNotFoundException;
import rniesler.gphotoshare.security.SecurityService;
import rniesler.gphotoshare.services.CircleService;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class CircleControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CircleService circleService;
    @Mock
    private SecurityService securityService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(new CircleController(circleService, securityService)).build();
    }

    @Test
    public void testListCircles() throws Exception {
        List<Circle> circleList = List.of(Circle.builder().name("test").build());
        when(circleService.findAll()).thenReturn(circleList);
        mockMvc.perform(get("/circles"))
                .andExpect(status().isOk())
                .andExpect(view().name("circlelist"))
                .andExpect(model().attribute("circles", circleList))
                .andExpect(model().attribute("newcircle", new Circle()));
    }

    @Test
    public void testNewCircle() throws Exception {
        String owner = "test@test";
        when(securityService.getAuthenticatedEmail()).thenReturn(owner);
        Circle circle = Circle.builder().name("test").build();
        mockMvc.perform(post("/circles").flashAttr("newcircle", circle))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/circles"));
        ArgumentCaptor<Circle> captor = ArgumentCaptor.forClass(Circle.class);
        verify(circleService).persist(captor.capture());
        assertEquals(owner, captor.getValue().getOwner());
    }

    @Test
    public void testNewCircleInvalid() throws Exception {
        String owner = "test@test";
        when(securityService.getAuthenticatedEmail()).thenReturn(owner);
        Circle circle = Circle.builder().build();
        mockMvc.perform(post("/circles").flashAttr("newcircle", circle))
                .andExpect(status().isOk())
                .andExpect(view().name("circlelist"))
                .andExpect(model().attributeExists("circles"))
                .andExpect(model().attribute("newcircle", circle));
        verify(circleService, never()).persist(any());
    }

    @Test
    public void testGetCircleWhenNotExisting() throws Exception {
        String testId = "wrong";
        when(circleService.get(testId)).thenReturn(Optional.empty());
        try {
            mockMvc.perform(get("/" + testId))
                    .andExpect(status().isNotFound());
        } catch (NestedServletException servletException) {
            assertTrue(servletException.getCause() instanceof CircleNotFoundException);
        }
    }

    @Test
    public void testGetCircle() throws Exception {
        ObjectId testId = new ObjectId();
        Circle circle = Circle.builder().id(testId).build();
        when(circleService.get(testId.toHexString())).thenReturn(Optional.of(circle));
        mockMvc.perform(get("/circles/" + testId.toHexString()))
                .andExpect(status().isOk())
                .andExpect(view().name("circle"))
                .andExpect(model().attribute("circle", circle))
                .andExpect(model().attribute("newMemberCommand", new Person()));
    }

    @Test
    public void testDeleteCircle() throws Exception {
        ObjectId testId = new ObjectId();
        mockMvc.perform(post("/circles/" + testId.toHexString() + "/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/circles"));
        verify(circleService).deleteCircle(testId.toHexString());
    }

    @Test
    public void testRemoveMemberWrongId() throws Exception {
        String testId = "wrong";
        when(circleService.get(testId)).thenReturn(Optional.empty());
        try {
            mockMvc.perform(post("/circles/" + testId + "/members/m@m"))
                    .andExpect(status().isNotFound());
        } catch (NestedServletException servletException) {
            assertTrue(servletException.getCause() instanceof CircleNotFoundException);
        } finally {
            verify(circleService, never()).persist(any(Circle.class));
        }
    }

    @Test
    public void testRemoveMember() throws Exception {
        ObjectId testId = new ObjectId();
        String amail = "a@a";
        String bmail = "b@b";
        List<String> members = new LinkedList<>(List.of(amail, bmail));
        Circle circle = Circle.builder().id(testId).members(members).build();
        when(circleService.get(testId.toHexString())).thenReturn(Optional.of(circle));
        mockMvc.perform(post("/circles/" + testId.toHexString() + "/members/" + bmail))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/circles/" + testId.toHexString()));
        ArgumentCaptor<Circle> captor = ArgumentCaptor.forClass(Circle.class);
        verify(circleService).persist(captor.capture());
        assertEquals(testId, captor.getValue().getId());
        assertEquals(List.of(amail), captor.getValue().getMembers());
    }

    @Test
    public void testAddMemberWrongId() throws Exception {
        String testId = "wrong";
        when(circleService.get(testId)).thenReturn(Optional.empty());
        Person command = Person.builder().id(new ObjectId()).email("test@test").build();
        try {
            mockMvc.perform(post("/circles/" + testId + "/members").flashAttr("newMemberCommand", command))
                    .andExpect(status().isNotFound());
        } catch (NestedServletException servletException) {
            assertTrue(servletException.getCause() instanceof CircleNotFoundException);
        } finally {
            verify(circleService, never()).persist(any(Circle.class));
        }
    }

    @Test
    public void testAddMember() throws Exception {
        ObjectId testId = new ObjectId();
        Circle circle = Circle.builder().id(testId).name("test").build();
        when(circleService.get(testId.toHexString())).thenReturn(Optional.of(circle));
        Person command = Person.builder().id(new ObjectId()).email("test@test").build();
        mockMvc.perform(post("/circles/" + testId.toHexString() + "/members").flashAttr("newMemberCommand", command))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/circles/" + testId.toHexString()));
        ArgumentCaptor<Circle> captor = ArgumentCaptor.forClass(Circle.class);
        verify(circleService).persist(captor.capture());
        assertEquals(command.getEmail(), captor.getValue().getMembers().get(0));
    }


    @Test
    public void testAddMemberInvalid() throws Exception {
        ObjectId testId = new ObjectId();
        Circle circle = Circle.builder().id(testId).name("test").build();
        when(circleService.get(testId.toHexString())).thenReturn(Optional.of(circle));
        Person command = Person.builder().id(new ObjectId()).build();
        mockMvc.perform(post("/circles/" + testId.toHexString() + "/members").flashAttr("newMemberCommand", command))
                .andExpect(status().isOk())
                .andExpect(view().name("circle"))
                .andExpect(model().attributeExists("circle"))
                .andExpect(model().attribute("newMemberCommand", command));
        verify(circleService, never()).persist(any());

    }
}
