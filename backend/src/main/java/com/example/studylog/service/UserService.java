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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    public static final String INCORRECT_LOGIN_MESSAGE = "Incorrect email or password";
    public static final String EMAIL_TAKEN_MESSAGE = "Email is already taken";
    public static final String USERNAME_TAKEN_MESSAGE = "Username is already taken";

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PasswordResetMailService passwordResetMailService;
    private final long resetExpirationMinutes;
    private final String resetPasswordBaseUrl;
    private final boolean exposeResetLink;

    public UserService(
            UserRepository userRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            PasswordResetMailService passwordResetMailService,
            @Value("${app.reset-password.expiration-minutes:60}") long resetExpirationMinutes,
            @Value("${app.reset-password.base-url:http://localhost:5173/reset-password}") String resetPasswordBaseUrl,
            @Value("${app.auth.expose-reset-link:true}") boolean exposeResetLink) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.passwordResetMailService = passwordResetMailService;
        this.resetExpirationMinutes = resetExpirationMinutes;
        this.resetPasswordBaseUrl = resetPasswordBaseUrl;
        this.exposeResetLink = exposeResetLink;
    }

    public RegisterResponse register(RegisterRequest request) {
        String username = request.getUsername().trim();
        String email = request.getEmail().trim();

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new UserAlreadyExistsException(USERNAME_TAKEN_MESSAGE);
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new UserAlreadyExistsException(EMAIL_TAKEN_MESSAGE);
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail()
        );
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException(INCORRECT_LOGIN_MESSAGE));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException(INCORRECT_LOGIN_MESSAGE);
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getEmail());
    }

    @Transactional
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        String genericMessage =
                "If an account exists for that email, password reset instructions have been sent.";

        return userRepository.findByEmail(request.getEmail())
                .map(user -> {
                    passwordResetTokenRepository.deleteByUser_Id(user.getId());

                    String token = UUID.randomUUID().toString();
                    PasswordResetToken resetToken = new PasswordResetToken();
                    resetToken.setToken(token);
                    resetToken.setUser(user);
                    resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(resetExpirationMinutes));
                    passwordResetTokenRepository.save(resetToken);

                    String resetLink = resetPasswordBaseUrl + "?token=" + token;
                    boolean emailSent =
                            passwordResetMailService.sendPasswordResetEmail(user.getEmail(), resetLink);

                    boolean showDevLink = exposeResetLink && !emailSent;
                    String exposedLink = showDevLink ? resetLink : null;
                    return new ForgotPasswordResponse(genericMessage, exposedLink);
                })
                .orElseGet(() -> new ForgotPasswordResponse(genericMessage, null));
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByTokenAndUsedFalse(request.getToken())
                .orElseThrow(() -> new InvalidResetTokenException("This reset link is invalid or has expired."));

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidResetTokenException("This reset link is invalid or has expired.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }
}
