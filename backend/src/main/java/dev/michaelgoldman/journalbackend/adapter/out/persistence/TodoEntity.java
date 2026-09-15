package dev.michaelgoldman.journalbackend.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "todos")
class TodoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private @Nullable EntryEntity entry;

    protected TodoEntity() {}

    TodoEntity(String value) {
        this.value = value;
    }

    String getValue() {
        return value;
    }

    @Nullable EntryEntity getEntry() {
        return entry;
    }

    void setValue(String value) {
        this.value = value;
    }

    void setEntry(@Nullable EntryEntity entry) {
        this.entry = entry;
    }
}
