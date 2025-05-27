package com.example.qrcodegenerator.model;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public enum ErrorCorrectionLevelEnum {
    L(ErrorCorrectionLevel.L), // Low (approx. 7% correction)
    M(ErrorCorrectionLevel.M), // Medium (approx. 15% correction)
    Q(ErrorCorrectionLevel.Q), // Quartile (approx. 25% correction)
    H(ErrorCorrectionLevel.H); // High (approx. 30% correction)

    private final ErrorCorrectionLevel zxingEnum;

    ErrorCorrectionLevelEnum(ErrorCorrectionLevel zxingEnum) {
        this.zxingEnum = zxingEnum;
    }

    public ErrorCorrectionLevel getZxingEnum() {
        return zxingEnum;
    }

    public static ErrorCorrectionLevelEnum fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            // Default to M if not specified or empty
            return M;
        }
        try {
            return ErrorCorrectionLevelEnum.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid error correction level: '" + value +
                    "'. Allowed values are L, M, Q, H (case-insensitive).", e);
        }
    }
}
