package com.backend.demo.utils;

import com.backend.demo.dtos.TodolistDTO;
import com.backend.demo.dtos.TodoDTO;
import com.backend.demo.model.Todolist;
import com.backend.demo.model.Todo;
import com.backend.demo.model.User;

import java.util.Comparator;
import java.util.stream.Collectors;

public class TodolistUtils {

    public static TodolistDTO toDTO(Todolist todolist) {
        return new TodolistDTO(
                todolist.getId(),
                todolist.getCreatedBy().getUsername(),
                todolist.getSharedWith().stream()
                        .map(User::getUsername)
                        .collect(Collectors.toList()),
                todolist.getTodos().stream()
                        .sorted(Comparator.comparing(Todo::getSequenceNumber))
                        .map(TodolistUtils::toTodoDTO)
                        .collect(Collectors.toList()),
                todolist.getCreated(),
                todolist.getUpdated(),
                todolist.getTitle(),
                todolist.getDescription()
        );
    }

    private static TodoDTO toTodoDTO(Todo todo) {
        return new TodoDTO(
                todo.getId(),
                todo.getDescription(),
                todo.isCompleted(),
                todo.getSequenceNumber()
        );
    }
}