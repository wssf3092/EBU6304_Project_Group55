package com.group55.ta.service;

import com.group55.ta.dao.UserDao;
import com.group55.ta.model.Role;
import com.group55.ta.model.User;
import com.group55.ta.util.AppPaths;
import com.group55.ta.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    @TempDir
    Path tempDir;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        AppPaths.overrideDataRoot(tempDir);
        authService = new AuthService();
    }

    @Test
    void registerThenAuthenticateByEmailAndByUserId() {
        User registered = authService.register("Bob", "bob@test.edu", "passw0rd", Role.TA);
        String stored = new UserDao().findById(registered.getUserId()).orElseThrow().getPasswordHash();
        assertEquals(PasswordUtil.hash("passw0rd"), stored);

        User byEmail = authService.authenticate("bob@test.edu", "passw0rd");
        assertEquals(registered.getUserId(), byEmail.getUserId());

        User byId = authService.authenticate(registered.getUserId(), "passw0rd");
        assertEquals(registered.getUserId(), byId.getUserId());
    }

    @Test
    void registerDuplicateEmail() {
        authService.register("A", "same@test.edu", "secret12", Role.TA);
        assertThrows(IllegalArgumentException.class,
                () -> authService.register("B", "same@test.edu", "secret12", Role.MO));
    }

    @Test
    void authenticateWrongPassword() {
        authService.register("C", "c@test.edu", "rightpass", Role.TA);
        assertThrows(IllegalArgumentException.class, () -> authService.authenticate("c@test.edu", "wrong"));
    }

    @Test
    void authenticateLegacyPlaintextPasswordHash() {
        UserDao dao = new UserDao();
        User legacy = dao.create("Legacy", "leg@test.edu", "plain-old", Role.TA);
        assertEquals("plain-old", legacy.getPasswordHash());

        User loggedIn = authService.authenticate("leg@test.edu", "plain-old");
        assertEquals(legacy.getUserId(), loggedIn.getUserId());
    }
}
