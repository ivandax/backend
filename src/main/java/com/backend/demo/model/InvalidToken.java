package com.backend.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.util.Date;

@Entity
public class InvalidToken {
    @Id
    @GeneratedValue
    private Integer tokenId;

    @Column(nullable = false, length = 1000, unique = true)
    private String token;

    @Column(name = "created", columnDefinition = "TIMESTAMP")
    private Date created;

    public InvalidToken() {
        setCreated();
    }

    public InvalidToken(String token) {
        this.token = token;
        setCreated();
    }

    public Integer getTokenId() {
        return tokenId;
    }

    public void setTokenId(Integer tokenId) {
        this.tokenId = tokenId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated() {
        this.created = new Date();
    }
}
