package com.invest.app.controllers;

import com.invest.app.dto.ActiveInvestmentDTO;
import com.invest.app.entities.*;
import com.invest.app.repos.*;
import com.invest.app.requests.CreateInvestmentRequest;
import com.invest.app.requests.InvestmentRequest;
import com.invest.app.requests.WithdrawRequest;
import com.invest.app.response.CreateInvestmentResponse;
import com.invest.app.security.JWTService;
import com.invest.app.services.RequestManager;
import com.invest.app.services.ResponseCode;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.management.relation.RoleNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    private LoginActivityRepository loginActivityRepo;
    @Autowired
    RequestManager requestManager;
     @Autowired
    UsersRepo usersRepo;
    @Autowired
    private UserInvestmentRepo userInvestmentRepo;
    @Autowired
    private UserActiveInvestmentRepo userActiveInvestmentRepo;
    @Autowired
    TransactionRepo transactionRepo;
    @Autowired
    private JWTService jwtService;

    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(12);

    @PostMapping("/create-investment")
    public CreateInvestmentResponse createInvestment(@RequestBody CreateInvestmentRequest request) throws RoleNotFoundException {

        CreateInvestmentResponse response = new CreateInvestmentResponse();
        try {
            requestManager.createInvestment(request);
        }catch (Exception e) {
            response.setResponseCode(ResponseCode.GENERAL_ERROR.getCode());
            response.setResponseDescription("An Error has been occurred, please contact service provider.");
            response.setSuccess(Boolean.FALSE);
            return response;
        }
        response.setResponseCode(ResponseCode.SUCCESS.getCode());
        response.setResponseDescription(ResponseCode.SUCCESS.getMessage());
        response.setSuccess(Boolean.TRUE);
        return response;

    }

    @GetMapping("/get-investments")
    public ResponseEntity<Map<String, Object>> showInvestment() {
        Map<String, Object> response = new HashMap<>();

        try {
            List<UserInvestment> userInvestmentList = userInvestmentRepo.findAll();

            response.put("status", "OK");
            response.put("message", "Investments fetched successfully.");
            response.put("data", userInvestmentList);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "An error occurred while fetching investments. Please contact the service provider.");
            response.put("data", null);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/update-investment/{id}")
    public ResponseEntity<Map<String, Object>> updateInvestment(@PathVariable Long id, @RequestBody UserInvestment updatedInvestment) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<UserInvestment> optionalInvestment = userInvestmentRepo.findById(id);

            if (optionalInvestment.isPresent()) {
                UserInvestment existingInvestment = optionalInvestment.get();

                // Update fields
                existingInvestment.setName(updatedInvestment.getName());
                existingInvestment.setCategory(updatedInvestment.getCategory());
                existingInvestment.setFundSize(updatedInvestment.getFundSize());
                existingInvestment.setProjectedReturn(updatedInvestment.getProjectedReturn());
                existingInvestment.setRiskLevel(updatedInvestment.getRiskLevel());
                existingInvestment.setDescription(updatedInvestment.getDescription());

                userInvestmentRepo.save(existingInvestment);

                response.put("status", "OK");
                response.put("message", "Investment updated successfully.");
                response.put("data", existingInvestment);
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "NOT_FOUND");
                response.put("message", "Investment not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "An error occurred while updating the investment.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/delete-investment/{id}")
    public ResponseEntity<Map<String, Object>> deleteInvestment(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (userInvestmentRepo.existsById(id)) {
                userInvestmentRepo.deleteById(id);
                response.put("status", "OK");
                response.put("message", "Investment deleted successfully.");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "NOT_FOUND");
                response.put("message", "Investment not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "An error occurred while deleting the investment.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }



    @GetMapping("/all-users")
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        Map<String, Object> response = new HashMap<>();

        try {
            List<UsersEntity> users = usersRepo.findAll();

            response.put("status", "OK");
            response.put("message", "Users fetched successfully.");
            response.put("data", users);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "An error occurred while fetching users.");
            response.put("data", null);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }




    @PutMapping("/user-update/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Long id,
            @RequestBody Map<String, String> userDetails) {

        Map<String, Object> response = new HashMap<>();

        try {
            Optional<UsersEntity> optionalUser = usersRepo.findById(id);

            if (optionalUser.isPresent()) {
                UsersEntity user = optionalUser.get();

                // Extract new values from request body
                String username = userDetails.get("username");
                String fullName = userDetails.get("fullName");
                String password = userDetails.get("password");
                String confirmPassword = userDetails.get("confirmPassword");

                // Validate username
                if (username == null || username.isEmpty()) {
                    response.put("status", "BAD_REQUEST");
                    response.put("message", "Username is required.");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }

                // Optional password change validation
                if (password != null && !password.isEmpty()) {
                    if (!password.equals(confirmPassword)) {
                        response.put("status", "BAD_REQUEST");
                        response.put("message", "Password and confirmPassword do not match.");
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                    }
     password = bCryptPasswordEncoder.encode(password);
                    // Optionally hash the password here
                    user.setPassword(password);
                }

                user.setUsername(username);
                user.setFullName(fullName); // fullName can be null

                usersRepo.save(user);

                response.put("status", "OK");
                response.put("message", "User updated successfully.");
                return ResponseEntity.ok(response);

            } else {
                response.put("status", "NOT_FOUND");
                response.put("message", "User not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "An error occurred while updating the user.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @PutMapping("/toggle-enabled/{id}")
    public ResponseEntity<Map<String, Object>> toggleUserEnabled(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body) {

        Map<String, Object> response = new HashMap<>();

        try {
            Optional<UsersEntity> optionalUser = usersRepo.findById(id);

            if (optionalUser.isPresent()) {
                UsersEntity user = optionalUser.get();

                Boolean enabled = body.get("enabled");

                if (enabled == null) {
                    response.put("status", "BAD_REQUEST");
                    response.put("message", "Missing 'enabled' value in request body.");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }

                user.setEnabled(enabled);
                usersRepo.save(user);

                response.put("status", "OK");
                response.put("message", "User account status updated successfully.");
                response.put("enabled", user.isEnabled());

                return ResponseEntity.ok(response);

            } else {
                response.put("status", "NOT_FOUND");
                response.put("message", "User not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "An error occurred while updating user status.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/withdrawals/update-status/{transactionId}")
    public ResponseEntity<Map<String, Object>> updateWithdrawalStatus(
            @PathVariable Long transactionId,
            @RequestBody Map<String, Boolean> requestBody) {

        Map<String, Object> response = new HashMap<>();

        try {
            // ✅ Find transaction by ID
            Optional<Transaction> optionalTx = transactionRepo.findById(transactionId);
            if (optionalTx.isEmpty()) {
                response.put("status", "NOT_FOUND");
                response.put("message", "Transaction not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Transaction tx = optionalTx.get();

            // ✅ Ensure it's a withdrawal transaction
            if (tx.getType() != TransactionType.WITHDRAWAL) {
                response.put("status", "BAD_REQUEST");
                response.put("message", "Transaction is not a withdrawal.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // ✅ Get boolean flag from request
            Boolean approved = requestBody.get("approved");
            if (approved == null) {
                response.put("status", "BAD_REQUEST");
                response.put("message", "Missing 'approved' value in request body.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // ✅ Set new status
            if (approved) {
                tx.setStatus(TransactionStatus.COMPLETED);
            } else {
                tx.setStatus(TransactionStatus.FAILED); // Rejected case
            }

            transactionRepo.save(tx);

            response.put("status", "OK");
            response.put("message", "Withdrawal status updated successfully.");
            response.put("transactionId", tx.getId());
            response.put("newStatus", tx.getStatus().toString());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "An error occurred while updating status.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }



    @GetMapping("/withdrawals/pending")
    public ResponseEntity<List<Map<String, Object>>> getPendingWithdrawalRequests() {
        try {
            List<Map<String, Object>> withdrawals = transactionRepo.findAll().stream()
                    .filter(tx -> tx.getType() == TransactionType.WITHDRAWAL)
                    .filter(tx -> tx.getStatus() == TransactionStatus.PENDING)
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .map(tx -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("transactionId", tx.getId());
                        map.put("fullName", tx.getUsersEntity().getFullName());
                        map.put("userId", tx.getUsersEntity().getId());
                        map.put("amount", tx.getAmount());
                        map.put("createdAt", tx.getCreatedAt());
                        map.put("status", tx.getStatus().name());
                        return map;
                    })
                    .toList();

            return ResponseEntity.ok(withdrawals);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null); // or return ResponseEntity.internalServerError().build();
        }
    }










//    @GetMapping("/transactions/history/{userId}")
//    public ResponseEntity<Map<String, Object>> getTransactionHistory(@PathVariable Long userId) {
//        Map<String, Object> response = new HashMap<>();
//
//        try {
//            Optional<UsersEntity> userOpt = usersRepo.findById(userId);
//            if (userOpt.isEmpty()) {
//                response.put("status", "NOT_FOUND");
//                response.put("message", "User not found.");
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//            }
//
//            List<Transaction> transactions = transactionRepo.findByUsersEntity_IdOrderByCreatedAtDesc(userId);
//
//            response.put("status", "OK");
//            response.put("message", "Transaction history fetched successfully.");
//            response.put("data", transactions);
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            response.put("status", "ERROR");
//            response.put("message", "An error occurred while fetching history.");
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//        }
//    }

    @GetMapping("/transactions/history/investment/{investmentId}")
    public ResponseEntity<Map<String, Object>> getInvestmentTransactionHistory(@PathVariable Long investmentId) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<UserActiveInvestment> investmentOpt = userActiveInvestmentRepo.findById(investmentId);
            if (investmentOpt.isEmpty()) {
                response.put("status", "NOT_FOUND");
                response.put("message", "Active investment not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            List<Transaction> transactions = transactionRepo.findByUserActiveInvestment_IdOrderByCreatedAtDesc(investmentId);

            response.put("status", "OK");
            response.put("message", "Transaction history for investment fetched successfully.");
            response.put("data", transactions);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "An error occurred while fetching investment history.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @PutMapping("/transactions/update-status/{transactionId}")
    public ResponseEntity<Map<String, Object>> updateTransactionStatus(
            @PathVariable Long transactionId,
            @RequestBody Map<String, String> body
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Transaction> transactionOpt = transactionRepo.findById(transactionId);
            if (transactionOpt.isEmpty()) {
                response.put("status", "NOT_FOUND");
                response.put("message", "Transaction not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Transaction transaction = transactionOpt.get();
            String newStatus = body.get("status");

            // Validate input
            TransactionStatus statusEnum;
            try {
                statusEnum = TransactionStatus.valueOf(newStatus.toUpperCase());
            } catch (IllegalArgumentException ex) {
                response.put("status", "BAD_REQUEST");
                response.put("message", "Invalid transaction status.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            transaction.setStatus(statusEnum);
            transactionRepo.save(transaction);

            response.put("status", "OK");
            response.put("message", "Transaction status updated.");
            response.put("data", transaction);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "An error occurred while updating transaction.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }



    @Transactional
    public UserActiveInvestment depositToActiveInvestment(Long activeInvestmentId, BigDecimal additionalAmount) {
        UserActiveInvestment activeInvestment = userActiveInvestmentRepo.findById(activeInvestmentId)
                .orElseThrow(() -> new EntityNotFoundException("Active Investment not found"));

        if (activeInvestment.getStatus() != InvestmentStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE investments can be topped up.");
        }

        // Increase the amount
        activeInvestment.setAmount(activeInvestment.getAmount().add(additionalAmount));

        // Optional: Recalculate profit if your model supports it
        BigDecimal newProfit = activeInvestment.calculateTotalProfit(); // This uses amount * rate * duration
        activeInvestment.setCurrentProfit(newProfit);

        return userActiveInvestmentRepo.save(activeInvestment);
    }

    @PostMapping("/analytics/logins-this-month")
    public ResponseEntity<Map<String, Object>> getMonthlyLoginCount() {
        long count = loginActivityRepo.countLoginsThisMonth();

        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("monthlyLoginCount", count);

        return ResponseEntity.ok(response);
    }
    @GetMapping("/dashboard-data")
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        Map<String, Object> response = new HashMap<>();

        // Total Registered Users
        long totalUsers = usersRepo.count();

        // Active Investments (Status = ACTIVE)
        long activeInvestments = userActiveInvestmentRepo.findAll()
                .stream()
                .filter(investment -> investment.getStatus() == InvestmentStatus.ACTIVE)
                .count();

        // Total Deposits = Initial Activation Amount + All Deposit Transactions
        double activationAmountSum = userActiveInvestmentRepo.findAll()
                .stream()
                .map(UserActiveInvestment::getAmount)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();

        double depositTransactionSum = transactionRepo.findAll()
                .stream()
                .filter(tx -> tx.getType().name().equalsIgnoreCase("DEPOSIT"))
                .map(Transaction::getAmount)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();

        double totalDeposits = activationAmountSum + depositTransactionSum;

        // Total Withdrawals (from transaction table)
        double totalWithdrawals = transactionRepo.findAll()
                .stream()
                .filter(tx -> tx.getType().name().equalsIgnoreCase("WITHDRAWAL"))
                .map(Transaction::getAmount)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();

        // Average Return from all admin-created investments
        double averageReturn = userInvestmentRepo.findAll()
                .stream()
                .map(UserInvestment::getProjectedReturn)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0.0);

        // Web Traffic - logins this month
        long webTraffic = loginActivityRepo.countLoginsThisMonth();

        // Weekly Web Traffic Data for Line Graph
        List<Map<String, Object>> graphData = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            LocalDateTime startOfDay = day.atStartOfDay();
            LocalDateTime endOfDay = day.atTime(LocalTime.MAX);

            long count = loginActivityRepo.countByLoginTimeBetween(startOfDay, endOfDay);

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("day", day.getDayOfWeek().toString());
            dayData.put("webTraffic", count);
            graphData.add(dayData);
        }

        // Final response
        response.put("totalUsers", totalUsers);
        response.put("activeInvestments", activeInvestments);
        response.put("totalDeposits", totalDeposits);
        response.put("totalWithdrawals", totalWithdrawals);
        response.put("averageReturn", String.format("%.2f%%", averageReturn));
        response.put("webTraffic", webTraffic);
        response.put("graphData", graphData);

        return ResponseEntity.ok(response);
    }



    @GetMapping("/transactions/history/{userId}")
    public ResponseEntity<?> getUserTransactionsByAdmin(@PathVariable Long userId) {
        try {
            Optional<UsersEntity> userOpt = usersRepo.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
            }

            List<Transaction> transactions = transactionRepo.findByUsersEntity_IdOrderByCreatedAtDesc(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "OK");
            response.put("message", "Transaction history fetched successfully.");
            response.put("data", transactions);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ERROR");
            response.put("message", "An error occurred while fetching history.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/active-investments")
    public ResponseEntity<?> getAllActiveInvestments() {
        try {
            List<UserActiveInvestment> allActiveInvestments = userActiveInvestmentRepo.findAll();

            List<ActiveInvestmentDTO> enrichedList = allActiveInvestments.stream().map(investment -> {
                ActiveInvestmentDTO dto = new ActiveInvestmentDTO();
                dto.setId(investment.getId());
                dto.setAmount(investment.getAmount());
                dto.setInvestedAt(investment.getInvestedAt());
                dto.setStatus(investment.getStatus().toString());
                dto.setCurrentProfit(investment.getCurrentProfit());
                dto.setStartDate(investment.getStartDate());
                dto.setDurationInMonths(investment.getDurationInMonths());

                // Investment metadata
                UserInvestment inv = investment.getInvestment();
                dto.setInvestmentName(inv.getName());
                dto.setCategory(inv.getCategory());
                dto.setProjectedReturn(inv.getProjectedReturn());
                dto.setFundSize(inv.getFundSize());
                dto.setRiskLevel(inv.getRiskLevel());

                // Transaction history
                List<Transaction> transactions = transactionRepo.findByUserActiveInvestment_IdOrderByCreatedAtDesc(investment.getId());
                dto.setTransactions(transactions);

                return dto;
            }).toList();

            return ResponseEntity.ok(enrichedList);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching all active investments.");
        }
    }

    @GetMapping("/user-investment-summary")
    public ResponseEntity<?> getAllUserInvestmentSummaries() {
        try {
            List<UserActiveInvestment> activeInvestments = userActiveInvestmentRepo.findAll();

            List<Map<String, Object>> summaries = activeInvestments.stream().map(inv -> {
                Map<String, Object> summary = new HashMap<>();
                UsersEntity user = inv.getUsersEntity();
                UserInvestment fund = inv.getInvestment();

                summary.put("userId", user.getId());
                summary.put("fullName", user.getFullName());
                summary.put("fundName", fund.getName());
                summary.put("amountInvested", inv.getAmount());
                summary.put("investedAt", inv.getInvestedAt());

                return summary;
            }).toList();

            return ResponseEntity.ok(summaries);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch user investment summary.");
        }
    }



}
