package com.invest.app.requests;


import com.invest.app.entities.UsersEntity;

import java.math.BigDecimal;

public class InvestmentRequest {

    private Long investmentId; // Investment ID (from the UserInvestment entity)
    private BigDecimal amount; // Amount to invest
    private UsersEntity userEntity;// The user making the investment
    private int durationInMonths;

    public int getDurationInMonths() {
        return durationInMonths;
    }

    public void setDurationInMonths(int durationInMonths) {
        this.durationInMonths = durationInMonths;
    }

    // Getters and setters
    public Long getInvestmentId() {
        return investmentId;
    }

    public void setInvestmentId(Long investmentId) {
        this.investmentId = investmentId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public UsersEntity getUserEntity() {
        return userEntity;
    }

    public void setUserEntity(UsersEntity userEntity) {
        this.userEntity = userEntity;
    }
}
