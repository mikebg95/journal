package dev.michaelgoldman.journalbackend.domain.model;

import org.jspecify.annotations.Nullable;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public record Enrichment(@Nullable String summary, Set<Tag> tags, List<Todo> todos, @Nullable Mood mood) {

    private static final Pattern WHITESPACE_RUN = Pattern.compile("\\s+");
    private static final int SUMMARY_CHAR_LIMIT = 500;
    private static final int TAGS_LIMIT = 10;
    private static final int TODOS_LIMIT = 20;

    public Enrichment {
        if (summary != null && !summary.equals(cleanSummary(summary))) {
            throw new IllegalArgumentException("Enrichment summary is not canonical");
        }
        if (tags == null || tags.size() > TAGS_LIMIT) {
            throw new IllegalArgumentException("Enrichment tags are not canonical");
        }
        if (todos == null || todos.size() > TODOS_LIMIT || Set.copyOf(todos).size() != todos.size()) {
            throw new IllegalArgumentException("Enrichment todos are not canonical");
        }

        tags = Set.copyOf(tags);
        todos = List.copyOf(todos);
    }

    public static Enrichment of(@Nullable String uncleanSummary, @Nullable List<String> uncleanTags, @Nullable List<String> uncleanTodos, @Nullable String uncleanMood) {
        String summary = cleanSummary(uncleanSummary);
        Set<Tag> tags = cleanTags(uncleanTags);
        List<Todo> todos = cleanTodos(uncleanTodos);
        Mood mood = cleanMood(uncleanMood);

        return new Enrichment(summary, tags, todos, mood);
    }

    private static @Nullable String cleanSummary(@Nullable String uncleanSummary) {
        if (uncleanSummary == null) {
            return null;
        }

        String cleaned = WHITESPACE_RUN.matcher(
                        Normalizer.normalize(uncleanSummary, Normalizer.Form.NFC).strip())
                .replaceAll(" ");

        if (cleaned.isBlank() || cleaned.length() > SUMMARY_CHAR_LIMIT) {
            return null;
        }

        return cleaned;
    }

    private static Set<Tag> cleanTags(@Nullable List<String> uncleanTags) {
        if (uncleanTags == null) {
            return Set.of();
        }

        Set<Tag> tags = new LinkedHashSet<>();

        for (String unclean : uncleanTags) {
            Optional<Tag> optionalTag = Tag.of(unclean);
            optionalTag.ifPresent(tags::add);
            if (tags.size() == TAGS_LIMIT) {
                break;
            }
        }

        return tags;
    }

    private static List<Todo> cleanTodos(@Nullable List<String> uncleanTodos) {
        if (uncleanTodos == null) {
            return List.of();
        }

        List<Todo> todos = new ArrayList<>();

        for (String unclean : uncleanTodos) {
            Optional<Todo> optionalTodo = Todo.of(unclean);

            if (optionalTodo.isPresent() && !todos.contains(optionalTodo.get())) {
                todos.add(optionalTodo.get());
            }

            if (todos.size() == TODOS_LIMIT) {
                break;
            }
        }

        return todos;
    }

    private static @Nullable Mood cleanMood(@Nullable String uncleanMood) {
        return Mood.from(uncleanMood).orElse(null);
    }
}
