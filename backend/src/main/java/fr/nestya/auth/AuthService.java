package fr.nestya.auth;

import fr.nestya.auth.dto.AuthResponse;
import fr.nestya.auth.dto.LoginRequest;
import fr.nestya.auth.dto.RegisterRequest;
import fr.nestya.exception.TokenNotFoundException;
import fr.nestya.exception.UserAlreadyExistsException;
import fr.nestya.security.JwtService;
import fr.nestya.user.User;
import fr.nestya.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    public AuthResponse register(RegisterRequest request) {
        if(userRepository.findByEmail(request.email()).isPresent()) {
            throw new UserAlreadyExistsException("An account with this email already exists.");
        }
        var user = new User();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));

        userRepository.save(user);

        var accessToken = jwtService.generateAccessToken(user);
        var refreshToken = createAndSaveRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken.getToken());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        var user = userRepository.findByEmail(request.email()).orElseThrow();

        var accessToken = jwtService.generateAccessToken(user);
        var refreshToken = createAndSaveRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken.getToken());
    }

    public AuthResponse refreshToken(String refreshTokenValue) {
        return refreshTokenRepository.findByToken(refreshTokenValue)
                .map(this::verifyRefreshTokenExpiration)
                .map(refreshToken -> {
                    User user = refreshToken.getUser();

                    refreshTokenRepository.delete(refreshToken);

                    RefreshToken newRefreshToken = createAndSaveRefreshToken(user);

                    String newAccessToken = jwtService.generateAccessToken(user);

                    return new AuthResponse(newAccessToken, newRefreshToken.getToken());
                })
                .orElseThrow(() -> new TokenNotFoundException("Refresh token not found or expired"));
    }

    public void logout(String refreshTokenValue) {
        refreshTokenRepository.findByToken(refreshTokenValue).ifPresent(refreshTokenRepository::delete);
    }

    private RefreshToken createAndSaveRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);

        var refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenExpiration));
        refreshToken.setToken(UUID.randomUUID().toString());

        return refreshTokenRepository.save(refreshToken);
    }

    private RefreshToken verifyRefreshTokenExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new TokenNotFoundException("Refresh token was expired. Please make a new signin request");
        }
        return token;
    }



}
