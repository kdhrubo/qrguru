package com.example.qrcodegenerator.service;

import com.example.qrcodegenerator.model.ErrorCorrectionLevelEnum;
import com.example.qrcodegenerator.model.OutputFormatEnum;
import com.example.qrcodegenerator.model.QrCodeType;
import com.example.qrcodegenerator.service.formatter.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QRCodeServiceTest {

    private QRCodeService qrCodeService;

    @Mock
    private TextQrDataFormatter textFormatter;
    @Mock
    private VCardQrDataFormatter vCardFormatter;
    @Mock
    private WifiQrDataFormatter wifiFormatter;
    @Mock
    private EmailQrDataFormatter emailFormatter;
    @Mock
    private SmsQrDataFormatter smsFormatter;
    
    // NoOpQrDataFormatter is not explicitly mocked as its 'supports' method currently returns false.
    // If it were to support some types, it would need to be part of this list.
    @Mock
    private NoOpQrDataFormatter noOpFormatter;


    private List<QrDataFormatter> formatters;

    @BeforeEach
    void setUp() {
        // Order can be important if multiple formatters could support the same type (not the case here)
        formatters = Arrays.asList(textFormatter, vCardFormatter, wifiFormatter, emailFormatter, smsFormatter, noOpFormatter);
        qrCodeService = new QRCodeService(formatters);
    }

    private void setupFormatterSupport(QrDataFormatter formatter, QrCodeType type, String formattedData) {
        when(formatter.supports(type)).thenReturn(true);
        // Only stub format if it's actually going to be called for this type.
        // If another formatter for the same type is earlier in the list, this one's format() won't be hit.
        lenient().when(formatter.format(anyMap())).thenReturn(formattedData);
    }
    
    private void setupFormatterSupportOnly(QrDataFormatter formatter, QrCodeType type) {
         when(formatter.supports(type)).thenReturn(true);
    }


    // Test Data Formatting Dispatch
    @Test
    void generateQRCode_textType_usesTextFormatter() {
        Map<String, String> params = Map.of(TextQrDataFormatter.KEY_TEXT_DATA, "Hello Text");
        setupFormatterSupport(textFormatter, QrCodeType.TEXT, "Hello Text");
        
        qrCodeService.generateQRCode(QrCodeType.TEXT, params, 200, 200, ErrorCorrectionLevelEnum.M, "#000000", "#FFFFFF", OutputFormatEnum.PNG);
        
        verify(textFormatter).format(params);
        verify(textFormatter).supports(QrCodeType.TEXT);
        verifyNoMoreInteractions(textFormatter); // Ensures format isn't called unexpectedly multiple times

        // Verify other formatters were not called to format
        verify(vCardFormatter, never()).format(anyMap());
        verify(wifiFormatter, never()).format(anyMap());
        verify(emailFormatter, never()).format(anyMap());
        verify(smsFormatter, never()).format(anyMap());
    }

    @Test
    void generateQRCode_vCardType_usesVCardFormatter() {
        Map<String, String> params = Map.of(VCardQrDataFormatter.KEY_VCARD_FIRST_NAME, "John");
        setupFormatterSupport(vCardFormatter, QrCodeType.VCARD, "BEGIN:VCARD...");
        
        qrCodeService.generateQRCode(QrCodeType.VCARD, params, 200, 200, ErrorCorrectionLevelEnum.M, "#000000", "#FFFFFF", OutputFormatEnum.PNG);
        
        verify(vCardFormatter).format(params);
        verify(vCardFormatter).supports(QrCodeType.VCARD);
        verifyNoMoreInteractions(vCardFormatter);

        verify(textFormatter, never()).format(anyMap());
    }
    
    // Test Error Correction Levels
    @Test
    void generateQRCode_withAllErrorCorrectionLevels_generatesSuccessfully() {
        Map<String, String> params = Map.of(TextQrDataFormatter.KEY_TEXT_DATA, "Test Error Levels");
        // Only need supports for this test, and a valid format response
        when(textFormatter.supports(QrCodeType.TEXT)).thenReturn(true);
        when(textFormatter.format(params)).thenReturn("Test Error Levels");


        for (ErrorCorrectionLevelEnum level : ErrorCorrectionLevelEnum.values()) {
            assertDoesNotThrow(() -> 
                qrCodeService.generateQRCode(QrCodeType.TEXT, params, 200, 200, level, "#000000", "#FFFFFF", OutputFormatEnum.PNG),
                "Failed for error correction level: " + level
            );
        }
    }

    // Test Color Customization (PNG & SVG)
    @Test
    void generateQRCode_pngWithCustomColors_generatesSuccessfully() {
        Map<String, String> params = Map.of(TextQrDataFormatter.KEY_TEXT_DATA, "Test PNG Colors");
        setupFormatterSupport(textFormatter, QrCodeType.TEXT, "Test PNG Colors");

        byte[] qrCode = qrCodeService.generateQRCode(QrCodeType.TEXT, params, 200, 200, ErrorCorrectionLevelEnum.M, "#FF0000", "#00FF00", OutputFormatEnum.PNG);
        assertNotNull(qrCode);
        assertTrue(qrCode.length > 0);
        // Deep byte inspection for PNG colors is complex. Trusting ZXing if no error.
    }

    @Test
    void generateQRCode_svgWithCustomColors_containsCorrectFillAttributes() {
        Map<String, String> params = Map.of(TextQrDataFormatter.KEY_TEXT_DATA, "Test SVG Colors");
        setupFormatterSupport(textFormatter, QrCodeType.TEXT, "Test SVG Colors");

        byte[] svgBytes = qrCodeService.generateQRCode(QrCodeType.TEXT, params, 200, 200, ErrorCorrectionLevelEnum.M, "#123456", "#ABCDEF", OutputFormatEnum.SVG);
        String svgContent = new String(svgBytes, StandardCharsets.UTF_8);

        assertTrue(svgContent.contains("fill=\"#abcdef\""), "SVG should contain background color #abcdef. Actual: " + svgContent); 
        assertTrue(svgContent.contains("fill=\"#123456\""), "SVG should contain foreground color #123456. Actual: " + svgContent);
    }
    
    @Test
    void generateQRCode_svgWithShortHexColors_containsCorrectFillAttributes() {
        Map<String, String> params = Map.of(TextQrDataFormatter.KEY_TEXT_DATA, "Test SVG Short Colors");
        setupFormatterSupport(textFormatter, QrCodeType.TEXT, "Test SVG Short Colors");

        byte[] svgBytes = qrCodeService.generateQRCode(QrCodeType.TEXT, params, 200, 200, ErrorCorrectionLevelEnum.M, "#F00", "#0F0", OutputFormatEnum.SVG);
        String svgContent = new String(svgBytes, StandardCharsets.UTF_8);
        
        assertTrue(svgContent.contains("fill=\"#00ff00\""), "SVG should contain background color #00ff00. Actual: " + svgContent);
        assertTrue(svgContent.contains("fill=\"#ff0000\""), "SVG should contain foreground color #ff0000. Actual: " + svgContent);
    }


    // Test SVG Output
    @Test
    void generateQRCode_svgOutput_producesValidSvgStructure() {
        Map<String, String> params = Map.of(TextQrDataFormatter.KEY_TEXT_DATA, "Test SVG Output");
        setupFormatterSupport(textFormatter, QrCodeType.TEXT, "Test SVG Output");

        int width = 300;
        int height = 350; // Asymmetric to test attributes
        byte[] svgBytes = qrCodeService.generateQRCode(QrCodeType.TEXT, params, width, height, ErrorCorrectionLevelEnum.Q, "#333333", "#EEEEEE", OutputFormatEnum.SVG);
        String svgContent = new String(svgBytes, StandardCharsets.UTF_8);

        assertTrue(svgContent.startsWith("<svg xmlns=\"http://www.w3.org/2000/svg\""), "SVG should start with <svg> tag");
        assertTrue(svgContent.endsWith("</svg>"), "SVG should end with </svg> tag");
        assertTrue(svgContent.contains("<path d="), "SVG should contain <path> element");
        assertTrue(svgContent.contains("<rect "), "SVG should contain <rect> element for background");
        assertTrue(svgContent.contains(String.format("width=\"%d\"", width)), "SVG width attribute mismatch");
        assertTrue(svgContent.contains(String.format("height=\"%d\"", height)), "SVG height attribute mismatch");
        assertTrue(svgContent.matches(".*viewBox=\"0 0 \\d+ \\d+\".*"), "SVG should contain valid viewBox attribute");
    }

    // Test UnsupportedOperationException
    @Test
    void generateQRCode_unsupportedType_throwsUnsupportedOperationException() {
        Map<String, String> params = Map.of("data", "some geo data");
        
        // Reset mocks for formatters to ensure none claim to support an unused type (e.g. VCARD here if not explicitly set up)
        // This makes the test more robust by ensuring that if no formatter.supports() returns true, the service throws.
        formatters.forEach(f -> when(f.supports(any(QrCodeType.class))).thenReturn(false));


        // Example: QrCodeType.VCARD is a valid enum, but if vCardFormatter.supports(VCARD) was false, it would fail.
        Exception exception = assertThrows(UnsupportedOperationException.class, () -> {
            qrCodeService.generateQRCode(QrCodeType.VCARD, params, 200, 200, ErrorCorrectionLevelEnum.M, "#000000", "#FFFFFF", OutputFormatEnum.PNG);
        });
        assertEquals("QR code type 'VCARD' is not supported.", exception.getMessage());
    }
    
    @Test
    void generateQRCode_nullParams_throwsIllegalArgumentException() {
         Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            qrCodeService.generateQRCode(QrCodeType.TEXT, null, 200, 200, ErrorCorrectionLevelEnum.M, "#000", "#FFF", OutputFormatEnum.PNG);
        });
        assertEquals("Parameters for QR code cannot be null or empty.", exception.getMessage());
    }

    @Test
    void generateQRCode_emptyParams_throwsIllegalArgumentException() {
         Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            qrCodeService.generateQRCode(QrCodeType.TEXT, Collections.emptyMap(), 200, 200, ErrorCorrectionLevelEnum.M, "#000", "#FFF", OutputFormatEnum.PNG);
        });
        assertEquals("Parameters for QR code cannot be null or empty.", exception.getMessage());
    }
    
    @Test
    void generateQRCode_formatterReturnsNullOrEmpty_throwsIllegalArgumentException() {
        Map<String, String> params = Map.of(TextQrDataFormatter.KEY_TEXT_DATA, "Data");
        setupFormatterSupport(textFormatter, QrCodeType.TEXT, ""); // Formatter returns empty string

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            qrCodeService.generateQRCode(QrCodeType.TEXT, params, 200, 200, ErrorCorrectionLevelEnum.M, "#000", "#FFF", OutputFormatEnum.PNG);
        });
        assertEquals("Formatted data for QR code cannot be null or empty.", exception.getMessage());

        // Re-setup for next case (null return)
        // Need to reset the specific mock interaction for format() or use lenient() if it's called multiple times across tests with different outcomes.
        // For simplicity, we assume this is the only call to format() in this test for this specific setup.
        when(textFormatter.format(anyMap())).thenReturn(null); // Formatter returns null
        
        exception = assertThrows(IllegalArgumentException.class, () -> {
            qrCodeService.generateQRCode(QrCodeType.TEXT, params, 200, 200, ErrorCorrectionLevelEnum.M, "#000", "#FFF", OutputFormatEnum.PNG);
        });
        assertEquals("Formatted data for QR code cannot be null or empty.", exception.getMessage());
    }
}
