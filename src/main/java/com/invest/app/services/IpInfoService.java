package com.invest.app.services;


import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
@Service
public class IpInfoService {

    //    private static final String API_URL = "https://api.ipinfo.info/check?access_key=830732c62437ea";
    private static final String API_URL = "https://ipinfo.io/json?token=830732c62437ea";
    //830732c62437ea
    public String getPublicIpAddress() {
        RestTemplate restTemplate = new RestTemplate();

        try {
            // Send the API request and get the response as a JSON string
            ResponseEntity<String> response = restTemplate.getForEntity(API_URL, String.class);
            System.out.println(response.getBody());
            // Parse the JSON response to extract the IP address
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            String ipAddress = rootNode.path("ip").asText();
            System.out.println(ipAddress);

            return ipAddress;
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to get IP address.";
        }
    }
}
