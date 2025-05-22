package com.invest.app.entities;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VpnDetectionResponseEntity {
    // Getters and Setters
    private boolean vpn;
    private boolean mobile;

    @Override
    public String toString() {
        return "VpnDetectionResponse{" +
                "vpn=" + vpn +
                ", mobile=" + mobile +
                '}';
    }
}
