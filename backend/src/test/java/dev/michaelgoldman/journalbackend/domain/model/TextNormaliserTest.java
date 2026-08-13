package dev.michaelgoldman.journalbackend.domain.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class TextNormaliserTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("uncleanValues")
    void whenUncleanValuePassed_shouldCleanValue(String name, String clean, String unclean) {
        assertEquals(clean, TextNormaliser.toSingleLine(unclean));
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
                )
        );
    }
}
