package dev.michaelgoldman.journalbackend.application.port.in;

public interface TodoUseCases {
    Page<TodoDetail> findTodoPage(int page, TodoSort sort);
}
