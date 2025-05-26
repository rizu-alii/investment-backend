package com.invest.app.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "user-entity")
public class UsersEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column( length = 100)
    private String fullName;

    @Column(nullable = false)
    private boolean enabled = true;

    @OneToMany(mappedBy = "usersEntity")
    @JsonManagedReference(value = "user-userActiveInvestment")
    private List<UserActiveInvestment> userActiveInvestments;

    @OneToMany(mappedBy = "usersEntity")
    @JsonManagedReference(value = "user-transaction")
    private List<Transaction> transactions;


    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }



    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }



    public List<UserActiveInvestment> getUserActiveInvestments() {
        return userActiveInvestments;
    }

    public void setUserActiveInvestments(List<UserActiveInvestment> userActiveInvestments) {
        this.userActiveInvestments = userActiveInvestments;
    }
}