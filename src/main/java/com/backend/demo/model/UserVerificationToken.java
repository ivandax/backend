package com.backend.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Entity
public class UserVerificationToken {
    @Id
    @GeneratedValue
    private Integer userVerificationTokenId;

    @OneToOne
    @JoinColumn(name = "belongsTo", referencedColumnName = "userId")
    private User belongsTo;

    @NotNull
    @NotEmpty
    private String verificationToken;

    public UserVerificationToken() {
    }

    public UserVerificationToken(User belongsTo, String verificationToken) {
        this.belongsTo = belongsTo;
        this.verificationToken = verificationToken;
    }

    public User getBelongsTo() {
        return belongsTo;
    }

    public void setBelongsTo(User belongsTo) {
        this.belongsTo = belongsTo;
    }

    public String getVerificationToken() {
        return verificationToken;
    }

    public void setVerificationToken(String verificationToken) {
        this.verificationToken = verificationToken;
    }
}
