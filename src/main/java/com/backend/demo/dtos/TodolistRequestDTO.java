package com.backend.demo.dtos;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public class TodolistRequestDTO {
    @NotNull
    private String title;
    @NotNull
    private String description;
    @NotNull
    private List<TodoRequestDTO> todos;

    public TodolistRequestDTO(String title, String description, List<TodoRequestDTO> todos) {
        this.title = title;
        this.description = description;
        this.todos = todos;
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

    public List<TodoRequestDTO> getTodos() {
        return todos;
    }

    public void setTodos(List<TodoRequestDTO> todos) {
        this.todos = todos;
    }
}
