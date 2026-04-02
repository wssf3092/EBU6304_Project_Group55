package com.group55.ta.service;

import com.group55.ta.dao.UserDao;
import com.group55.ta.model.Role;
import com.group55.ta.model.User;
import com.group55.ta.util.PasswordUtil;
import com.group55.ta.util.ValidationUtil;

import java.util.Optional;

/**
 * Registration and authentication (hashed passwords).
 */
public class AuthService {
    private final UserDao userDao = new UserDao();

    public User register(String name, String email, String password, Role role) {
        if (role == null) {
            throw new IllegalArgumentException("请选择有效角色。");
        }
        if (ValidationUtil.trim(name).length() < 2) {
            throw new IllegalArgumentException("姓名至少 2 个字符。");
        }
        if (!ValidationUtil.isValidEmail(email)) {
            throw new IllegalArgumentException("邮箱格式不正确。");
        }
        if (ValidationUtil.trim(password).length() < 6) {
            throw new IllegalArgumentException("密码至少 6 位。");
        }
        if (userDao.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("该邮箱已注册。");
        }
        return userDao.create(name, email, PasswordUtil.hash(ValidationUtil.trim(password)), role);
    }

    /**
     * Login with email (normalized) or raw userId (e.g. TA_001).
     */
    public User authenticate(String identifier, String password) {
        if (ValidationUtil.isBlank(identifier) || password == null) {
            throw new IllegalArgumentException("邮箱/用户ID 和密码不能为空。");
        }
        String trimmedId = identifier.trim();
        Optional<User> userOpt = userDao.findByEmail(trimmedId);
        if (!userOpt.isPresent()) {
            userOpt = userDao.findById(trimmedId);
        }
        if (!userOpt.isPresent()) {
            throw new IllegalArgumentException("邮箱或密码错误。");
        }
        User user = userOpt.get();
        if (!user.isActive()) {
            throw new IllegalStateException("该账号已被禁用。");
        }
        if (!PasswordUtil.verify(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("邮箱或密码错误。");
        }
        return user;
    }

    public Optional<User> findById(String userId) {
        return userDao.findById(userId);
    }
}
