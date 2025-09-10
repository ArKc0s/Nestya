package fr.nestya.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.nestya.auth.dto.AuthResponse;
import fr.nestya.auth.dto.LoginRequest;
import fr.nestya.auth.dto.RefreshTokenRequest;
import fr.nestya.auth.dto.RegisterRequest;
import fr.nestya.exception.TokenNotFoundException;
import fr.nestya.exception.UserAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;


    @Test
    void register_shouldReturnOkAndTokens_whenRequestIsValid() throws Exception {
        // ARRANGE
        var request = new RegisterRequest("John", "Doe", "john.doe@test.com", "password123");
        var response = new AuthResponse("fake-access-token", "fake-refresh-token");

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void register_shouldReturnConflict_whenUserAlreadyExists() throws Exception {
        // ARRANGE
        var request = new RegisterRequest("Jane", "Doe", "jane.doe@test.com", "password123");

        // Simulate that the user already exists
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new UserAlreadyExistsException("An account with this email already exists."));

        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("An account with this email already exists."));
    }

    @Test
    void login_shouldReturnOkAndTokens_whenRequestIsValid() throws Exception {
        // ARRANGE
        var request = new LoginRequest("john.doe@test.com", "password123");
        var response = new AuthResponse("fake-access-token", "fake-refresh-token");

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void login_shouldReturnUnauthorized_whenCredentialsAreInvalid() throws Exception {
        // ARRANGE
        var request = new LoginRequest("jane.doe@test.com", "wrongpassword");

        // Simulate that the user already exists
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid email or password."));

        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid email or password."));
    }

    @Test
    void refreshToken_shouldReturnOkAndTokens_whenRequestIsValid() throws Exception {
        // ARRANGE
        var refreshToken = new RefreshToken();
        refreshToken.setToken("fake-refresh-token");

        var request = new RefreshTokenRequest(refreshToken.getToken());
        var response = new AuthResponse("fake-access-token", "fake-refresh-token");

        when(authService.refreshToken(refreshToken.getToken())).thenReturn(response);

        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void refreshToken_shouldReturnUnauthorized_whenRefreshTokenIsInvalid() throws Exception {
        // ARRANGE
        var refreshToken = new RefreshToken();
        refreshToken.setToken("fake-refresh-token");

        var request = new RefreshTokenRequest(refreshToken.getToken());

        when(authService.refreshToken(refreshToken.getToken()))
                .thenThrow(new TokenNotFoundException("Refresh token not found or expired"));

        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Refresh token not found or expired"));
    }

    @Test
    void refreshToken_shouldReturnUnauthorized_whenTokenIsExpired() throws Exception {
        // ARRANGE
        var refreshToken = new RefreshToken();
        refreshToken.setToken("fake-refresh-token");

        var request = new RefreshTokenRequest(refreshToken.getToken());

        when(authService.refreshToken(refreshToken.getToken()))
                .thenThrow(new TokenNotFoundException("Refresh token was expired. Please make a new signin request"));

        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Refresh token was expired. Please make a new signin request"));
    }

    @Test
    void logout_shouldReturnOk_whenRequestIsValid() throws Exception {
        //ARRANGE
        var refreshToken = new RefreshToken();
        refreshToken.setToken("fake-refresh-token");

        var request = new RefreshTokenRequest(refreshToken.getToken());

        doNothing().when(authService).logout(request.refreshToken());

        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authService, times(1)).logout(request.refreshToken());
    }
}
