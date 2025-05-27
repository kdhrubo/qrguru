package com.example.qrcodegenerator.util;

import java.awt.Color;

public class ColorUtil {

    /**
     * Parses a hex color string into a java.awt.Color object.
     * Supports formats: #RRGGBB, #RGB, RRGGBB, RGB.
     *
     * @param hexColor     The hex color string.
     * @param defaultColor The color to return if parsing fails or hexColor is null/empty.
     * @return The parsed Color object, or defaultColor if parsing fails.
     */
    public static Color parseHexColor(String hexColor, Color defaultColor) {
        if (hexColor == null || hexColor.trim().isEmpty()) {
            return defaultColor;
        }

        String cleanHex = hexColor.trim();
        if (cleanHex.startsWith("#")) {
            cleanHex = cleanHex.substring(1);
        }

        try {
            if (cleanHex.length() == 6) { // RRGGBB
                int r = Integer.parseInt(cleanHex.substring(0, 2), 16);
                int g = Integer.parseInt(cleanHex.substring(2, 4), 16);
                int b = Integer.parseInt(cleanHex.substring(4, 6), 16);
                return new Color(r, g, b);
            } else if (cleanHex.length() == 3) { // RGB
                int r = Integer.parseInt(cleanHex.substring(0, 1) + cleanHex.substring(0, 1), 16);
                int g = Integer.parseInt(cleanHex.substring(1, 2) + cleanHex.substring(1, 2), 16);
                int b = Integer.parseInt(cleanHex.substring(2, 3) + cleanHex.substring(2, 3), 16);
                return new Color(r, g, b);
            } else {
                // Invalid length
                System.err.println("Warning: Invalid hex color string length: " + hexColor + ". Using default color.");
                return defaultColor;
            }
        } catch (NumberFormatException e) {
            System.err.println("Warning: Could not parse hex color string: " + hexColor + ". Error: " + e.getMessage() + ". Using default color.");
            return defaultColor;
        }
    }
}
