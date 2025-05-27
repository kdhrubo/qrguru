package com.example.qrcodegenerator.model;

public enum QrCodeType {
    TEXT,
    URL,
    VCARD,
    WIFI,
    EMAIL,
    SMS,
    CALENDAR, // Added CALENDAR type
    GEO;      // Added GEO type

    // Optional: Add a method to safely convert from String, handling case-insensitivity or invalid values
    // The existing fromString method is fine as valueOf handles all enum constants automatically.
    // No change needed here unless we want specific error messages or default behavior not covered by valueOf.
    public static QrCodeType fromString(String type) {
        if (type == null || type.trim().isEmpty()) {
            // Or throw an IllegalArgumentException if type is mandatory
            return null; 
        }
        try {
            return QrCodeType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            // Consider throwing a more specific exception or logging
            // For now, returning null or re-throwing is an option.
            // The controller currently handles null by returning bad request.
            throw new IllegalArgumentException("Unknown QrCodeType: '" + type + "'. Supported types are: " +
                java.util.Arrays.stream(QrCodeType.values()).map(Enum::name).collect(java.util.stream.Collectors.joining(", ")), e);
        }
    }
}
