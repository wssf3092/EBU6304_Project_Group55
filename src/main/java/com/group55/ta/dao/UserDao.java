package com.group55.ta.dao;

import com.group55.ta.model.Role;
import com.group55.ta.model.User;
import com.group55.ta.util.AppPaths;
import com.group55.ta.util.DateTimeUtil;
import com.group55.ta.util.JsonFileUtil;
import com.group55.ta.util.ValidationUtil;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * User accounts stored as {@code data/users/{role}/{userId}.json}.
 */
public class UserDao {
    private static final Object LOCK = new Object();

    public Optional<User> findByEmail(String email) {
        String normalized = ValidationUtil.normalizeEmail(email);
        return listAll().stream()
                .filter(user -> normalized.equals(ValidationUtil.normalizeEmail(user.getEmail())))
                .findFirst();
    }

    public Optional<User> findById(String userId) {
        if (ValidationUtil.isBlank(userId)) {
            return Optional.empty();
        }
        for (Role role : Role.values()) {
            Path file = AppPaths.users(role).resolve(userId + ".json");
            Optional<User> user = JsonFileUtil.read(file, User.class);
            if (user.isPresent()) {
                return user;
            }
        }
        return Optional.empty();
    }

    public List<User> listByRole(Role role) {
        synchronized (LOCK) {
            return JsonFileUtil.readAll(AppPaths.users(role), User.class);
        }
    }

    public List<User> listAll() {
        synchronized (LOCK) {
            List<User> users = new ArrayList<>();
            for (Role role : Role.values()) {
                users.addAll(JsonFileUtil.readAll(AppPaths.users(role), User.class));
            }
            users.sort(Comparator.comparing(User::getCreatedAt, Comparator.nullsLast(String::compareTo)));
            return users;
        }
    }

    public User create(String name, String email, String passwordHash, Role role) {
        synchronized (LOCK) {
            Optional<User> existing = findByEmail(email);
            if (existing.isPresent()) {
                throw new IllegalStateException("Email already registered.");
            }
            User user = new User();
            user.setUserId(nextUserId(role));
            user.setName(ValidationUtil.trim(name));
            user.setEmail(ValidationUtil.normalizeEmail(email));
            user.setPasswordHash(passwordHash);
            user.setRole(role.name());
            user.setActive(true);
            user.setCreatedAt(DateTimeUtil.nowIso());
            saveInternal(user, role);
            return user;
        }
    }

    public void update(User user) {
        synchronized (LOCK) {
            Role r = Role.fromString(user.getRole());
            if (r == null) {
                throw new IllegalStateException("Invalid role.");
            }
            saveInternal(user, r);
        }
    }

    public boolean setActive(String userId, boolean active) {
        synchronized (LOCK) {
            Optional<User> target = findById(userId);
            if (!target.isPresent()) {
                return false;
            }
            User user = target.get();
            user.setActive(active);
            saveInternal(user, Role.fromString(user.getRole()));
            return true;
        }
    }

    private void saveInternal(User user, Role role) {
        Path file = AppPaths.users(role).resolve(user.getUserId() + ".json");
        JsonFileUtil.write(file, user);
    }

    private String nextUserId(Role role) {
        List<User> users = listByRole(role);
        String prefix = role.getIdPrefix();
        int max = 0;
        for (User user : users) {
            String userId = user.getUserId();
            if (userId == null || !userId.startsWith(prefix + "_")) {
                continue;
            }
            String suffix = userId.substring(prefix.length() + 1);
            try {
                max = Math.max(max, Integer.parseInt(suffix));
            } catch (NumberFormatException ignored) {
                // skip
            }
        }
        return prefix + "_" + String.format("%03d", max + 1);
    }
}
