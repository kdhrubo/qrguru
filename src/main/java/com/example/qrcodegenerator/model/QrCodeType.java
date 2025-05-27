package com.example.qrcodegenerator.model;

public enum QrCodeType {
    TEXT,
    URL,
    VCARD,
    WIFI,
    EMAIL,
    SMS;

    // Optional: Add a method to safely convert from String, handling case-insensitivity or invalid values
    public static QrCodeType fromString(String type) {
        if (type == null) {
            return null;
        }
        try {
            return QrCodeType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null; // Or throw a custom exception
        }
    }
}
