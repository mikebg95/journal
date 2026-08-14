package dev.michaelgoldman.journalbackend.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class EnrichmentTest {

    private static final String VALID_SUMMARY = "This is a valid summary.";
    private static final List<String> VALID_TAGS = Collections.emptyList();
    private static final List<String> VALID_TODOS = Collections.emptyList();
    private static final String VALID_MOOD = "HAPPY";
    private static final int SUMMARY_CHAR_LIMIT = 500;
    private static final Set<Tag> ELEVEN_TAGS_SET = Set.of(
            new Tag("0"),
            new Tag("1"),
            new Tag("2"),
            new Tag("3"),
            new Tag("4"),
            new Tag("5"),
            new Tag("6"),
            new Tag("7"),
            new Tag("8"),
            new Tag("9"),
            new Tag("10"));
    private static final List<Todo> TWENTY_ONE_TODOS_LIST = List.of(
            new Todo("0"),
            new Todo("1"),
            new Todo("2"),
            new Todo("3"),
            new Todo("4"),
            new Todo("5"),
            new Todo("6"),
            new Todo("7"),
            new Todo("8"),
            new Todo("9"),
            new Todo("10"),
            new Todo("11"),
            new Todo("12"),
            new Todo("13"),
            new Todo("14"),
            new Todo("15"),
            new Todo("16"),
            new Todo("17"),
            new Todo("18"),
            new Todo("19"),
            new Todo("20"));

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   ", "\t"})
    void whenBlankSummaryPassed_shouldReturnNullSummary(String value) {
        assertNull(Enrichment.of(value, VALID_TAGS, VALID_TODOS, VALID_MOOD).summary());
    }

    @Test
    void whenSummaryAboveCharLimit_shouldReturnNullSummary() {
        String atLimit = "a".repeat(SUMMARY_CHAR_LIMIT);
        String paddedButValid = " ".repeat(600) + "a".repeat(50) + "\t" + "a".repeat(35);
        String tooLong = ("a").repeat(SUMMARY_CHAR_LIMIT + 1);

        assertNotNull(
                Enrichment.of(atLimit, VALID_TAGS, VALID_TODOS, VALID_MOOD).summary());
        assertNotNull(Enrichment.of(paddedButValid, VALID_TAGS, VALID_TODOS, VALID_MOOD)
                .summary());
        assertNull(Enrichment.of(tooLong, VALID_TAGS, VALID_TODOS, VALID_MOOD).summary());
    }

    @Test
    void whenPassingBlankTagValues_shouldReturnSetWithoutBlank() {
        List<String> listWithBlankTags = List.of("0", "   ", "1", "2", "", "3", "\t", "4");
        Set<Tag> tags = Enrichment.of(VALID_SUMMARY, listWithBlankTags, VALID_TODOS, VALID_MOOD)
                .tags();

        assertEquals(Set.of(new Tag("0"), new Tag("1"), new Tag("2"), new Tag("3"), new Tag("4")), tags);
    }

    @Test
    void whenTagsAreMissing_shouldReturnEmptySet() {
        Set<Tag> tags =
                Enrichment.of(VALID_SUMMARY, null, VALID_TODOS, VALID_MOOD).tags();

        assertTrue(tags.isEmpty());
    }

    @Test
    void whenDuplicateTags_duplicatesShouldNotUseUpTheLimit() {
        List<String> tooLongListWithDuplicateTags =
                List.of("0", "1", "1", "1", "2", "2", "3", "4", "4", "5", "6", "7", "8", "9", "10", "11");
        Set<Tag> tags = Enrichment.of(VALID_SUMMARY, tooLongListWithDuplicateTags, VALID_TODOS, VALID_MOOD)
                .tags();

        assertEquals(
                Set.of(
                        new Tag("0"),
                        new Tag("1"),
                        new Tag("2"),
                        new Tag("3"),
                        new Tag("4"),
                        new Tag("5"),
                        new Tag("6"),
                        new Tag("7"),
                        new Tag("8"),
                        new Tag("9")),
                tags);
    }

    @Test
    void whenPassingBlankTodoValues_shouldReturnListWithoutBlank() {
        List<String> listWithBlankTodos = List.of("0", "   ", "1", "2", "", "3", "\t", "4");
        List<Todo> todos = Enrichment.of(VALID_SUMMARY, VALID_TAGS, listWithBlankTodos, VALID_MOOD)
                .todos();

        assertEquals(List.of(new Todo("0"), new Todo("1"), new Todo("2"), new Todo("3"), new Todo("4")), todos);
    }

    @Test
    void whenTodosAreMissing_shouldReturnEmptyList() {
        assertTrue(Enrichment.of(VALID_SUMMARY, VALID_TAGS, null, VALID_MOOD)
                .todos()
                .isEmpty());
    }

    @Test
    void whenDuplicateTodos_duplicatesShouldNotUseUpTheLimit() {
        List<String> tooLongListWithDuplicateTodos = List.of(
                "0", "0", "1", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16",
                "17", "18", "19", "20", "21");

        List<Todo> todos = Enrichment.of(VALID_SUMMARY, VALID_TAGS, tooLongListWithDuplicateTodos, VALID_MOOD)
                .todos();

        assertEquals(
                List.of(
                        new Todo("0"),
                        new Todo("1"),
                        new Todo("2"),
                        new Todo("3"),
                        new Todo("4"),
                        new Todo("5"),
                        new Todo("6"),
                        new Todo("7"),
                        new Todo("8"),
                        new Todo("9"),
                        new Todo("10"),
                        new Todo("11"),
                        new Todo("12"),
                        new Todo("13"),
                        new Todo("14"),
                        new Todo("15"),
                        new Todo("16"),
                        new Todo("17"),
                        new Todo("18"),
                        new Todo("19")),
                todos);
    }

    @Test
    void whenConstructedWithNonCanonicalValue_shouldThrow() {
        assertThrows(
                IllegalArgumentException.class, () -> new Enrichment("  untrimmed  ", Set.of(), List.of(), Mood.HAPPY));
        assertThrows(
                IllegalArgumentException.class,
                () -> new Enrichment("a".repeat(SUMMARY_CHAR_LIMIT + 1), Set.of(), List.of(), Mood.HAPPY));
        assertThrows(
                IllegalArgumentException.class,
                () -> new Enrichment(VALID_SUMMARY, ELEVEN_TAGS_SET, List.of(), Mood.HAPPY));
        assertThrows(
                IllegalArgumentException.class,
                () -> new Enrichment(VALID_SUMMARY, Set.of(), TWENTY_ONE_TODOS_LIST, Mood.HAPPY));
        assertThrows(
                IllegalArgumentException.class,
                () -> new Enrichment(VALID_SUMMARY, Set.of(), List.of(new Todo("0"), new Todo("0")), Mood.HAPPY));
    }
}
