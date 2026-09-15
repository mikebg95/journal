package dev.michaelgoldman.journalbackend.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.michaelgoldman.journalbackend.AbstractIntegrationTest;
import dev.michaelgoldman.journalbackend.domain.model.Tag;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class JpaTagStoreIT extends AbstractIntegrationTest {

    private final JpaTagStore jpaTagStore;
    private final TagRepository tagRepository;

    @Autowired
    JpaTagStoreIT(TagRepository tagRepository, JpaTagStore jpaTagStore) {
        this.jpaTagStore = jpaTagStore;
        this.tagRepository = tagRepository;
    }

    @Test
    void whenTagsExist_shouldReturnThemSortedByValue() {
        // Arrange
        List<TagEntity> tags = List.of(new TagEntity("work"), new TagEntity("cleaning"), new TagEntity("gym"));
        tagRepository.saveAll(tags);

        // Act
        List<Tag> fetchedTags = jpaTagStore.findAll();

        // Assert
        assertEquals(List.of(new Tag("cleaning"), new Tag("gym"), new Tag("work")), fetchedTags);
    }
}
