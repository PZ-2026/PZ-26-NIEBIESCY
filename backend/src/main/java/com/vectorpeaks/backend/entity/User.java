/*
 * User.java
 *
 * Version: 1.1
 * Date: 2026-05-17
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents a user entity mapped to the "users" database table.
 * Contains personal information, authentication credentials, and account status.
 *
 * @version 1.1
 * @author EduLink Team
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Identifier of the user's role (e.g., 2 = Student, 3 = Tutor). */
    @Column(name = "role_id")
    private Integer roleId;

    /** Hashed password – never returned in JSON responses. */
    @JsonIgnore
    private String password;

    /** First name of the user. */
    private String firstName;

    /** Last name of the user. */
    private String lastName;

    /** Unique email address (used for login). */
    @Column(name = "email", unique = true)
    private String email;

    /** Status identifier (e.g., 1 = active, 9 = deleted/anonymized). */
    private Integer accountStatusId;

    /** City or full address of the user (used for offline offers). */
    private String address;

    /** Phone number (9 digits). */
    private String phoneNumber;

    /** Firebase Cloud Messaging token for push notifications. */
    @Column(name = "fcm_token")
    private String fcmToken;

    /** Timestamp when the user account was created. */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getRoleId() { return roleId; }
    public void setRoleId(Integer roleId) { this.roleId = roleId; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getAccountStatusId() { return accountStatusId; }
    public void setAccountStatusId(Integer accountStatusId) { this.accountStatusId = accountStatusId; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}