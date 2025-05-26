package com.invest.app.requests;


import java.math.BigDecimal;

public class WithdrawRequest {

    private BigDecimal amount; // Amount to withdraw

    // Getters and setters
    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
