package com.invest.app.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "user_active_investments")
public class UserActiveInvestment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference(value = "user-userActiveInvestment")
    private UsersEntity usersEntity;

    @ManyToOne
    @JoinColumn(name = "investment_id", nullable = false)
    @JsonBackReference(value = "investment-userActiveInvestment")
    private UserInvestment investment;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;  // The initial invested amount

    @Column(nullable = false)
    private LocalDateTime investedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvestmentStatus status;  // ACTIVE, COMPLETED, WITHDRAWN

    @OneToMany(mappedBy = "userActiveInvestment")
    @JsonManagedReference(value = "investment-transaction")
    private List<Transaction> transactions;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal withdrawnAmount = BigDecimal.ZERO;  // Track amount withdrawn

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal currentProfit = BigDecimal.ZERO;  // Calculate the current profit dynamically

    @Column(nullable = false)
    private int durationInMonths;

    // --- CHANGE 1: Override getCurrentProfit to calculate dynamically ---
    public BigDecimal getCurrentProfit() {
        BigDecimal grossProfit = calculateCurrentProfit();
        BigDecimal netProfit = grossProfit.subtract(withdrawnAmount);
        return netProfit.max(BigDecimal.ZERO); // Ensures it never goes negative
    }


    // --- CHANGE 2: Add method to calculate current profit based on elapsed days ---
    public BigDecimal calculateCurrentProfit() {
        BigDecimal monthlyReturnRate = investment.getProjectedReturn(); // e.g. 0.05 for 5%
        long daysElapsed = Duration.between(startDate, LocalDateTime.now()).toDays();
        // Convert days to months (approximate)
        BigDecimal monthsElapsed = BigDecimal.valueOf(daysElapsed).divide(BigDecimal.valueOf(30), 4, BigDecimal.ROUND_HALF_UP);
        return amount.multiply(monthlyReturnRate).multiply(monthsElapsed);
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public UsersEntity getUsersEntity() {
        return usersEntity;
    }
    public void setUsersEntity(UsersEntity usersEntity) {
        this.usersEntity = usersEntity;
    }
    public UserInvestment getInvestment() {
        return investment;
    }
    public void setInvestment(UserInvestment investment) {
        this.investment = investment;
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

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public InvestmentStatus getStatus() {
        return status;
    }
    public void setStatus(InvestmentStatus status) {
        this.status = status;
    }
    public BigDecimal getWithdrawnAmount() {
        return withdrawnAmount;
    }
    public void setWithdrawnAmount(BigDecimal withdrawnAmount) {
        this.withdrawnAmount = withdrawnAmount;
    }
    public LocalDateTime getStartDate() {
        return startDate;
    }
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }
    public void setCurrentProfit(BigDecimal currentProfit) {
        this.currentProfit = currentProfit;
    }
    public int getDurationInMonths() {
        return durationInMonths;
    }
    public void setDurationInMonths(int durationInMonths) {
        this.durationInMonths = durationInMonths;
    }

    // Existing method (optional you may keep or remove if unused)
    public BigDecimal calculateTotalProfit() {
        BigDecimal monthlyReturnRate = investment.getProjectedReturn();
        return amount.multiply(monthlyReturnRate).multiply(BigDecimal.valueOf(durationInMonths));
    }

    public void withdrawProfit(BigDecimal amount) {
        if (getCurrentProfit().subtract(withdrawnAmount).compareTo(amount) >= 0) {
            withdrawnAmount = withdrawnAmount.add(amount);
            // currentProfit = currentProfit.subtract(amount);  <-- better NOT to keep here since getCurrentProfit is dynamic now
        } else {
            throw new IllegalArgumentException("Insufficient profit for withdrawal.");
        }
    }
}
