package org.acme.common;

public final class ConstraintIdSanitizer {

    private ConstraintIdSanitizer() {
    }

    public static String sanitize(String id) {
        if (id == null) {
            return null;
        }
        // Keep alphanumeric, space, underscore, hyphen, apostrophe, parentheses, dot.
        // Replace any other character with a hyphen.
        return id.replaceAll("[^A-Za-z0-9 _'()\\.\\-]", "-");
    }
}
