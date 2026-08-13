package dev.michaelgoldman.journalbackend.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class EntryTest {
    private static final String VALID_TITLE = "A valid title";
    private static final String VALID_CONTENT = "This is an example of some valid content.";
    private static final int TITLE_CHAR_LIMIT = 100;
    private static final int CONTENT_CHAR_LIMIT = 20000;

    @ParameterizedTest(name = "{0}")
    @MethodSource("uncleanValues")
    void whenUncleanTitleOrContentPassed_shouldCleanValue(String name, String clean, String unclean) {
        assertEquals(clean, Entry.of(unclean, VALID_CONTENT).getTitle());
        assertEquals(clean, Entry.of(VALID_TITLE, unclean).getContent());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("blankValues")
    void whenBlankTitleOrContentPassed_shouldThrowIllegalArgumentException(String name, String value) {
        assertThrows(IllegalArgumentException.class, () -> Entry.of(value, VALID_CONTENT));
        assertThrows(IllegalArgumentException.class, () -> Entry.of(VALID_TITLE, value));
    }

    @Test
    void whenTitleExceedsCharLimit_shouldThrowException() {
        String atLimit = "a".repeat(TITLE_CHAR_LIMIT);
        String paddedButValid = " ".repeat(120) + "Clean up the house" + " ".repeat(30);
        String tooLong = "a".repeat(TITLE_CHAR_LIMIT + 1);

        assertDoesNotThrow(() -> Entry.of(atLimit, VALID_CONTENT));
        assertDoesNotThrow(() -> Entry.of(paddedButValid, VALID_CONTENT));
        assertThrows(IllegalArgumentException.class, () -> Entry.of(tooLong, VALID_CONTENT));
    }

    @Test
    void whenContentExceedsCharLimit_shouldThrowException() {
        String atLimit = "a".repeat(CONTENT_CHAR_LIMIT);
        String paddedButValid = " ".repeat(30000) + "Clean up the house" + " ".repeat(30);
        String tooLong = "a".repeat(CONTENT_CHAR_LIMIT + 1);

        assertDoesNotThrow(() -> Entry.of(VALID_TITLE, atLimit));
        assertDoesNotThrow(() -> Entry.of(VALID_TITLE, paddedButValid));
        assertThrows(IllegalArgumentException.class, () -> Entry.of(VALID_TITLE, tooLong));
    }

    @Test
    void whenConstructedWithNonCanonicalValue_shouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> new Entry(VALID_TITLE, "  Clean up the house "));
        assertThrows(IllegalArgumentException.class, () -> new Entry("  Clean up the house ", VALID_CONTENT));
    }

    @Test
    void whenContentHasParagraphs_shouldKeepThem() {
        String prose = "First paragraph.\n\nSecond paragraph.";

        assertEquals(prose, Entry.of(VALID_TITLE, "  " + prose + "  ").getContent());
    }

    @SuppressWarnings("UnnecessaryUnicodeEscape")
    static Stream<Arguments> uncleanValues() {
        return Stream.of(
                arguments(
                        "NFC normalisation",
                        "caf\u00E9",
                        "cafe\u0301"
                ),
                arguments(
                        "Preserves case and inner whitespace, trims outer",
                        "Two  Words With   Caps",
                        "   Two  Words With   Caps   "
                )
        );
    }

    static Stream<Arguments> blankValues() {
        return Stream.of(
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
}
