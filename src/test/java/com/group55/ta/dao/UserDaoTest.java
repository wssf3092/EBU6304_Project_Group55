package com.group55.ta.dao;

import com.group55.ta.model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for UserDao based on TDD expectations.
 */
public class UserDaoTest {

    private UserDao userDao;

    @Before
    public void setUp() {
        // TDD assumption: UserDao can be wired with a specific test data file path
        userDao = new UserDao("data/test_users.txt");
        userDao.clearAll(); // Ensure a clean dataset
    }

    @After
    public void tearDown() {
        userDao.clearAll();
    }

    @Test
    public void testSaveAndFindByUsername() {
        User user = new User("teststudent", "password123", "Student", "Test Student", "test@university.edu");
        
        boolean saveResult = userDao.save(user);
        assertTrue("User should be saved successfully", saveResult);

        User retrievedUser = userDao.findByUsername("teststudent");
        assertNotNull("Should find the user uniquely by username", retrievedUser);
        assertEquals("Usernames should exactly match", "teststudent", retrievedUser.getUsername());
        assertEquals("Stored roles should match", "Student", retrievedUser.getRole());
    }

    @Test
    public void testAuthenticateSuccess() {
        User user = new User("authuser", "correctpass", "Teacher", "Auth Teacher", "auth@university.edu");
        userDao.save(user);

        User authenticatedUser = userDao.authenticate("authuser", "correctpass");
        assertNotNull("Authentication should yield user object with correct credentials", authenticatedUser);
        assertEquals("authuser", authenticatedUser.getUsername());
    }

    @Test
    public void testAuthenticateFailure() {
        User user = new User("failuser", "correctpass", "Student", "Fail Student", "fail@university.edu");
        userDao.save(user);

        User wrongPassUser = userDao.authenticate("failuser", "wrongpass");
        assertNull("Authentication should prevent access with incorrect password", wrongPassUser);

        User nonExistent = userDao.authenticate("nobody", "pass");
        assertNull("Authentication should fail entirely for a non-existent account", nonExistent);
    }

    @Test
    public void testFindAll() {
        userDao.save(new User("user1", "pass1", "Student", "Name1", "email1"));
        userDao.save(new User("user2", "pass2", "Teacher", "Name2", "email2"));

        List<User> users = userDao.findAll();
        assertNotNull("User list should not be null", users);
        assertEquals("Should retrieve correctly 2 seeded users", 2, users.size());
    }
}
