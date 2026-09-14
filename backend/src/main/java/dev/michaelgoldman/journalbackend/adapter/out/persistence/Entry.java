package dev.michaelgoldman.journalbackend.adapter.out.persistence;

import dev.michaelgoldman.journalbackend.domain.model.Mood;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "entries")
class Entry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(length = 500)
    private @Nullable String summary;

    @OneToMany(mappedBy = "entry", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<Todo> todos = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "entries_tags",
            joinColumns = @JoinColumn(name = "entry_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new HashSet<>();

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private @Nullable Mood mood;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated;

    @Column(name = "analysed_at")
    private @Nullable Instant analysedAt;

    protected Entry() {}

    Entry(String title, String content, Instant createdAt, Instant lastUpdated) {
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.lastUpdated = lastUpdated;
    }

    @Nullable Long getId() {
        return id;
    }

    @Nullable Long getVersion() {
        return version;
    }

    String getTitle() {
        return title;
    }

    String getContent() {
        return content;
    }

    @Nullable String getSummary() {
        return summary;
    }

    List<Todo> getTodos() {
        return List.copyOf(todos);
    }

    Set<Tag> getTags() {
        return Set.copyOf(tags);
    }

    @Nullable Mood getMood() {
        return mood;
    }

    Instant getCreatedAt() {
        return createdAt;
    }

    Instant getLastUpdated() {
        return lastUpdated;
    }

    @Nullable Instant getAnalysedAt() {
        return analysedAt;
    }

    void setTitle(String title) {
        this.title = title;
    }

    void setContent(String content) {
        this.content = content;
    }

    void setSummary(@Nullable String summary) {
        this.summary = summary;
    }

    void setMood(@Nullable Mood mood) {
        this.mood = mood;
    }

    void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    void setAnalysedAt(@Nullable Instant analysedAt) {
        this.analysedAt = analysedAt;
    }

    void addTodo(Todo todo) {
        this.todos.add(todo);
        todo.setEntry(this);
    }

    void removeTodo(Todo todo) {
        this.todos.remove(todo);
        todo.setEntry(null);
    }

    void replaceTags(Set<Tag> newTags) {
        this.tags.clear();
        this.tags.addAll(newTags);
    }
}
