package com.backend.demo.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
public class SetNewPasswordDTO {
    @NotNull
    @NotEmpty
    private String passwordRecoveryToken;

    @NotNull
    @NotEmpty
    private String newPassword;

    public SetNewPasswordDTO(){}

    public SetNewPasswordDTO(String passwordRecoveryToken, String newPassword) {
        this.passwordRecoveryToken = passwordRecoveryToken;
        this.newPassword = newPassword;
    }

    public String getPasswordRecoveryToken() {
        return passwordRecoveryToken;
    }

    public void setPasswordRecoveryToken(String passwordRecoveryToken) {
        this.passwordRecoveryToken = passwordRecoveryToken;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
