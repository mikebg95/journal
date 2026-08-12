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

class TodoTest {
    private static final int TODO_CHAR_LIMIT = 1000;

    @ParameterizedTest(name = "{0}")
    @MethodSource("uncleanValues")
    void whenUncleanTodoPassed_shouldCleanTodo(String name, String clean, String unclean) {
        assertEquals(clean, Todo.of(unclean).orElseThrow().text());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("blankValues")
    void whenBlankTodoPassed_shouldReturnEmptyOptional(String name, String passed) {
        assertTrue(Todo.of(passed).isEmpty());
    }

    @Test
    void whenTodoExceedsCharLimit_shouldReturnEmptyOptional() {
        String atLimit = "a".repeat(TODO_CHAR_LIMIT);
        String tooLong = "a".repeat(TODO_CHAR_LIMIT + 1);
        String paddedButValid = " ".repeat(1050) + "Clean up the house" + " ".repeat(30);

        assertTrue(Todo.of(atLimit).isPresent());
        assertTrue(Todo.of(paddedButValid).isPresent());
        assertTrue(Todo.of(tooLong).isEmpty());
    }

    @Test
    void whenConstructedWithNonCanonicalValue_shouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> new Todo("  Clean up the house "));
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
                        "Trim start and end",
                        "Go to work",
                        "   Go to work   "
                ),
                arguments(
                        "Collapse inner whitespace (multiple spaces)",
                        "Clean up the house",
                        "Clean     up  the    house"
                ),
                arguments(
                        "Collapse inner whitespace (tab)",
                        "Clean up the house",
                        "Clean  \t  up    the \thouse"
                )
        );
    }

    static Stream<Arguments> blankValues() {
        return Stream.of(
                arguments(
                        "Null text",
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
}
