package com.group55.ta.util;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Utility methods for JSON HTTP responses.
 */
public final class JsonResponseUtil {
    private JsonResponseUtil() {
    }

    /**
     * Serializes {@code payload} with {@link com.group55.ta.util.GsonProvider} as UTF-8 {@code application/json}.
     */
    public static void write(HttpServletResponse response, Object payload) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(GsonProvider.gson().toJson(payload));
    }
}
