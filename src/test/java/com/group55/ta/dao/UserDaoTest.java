package com.group55.ta.dao;

import com.group55.ta.model.Role;
import com.group55.ta.model.User;
import com.group55.ta.util.AppPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserDao JSON persistence tests (isolated {@code data} root).
 */
class UserDaoTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        AppPaths.overrideDataRoot(tempDir);
    }

    @Test
    void createAndFindByEmail() {
        UserDao dao = new UserDao();
        User u = dao.create("Test TA", "ta@test.edu", "secret", Role.TA);

        assertNotNull(u.getUserId());
        assertTrue(u.getUserId().startsWith("TA_"));
        Optional<User> found = dao.findByEmail("TA@Test.Edu");
        assertTrue(found.isPresent());
        assertEquals(u.getUserId(), found.get().getUserId());
    }

    @Test
    void authenticateSuccess() {
        UserDao dao = new UserDao();
        dao.create("Auth User", "auth@test.edu", "correctpass", Role.MO);

        Optional<User> u = dao.authenticate("auth@test.edu", "correctpass");
        assertTrue(u.isPresent());
        assertEquals("MO", u.get().getRole());
    }

    @Test
    void authenticateFailure() {
        UserDao dao = new UserDao();
        dao.create("X", "x@test.edu", "pw", Role.TA);

        assertTrue(dao.authenticate("x@test.edu", "wrong").isEmpty());
        assertTrue(dao.authenticate("nobody@test.edu", "pw").isEmpty());
    }

    @Test
    void findByIdAndListAll() {
        UserDao dao = new UserDao();
        User a = dao.create("A", "a@test.edu", "p", Role.TA);
        User b = dao.create("B", "b@test.edu", "p", Role.MO);

        assertTrue(dao.findById(a.getUserId()).isPresent());
        List<User> all = dao.listAll();
        assertEquals(2, all.size());
    }
}
