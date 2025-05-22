package com.invest.app.controllers;

import com.invest.app.entities.UserInvestment;
import com.invest.app.entities.UsersEntity;
import com.invest.app.repos.UserInvestmentRepo;
import com.invest.app.repos.UsersRepo;
import com.invest.app.requests.CreateInvestmentRequest;
import com.invest.app.response.CreateInvestmentResponse;
import com.invest.app.services.RequestManager;
import com.invest.app.services.ResponseCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.management.relation.RoleNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    RequestManager requestManager;
@Autowired
    UsersRepo usersRepo;
    @Autowired
    private UserInvestmentRepo userInvestmentRepo;

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

                // Validate not null or empty if needed
                if (username == null || username.isEmpty()) {
                    response.put("status", "BAD_REQUEST");
                    response.put("message", "Username is required.");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }

                user.setUsername(username);
                user.setFullName(fullName); // fullName can be null

                usersRepo.save(user);

                response.put("status", "OK");
                response.put("message", "User updated successfully.");
                response.put("data", user);

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


}
