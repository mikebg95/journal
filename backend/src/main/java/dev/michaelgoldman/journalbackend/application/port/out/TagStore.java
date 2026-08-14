package dev.michaelgoldman.journalbackend.application.port.out;

import dev.michaelgoldman.journalbackend.domain.model.Tag;
import java.util.List;
import java.util.Set;

public interface TagStore {
    void ensureExist(Set<Tag> tags);

    // returns sorted list in alphabetical order
    List<Tag> findAll();
}
