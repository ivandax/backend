package com.backend.demo.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class SignUpDTO {
    @Email
    @NotNull
    @NotEmpty
    private String email;
    @NotNull
    @NotEmpty
    private String password;

    @NotNull
    @NotEmpty
    private String organizationName;

    public SignUpDTO() {
    }

    public SignUpDTO(String email, String password, String organizationName) {
        this.email = email;
        this.password = password;
        this.organizationName = organizationName;
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

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }
}
