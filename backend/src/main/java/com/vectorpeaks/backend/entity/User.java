package com.vectorpeaks.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

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

    @Column(name = "role_id")
    private Integer roleId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", insertable = false, updatable = false)
    private Role role;

    @JsonIgnore
    private String password;

    private String firstName;
    private String lastName;

    @Column(name = "email", unique = true)
    private String email;

    private Integer accountStatusId;
    private String address;
    private String phoneNumber;

    // Getters and setters...
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getRoleId() { return roleId; }
    public void setRoleId(Integer roleId) { this.roleId = roleId; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

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

    @Transient
    public String getRoleName() {
        return (role != null) ? role.getName() : "STUDENT";
    }
}