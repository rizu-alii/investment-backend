package com.invest.app.services;

/**
 * Enum to represent standard response codes.
 */
public enum ResponseCode {
    // Success Codes
    SUCCESS("200", "Operation completed successfully"),
    CREATED("201", "Resource created successfully"),
    BAD_REQUEST("400", "Bad request"),
    UNAUTHORIZED("401", "Unauthorized"),
    FORBIDDEN("403", "Forbidden"),
    NOT_FOUND("404", "Resource not found"),
    INTERNAL_SERVER_ERROR("500", "Internal server error"),
    SERVICE_UNAVAILABLE("503", "Service unavailable"),
    USER_NOT_FOUND("14", "User not Found"),
    DATA_MANDATORY("63", "Mandatory Data"),
    VPN_DETECTED("83", "Vpn Detected"),
    GENERAL_ERROR("10", "An Error has been occurred, please contact service provider.");

    private final String code;
    private final String message;

    ResponseCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return code + ": " + message;
    }
}
