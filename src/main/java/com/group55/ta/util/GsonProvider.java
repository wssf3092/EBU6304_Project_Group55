package com.group55.ta.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Shared Gson instance provider.
 */
public final class GsonProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private GsonProvider() {
    }

    public static Gson gson() {
        return GSON;
    }
}
