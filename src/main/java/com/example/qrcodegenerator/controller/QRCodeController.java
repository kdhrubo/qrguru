package com.example.qrcodegenerator.controller;

import com.example.qrcodegenerator.service.QRCodeService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/qrcode")
public class QRCodeController {

    private final QRCodeService qrCodeService;

    // Constructor injection for QRCodeService
    public QRCodeController(QRCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }

    @GetMapping("/generate")
    public ResponseEntity<byte[]> generate(
            @RequestParam String data,
            @RequestParam(defaultValue = "250") int width,
            @RequestParam(defaultValue = "250") int height) {
        
        try {
            byte[] qrCodeImage = qrCodeService.generateQRCode(data, width, height);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(qrCodeImage);
        } catch (IllegalArgumentException e) {
            // Handle cases like empty data or invalid dimensions
            return ResponseEntity.badRequest().body(null); // Or a more descriptive error response
        } catch (RuntimeException e) {
            // Handle errors from QR code generation (e.g., WriterException)
            // Consider logging the exception e.getMessage()
            return ResponseEntity.internalServerError().body(null); // Or a more descriptive error response
        }
    }
}
