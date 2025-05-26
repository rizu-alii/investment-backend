package com.invest.app.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VpnDetectionResponseEntity {
    private boolean vpn;
    private boolean mobile;
    @JsonProperty("country_code")
    private String countryCode;

    public boolean isMobile() {
        return mobile;
    }

    public void setMobile(boolean mobile) {
        this.mobile = mobile;
    }

    public boolean isVpn() {
        return vpn;
    }

    public void setVpn(boolean vpn) {
        this.vpn = vpn;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    @Override
    public String toString() {
        return "VpnDetectionResponse{" +
                "vpn=" + vpn +
                ", mobile=" + mobile +
                ", countryCode=" + countryCode +
                '}';
    }
}

