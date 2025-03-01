package com.backend.demo.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TodoUpdateDTO {
    @NotNull
    @NotBlank
    private String description;
    @NotNull
    private boolean isCompleted;

    @NotNull
    @NotBlank
    private Integer id;

    @NotNull
    private Integer sequenceNumber;

    private Integer assignToUserId;

    public TodoUpdateDTO() {
    }

    public TodoUpdateDTO(Integer id, String description, boolean isCompleted, Integer sequenceNumber) {
        this.description = description;
        this.isCompleted = isCompleted;
        this.sequenceNumber = sequenceNumber;
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

    public Integer getAssignToUserId() {
        return assignToUserId;
    }

    public void setAssignToUserId(Integer assignToUserId) {
        this.assignToUserId = assignToUserId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}