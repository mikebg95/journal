package dev.michaelgoldman.journalbackend.application.port.out;

public interface TodoStore {
    TodoPage findPage(int pageNumber, int pageSize, TodoSort sort);
}
