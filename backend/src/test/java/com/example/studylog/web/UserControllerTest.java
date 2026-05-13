package com.example.studylog.web;

import com.example.studylog.dto.RegisterRequest;
import com.example.studylog.dto.RegisterResponse;
import com.example.studylog.exception.GlobalExceptionHandler;
import com.example.studylog.exception.UserAlreadyExistsException;
import com.example.studylog.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    RegisterRequest badRequest = new RegisterRequest();

    @BeforeEach
    public void setUp() {
        badRequest = new RegisterRequest();
        badRequest.setEmail("alice@example.com");
        badRequest.setUsername("alice");
        badRequest.setPassword("password123");
    }

    @Test
    void shouldReturn400_WhenPasswordIsEmpty() throws Exception {
        badRequest.setPassword("");

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400_WhenEmailIsInvalid() throws Exception {
        badRequest.setEmail("notAnEmail");

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest());

        badRequest.setEmail("");

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400_WhenUsernameIsInvalid() throws Exception {
        badRequest.setUsername("");

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn201WhenRegisterSucceeds() throws Exception {
        when(userService.register(any(RegisterRequest.class)))
                .thenReturn(new RegisterResponse(42L, "alice", "alice@example.com"));

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void shouldReturn409WhenEmailAlreadyExists() throws Exception {
        when(userService.register(any(RegisterRequest.class)))
                .thenThrow(new UserAlreadyExistsException("Email is already taken"));

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict"));
    }
}
