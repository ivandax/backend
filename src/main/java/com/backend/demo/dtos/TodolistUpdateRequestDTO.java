package com.backend.demo.dtos;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public class TodolistUpdateRequestDTO {
    @NotNull
    private String title;
    @NotNull
    private String description;

    public TodolistUpdateRequestDTO(String title, String description, List<TodoRequestDTO> todos) {
        this.title = title;
        this.description = description;
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
}
