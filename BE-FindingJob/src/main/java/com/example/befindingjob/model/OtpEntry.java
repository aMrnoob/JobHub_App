package com.example.befindingjob.model;

public class OtpEntry {
    private String otp;
    private long expiryTime;

    public OtpEntry(String otp, long expiryTime) {
        this.otp = otp;
        this.expiryTime = expiryTime;
    }

    public String getOtp() {
        return otp;
    }

    public long getExpiryTime() {
        return expiryTime;
    }
}

