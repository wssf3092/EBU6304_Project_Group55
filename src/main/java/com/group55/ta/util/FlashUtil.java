package com.group55.ta.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Flash messages: session → request on next render, then cleared (Step 5).
 */
public final class FlashUtil {
    public static final String SUCCESS_KEY = "flashSuccess";
    public static final String ERROR_KEY = "flashError";

    private FlashUtil() {
    }

    public static void success(HttpServletRequest request, String message) {
        put(request, SUCCESS_KEY, message);
    }

    public static void error(HttpServletRequest request, String message) {
        put(request, ERROR_KEY, message);
    }

    /**
     * Moves flash keys from session to request attributes (one-shot display).
     */
    public static void expose(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        move(session, request, SUCCESS_KEY);
        move(session, request, ERROR_KEY);
    }

    private static void put(HttpServletRequest request, String key, String message) {
        if (request == null || ValidationUtil.isBlank(message)) {
            return;
        }
        request.getSession(true).setAttribute(key, message);
    }

    private static void move(HttpSession session, HttpServletRequest request, String key) {
        Object value = session.getAttribute(key);
        if (value != null) {
            request.setAttribute(key, value);
            session.removeAttribute(key);
        }
    }
}
