package com.invest.app.controllers;



import com.invest.app.entities.UsersEntity;
import com.invest.app.entities.VpnDetectionResponseEntity;
import com.invest.app.repos.UsersRepo;
import com.invest.app.security.IpInfoService;
import com.invest.app.security.JWTService;
import com.invest.app.security.MyUserDetailService;
import com.invest.app.services.VpnDetectionService;
import jakarta.servlet.http.HttpServletRequest;

//import jakarta.transaction.Transacxtional;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.management.relation.RoleNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private IpInfoService ipInfoService;
    @Autowired
    private VpnDetectionService vpnDetectionService;
    @Autowired
    private MyUserDetailService myUserDetailService;
    @Autowired
    private JWTService jwtService;

    @Autowired
    private UsersRepo usersRepo;

    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(12);

    @PostMapping("/register")
    public ResponseEntity<UsersEntity> register(@RequestBody UsersEntity users) throws RoleNotFoundException {
        if (usersRepo.findByUsername(users.getUsername()) == null) {
            users.setPassword(bCryptPasswordEncoder.encode(users.getPassword()));
            UsersEntity savedUser = usersRepo.saveAndFlush(users);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .header("Move to this page", "http://localhost:8080/meditrack/login")
                    .body(savedUser);
        } else {
            throw new RuntimeException("User with username " + users.getUsername() + " already exists.");
        }

    }

    @PostMapping("/admin/login")
    public ResponseEntity<Map<String, Object>> adminLogin(@RequestBody UsersEntity users) throws RoleNotFoundException {
        // Validate admin credentials
        if ("admin".equals(users.getUsername()) && "admin".equals(users.getPassword())) {

            String jwtToken = jwtService.generateToken(users.getUsername());
            Map<String, Object> response = new HashMap<>();
            response.put("status", "OK");
            response.put("message", "Logged in successfully");
            response.put("jwtToken", "Bearer "+jwtToken);
            return ResponseEntity.status(HttpStatus.OK).body(response);
            // Success: Return OK status with success message
        } else {
            // Failure: Return UNAUTHORIZED status with error message
            Map<String, Object> response = new HashMap<>();
            response.put("status", "Internal_Server_error");
            response.put("message", "Something went wrong");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
    }


    @PostMapping("/login")
    @Transactional
    public ResponseEntity <Map<String,Object>> login(@RequestBody UsersEntity users, HttpServletRequest request) {
        try {
            // Step 1: Check if the username exists
            Optional<UsersEntity> existingUser = Optional.ofNullable(usersRepo.findByUsername(users.getUsername()));
            if (existingUser.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "Not Found");
                response.put("message", "Username not found");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            UsersEntity user = existingUser.get();


            // Step 2: Check if the user is banned
            if (user.isEnabled()==false) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "FORBIDDEN");
                response.put("message", "Your Account is Banned");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }



            // Step 4: Store the username in session
            HttpSession session = request.getSession();
            session.setAttribute("username", user.getUsername());

            // Step 4: Get VPN and Mobile detection info
//            VpnDetectionResponseEntity vpnAndMobileInfo = vpnDetectionService.getVpnAndMobileStatus(ipInfoService.getPublicIpAddress());
//
//            if (vpnAndMobileInfo == null) {
//                Map<String, Object> response = new HashMap<>();
//                response.put("status", "Internal_Server_Error");
//                response.put("message", "Failed to fetch vpn or mobile detection");
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//            }

            System.out.println("IP Address: " + ipInfoService.getPublicIpAddress());
//            System.out.println("VPN Status: " + vpnAndMobileInfo.isVpn());
//            System.out.println("Mobile Status: " + vpnAndMobileInfo.isMobile());

//
            // Check for mobile device usage
//            if (vpnAndMobileInfo.isMobile()) {
//                Map<String, Object> response = new HashMap<>();
//                response.put("status", "FORBIDDEN");
//                response.put("message", "Login through mobile device is not allowed");
//                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
//            }
//            if(vpnDetectionService1.isVPNConnected()) {
//              user.setIsBanned(true);
//              usersRepo.save(user);
//              Map<String, Object> response = new HashMap<>();
//               response.put("status", "UNAVAILABLE_FOR_LEGAL_REASONS");
//               response.put("message", "Your account has been  banned due to vpn detection");
//               return ResponseEntity.status(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS).body(response);
//            }

            // Step 5: Generate JWT token after successful login
            String jwtToken = jwtService.generateToken(user.getUsername());

            // Construct the JSON response
            Map<String, Object> response = new HashMap<>();
            response.put("status", "OK");
            response.put("message", "Logged in successfully");
            response.put("jwtToken", "Bearer "+jwtToken);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("status", "Internal_Server_error");
            response.put("message", "Something went wrong");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response); }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {

        return ResponseEntity.ok("Logged out successfully.");
        }
    @GetMapping("/welcome")
    public ResponseEntity<String> welcome() {
        return ResponseEntity.ok("Hello");
    }

    @GetMapping("/welcome2")
    public ResponseEntity<String> welcome2() {
        return ResponseEntity.ok("Hello2");
    }

    }



