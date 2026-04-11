package com.group55.ta.service;

import com.group55.ta.model.User;
import com.group55.ta.util.AppPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthServiceTest {
    @TempDir
    Path tempDir;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        AppPaths.overrideDataRoot(tempDir.resolve("data"));
        authService = new AuthService();
    }

    @Test
    void registerAndAuthenticateRoundTrip() {
        User created = authService.register("Taylor Applicant", "taylor@example.com", "Pass1234", "TA");
        User authenticated = authService.authenticate("taylor@example.com", "Pass1234");

        assertNotNull(created.getUserId());
        assertEquals(created.getUserId(), authenticated.getUserId());
        assertEquals("TA", authenticated.getRole());
    }

    @Test
    void rejectInvalidPassword() {
        authService.register("Morgan Organiser", "morgan@example.com", "Pass1234", "MO");
        assertThrows(IllegalArgumentException.class, () -> authService.authenticate("morgan@example.com", "wrong-pass"));
    }
}
