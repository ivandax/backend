package com.backend.demo.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class VerificationTokenRequestDTO {
    @NotNull
    @NotEmpty
    private String verificationToken;

    public VerificationTokenRequestDTO(){}

    public VerificationTokenRequestDTO(String verificationToken) {
        this.verificationToken = verificationToken;
    }

    public String getVerificationToken() {
        return verificationToken;
    }

    public void setVerificationToken(String verificationToken) {
        this.verificationToken = verificationToken;
    }
}
