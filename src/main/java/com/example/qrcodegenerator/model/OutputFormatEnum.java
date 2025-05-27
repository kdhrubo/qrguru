package com.example.qrcodegenerator.model;

public enum OutputFormatEnum {
    PNG,
    SVG;

    /**
     * Converts a string to the corresponding OutputFormatEnum.
     * Case-insensitive. If the string is null, empty, or invalid,
     * the defaultValue is returned.
     *
     * @param value        The string value to convert (e.g., "png", "SVG").
     * @param defaultValue The enum value to return if conversion fails.
     * @return The corresponding OutputFormatEnum or the defaultValue.
     */
    public static OutputFormatEnum fromString(String value, OutputFormatEnum defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return OutputFormatEnum.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }
}
