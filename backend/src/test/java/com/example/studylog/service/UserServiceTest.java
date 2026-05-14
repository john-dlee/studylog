package com.example.studylog.service;

import com.example.studylog.domain.User;
import com.example.studylog.dto.RegisterRequest;
import com.example.studylog.dto.RegisterResponse;
import com.example.studylog.exception.UserAlreadyExistsException;
import com.example.studylog.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private RegisterRequest request;

    @BeforeEach
    public void setUp() {
        request = new RegisterRequest();
        request.setEmail("alice@example.com");
        request.setUsername("alice");
        request.setPassword("password123");
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(99L);
            return u;
        });

        RegisterResponse result = userService.register(request);

        assertEquals(99L, result.getId());
        assertEquals("alice", result.getUsername());
        assertEquals("alice@example.com", result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldFailWhenEmailExists() {
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> {
            userService.register(request);
        });

        verify(userRepository, never()).save(any(User.class));
    }
}
