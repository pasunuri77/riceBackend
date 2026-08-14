package com.rice.controller;

import com.rice.dto.auth.UserResponse;
import com.rice.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserProfilePhotoControllerTest {

    private AuthService authService;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        UserController userController = new UserController(authService);
        mvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    void userCanUploadPhotoAndGetPhotoUrl() throws Exception {
        UserResponse response = UserResponse.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .photoUrl("https://cdn.example.com/user.png")
                .build();
        when(authService.uploadPhoto(any(), any())).thenReturn(response);

        MockMultipartFile file = new MockMultipartFile("file", "photo.png", MediaType.IMAGE_PNG_VALUE, "img".getBytes());
        mvc.perform(multipart("/api/users/me/photo")
                        .file(file)
                        .principal(new UsernamePasswordAuthenticationToken("user@example.com", "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.photoUrl").value("https://cdn.example.com/user.png"));

        verify(authService).uploadPhoto("user@example.com", file);
    }

    @Test
    void userCanDeletePhotoAndGetUpdatedProfile() throws Exception {
        UserResponse response = UserResponse.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .photoUrl(null)
                .build();
        when(authService.deletePhoto("user@example.com")).thenReturn(response);

        mvc.perform(delete("/api/users/me/photo")
                        .principal(new UsernamePasswordAuthenticationToken("user@example.com", "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.photoUrl").isEmpty());

        verify(authService).deletePhoto("user@example.com");
    }
}
