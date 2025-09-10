package fr.nestya.auth;

import fr.nestya.auth.dto.AuthResponse;
import fr.nestya.auth.dto.LoginRequest;
import fr.nestya.auth.dto.RegisterRequest;
import fr.nestya.exception.UserAlreadyExistsException;
import fr.nestya.security.JwtService;
import fr.nestya.user.User;
import fr.nestya.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldCreateUserAndReturnTokens_whenEmailIsUnique() {
        // ARRANGE
        var request = new RegisterRequest("John", "Doe", "john.doe@test.com", "password123");

        // Mock definitions
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.password())).thenReturn("hashedPassword");
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("fake-access-token");

        // Simulate creation of a refresh token
        RefreshToken fakeRefreshToken = new RefreshToken();
        fakeRefreshToken.setToken("fake-refresh-token");
        fakeRefreshToken.setUser(new User()); // Simulate associated user
        fakeRefreshToken.setExpiryDate(Instant.now().plusMillis(604800000));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(fakeRefreshToken);


        // ACT (Action)
        AuthResponse response = authService.register(request);

        // ASSERT (Vérification)
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("fake-access-token");
        assertThat(response.refreshToken()).isEqualTo("fake-refresh-token");

        // We check that userRepository.save was called once with a User having the expected properties
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode("password123");
    }

    @Test
    void register_shouldThrowUserAlreadyExistsException_whenEmailExists() {
        // ARRANGE
        var request = new RegisterRequest("Jane", "Doe", "jane.doe@test.com", "password123");

        // Simulate that a user with the given email already exists
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(new User()));

        // ACT & ASSERT
        // We expect an exception to be thrown
        assertThrows(UserAlreadyExistsException.class, () -> {
            authService.register(request);
        });

        // We verify that userRepository.save was never called
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_shouldAuthenticateAndReturnTokens_whenCredentialsAreValid() {

        // ARRANGE
        var request = new fr.nestya.auth.dto.LoginRequest("john.doe@test.com", "password123");
        var user = new User();
        user.setEmail(request.email());
        user.setPassword("hashedPassword");

        // Mock definitions
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(user)).thenReturn("fake-access-token");

        // Simulate creation of a refresh token
        RefreshToken fakeRefreshToken = new RefreshToken();
        fakeRefreshToken.setToken("fake-refresh-token");
        fakeRefreshToken.setUser(user); // Simulate associated user
        fakeRefreshToken.setExpiryDate(Instant.now().plusMillis(604800000));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(fakeRefreshToken);
        when(authenticationManager.authenticate(any())).thenReturn(null);

        // ACT
        AuthResponse response = authService.login(request);

        // ASSERT
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("fake-access-token");
        assertThat(response.refreshToken()).isEqualTo("fake-refresh-token");
        verify(authenticationManager, times(1)).authenticate(any());
    }

    @Test
    void login_shouldThrowBadCredentialsException_whenCredentialsAreInvalid() {
        // ARRANGE
        var request = new LoginRequest("john.doe@test.com", "wrongpassword");

        // Simulate authentication failure
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // ACT & ASSERT
        assertThrows(BadCredentialsException.class, () -> {
            authService.login(request);
        });

        verify(userRepository, never()).findByEmail(anyString());
        verify(jwtService, never()).generateAccessToken(any(User.class));
    }

    @Test
    void refreshToken_shouldAuthenticateAndReturnTokens_whenTokenIsValid() {
        // ARRANGE
        String oldRefreshTokenValue = "valid-refresh-token";

        var user = new User();
        user.setEmail("john.doe@test.fr");

        var oldRefreshToken = new RefreshToken();
        oldRefreshToken.setToken(oldRefreshTokenValue);
        oldRefreshToken.setUser(user);
        oldRefreshToken.setExpiryDate(Instant.now().plusMillis(604800000));

        // Mock definitions
        when(refreshTokenRepository.findByToken(oldRefreshTokenValue)).thenReturn(Optional.of(oldRefreshToken));
        when(jwtService.generateAccessToken(user)).thenReturn("new-fake-access-token");

        // Simulate creation of a new refresh token
        RefreshToken newFakeRefreshToken = new RefreshToken();
        newFakeRefreshToken.setToken("new-fake-refresh-token");
        newFakeRefreshToken.setUser(user); // Simulate associated user
        newFakeRefreshToken.setExpiryDate(Instant.now().plusMillis(604800000));

        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(newFakeRefreshToken);
        doNothing().when(refreshTokenRepository).delete(oldRefreshToken);

        // ACT
        AuthResponse response = authService.refreshToken(oldRefreshTokenValue);

        // ASSERT
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("new-fake-access-token");
        assertThat(response.refreshToken()).isEqualTo(newFakeRefreshToken.getToken());

        verify(refreshTokenRepository, times(1)).delete(oldRefreshToken);
    }

    @Test
    void refreshToken_shouldThrowTokenNotFoundException_whenRefreshTokenIsInvalid() {
        // ARRANGE
        String invalidRefreshTokenValue = "invalid-refresh-token";
        when(refreshTokenRepository.findByToken(invalidRefreshTokenValue)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(fr.nestya.exception.TokenNotFoundException.class, () -> {
            authService.refreshToken(invalidRefreshTokenValue);
        });

        verify(jwtService, never()).generateAccessToken(any(User.class));
        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
    }

    @Test
    void refreshToken_shouldThrowTokenNotFoundException_whenTokenIsExpired() {
        // ARRANGE
        String expiredTokenValue = "expired-refresh-token";
        var user = new User();
        user.setEmail("john.doe@test.fr");

        var expiredRefreshToken = new RefreshToken();
        expiredRefreshToken.setToken(expiredTokenValue);
        expiredRefreshToken.setUser(user);
        expiredRefreshToken.setExpiryDate(Instant.now().minusSeconds(60));

        when(refreshTokenRepository.findByToken(expiredTokenValue)).thenReturn(Optional.of(expiredRefreshToken));

        // ACT & ASSERT
        assertThrows(fr.nestya.exception.TokenNotFoundException.class, () -> {
            authService.refreshToken(expiredTokenValue);
        });

        verify(refreshTokenRepository, times(1)).delete(expiredRefreshToken);
        verify(jwtService, never()).generateAccessToken(any(User.class));
    }


    @Test
    void logout_shouldDeleteRefreshToken_whenTokenExists() {
        // ARRANGE
        String refreshTokenValue = "valid-refresh-token";

        var user = new User();
        user.setEmail("john.doe@test.com");

        when(refreshTokenRepository.findByToken(refreshTokenValue)).thenReturn(Optional.of(new RefreshToken()));
        doNothing().when(refreshTokenRepository).delete(any(RefreshToken.class));

        // ACT
        authService.logout(refreshTokenValue);

        // ASSERT
        verify(refreshTokenRepository, times(1)).delete(any(RefreshToken.class));
    }

    @Test
    void logout_shouldDoNothing_whenTokenDoesNotExist() {
        // ARRANGE
        String refreshTokenValue = "nonexistent-refresh-token";
        when(refreshTokenRepository.findByToken(refreshTokenValue)).thenReturn(Optional.empty());

        // ACT
        authService.logout(refreshTokenValue);
        // ASSERT
        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
    }
}
