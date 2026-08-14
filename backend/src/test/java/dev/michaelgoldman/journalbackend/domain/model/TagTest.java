package dev.michaelgoldman.journalbackend.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class TagTest {
    private static final int TAG_CHAR_LIMIT = 50;

    @Test
    void whenTagHasCapitals_shouldLowerCase() {
        assertEquals("gym", Tag.of("GYM").orElseThrow().value());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   ", "\t"})
    void whenBlankTagPassed_shouldReturnEmptyOptional(String passed) {
        assertTrue(Tag.of(passed).isEmpty());
    }

    @Test
    void whenTagExceedsCharLimit_shouldReturnEmptyOptional() {
        String atLimit = "a".repeat(TAG_CHAR_LIMIT);
        String tooLong = "a".repeat(TAG_CHAR_LIMIT + 1);
        String paddedButValid = " ".repeat(100) + "work" + " ".repeat(20);

        assertTrue(Tag.of(atLimit).isPresent());
        assertTrue(Tag.of(paddedButValid).isPresent());
        assertTrue(Tag.of(tooLong).isEmpty());
    }

    @Test
    void whenTagsShareCanonicalValue_shouldBeEqual() {
        assertEquals(Tag.of("clean up"), Tag.of("  ClEAn   \tUP "));
    }

    @Test
    void whenConstructedWithNonCanonicalValue_shouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> new Tag("  GYM "));
    }
}
