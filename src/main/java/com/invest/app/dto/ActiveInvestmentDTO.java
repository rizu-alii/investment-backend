package com.invest.app.dto;

import com.invest.app.entities.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ActiveInvestmentDTO {
    private Long id;
    private BigDecimal amount;
    private LocalDateTime investedAt;
    private String status;
    private BigDecimal currentProfit;
    private LocalDateTime startDate;
    private int durationInMonths;

    // From UserInvestment
    private String investmentName;
    private String category;
    private BigDecimal projectedReturn;
    private String fundSize;
    private String riskLevel;

    // Related Transactions
    private List<Transaction> transactions;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getInvestedAt() {
        return investedAt;
    }

    public void setInvestedAt(LocalDateTime investedAt) {
        this.investedAt = investedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getCurrentProfit() {
        return currentProfit;
    }

    public void setCurrentProfit(BigDecimal currentProfit) {
        this.currentProfit = currentProfit;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public int getDurationInMonths() {
        return durationInMonths;
    }

    public void setDurationInMonths(int durationInMonths) {
        this.durationInMonths = durationInMonths;
    }

    public String getInvestmentName() {
        return investmentName;
    }

    public void setInvestmentName(String investmentName) {
        this.investmentName = investmentName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getProjectedReturn() {
        return projectedReturn;
    }

    public void setProjectedReturn(BigDecimal projectedReturn) {
        this.projectedReturn = projectedReturn;
    }

    public String getFundSize() {
        return fundSize;
    }

    public void setFundSize(String fundSize) {
        this.fundSize = fundSize;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
// Getters and setters (or use Lombok @Data for brevity)
}

