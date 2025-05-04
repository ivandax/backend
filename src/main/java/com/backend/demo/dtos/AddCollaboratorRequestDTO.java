package com.backend.demo.dtos;

import jakarta.validation.constraints.NotNull;

public class AddCollaboratorRequestDTO {
    @NotNull
    private Integer collaboratorId;

    public Integer getCollaboratorId() {
        return collaboratorId;
    }

    public void setCollaboratorId(Integer collaboratorId) {
        this.collaboratorId = collaboratorId;
    }
}