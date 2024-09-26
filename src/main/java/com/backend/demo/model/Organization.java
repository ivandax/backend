package com.backend.demo.model;

import java.util.Date;
import java.util.List;
import jakarta.persistence.*;

@Entity
public class Organization { @Id
@GeneratedValue
private Integer organizationId;

    @Column(unique = true, nullable = false)
    private String organizationName;

    @Column(name = "created", columnDefinition = "TIMESTAMP")
    private Date created;

    @Column(name = "updated", columnDefinition = "TIMESTAMP")
    private Date updated;

    @OneToMany(mappedBy="organization", fetch = FetchType.EAGER)
    private List<User> users;

    @Column(nullable = false)
    private Integer userQuota;

    public Organization(){}
    public Organization(String organizationName, Integer userQuota) {
        this.organizationName = organizationName;
        this.userQuota = userQuota;
        setCreated();
        setUpdated();
    }

    public Integer getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Integer organizationId) {
        this.organizationId = organizationId;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String name) {
        this.organizationName = name;
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

    public void addUser(User user) {
        users.add(user);
    }

    public List<User> getUsers() {
        return users;
    }

    public Integer getUserQuota() {
        return userQuota;
    }

    public void setUserQuota(Integer userQuota) {
        this.userQuota = userQuota;
    }

}
