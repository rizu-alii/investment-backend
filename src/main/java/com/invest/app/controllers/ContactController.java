package com.invest.app.controllers;

import com.invest.app.dto.EmailRequest;
import com.invest.app.services.EmailValidationService;
import com.invest.app.services.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ContactController {

    @Autowired
    private EmailValidationService emailValidationService;

    @Autowired
    private MailService mailService;

    @Value("${receiver.email}")
    private String RECEIVER_EMAIL;

    @PostMapping("/api/contact")
    public ResponseEntity<String> sendContactEmail(@RequestBody EmailRequest request) {
        boolean isValid = emailValidationService.isValidEmail(request.getEmail());

        if (!isValid) {
            return ResponseEntity.badRequest().body("Invalid or risky email address.");
        }

        String content = "From: " + request.getName() + "\n" +
                "Email: " + request.getEmail() + "\n\n" +
                "Message:\n" + request.getDescription();

        mailService.sendEmail(RECEIVER_EMAIL, request.getSubject(), content);

        return ResponseEntity.ok("Email sent successfully.");
    }
}
