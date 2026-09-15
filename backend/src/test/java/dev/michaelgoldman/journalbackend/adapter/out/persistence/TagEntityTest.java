package dev.michaelgoldman.journalbackend.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TagEntityTest {

    @Test
    void whenValuesAreEqual_shouldBeEqualAndShareHashCode() {
        TagEntity tagEntity1 = new TagEntity("work");
        TagEntity tagEntity2 = new TagEntity("work");
        boolean isEqual = tagEntity1.equals(tagEntity2);

        assertTrue(isEqual);
        assertEquals(tagEntity1.hashCode(), tagEntity2.hashCode());
    }
}
