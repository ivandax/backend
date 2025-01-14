package com.backend.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class InvalidToken {
    @Id
    @GeneratedValue
    private Integer tokenId;

    @Column(nullable = false)
    private String token;

    public InvalidToken() {
    }

    public InvalidToken(String token) {
        this.token = token;
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
}
