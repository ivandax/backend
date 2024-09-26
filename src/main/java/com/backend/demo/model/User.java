package com.backend.demo.model;

import com.backend.demo.model.enums.UserStatus;
import com.backend.demo.utils.PasswordUtils;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    @Column(unique = true, nullable = false)
    @Email
    @NotNull
    private String username;

    @NotNull
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    private List<Role> roles = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization")
    private Organization organization;

    @Column(name = "created", columnDefinition = "TIMESTAMP")
    private Date created;

    @Column(name = "updated", columnDefinition = "TIMESTAMP")
    private Date updated;

    private boolean isVerified = false;

    private UserStatus userStatus = UserStatus.ACTIVE;

    public User() {
        setCreated();
        setUpdated();
    }

    public User(String username, String password) {
        setCreated();
        setUpdated();
        setUsername(username);
        setPassword(password);
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated() {
        this.created = new Date();
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated() {
        this.updated = new Date();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = PasswordUtils.encryptPassword(password);
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public void setCreated(Date date) {
        this.created = date;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public boolean isActive() {
        return userStatus == UserStatus.ACTIVE;
    }

    public void setActive(UserStatus status) {
        userStatus = status;
    }
}
