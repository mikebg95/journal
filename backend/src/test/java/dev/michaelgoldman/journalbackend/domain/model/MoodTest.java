package dev.michaelgoldman.journalbackend.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class MoodTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("recognizableValues")
    void whenValueIsRecognizable_shouldReturnMatchingMood(String name, Mood expected, String raw) {
        assertEquals(Optional.of(expected), Mood.from(raw));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("blankOrUnrecognizableValues")
    void whenValueIsBlankOrUnrecognizable_shouldReturnEmptyOptional(String name, String value) {
        assertEquals(Optional.empty(), Mood.from(value));
    }

    static Stream<Arguments> recognizableValues() {
        return Stream.of(
                arguments("Case normalisation", Mood.HAPPY, "happy"),
                arguments("Trim start and end", Mood.FRUSTRATED, "   FRUSTRATED  "));
    }

    static Stream<Arguments> blankOrUnrecognizableValues() {
        return Stream.of(
                arguments("Null text", null),
                arguments("Empty string", ""),
                arguments("Whitespaces", "    "),
                arguments("Tab", "\t"),
                arguments("Unrecognizable", "ECSTATIC"),
                arguments("Almost a mood", "VERY HAPPY"));
    }
}
