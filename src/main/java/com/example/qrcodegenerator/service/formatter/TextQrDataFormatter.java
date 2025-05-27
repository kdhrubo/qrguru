package com.example.qrcodegenerator.service.formatter;

import com.example.qrcodegenerator.model.QrCodeType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TextQrDataFormatter implements QrDataFormatter {

    // Define a standard key for simple text/URL data within the params map
    public static final String KEY_TEXT_DATA = "text_data";

    @Override
    public String format(Map<String, String> params) {
        if (params == null || !params.containsKey(KEY_TEXT_DATA)) {
            throw new IllegalArgumentException("Parameters must contain '" + KEY_TEXT_DATA + "' for TEXT or URL QR codes.");
        }
        String textData = params.get(KEY_TEXT_DATA);
        if (textData == null || textData.trim().isEmpty()) {
            throw new IllegalArgumentException("Value for '" + KEY_TEXT_DATA + "' cannot be null or empty.");
        }
        return textData;
    }

    @Override
    public boolean supports(QrCodeType type) {
        return type == QrCodeType.TEXT || type == QrCodeType.URL;
    }
}
