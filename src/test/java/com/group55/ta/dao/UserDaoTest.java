package com.group55.ta.dao;

import com.group55.ta.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UserDao based on TDD expectations.
 */
class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setUp() {
        userDao = new UserDao("data/test_users.txt");
        userDao.clearAll();
    }

    @AfterEach
    void tearDown() {
        userDao.clearAll();
    }

    @Test
    void testSaveAndFindByUsername() {
        User user = new User("teststudent", "password123", "Student", "Test Student", "test@university.edu");

        boolean saveResult = userDao.save(user);
        assertTrue(saveResult, "User should be saved successfully");

        User retrievedUser = userDao.findByUsername("teststudent");
        assertNotNull(retrievedUser, "Should find the user uniquely by username");
        assertEquals("teststudent", retrievedUser.getUsername(), "Usernames should exactly match");
        assertEquals("Student", retrievedUser.getRole(), "Stored roles should match");
    }

    @Test
    void testAuthenticateSuccess() {
        User user = new User("authuser", "correctpass", "Teacher", "Auth Teacher", "auth@university.edu");
        userDao.save(user);

        User authenticatedUser = userDao.authenticate("authuser", "correctpass");
        assertNotNull(authenticatedUser, "Authentication should yield user object with correct credentials");
        assertEquals("authuser", authenticatedUser.getUsername());
    }

    @Test
    void testAuthenticateFailure() {
        User user = new User("failuser", "correctpass", "Student", "Fail Student", "fail@university.edu");
        userDao.save(user);

        User wrongPassUser = userDao.authenticate("failuser", "wrongpass");
        assertNull(wrongPassUser, "Authentication should prevent access with incorrect password");

        User nonExistent = userDao.authenticate("nobody", "pass");
        assertNull(nonExistent, "Authentication should fail entirely for a non-existent account");
    }

    @Test
    void testFindAll() {
        userDao.save(new User("user1", "pass1", "Student", "Name1", "email1"));
        userDao.save(new User("user2", "pass2", "Teacher", "Name2", "email2"));

        List<User> users = userDao.findAll();
        assertNotNull(users, "User list should not be null");
        assertEquals(2, users.size(), "Should retrieve correctly 2 seeded users");
    }
}
