package com.example.qrcodegenerator.service;

import com.example.qrcodegenerator.model.ErrorCorrectionLevelEnum;
import com.example.qrcodegenerator.model.QrCodeType;
import com.example.qrcodegenerator.model.OutputFormatEnum; // Import OutputFormatEnum
import com.example.qrcodegenerator.service.formatter.QrDataFormatter;
import com.example.qrcodegenerator.util.ColorUtil;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets; // Import StandardCharsets
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class QRCodeService {

    private final List<QrDataFormatter> formatters;

    public QRCodeService(List<QrDataFormatter> formatters) {
        this.formatters = formatters;
    }

    public byte[] generateQRCode(QrCodeType type, Map<String, String> params, int requestedWidth, int requestedHeight,
                                 ErrorCorrectionLevelEnum errorCorrectionLevelEnum,
                                 String foregroundColorHex, String backgroundColorHex,
                                 OutputFormatEnum outputFormat) { // Add outputFormat
        if (type == null) {
            throw new IllegalArgumentException("QrCodeType cannot be null.");
        }
        if (params == null || params.isEmpty()) {
            throw new IllegalArgumentException("Parameters for QR code cannot be null or empty.");
        }
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be positive integers.");
        }

        Optional<QrDataFormatter> formatterOpt = formatters.stream()
                .filter(f -> f.supports(type))
                .findFirst();

        if (formatterOpt.isEmpty()) {
            throw new UnsupportedOperationException("QR code type '" + type + "' is not supported.");
        }

        String dataToEncode = formatterOpt.get().format(params);
        if (dataToEncode == null || dataToEncode.isEmpty()) {
            throw new IllegalArgumentException("Formatted data for QR code cannot be null or empty.");
        }

        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            
            if (errorCorrectionLevelEnum != null) {
                hints.put(EncodeHintType.ERROR_CORRECTION, errorCorrectionLevelEnum.getZxingEnum());
            } else {
                // Default to M if somehow null, though controller should handle default from DTO
                hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            }
            // Consider adding EncodeHintType.MARGIN if you want to control the QR code margin

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            // Generate BitMatrix with requestedWidth and requestedHeight for encoding,
            // but the actual matrix dimensions will be determined by ZXing.
            // For SVG, we use these matrix dimensions for viewBox, and requestedWidth/Height for SVG element size.
            BitMatrix bitMatrix = qrCodeWriter.encode(dataToEncode, BarcodeFormat.QR_CODE, requestedWidth, requestedHeight, hints);

            if (outputFormat == OutputFormatEnum.SVG) {
                // SVG Generation
                Color fgColor = ColorUtil.parseHexColor(foregroundColorHex, Color.BLACK);
                Color bgColor = ColorUtil.parseHexColor(backgroundColorHex, Color.WHITE);
                String fgHex = String.format("#%02x%02x%02x", fgColor.getRed(), fgColor.getGreen(), fgColor.getBlue());
                String bgHex = String.format("#%02x%02x%02x", bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue());

                int matrixWidth = bitMatrix.getWidth();
                int matrixHeight = bitMatrix.getHeight();

                StringBuilder svgPath = new StringBuilder();
                for (int y = 0; y < matrixHeight; y++) {
                    for (int x = 0; x < matrixWidth; x++) {
                        if (bitMatrix.get(x, y)) {
                            svgPath.append(String.format("M%d,%d h1v1h-1z ", x, y));
                        }
                    }
                }
                // Trim trailing space for cleaner SVG
                if (svgPath.length() > 0) {
                    svgPath.setLength(svgPath.length() - 1);
                }


                String svgContent = String.format(
                    "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"%d\" height=\"%d\" viewBox=\"0 0 %d %d\">" +
                    "<rect x=\"0\" y=\"0\" width=\"%d\" height=\"%d\" fill=\"%s\"/>" +
                    "<path d=\"%s\" fill=\"%s\"/>" +
                    "</svg>",
                    requestedWidth, requestedHeight, // requested output dimensions for <svg> element
                    matrixWidth, matrixHeight,    // viewBox dimensions based on matrix
                    matrixWidth, matrixHeight, bgHex, // background rect dimensions
                    svgPath.toString(), fgHex         // path data and fill
                );
                return svgContent.getBytes(StandardCharsets.UTF_8);

            } else { // Default to PNG
                Color foreground = ColorUtil.parseHexColor(foregroundColorHex, Color.BLACK);
                Color background = ColorUtil.parseHexColor(backgroundColorHex, Color.WHITE);
                MatrixToImageConfig config = new MatrixToImageConfig(foreground.getRGB(), background.getRGB());

                ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
                MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream, config);
                return pngOutputStream.toByteArray();
            }

        } catch (WriterException e) {
            throw new RuntimeException("Error generating QR code: " + e.getMessage(), e);
        } catch (IOException e) { // Only relevant for PNG's ByteArrayOutputStream
            throw new RuntimeException("Error writing PNG QR code to byte stream: " + e.getMessage(), e);
        }
    }
}
