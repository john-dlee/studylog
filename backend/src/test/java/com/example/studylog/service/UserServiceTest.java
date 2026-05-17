package com.example.studylog.service;

import com.example.studylog.domain.User;
import com.example.studylog.dto.LoginRequest;
import com.example.studylog.dto.LoginResponse;
import com.example.studylog.dto.RegisterRequest;
import com.example.studylog.dto.RegisterResponse;
import com.example.studylog.exception.InvalidCredentialsException;
import com.example.studylog.exception.UserAlreadyExistsException;
import com.example.studylog.repository.UserRepository;
import com.example.studylog.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    private RegisterRequest registerRequest;

    @BeforeEach
    public void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("alice@example.com");
        registerRequest.setUsername("alice");
        registerRequest.setPassword("password123");
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(99L);
            return u;
        });

        RegisterResponse result = userService.register(registerRequest);

        assertEquals(99L, result.getId());
        assertEquals("alice", result.getUsername());
        assertEquals("alice@example.com", result.getEmail());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("hashed-password", captor.getValue().getPassword());
    }

    @Test
    void shouldFailWhenEmailExists() {
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.register(registerRequest));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldLoginSuccessfully() {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setEmail("alice@example.com");
        user.setPassword("hashed-password");

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("alice@example.com");
        loginRequest.setPassword("password123");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed-password")).thenReturn(true);
        when(jwtService.generateToken(1L, "alice@example.com")).thenReturn("jwt-token");

        LoginResponse result = userService.login(loginRequest);

        assertEquals("jwt-token", result.getToken());
        assertEquals(1L, result.getId());
        assertEquals("alice", result.getUsername());
        assertEquals("alice@example.com", result.getEmail());
    }

    @Test
    void shouldFailLoginWhenUserNotFound() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("missing@example.com");
        loginRequest.setPassword("password123");

        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> userService.login(loginRequest));
    }

    @Test
    void shouldFailLoginWhenPasswordWrong() {
        User user = new User();
        user.setEmail("alice@example.com");
        user.setPassword("hashed-password");

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("alice@example.com");
        loginRequest.setPassword("wrong");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed-password")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> userService.login(loginRequest));
        verify(jwtService, never()).generateToken(any(), any());
    }
}
