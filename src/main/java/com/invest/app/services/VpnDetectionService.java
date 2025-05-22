package com.invest.app.services;


import com.invest.app.entities.VpnDetectionResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class VpnDetectionService {

    @Value("${vpn.api.url}")
    private String vpnApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Method to get VPN and Mobile detection information.
     * @param ipAddress IP address to check.
     * @return VpnDetectionResponse containing VPN and mobile status.
     */
    public VpnDetectionResponseEntity getVpnAndMobileStatus(String ipAddress) {
        String url = UriComponentsBuilder.fromHttpUrl(vpnApiUrl)
                .queryParam("ip", ipAddress)
                .toUriString();

        try {
            VpnDetectionResponseEntity response = restTemplate.getForObject(url, VpnDetectionResponseEntity.class);
            return (response != null) ? response : new VpnDetectionResponseEntity();
        } catch (Exception e) {
            e.printStackTrace();
            return new VpnDetectionResponseEntity(); // Return an empty response if API call fails
        }
    }
}
