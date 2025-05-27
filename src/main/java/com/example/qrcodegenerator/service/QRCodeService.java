package com.example.qrcodegenerator.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class QRCodeService {

    public byte[] generateQRCode(String data, int width, int height) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Data for QR code cannot be null or empty.");
        }
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be positive integers.");
        }

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            return pngOutputStream.toByteArray();

        } catch (WriterException e) {
            // Log the exception or handle it more gracefully
            // For now, wrap it in a runtime exception
            throw new RuntimeException("Error generating QR code: " + e.getMessage(), e);
        } catch (IOException e) {
            // This is less likely with ByteArrayOutputStream but good practice to handle
            throw new RuntimeException("Error writing QR code to byte stream: " + e.getMessage(), e);
        }
    }
}
