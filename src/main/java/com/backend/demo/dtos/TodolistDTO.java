package com.backend.demo.dtos;

import java.util.Date;
import java.util.List;

public class TodolistDTO {
    private Integer id;
    private String createdBy;
    private List<String> sharedWith;
    private List<TodoDTO> todos;
    private Date created;
    private Date updated;

    private String title;

    private String description;

    public TodolistDTO() {
    }

    public TodolistDTO(Integer id, String createdBy, List<String> sharedWith, List<TodoDTO> todos
            , Date created, Date updated, String title, String description) {
        this.id = id;
        this.createdBy = createdBy;
        this.sharedWith = sharedWith;
        this.todos = todos;
        this.created = created;
        this.updated = updated;
        this.title = title;
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public List<String> getSharedWith() {
        return sharedWith;
    }

    public void setSharedWith(List<String> sharedWith) {
        this.sharedWith = sharedWith;
    }

    public List<TodoDTO> getTodos() {
        return todos;
    }

    public void setTodos(List<TodoDTO> todos) {
        this.todos = todos;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
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
