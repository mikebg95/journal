package dev.michaelgoldman.journalbackend.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TagTest {

    @Test
    void whenValuesAreEqual_shouldBeEqualAndShareHashCode() {
        Tag tag1 = new Tag("work");
        Tag tag2 = new Tag("work");
        boolean isEqual = tag1.equals(tag2);

        assertTrue(isEqual);
        assertEquals(tag1.hashCode(), tag2.hashCode());
    }
}
