package dev.michaelgoldman.journalbackend.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class TagTest {
    private static final int TAG_CHAR_LIMIT = 50;

    @ParameterizedTest(name = "{0}")
    @MethodSource("uncleanValues")
    void whenUncleanTagPassed_shouldCleanTag(String name, String clean, String unclean) {
        assertEquals(clean, Tag.of(unclean).orElseThrow().value());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("blankValues")
    void whenBlankTagPassed_shouldReturnEmptyOptional(String name, String passed) {
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

    static Stream<Arguments> blankValues() {
        return Stream.of(
                arguments(
                        "Null value",
                        null
                ),
                arguments(
                        "Empty string",
                        ""
                ),
                arguments(
                        "Whitespaces",
                        "    "
                ),
                arguments(
                        "Tab",
                        "\t"
                )
        );
    }

    static Stream<Arguments> uncleanValues() {
        return Stream.of(
                arguments(
                        "NFC normalisation",
                        "café",
                        "cafe\u0301"
                ),
                arguments(
                        "Trim start and end",
                        "work",
                        "   work   "
                ),
                arguments(
                        "Collapse inner whitespace (multiple spaces)",
                        "clean up",
                        "clean     up"
                ),
                arguments(
                        "Collapse inner whitespace (tab)",
                        "clean up",
                        "clean\tup"
                ),
                arguments(
                        "Case normalisation",
                        "gym",
                        "GYM"
                )
        );
    }
}