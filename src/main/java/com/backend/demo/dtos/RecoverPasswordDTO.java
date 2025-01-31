package com.backend.demo.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class RecoverPasswordDTO {
    @Email
    @NotNull
    @NotEmpty
    private String email;

    public RecoverPasswordDTO() {
    }

    public RecoverPasswordDTO(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
