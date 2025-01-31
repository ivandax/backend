package com.backend.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Entity
public class PasswordRecoveryToken {
    @Id
    @GeneratedValue
    private Integer passwordRecoveryTokenId;

    @OneToOne
    @JoinColumn(name = "belongsTo", referencedColumnName = "userId")
    private User belongsTo;

    @NotNull
    @NotEmpty
    private String passwordRecoveryToken;

    public PasswordRecoveryToken() {
    }

    public PasswordRecoveryToken(User belongsTo, String passwordRecoveryToken) {
        this.belongsTo = belongsTo;
        this.passwordRecoveryToken = passwordRecoveryToken;
    }

    public Integer getPasswordRecoveryTokenId() {
        return passwordRecoveryTokenId;
    }

    public void setPasswordRecoveryTokenId(Integer passwordRecoveryTokenId) {
        this.passwordRecoveryTokenId = passwordRecoveryTokenId;
    }

    public User getBelongsTo() {
        return belongsTo;
    }

    public void setBelongsTo(User belongsTo) {
        this.belongsTo = belongsTo;
    }

    public String getPasswordRecoveryToken() {
        return passwordRecoveryToken;
    }

    public void setPasswordRecoveryToken(String passwordRecoveryToken) {
        this.passwordRecoveryToken = passwordRecoveryToken;
    }
}
