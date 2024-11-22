package com.backend.demo.dtos.User;

import java.util.List;

public class UserResponseDTO {
    private Integer userId;
    private String username;

    private String organizationName;
    private List<String> roles;

    private boolean isActive;

    private boolean isVerified;

    public UserResponseDTO(){}

    public UserResponseDTO(Integer userId, String username, String organizationName,
                           List<String> roles, boolean isVerified, boolean isActive) {
        this.userId = userId;
        this.username = username;
        this.organizationName = organizationName;
        this.roles = roles;
        this.isActive = isActive;
        this.isVerified = isVerified;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }
}
