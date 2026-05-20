package com.example.studylog.service;

import com.example.studylog.domain.PasswordResetToken;
import com.example.studylog.domain.User;
import com.example.studylog.dto.ForgotPasswordRequest;
import com.example.studylog.dto.ForgotPasswordResponse;
import com.example.studylog.dto.LoginRequest;
import com.example.studylog.dto.LoginResponse;
import com.example.studylog.dto.RegisterRequest;
import com.example.studylog.dto.RegisterResponse;
import com.example.studylog.dto.ResetPasswordRequest;
import com.example.studylog.exception.InvalidCredentialsException;
import com.example.studylog.exception.InvalidResetTokenException;
import com.example.studylog.exception.UserAlreadyExistsException;
import com.example.studylog.repository.PasswordResetTokenRepository;
import com.example.studylog.repository.UserRepository;
import com.example.studylog.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordResetMailService passwordResetMailService;

    private UserService userService;

    private RegisterRequest registerRequest;

    @BeforeEach
    public void setUp() {
        userService = new UserService(
                userRepository,
                passwordResetTokenRepository,
                passwordEncoder,
                jwtService,
                passwordResetMailService,
                60L,
                "http://localhost:5173/reset-password",
                true);
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("alice@example.com");
        registerRequest.setUsername("alice");
        registerRequest.setPassword("password123");
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        when(userRepository.existsByUsernameIgnoreCase("alice")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("alice@example.com")).thenReturn(false);
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
        when(userRepository.existsByUsernameIgnoreCase("alice")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("alice@example.com")).thenReturn(true);

        UserAlreadyExistsException ex =
                assertThrows(UserAlreadyExistsException.class, () -> userService.register(registerRequest));
        assertEquals(UserService.EMAIL_TAKEN_MESSAGE, ex.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldFailWhenUsernameExists() {
        when(userRepository.existsByUsernameIgnoreCase("alice")).thenReturn(true);

        UserAlreadyExistsException ex =
                assertThrows(UserAlreadyExistsException.class, () -> userService.register(registerRequest));
        assertEquals(UserService.USERNAME_TAKEN_MESSAGE, ex.getMessage());

        verify(userRepository, never()).existsByEmailIgnoreCase(any());
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

        InvalidCredentialsException ex =
                assertThrows(InvalidCredentialsException.class, () -> userService.login(loginRequest));
        assertEquals(UserService.INCORRECT_LOGIN_MESSAGE, ex.getMessage());
        verify(jwtService, never()).generateToken(any(), any());
    }

    @Test
    void shouldCreateResetLinkWhenEmailExists() {
        User user = new User();
        user.setId(5L);
        user.setEmail("alice@example.com");

        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("alice@example.com");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordResetMailService.sendPasswordResetEmail(any(), any())).thenReturn(false);

        ForgotPasswordResponse response = userService.forgotPassword(request);

        assertNotNull(response.getResetLink());
        assertEquals(true, response.getResetLink().contains("token="));
        verify(passwordResetTokenRepository).deleteByUser_Id(5L);
    }

    @Test
    void shouldNotExposeResetLinkWhenEmailMissing() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("missing@example.com");

        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        ForgotPasswordResponse response = userService.forgotPassword(request);

        assertNull(response.getResetLink());
        verify(passwordResetTokenRepository, never()).save(any(PasswordResetToken.class));
    }

    @Test
    void shouldResetPasswordWithValidToken() {
        User user = new User();
        user.setId(1L);
        user.setPassword("old-hash");

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken("reset-token");
        resetToken.setUser(user);
        resetToken.setExpiresAt(LocalDateTime.now().plusHours(1));

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("reset-token");
        request.setNewPassword("newpassword123");

        when(passwordResetTokenRepository.findByTokenAndUsedFalse("reset-token"))
                .thenReturn(Optional.of(resetToken));
        when(passwordEncoder.encode("newpassword123")).thenReturn("new-hash");
        when(userRepository.save(user)).thenReturn(user);
        when(passwordResetTokenRepository.save(resetToken)).thenReturn(resetToken);

        userService.resetPassword(request);

        assertEquals("new-hash", user.getPassword());
        assertEquals(true, resetToken.isUsed());
    }

    @Test
    void shouldRejectExpiredResetToken() {
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken("expired");
        resetToken.setUser(new User());
        resetToken.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("expired");
        request.setNewPassword("newpassword123");

        when(passwordResetTokenRepository.findByTokenAndUsedFalse("expired"))
                .thenReturn(Optional.of(resetToken));

        assertThrows(InvalidResetTokenException.class, () -> userService.resetPassword(request));
    }
}
