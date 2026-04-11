package com.group55.ta.service;

import com.group55.ta.dao.UserDao;
import com.group55.ta.model.Role;
import com.group55.ta.model.User;
import com.group55.ta.util.PasswordUtil;
import com.group55.ta.util.ValidationUtil;

import java.util.Optional;

/**
 * Authentication and registration logic.
 */
public class AuthService {
    private final UserDao userDao = new UserDao();

    public User register(String name, String email, String password, String roleValue) {
        Role role = Role.fromString(roleValue);
        if (role == null) {
            throw new IllegalArgumentException("Choose a valid account role.");
        }
        if (ValidationUtil.trim(name).length() < 2) {
            throw new IllegalArgumentException("Enter a full name with at least 2 characters.");
        }
        if (!ValidationUtil.isValidEmail(email)) {
            throw new IllegalArgumentException("Enter a valid email address.");
        }
        if (ValidationUtil.trim(password).length() < 6) {
            throw new IllegalArgumentException("Password must contain at least 6 characters.");
        }
        return userDao.create(name, email, PasswordUtil.sha256(password), role);
    }

    public User authenticate(String email, String password) {
        Optional<User> userOpt = userDao.findByEmail(email);
        if (!userOpt.isPresent()) {
            throw new IllegalArgumentException("Invalid email or password.");
        }
        User user = userOpt.get();
        String hashed = PasswordUtil.sha256(ValidationUtil.trim(password));
        if (!hashed.equals(user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }
        if (!user.isActive()) {
            throw new IllegalStateException("This account is disabled.");
        }
        return user;
    }

    public Optional<User> findById(String userId) {
        return userDao.findById(userId);
    }
}
