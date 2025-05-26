package com.invest.app.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "investments")
@AllArgsConstructor
@NoArgsConstructor
public class UserInvestment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    // Remove minAmount and maxAmount

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal projectedReturn; // e.g., 8.00 for 8%

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false, length = 100)
    private String riskLevel;

    @Column(nullable = false, length = 100)
    private String fundSize;

    @OneToMany(mappedBy = "investment")
    @JsonManagedReference(value = "investment-userActiveInvestment")
    private List<UserActiveInvestment> userInvestments;

    @Column(name = "return_percentage")
    private Double returnPercentage;

    public double getReturnPercentage() {
        return returnPercentage != null ? returnPercentage : 0.0;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getProjectedReturn() {
        return projectedReturn;
    }

    public void setProjectedReturn(BigDecimal projectedReturn) {
        this.projectedReturn = projectedReturn;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getFundSize() {
        return fundSize;
    }

    public void setFundSize(String fundSize) {
        this.fundSize = fundSize;
    }

    public List<UserActiveInvestment> getUserInvestments() {
        return userInvestments;
    }

    public void setUserInvestments(List<UserActiveInvestment> userInvestments) {
        this.userInvestments = userInvestments;
    }
}