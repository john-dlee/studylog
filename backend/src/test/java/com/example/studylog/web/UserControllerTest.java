package com.example.studylog.web;

import com.example.studylog.dto.LoginRequest;
import com.example.studylog.dto.LoginResponse;
import com.example.studylog.dto.RegisterRequest;
import com.example.studylog.dto.RegisterResponse;
import com.example.studylog.exception.InvalidCredentialsException;
import com.example.studylog.exception.GlobalExceptionHandler;
import com.example.studylog.exception.UserAlreadyExistsException;
import com.example.studylog.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
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
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("web-slice-test")
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

    @Test
    void shouldReturn200WhenLoginSucceeds() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("alice@example.com");
        loginRequest.setPassword("password123");

        when(userService.login(any(LoginRequest.class)))
                .thenReturn(new LoginResponse("jwt-token", 1L, "alice", "alice@example.com"));

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    void shouldReturn401WhenLoginFails() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("alice@example.com");
        loginRequest.setPassword("wrong");

        when(userService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Unauthorized"));
    }
}
