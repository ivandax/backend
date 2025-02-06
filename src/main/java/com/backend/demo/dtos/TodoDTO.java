package com.backend.demo.dtos;

public class TodoDTO {

    private Integer id;
    private String description;
    private boolean isCompleted;
    private Integer sequenceNumber;

    public TodoDTO() {
    }

    public TodoDTO(Integer id, String description, boolean isCompleted, Integer sequenceNumber) {
        this.id = id;
        this.description = description;
        this.isCompleted = isCompleted;
        this.sequenceNumber = sequenceNumber;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
}