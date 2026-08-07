package org.acme.common;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ConstraintIdSanitizerTest {

    @Test
    public void testSanitizeWithValidCharacters() {
        String input = "Valid Constraint ID with-numbers_123 and (parentheses) and apostrophe's and period.";
        String result = ConstraintIdSanitizer.sanitize(input);
        assertNotNull(result);
        assertTrue(result.matches("[A-Za-z0-9 _'()\\-\\.]*"), "Sanitized ID contains invalid characters: " + result);
    }

    @Test
    public void testSanitizeWithColons() {
        String input = "Goal: target shifts per employee per week (HARD)";
        String result = ConstraintIdSanitizer.sanitize(input);
        assertNotNull(result);
        assertFalse(result.contains(":"));
        assertEquals("Goal- target shifts per employee per week (HARD)", result);
        assertTrue(result.matches("[A-Za-z0-9 _'()\\-\\.]*"));
    }

    @Test
    public void testSanitizeWithCommas() {
        String input = "Constraint, with, commas";
        String result = ConstraintIdSanitizer.sanitize(input);
        assertNotNull(result);
        assertFalse(result.contains(","));
        assertTrue(result.matches("[A-Za-z0-9 _'()\\-\\.]*"));
    }

    @Test
    public void testSanitizeWithSpecialCharacters() {
        String input = "Constraint[with]special{chars}/and\\backslash";
        String result = ConstraintIdSanitizer.sanitize(input);
        assertNotNull(result);
        assertFalse(result.contains("["));
        assertFalse(result.contains("]"));
        assertFalse(result.contains("{"));
        assertFalse(result.contains("}"));
        assertFalse(result.contains("/"));
        assertFalse(result.contains("\\"));
        assertTrue(result.matches("[A-Za-z0-9 _'()\\-\\.]*"));
    }

    @Test
    public void testSanitizeNull() {
        assertNull(ConstraintIdSanitizer.sanitize(null));
    }

    @Test
    public void testSanitizeEmpty() {
        String result = ConstraintIdSanitizer.sanitize("");
        assertNotNull(result);
        assertEquals("", result);
    }
}
