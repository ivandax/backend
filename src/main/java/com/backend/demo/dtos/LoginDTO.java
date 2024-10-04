package com.backend.demo.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class LoginDTO {
    @Email
    @NotNull
    @NotEmpty
    private String email;
    @NotNull
    @NotEmpty
    private String password;

    public LoginDTO() {
    }

    public LoginDTO(String email, String password, String organizationName) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
