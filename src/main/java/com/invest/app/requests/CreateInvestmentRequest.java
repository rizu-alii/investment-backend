package com.invest.app.requests;

import lombok.Data;
import java.math.BigDecimal;


public class CreateInvestmentRequest {
    private String name;
    private String description;
    private BigDecimal projectedReturn; // e.g., 8.00 for 8%
    private String category;
    private String riskLevel;
    private String fundSize;

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
}