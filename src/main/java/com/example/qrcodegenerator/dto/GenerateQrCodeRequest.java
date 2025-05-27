package com.example.qrcodegenerator.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateQrCodeRequest {

    @NotBlank(message = "qrCodeType cannot be blank")
    private String qrCodeType; // Will be converted to QrCodeType enum in service/controller

    // data can be the primary content for simple types like TEXT or URL
    // For complex types, 'params' will be used more extensively.
    // Depending on the qrCodeType, 'data' might be optional if 'params' provides everything.
    // For now, let's make it non-blank, assuming it's always used for the primary piece of data.
    @NotBlank(message = "data cannot be blank")
    private String data;

    private Map<String, String> params = new HashMap<>(); // Initialize to avoid null pointer issues

    @Min(value = 1, message = "width must be positive")
    private int width = 250; // Default value

    @Min(value = 1, message = "height must be positive")
    private int height = 250; // Default value

    private String errorCorrectionLevel = "M"; // Default to Medium

    // Hex color format: #RRGGBB or #RGB
    // This pattern allows for # followed by 6 hex chars OR # followed by 3 hex chars
    @jakarta.validation.constraints.Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Invalid hex color format for foregroundColor. Use #RRGGBB or #RGB.")
    private String foregroundColor = "#000000"; // Default to black

    @jakarta.validation.constraints.Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Invalid hex color format for backgroundColor. Use #RRGGBB or #RGB.")
    private String backgroundColor = "#FFFFFF"; // Default to white

    private String outputFormat = "PNG"; // Default to PNG

    // Custom constructor or setters can be added if specific logic is needed
    // for initializing params from data, etc.
}
