package com.example.qrcodegenerator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class QRCodeServiceTest {

    private QRCodeService qrCodeService;

    @BeforeEach
    void setUp() {
        qrCodeService = new QRCodeService();
    }

    @Test
    void generateQRCode_success() {
        String data = "Test QR Data";
        int width = 200;
        int height = 200;

        byte[] qrCodeBytes = qrCodeService.generateQRCode(data, width, height);

        assertNotNull(qrCodeBytes, "QR code bytes should not be null");
        assertTrue(qrCodeBytes.length > 0, "QR code bytes should not be empty");
        
        // Optional: Basic PNG check (first 8 bytes are PNG signature)
        // 89 50 4E 47 0D 0A 1A 0A
        byte[] pngSignature = {(byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47, (byte) 0x0D, (byte) 0x0A, (byte) 0x1A, (byte) 0x0A};
        assertTrue(qrCodeBytes.length >= pngSignature.length, "QR code bytes too short for PNG signature");
        for (int i = 0; i < pngSignature.length; i++) {
            assertEquals(pngSignature[i], qrCodeBytes[i], "Byte " + i + " of PNG signature does not match");
        }
    }

    @Test
    void generateQRCode_nullData_throwsIllegalArgumentException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            qrCodeService.generateQRCode(null, 200, 200);
        });
        assertEquals("Data for QR code cannot be null or empty.", exception.getMessage());
    }

    @Test
    void generateQRCode_emptyData_throwsIllegalArgumentException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            qrCodeService.generateQRCode("", 200, 200);
        });
        assertEquals("Data for QR code cannot be null or empty.", exception.getMessage());
    }

    @Test
    void generateQRCode_zeroWidth_throwsIllegalArgumentException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            qrCodeService.generateQRCode("Test Data", 0, 200);
        });
        assertEquals("Width and height must be positive integers.", exception.getMessage());
    }

    @Test
    void generateQRCode_negativeHeight_throwsIllegalArgumentException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            qrCodeService.generateQRCode("Test Data", 200, -50);
        });
        assertEquals("Width and height must be positive integers.", exception.getMessage());
    }
}
