package com.example.qrcodegenerator.service.formatter;

import com.example.qrcodegenerator.model.QrCodeType;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class SmsQrDataFormatter implements QrDataFormatter {

    public static final String KEY_SMS_NUMBER = "number";
    public static final String KEY_SMS_MESSAGE = "message";

    @Override
    public String format(Map<String, String> params) {
        if (params == null || !params.containsKey(KEY_SMS_NUMBER)) {
            throw new IllegalArgumentException("Parameters for SMS must contain '" + KEY_SMS_NUMBER + "'.");
        }

        String number = params.get(KEY_SMS_NUMBER);
        if (number == null || number.trim().isEmpty()) {
            throw new IllegalArgumentException("SMS number cannot be null or empty.");
        }
        // Basic validation for phone number (digits, optional +, etc.) could be added
        // For simplicity, we assume it's a valid number string.

        String message = params.get(KEY_SMS_MESSAGE); // Message is optional

        StringBuilder smsToBuilder = new StringBuilder("smsto:");
        smsToBuilder.append(number.trim()); // No encoding for the number itself

        if (message != null && !message.trim().isEmpty()) {
            smsToBuilder.append(":").append(encodeValue(message.trim()));
        }

        return smsToBuilder.toString();
    }

    private String encodeValue(String value) {
        try {
            // According to some sources, SMS body might not need full URL encoding,
            // but using it is safer for special characters.
            // Others suggest simple replacement of spaces with `+`.
            // For maximum compatibility and safety, URLEncoder is used.
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            // This should not happen with UTF-8
            throw new RuntimeException("Error encoding URL parameter for SMS: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean supports(QrCodeType type) {
        return type == QrCodeType.SMS;
    }
}
