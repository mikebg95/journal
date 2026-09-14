package dev.michaelgoldman.journalbackend.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "tags")
class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String value;

    protected Tag() {}

    Tag(String value) {
        this.value = value;
    }

    String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Tag otherTag)) {
            return false;
        }
        return Objects.equals(value, otherTag.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
