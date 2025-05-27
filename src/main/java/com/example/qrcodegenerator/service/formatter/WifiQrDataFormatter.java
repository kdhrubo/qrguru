package com.example.qrcodegenerator.service.formatter;

import com.example.qrcodegenerator.model.QrCodeType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class WifiQrDataFormatter implements QrDataFormatter {

    public static final String KEY_WIFI_SSID = "ssid";
    public static final String KEY_WIFI_PASSWORD = "password";
    public static final String KEY_WIFI_ENCRYPTION = "encryption"; // WPA, WPA2, WEP, nopass
    public static final String KEY_WIFI_HIDDEN = "hidden"; // true or false

    @Override
    public String format(Map<String, String> params) {
        if (params == null || !params.containsKey(KEY_WIFI_SSID)) {
            throw new IllegalArgumentException("Parameters for WiFi must contain '" + KEY_WIFI_SSID + "'.");
        }

        String ssid = params.get(KEY_WIFI_SSID);
        if (ssid == null || ssid.trim().isEmpty()) {
            throw new IllegalArgumentException("SSID cannot be null or empty.");
        }

        String encryption = params.getOrDefault(KEY_WIFI_ENCRYPTION, "nopass").toUpperCase();
        String password = params.get(KEY_WIFI_PASSWORD);
        String hidden = params.getOrDefault(KEY_WIFI_HIDDEN, "false");

        // Validate encryption type and password presence
        switch (encryption) {
            case "WPA":
            case "WPA2": // WPA2 is often just specified as WPA in QR codes
            case "WEP":
                if (password == null || password.isEmpty()) {
                    throw new IllegalArgumentException("Password is required for " + encryption + " encryption.");
                }
                break;
            case "NOPASS":
                password = ""; // Ensure password is empty for nopass
                break;
            default:
                throw new IllegalArgumentException("Invalid WiFi encryption type: " + encryption + ". Supported types are WPA, WPA2, WEP, nopass.");
        }
        
        // Escape special characters: \, ;, ,, ", and :
        ssid = escapeWifiString(ssid);
        if (password != null) { // Password can be null if not provided and encryption is nopass
            password = escapeWifiString(password);
        }


        StringBuilder wifiBuilder = new StringBuilder("WIFI:");
        wifiBuilder.append("S:").append(ssid).append(";");
        wifiBuilder.append("T:").append(encryption).append(";");
        if (password != null && !password.isEmpty()) {
            wifiBuilder.append("P:").append(password).append(";");
        }
        if ("true".equalsIgnoreCase(hidden)) {
            wifiBuilder.append("H:true;");
        }
        wifiBuilder.append(";"); // Trailing ;; is common practice

        return wifiBuilder.toString();
    }
    
    private String escapeWifiString(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace(";", "\\;")
                   .replace(",", "\\,")
                   .replace("\"", "\\\"")
                   .replace(":", "\\:");
    }


    @Override
    public boolean supports(QrCodeType type) {
        return type == QrCodeType.WIFI;
    }
}
