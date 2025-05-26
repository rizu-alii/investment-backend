package com.invest.app.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class EmailValidationService {

    @Value("${ipqs.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean isValidEmail(String email) {
        String url = String.format("https://www.ipqualityscore.com/api/json/email/%s/%s", apiKey, email);

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                return Boolean.TRUE.equals(response.get("valid"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
