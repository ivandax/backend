package com.backend.demo.dtos;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public class TodolistUpdateRequestDTO {
    @NotNull
    private String title;
    @NotNull
    private String description;

    @NotNull
    private List<TodoUpdateDTO> updateTodos;

    @NotNull
    private List<TodoRequestDTO> createTodos;

    @NotNull
    private List<String> deleteTodoIds;

    public TodolistUpdateRequestDTO(String title, String description,
                                    List<TodoRequestDTO> createTodos,
                                    List<TodoUpdateDTO> updateTodos,
                                    List<String> deleteTodoIds) {
        this.title = title;
        this.description = description;
        this.updateTodos = updateTodos;
        this.createTodos = createTodos;
        this.deleteTodoIds = deleteTodoIds;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<TodoUpdateDTO> getUpdateTodos() {
        return updateTodos;
    }

    public void setUpdateTodos(List<TodoUpdateDTO> updateTodos) {
        this.updateTodos = updateTodos;
    }

    public List<TodoRequestDTO> getCreateTodos() {
        return createTodos;
    }

    public void setCreateTodos(List<TodoRequestDTO> createTodos) {
        this.createTodos = createTodos;
    }

    public List<String> getDeleteTodoIds() {
        return deleteTodoIds;
    }

    public void setDeleteTodoIds(List<String> deleteTodoIds) {
        this.deleteTodoIds = deleteTodoIds;
    }
}
