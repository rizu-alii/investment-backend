package com.invest.app.controllers;

import com.invest.app.dto.ActiveInvestmentDTO;
import com.invest.app.entities.*;
import com.invest.app.repos.TransactionRepo;
import com.invest.app.repos.UserActiveInvestmentRepo;
import com.invest.app.repos.UserInvestmentRepo;
import com.invest.app.repos.UsersRepo;
import com.invest.app.requests.CreateInvestmentRequest;
import com.invest.app.requests.InvestmentRequest;
import com.invest.app.requests.UserProfileUpdateRequest;
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
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

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
    JWTService jwtService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(12);

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createActiveInvestment(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody InvestmentRequest request) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Extract username from JWT
            String jwt = authHeader.replace("Bearer ", "");
            String username = jwtService.extractUserName(jwt);
            UsersEntity user = usersRepo.findByUsername(username);

            if (user == null) {
                response.put("status", "NOT_FOUND");
                response.put("message", "User not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Find investment by ID
            Optional<UserInvestment> userInvestmentOpt = userInvestmentRepo.findById(request.getInvestmentId());
            if (userInvestmentOpt.isEmpty()) {
                response.put("status", "NOT_FOUND");
                response.put("message", "Investment not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            UserInvestment investment = userInvestmentOpt.get();

            // Create active investment
            UserActiveInvestment activeInvestment = new UserActiveInvestment();
            activeInvestment.setAmount(request.getAmount());
            activeInvestment.setInvestedAt(LocalDateTime.now());
            activeInvestment.setStartDate(LocalDateTime.now());
            activeInvestment.setInvestment(investment);
            activeInvestment.setUsersEntity(user);
            activeInvestment.setStatus(InvestmentStatus.ACTIVE);
            activeInvestment.setDurationInMonths(request.getDurationInMonths());
            activeInvestment.setCurrentProfit(activeInvestment.calculateTotalProfit());

            userActiveInvestmentRepo.save(activeInvestment);

            response.put("status", "OK");
            response.put("message", "Investment created successfully.");
            response.put("data", activeInvestment);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "An error occurred while creating the investment.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @PostMapping("/active-investments")
    public ResponseEntity<?> getUserActiveInvestmentsFromToken(@RequestHeader("Authorization") String token) {
        try {
            String jwt = token.replace("Bearer ", "");
            String username = jwtService.extractUserName(jwt);
            UsersEntity user = usersRepo.findByUsername(username);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
            }

            List<UserActiveInvestment> activeInvestments = userActiveInvestmentRepo.findByUsersEntity(user);

            List<ActiveInvestmentDTO> enrichedList = activeInvestments.stream().map(investment -> {
                ActiveInvestmentDTO dto = new ActiveInvestmentDTO();
                dto.setId(investment.getId());
                dto.setAmount(investment.getAmount());
                dto.setInvestedAt(investment.getInvestedAt());
                dto.setStatus(investment.getStatus().toString());
                dto.setCurrentProfit(investment.getCurrentProfit());
                dto.setStartDate(investment.getStartDate());
                dto.setDurationInMonths(investment.getDurationInMonths());

                // From investment table
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching active investments.");
        }
    }



    @PostMapping("/calculate-profit")
    public ResponseEntity<Map<String, BigDecimal>> calculateProfit(@RequestBody Map<String, Long> requestBody) {
        Long activeInvestmentId = requestBody.get("activeInvestmentId");

        if (activeInvestmentId == null) {
            return ResponseEntity.badRequest().build();
        }

        Optional<UserActiveInvestment> optionalActiveInvestment = userActiveInvestmentRepo.findById(activeInvestmentId);
        if (optionalActiveInvestment.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        UserActiveInvestment activeInvestment = optionalActiveInvestment.get();

        BigDecimal amount = activeInvestment.getAmount();
        BigDecimal projectedReturn = activeInvestment.getInvestment().getProjectedReturn();  // e.g. 8 for 8%
        LocalDateTime startDate = activeInvestment.getStartDate();
        LocalDateTime now = LocalDateTime.now();

        long daysElapsed = Duration.between(startDate, now).toDays();
        if (daysElapsed < 0) daysElapsed = 0;

        // Use the same profit formula as the withdraw API
        BigDecimal monthlyReturnRate = projectedReturn.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
        BigDecimal durationFraction = BigDecimal.valueOf(daysElapsed).divide(BigDecimal.valueOf(30), 10, RoundingMode.HALF_UP);
        BigDecimal profit = amount.multiply(monthlyReturnRate).multiply(durationFraction).setScale(4, RoundingMode.HALF_UP);

        Map<String, BigDecimal> response = new HashMap<>();
        response.put("profit", profit);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getUserDashboard(@RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Step 1: Extract username from token
            String jwt = token.replace("Bearer ", "");
            String username = jwtService.extractUserName(jwt);

            // Step 2: Find user
            UsersEntity user = usersRepo.findByUsername(username);

            // Step 3: Get all active investments for user
            List<UserActiveInvestment> investments = userActiveInvestmentRepo.findByUsersEntity(user);

            // Total Investment
            BigDecimal totalInvestment = investments.stream()
                    .map(UserActiveInvestment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Total Withdraw
            BigDecimal totalWithdraw = investments.stream()
                    .map(UserActiveInvestment::getWithdrawnAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Total Profit
            BigDecimal totalProfit = investments.stream()
                    .map(UserActiveInvestment::getCurrentProfit)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Active Investments Count
            long activeInvestments = investments.stream()
                    .filter(inv -> inv.getStatus().name().equals("ACTIVE"))
                    .count();

            // Monthly Profit
            Map<String, BigDecimal> profitByMonth = new TreeMap<>();
            List<Transaction> transactions = transactionRepo.findByUsersEntity(user);
            for (Transaction tx : transactions) {
                if (tx.getType() == TransactionType.WITHDRAWAL && tx.getStatus() == TransactionStatus.COMPLETED) {
                    String month = tx.getCreatedAt().getMonth().toString() + " " + tx.getCreatedAt().getYear();
                    profitByMonth.put(month, profitByMonth.getOrDefault(month, BigDecimal.ZERO).add(tx.getAmount()));
                }
            }

            // Construct response
            Map<String, Object> data = new HashMap<>();
            data.put("totalInvestment", totalInvestment);
            data.put("totalWithdraw", totalWithdraw);
            data.put("totalProfit", totalProfit);
            data.put("activeInvestments", activeInvestments);
            data.put("monthlyProfit", profitByMonth);

            response.put("status", "OK");
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "An error occurred while fetching dashboard data.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/transactions/dashboard/history")
    public ResponseEntity<Map<String, Object>> getTransactionHistoryFromToken(@RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();

        try {
            // ✅ Extract username from token
            String jwt = token.replace("Bearer ", "");
            String username = jwtService.extractUserName(jwt);

            // ✅ Fetch user from DB
            UsersEntity user = usersRepo.findByUsername(username);
            if (user == null) {
                response.put("status", "NOT_FOUND");
                response.put("message", "User not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // ✅ Fetch latest 10 transactions for the authenticated user
            List<Transaction> transactions = transactionRepo.findTop10ByUsersEntity_IdOrderByCreatedAtDesc(user.getId());

            response.put("status", "OK");
            response.put("message", "Latest 10 transactions fetched successfully.");
            response.put("data", transactions);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "An error occurred while fetching history.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }




    @PutMapping("/withdraw/{investmentId}")
    public ResponseEntity<Map<String, Object>> withdrawProfit(@PathVariable Long investmentId, @RequestBody WithdrawRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<UserActiveInvestment> activeInvestmentOpt = userActiveInvestmentRepo.findById(investmentId);
            if (activeInvestmentOpt.isEmpty()) {
                response.put("status", "NOT_FOUND");
                response.put("message", "Active investment not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            UserActiveInvestment activeInvestment = activeInvestmentOpt.get();
            BigDecimal amountToWithdraw = request.getAmount();
            BigDecimal userAmount = activeInvestment.getAmount();
            BigDecimal projectedReturn = activeInvestment.getInvestment().getProjectedReturn();
            LocalDateTime now = LocalDateTime.now();

            long daysElapsed = Duration.between(activeInvestment.getStartDate(), now).toDays();
            if (daysElapsed < 0) daysElapsed = 0;

            BigDecimal monthlyReturnRate = projectedReturn.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
            BigDecimal durationFraction = BigDecimal.valueOf(daysElapsed).divide(BigDecimal.valueOf(30), 10, RoundingMode.HALF_UP);
            BigDecimal currentProfit = userAmount.multiply(monthlyReturnRate).multiply(durationFraction).setScale(4, RoundingMode.HALF_UP);

            BigDecimal availableProfit = currentProfit.subtract(activeInvestment.getWithdrawnAmount());

            if (availableProfit.compareTo(BigDecimal.ZERO) <= 0) {
                activeInvestment.setStatus(InvestmentStatus.WITHDRAWN);
                activeInvestment.setCurrentProfit(currentProfit);
                userActiveInvestmentRepo.save(activeInvestment);

                response.put("status", "OK");
                response.put("message", "No profit left to withdraw.");
                response.put("data", activeInvestment);
                return ResponseEntity.ok(response);
            }

            if (amountToWithdraw.compareTo(BigDecimal.ZERO) <= 0 || amountToWithdraw.compareTo(availableProfit) > 0) {
                response.put("status", "BAD_REQUEST");
                response.put("message", "Invalid or insufficient profit for withdrawal.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Withdraw and update
            activeInvestment.withdrawProfit(amountToWithdraw);
            activeInvestment.setCurrentProfit(currentProfit);
            userActiveInvestmentRepo.save(activeInvestment);


            // Save transaction with PENDING status and active investment reference
            Transaction transaction = new Transaction();
            transaction.setUsersEntity(activeInvestment.getUsersEntity());
            transaction.setAmount(amountToWithdraw);
            transaction.setType(TransactionType.WITHDRAWAL);
            transaction.setStatus(TransactionStatus.PENDING);
            transaction.setCreatedAt(LocalDateTime.now());
            transaction.setUserActiveInvestment(activeInvestment); // ✅ This line sets the active investment
            transactionRepo.save(transaction);


            response.put("status", "OK");
            response.put("message", "Withdrawal request submitted.");
            response.put("data", activeInvestment);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "An error occurred while withdrawing profit.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/investments/deposit")
    public ResponseEntity<Map<String, Object>> depositToActiveInvestmentApi(@RequestBody Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<>();

        try {
            Long activeInvestmentId = ((Number) requestBody.get("activeInvestmentId")).longValue();
            BigDecimal amount = new BigDecimal(requestBody.get("amount").toString());

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                response.put("status", "BAD_REQUEST");
                response.put("message", "Deposit amount must be greater than zero.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            Optional<UserActiveInvestment> activeInvestmentOpt = userActiveInvestmentRepo.findById(activeInvestmentId);
            if (activeInvestmentOpt.isEmpty()) {
                response.put("status", "NOT_FOUND");
                response.put("message", "Active investment not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            UserActiveInvestment activeInvestment = activeInvestmentOpt.get();
            BigDecimal updatedAmount = activeInvestment.getAmount().add(amount);
            activeInvestment.setAmount(updatedAmount);

            BigDecimal projectedReturn = activeInvestment.getInvestment().getProjectedReturn();
            long daysElapsed = Duration.between(activeInvestment.getStartDate(), LocalDateTime.now()).toDays();
            if (daysElapsed < 0) daysElapsed = 0;

            BigDecimal monthlyReturnRate = projectedReturn.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
            BigDecimal durationFraction = BigDecimal.valueOf(daysElapsed).divide(BigDecimal.valueOf(30), 10, RoundingMode.HALF_UP);
            BigDecimal currentProfit = updatedAmount.multiply(monthlyReturnRate).multiply(durationFraction).setScale(4, RoundingMode.HALF_UP);

            activeInvestment.setCurrentProfit(currentProfit);
            userActiveInvestmentRepo.save(activeInvestment);

            // Save transaction with COMPLETED status
            Transaction transaction = new Transaction();
            transaction.setUsersEntity(activeInvestment.getUsersEntity());
            transaction.setAmount(amount);
            transaction.setType(TransactionType.DEPOSIT);
            transaction.setStatus(TransactionStatus.COMPLETED); // <-- COMPLETED
            transaction.setCreatedAt(LocalDateTime.now());
            transaction.setUserActiveInvestment(activeInvestment);
            transactionRepo.save(transaction);

            response.put("status", "OK");
            response.put("message", "Deposit successful.");
            response.put("data", activeInvestment);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "An error occurred while depositing.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(
            @RequestHeader("Authorization") String token,
            @RequestBody UserProfileUpdateRequest request
    ) {
        try {
            String jwt = token.replace("Bearer ", "");
            String username = jwtService.extractUserName(jwt);
            UsersEntity user = usersRepo.findByUsername(username);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
            }

            // ✅ Update username and full name if provided
            if (request.getUsername() != null && !request.getUsername().isBlank()) {
                user.setUsername(request.getUsername());
            }
            if (request.getFullName() != null) {
                user.setFullName(request.getFullName());
            }

            // ✅ Handle password update with confirmation
            if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
                if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Passwords do not match.");
                }
                // Encrypt password (replace with your actual password encoder)
                String encodedPassword = bCryptPasswordEncoder.encode(request.getNewPassword());
                user.setPassword(encodedPassword);
            }

            usersRepo.save(user);

            return ResponseEntity.ok("Profile updated successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating profile.");
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(@RequestHeader("Authorization") String token) {
        try {
            String jwt = token.replace("Bearer ", "");
            String username = jwtService.extractUserName(jwt);
            UsersEntity user = usersRepo.findByUsername(username);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
            }

            Map<String, String> profileData = new HashMap<>();
            profileData.put("username", user.getUsername());
            profileData.put("fullName", user.getFullName());

            return ResponseEntity.ok(profileData);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching profile.");
        }
    }



}







