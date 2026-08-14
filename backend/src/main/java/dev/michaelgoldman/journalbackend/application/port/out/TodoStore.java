package dev.michaelgoldman.journalbackend.application.port.out;

import dev.michaelgoldman.journalbackend.application.port.in.Page;
import dev.michaelgoldman.journalbackend.application.port.in.TodoDetail;
import dev.michaelgoldman.journalbackend.application.port.in.TodoSort;

public interface TodoStore {
    Page<TodoDetail> findPage(int pageNumber, int pageSize, TodoSort sort);
}
