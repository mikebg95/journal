package dev.michaelgoldman.journalbackend.application.port.in;

import java.util.List;

public record Page<T>(List<T> content, int pageNumber, int pageSize, long totalElements) {
    public Page {
        content = List.copyOf(content);
    }
}
