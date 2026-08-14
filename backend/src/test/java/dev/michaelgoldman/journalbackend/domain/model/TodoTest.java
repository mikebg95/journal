package dev.michaelgoldman.journalbackend.domain.model;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class TodoTest {
    private static final int TODO_CHAR_LIMIT = 1000;

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   ", "\t"})
    void whenBlankTodoPassed_shouldReturnEmptyOptional(String passed) {
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
}
