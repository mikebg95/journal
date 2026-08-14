package dev.michaelgoldman.journalbackend.application.port.out;

import java.util.List;

public record TodoPage(List<TodoDetail> todos, int pageNumber, int pageSize, long totalElements) {
    public TodoPage {
        todos = List.copyOf(todos);
    }
}
