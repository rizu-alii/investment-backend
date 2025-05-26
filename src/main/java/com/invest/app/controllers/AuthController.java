package com.invest.app.controllers;



import com.invest.app.entities.LoginActivity;
import com.invest.app.entities.UsersEntity;
import com.invest.app.entities.VpnDetectionResponseEntity;
import com.invest.app.repos.LoginActivityRepository;
import com.invest.app.repos.UsersRepo;

import com.invest.app.security.JWTService;
import com.invest.app.security.MyUserDetailService;
import com.invest.app.services.IpInfoService;
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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
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
    private JWTService jwtService;
    @Autowired
    private LoginActivityRepository loginActivityRepo;

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
    public ResponseEntity<Map<String, Object>> login(@RequestBody UsersEntity users, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Step 1: Check if the username exists
            Optional<UsersEntity> existingUser = Optional.ofNullable(usersRepo.findByUsername(users.getUsername()));
            if (existingUser.isEmpty()) {
                response.put("status", "Not Found");
                response.put("message", "Username not found");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }



            UsersEntity user = existingUser.get();

            // Step 2: Check if the user is banned
            if (!user.isEnabled()) {
                response.put("status", "FORBIDDEN");
                response.put("message", "Your Account is Banned. Please contact support");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            if (!bCryptPasswordEncoder.matches(users.getPassword(), user.getPassword())) {
                response.put("status", "FORBIDDEN");
                response.put("message", "Invalid password");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }



            // Step 4: Get VPN and Mobile detection info
            String ip = ipInfoService.getPublicIpAddress();
            VpnDetectionResponseEntity vpnInfo = vpnDetectionService.getVpnAndMobileStatus(ip);

            // Step 3: Save login record
            LoginActivity activity = new LoginActivity();
            activity.setUsername(user.getUsername());
            activity.setLoginTime(LocalDateTime.now());
            activity.setMobile(vpnInfo.isMobile());  // Set mobile or desktop
            activity.setCountryCode(vpnInfo.getCountryCode());  // Set country code
            loginActivityRepo.save(activity);

            // Step 5: Store username in session
            HttpSession session = request.getSession();
            session.setAttribute("username", user.getUsername());

            // Step 6: Generate JWT token after successful login
            String jwtToken = jwtService.generateToken(user.getUsername());

            // Step 7: Construct and return response
            response.put("status", "OK");
            response.put("message", "Logged in successfully");
            response.put("jwtToken", "Bearer " + jwtToken);
            response.put("vpn", vpnInfo.isVpn());
            response.put("mobile", vpnInfo.isMobile());
            response.put("countryCode", vpnInfo.getCountryCode());

            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "Internal_Server_Error");
            response.put("message", "Something went wrong");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @GetMapping("/login-stats")
    public ResponseEntity<Map<String, Object>> getLoginStats() {
        Map<String, Object> response = new HashMap<>();

        // 1. Count mobile vs desktop logins
        long mobileCount = loginActivityRepo.countByMobileTrue();
        long desktopCount = loginActivityRepo.countByMobileFalse();

        // 2. Group login counts by country
        List<Object[]> countryStats = loginActivityRepo.countLoginsByCountry();
        Map<String, Long> countryCount = new HashMap<>();
        for (Object[] row : countryStats) {
            String countryCode = row[0] != null ? (String) row[0] : "UNKNOWN";
            Long count = (Long) row[1];
            countryCount.put(countryCode, count);
        }

        // 3. Get last 5 unique user logins
        List<LoginActivity> uniqueLast5 = loginActivityRepo.findTop5LatestLoginsByUniqueUsers();
        List<Map<String, Object>> uniqueLoginInfo = uniqueLast5.stream().map(login -> {
            Map<String, Object> info = new HashMap<>();
            info.put("username", login.getUsername());
            info.put("device", login.isMobile() ? "Mobile" : "Desktop");
            info.put("country", login.getCountryCode() != null ? login.getCountryCode() : "UNKNOWN");
            info.put("loginTime", login.getLoginTime());
            return info;
        }).toList();

        // 4. Compile and return
        response.put("mobileLogins", mobileCount);
        response.put("desktopLogins", desktopCount);
        response.put("loginsByCountry", countryCount);
        response.put("last5UniqueUserLogins", uniqueLoginInfo);

        return ResponseEntity.ok(response);
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



