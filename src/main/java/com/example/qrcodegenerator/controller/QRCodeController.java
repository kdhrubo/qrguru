package com.example.qrcodegenerator.controller;

import com.example.qrcodegenerator.dto.GenerateQrCodeRequest;
import com.example.qrcodegenerator.model.QrCodeType;
import com.example.qrcodegenerator.model.OutputFormatEnum; // Import OutputFormatEnum
import com.example.qrcodegenerator.service.QRCodeService;
import com.example.qrcodegenerator.service.formatter.TextQrDataFormatter; // For the key
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/qrcode")
public class QRCodeController {

    private final QRCodeService qrCodeService;

    public QRCodeController(QRCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateQrCode(@Valid @RequestBody GenerateQrCodeRequest request) {
        QrCodeType type;
        try {
            type = QrCodeType.valueOf(request.getQrCodeType().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid qrCodeType: " + request.getQrCodeType()));
        }

        // Prepare parameters for the service
        Map<String, String> params = new HashMap<>(request.getParams()); // Start with existing params

        // For TEXT and URL types, the 'data' field from the request is the primary content.
        // We put it into the params map with a key that TextQrDataFormatter expects.
        if (type == QrCodeType.TEXT || type == QrCodeType.URL) {
            if (request.getData() == null || request.getData().trim().isEmpty()) {
                 return ResponseEntity.badRequest().body(Map.of("error", "'data' field is required for type " + type));
            }
            params.put(TextQrDataFormatter.KEY_TEXT_DATA, request.getData());
        }
        // For other types, 'data' might be ignored if 'params' is comprehensive,
        // or it could be added to params under a specific key if needed.
        // For now, we rely on formatters to use what they need from params.
        // If params is empty after this, and the formatter expects something, it will fail.
        // This check is now inside the service, which is better.

        ErrorCorrectionLevelEnum errorCorrectionLevel;
        try {
            errorCorrectionLevel = ErrorCorrectionLevelEnum.fromString(request.getErrorCorrectionLevel());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }

        try {
            byte[] qrCodeImage = qrCodeService.generateQRCode(
                    type,
                    params,
                    request.getWidth(),
                    request.getHeight(),
                    errorCorrectionLevel,
                    request.getForegroundColor(),
                    request.getBackgroundColor(),
                    outputFormat // Pass OutputFormatEnum to service
            );

            MediaType contentType = MediaType.IMAGE_PNG; // Default
            if (outputFormat == OutputFormatEnum.SVG) {
                contentType = MediaType.valueOf("image/svg+xml");
            }

            return ResponseEntity.ok()
                    .contentType(contentType)
                    .body(qrCodeImage);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (UnsupportedOperationException e) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            // Log e.getMessage() or e
            return ResponseEntity.internalServerError().body(Map.of("error", "An unexpected error occurred while generating QR code."));
        }
    }
}
