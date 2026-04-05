package com.group55.ta.dao;

import com.group55.ta.model.Role;
import com.group55.ta.model.User;
import com.group55.ta.util.AppPaths;
import com.group55.ta.util.PasswordUtil;
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

    private UserDao userDao;

    @BeforeEach
    void setUp() {
        AppPaths.overrideDataRoot(tempDir);
        userDao = new UserDao();
    }

    @Test
    void createFindByEmailAndId() {
        User u = userDao.create("Alice", "Alice@Example.COM", PasswordUtil.hash("secret12"), Role.TA);

        assertNotNull(u.getUserId());
        assertTrue(u.getUserId().startsWith("TA_"));
        assertEquals("alice@example.com", u.getEmail());

        Optional<User> byEmail = userDao.findByEmail("ALICE@example.com");
        assertTrue(byEmail.isPresent());
        assertEquals(u.getUserId(), byEmail.get().getUserId());

        Optional<User> byId = userDao.findById(u.getUserId());
        assertTrue(byId.isPresent());
        assertEquals("Alice", byId.get().getName());
    }

    @Test
    void duplicateEmailOnCreate() {
        userDao.create("A", "dup@test.edu", "h", Role.TA);
        assertThrows(IllegalStateException.class, () -> userDao.create("B", "dup@test.edu", "h2", Role.MO));
    }

    @Test
    void listAllAndUpdate() {
        userDao.create("One", "o1@test.edu", "x", Role.TA);
        userDao.create("Two", "o2@test.edu", "y", Role.MO);

        List<User> all = userDao.listAll();
        assertEquals(2, all.size());

        User first = userDao.findByEmail("o1@test.edu").orElseThrow();
        first.setName("OneRenamed");
        userDao.update(first);

        assertEquals("OneRenamed", userDao.findById(first.getUserId()).orElseThrow().getName());
    }

    @Test
    void setActive() {
        User u = userDao.create("X", "x@test.edu", "h", Role.ADMIN);
        assertTrue(userDao.setActive(u.getUserId(), false));
        assertFalse(userDao.findById(u.getUserId()).orElseThrow().isActive());
    }
}
