package com.backend.demo.utils;

public class StringUtils {
    public static String slugify(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }

        // Convert to lowercase
        String result = input.toLowerCase();

        // Replace all whitespace with underscores
        result = result.replaceAll("\\s+", "_");

        // Remove all special characters except underscores
        result = result.replaceAll("[^a-z0-9_]", "");

        // Remove multiple consecutive underscores
        result = result.replaceAll("_+", "_");

        // Remove leading and trailing underscores
        result = result.replaceAll("^_+|_+$", "");

        return result;
    }
}
