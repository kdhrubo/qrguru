package com.example.qrcodegenerator.controller;

import com.example.qrcodegenerator.dto.GenerateQrCodeRequest;
import com.example.qrcodegenerator.model.ErrorCorrectionLevelEnum;
import com.example.qrcodegenerator.model.OutputFormatEnum;
import com.example.qrcodegenerator.model.QrCodeType;
import com.example.qrcodegenerator.service.QRCodeService;
import com.example.qrcodegenerator.service.formatter.CalendarEventQrDataFormatter;
import com.example.qrcodegenerator.service.formatter.GeoLocationQrDataFormatter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;


import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QRCodeController.class)
class QRCodeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private QRCodeService qrCodeService;

    // Valid 1x1 PNG Base64 for logo tests
    private static final String VALID_LOGO_RAW_BASE64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==";

    private GenerateQrCodeRequest createDefaultRequest() {
        GenerateQrCodeRequest request = new GenerateQrCodeRequest();
        request.setQrCodeType("TEXT");
        request.setData("Default Data");
        // Other fields will use their defaults (width, height, errorCorrectionLevel, colors, outputFormat, logoFactor)
        return request;
    }

    @Test
    void generateQrCode_textType_success() throws Exception {
        GenerateQrCodeRequest request = createDefaultRequest();
        request.setQrCodeType("TEXT");
        request.setData("Hello Text!");
        Map<String, String> expectedParams = Map.of("text_data", "Hello Text!");

        byte[] dummyPngBytes = "dummy-png-bytes".getBytes();
        given(qrCodeService.generateQRCode(
                eq(QrCodeType.TEXT),
                eq(expectedParams), 
                eq(request.getWidth()),
                eq(request.getHeight()),
                eq(ErrorCorrectionLevelEnum.M),
                eq(request.getForegroundColor()),
                eq(request.getBackgroundColor()),
                eq(OutputFormatEnum.PNG),
                eq(request.getLogoBase64()), // null by default
                eq(request.getLogoTargetAreaFactor()), // 0.2 by default
                eq(request.getFrameText()), // null by default
                eq(request.getFrameColor()), // #000000 by default
                eq(request.getFramePadding()) // null by default
        )).willReturn(dummyPngBytes);

        mockMvc.perform(post("/api/qrcode/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(dummyPngBytes));

        verify(qrCodeService).generateQRCode(
                eq(QrCodeType.TEXT),
                eq(expectedParams),
                anyInt(), anyInt(),
                any(ErrorCorrectionLevelEnum.class),
                anyString(), anyString(),
                any(OutputFormatEnum.class),
                any(), any(), // logoBase64, logoTargetAreaFactor
                any(), anyString(), any() // frameText, frameColor, framePadding
        );
    }
    
    // --- New QR Type Tests ---
    @Test
    void generateQrCode_calendarType_success() throws Exception {
        GenerateQrCodeRequest request = createDefaultRequest();
        request.setQrCodeType("CALENDAR");
        request.setData(""); // Data might be ignored for complex types if params are used
        Map<String, String> calendarParams = new HashMap<>();
        calendarParams.put(CalendarEventQrDataFormatter.KEY_CALENDAR_SUMMARY, "Team Meeting");
        calendarParams.put(CalendarEventQrDataFormatter.KEY_CALENDAR_DTSTART, "20240101T100000Z");
        calendarParams.put(CalendarEventQrDataFormatter.KEY_CALENDAR_DTEND, "20240101T110000Z");
        request.setParams(calendarParams);

        given(qrCodeService.generateQRCode(
                eq(QrCodeType.CALENDAR), eq(calendarParams), anyInt(), anyInt(), any(ErrorCorrectionLevelEnum.class),
                anyString(), anyString(), any(OutputFormatEnum.class), any(), any(), any(), anyString(), any()
        )).willReturn("dummy-calendar-qr".getBytes());

        mockMvc.perform(post("/api/qrcode/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        
        verify(qrCodeService).generateQRCode(
            eq(QrCodeType.CALENDAR), eq(calendarParams), anyInt(), anyInt(), any(ErrorCorrectionLevelEnum.class),
            anyString(), anyString(), any(OutputFormatEnum.class), any(), any(), any(), anyString(), any()
        );
    }

    @Test
    void generateQrCode_geoLocationType_success() throws Exception {
        GenerateQrCodeRequest request = createDefaultRequest();
        request.setQrCodeType("GEO");
        request.setData(""); 
        Map<String, String> geoParams = new HashMap<>();
        geoParams.put(GeoLocationQrDataFormatter.KEY_GEO_LATITUDE, "34.0522");
        geoParams.put(GeoLocationQrDataFormatter.KEY_GEO_LONGITUDE, "-118.2437");
        request.setParams(geoParams);

        given(qrCodeService.generateQRCode(
                eq(QrCodeType.GEO), eq(geoParams), anyInt(), anyInt(), any(ErrorCorrectionLevelEnum.class),
                anyString(), anyString(), any(OutputFormatEnum.class), any(), any(), any(), anyString(), any()
        )).willReturn("dummy-geo-qr".getBytes());

        mockMvc.perform(post("/api/qrcode/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(qrCodeService).generateQRCode(
            eq(QrCodeType.GEO), eq(geoParams), anyInt(), anyInt(), any(ErrorCorrectionLevelEnum.class),
            anyString(), anyString(), any(OutputFormatEnum.class), any(), any(), any(), anyString(), any()
        );
    }

    // --- Logo Parameter Tests ---
    @Test
    void generateQrCode_withValidLogoParams_callsServiceWithLogoParams() throws Exception {
        GenerateQrCodeRequest request = createDefaultRequest();
        request.setLogoBase64(VALID_LOGO_RAW_BASE64);
        request.setLogoTargetAreaFactor(0.15);

        mockMvc.perform(post("/api/qrcode/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(qrCodeService).generateQRCode(
            any(QrCodeType.class), anyMap(), anyInt(), anyInt(), any(ErrorCorrectionLevelEnum.class),
            anyString(), anyString(), any(OutputFormatEnum.class),
            eq(VALID_LOGO_RAW_BASE64), eq(0.15), // Verify logo params
            any(), anyString(), any()
        );
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.05, 0.35}) // Below min 0.1, above max 0.3
    void generateQrCode_invalidLogoTargetAreaFactor_returnsBadRequest(double invalidFactor) throws Exception {
        GenerateQrCodeRequest request = createDefaultRequest();
        request.setLogoTargetAreaFactor(invalidFactor);

        mockMvc.perform(post("/api/qrcode/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); 
                // DTO validation handles this. Specific error message check can be added.
    }
    
    // --- EPS Output Test ---
    @Test
    void generateQrCode_epsOutputFormat_callsServiceAndSetsEpsContentType() throws Exception {
        GenerateQrCodeRequest request = createDefaultRequest();
        request.setOutputFormat("EPS");
        byte[] dummyEpsBytes = "%!PS-Adobe-3.0 EPSF-3.0 ...".getBytes();

        given(qrCodeService.generateQRCode(
                any(QrCodeType.class), anyMap(), anyInt(), anyInt(), any(ErrorCorrectionLevelEnum.class),
                anyString(), anyString(), eq(OutputFormatEnum.EPS), // Expect EPS enum
                any(), any(), any(), anyString(), any()
        )).willReturn(dummyEpsBytes);

        mockMvc.perform(post("/api/qrcode/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.valueOf("application/postscript")))
                .andExpect(content().bytes(dummyEpsBytes));

        verify(qrCodeService).generateQRCode(
            any(QrCodeType.class), anyMap(), anyInt(), anyInt(), any(ErrorCorrectionLevelEnum.class),
            anyString(), anyString(), eq(OutputFormatEnum.EPS),
            any(), any(), any(), anyString(), any()
        );
    }


    // --- Existing Tests (abbreviated, ensure they still pass or adapt them) ---
    @ParameterizedTest
    @CsvSource({"L,L", "M,M", "Q,Q", "H,H", " M ,M", ",M"})
    void generateQrCode_validErrorCorrectionLevels_callsServiceWithCorrectEnum(String inputLevel, String expectedEnumName) throws Exception {
        GenerateQrCodeRequest request = createDefaultRequest();
        if (inputLevel != null && inputLevel.isEmpty()) { 
             request.setErrorCorrectionLevel(""); 
        } else if (inputLevel != null) {
            request.setErrorCorrectionLevel(inputLevel);
        }
        ErrorCorrectionLevelEnum expectedEnum = ErrorCorrectionLevelEnum.valueOf(expectedEnumName);
        
        given(qrCodeService.generateQRCode(any(QrCodeType.class), anyMap(), anyInt(), anyInt(), 
                                           eq(expectedEnum), 
                                           anyString(), anyString(), any(OutputFormatEnum.class),
                                           any(), any(), any(), anyString(), any()))
            .willReturn("dummy".getBytes());

        mockMvc.perform(post("/api/qrcode/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void generateQrCode_invalidErrorCorrectionLevel_returnsBadRequest() throws Exception {
        GenerateQrCodeRequest request = createDefaultRequest();
        request.setErrorCorrectionLevel("X"); 

        mockMvc.perform(post("/api/qrcode/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid error correction level: 'X'. Allowed values are L, M, Q, H (case-insensitive)."));
    }
    
    @Test
    void generateQrCode_svgOutputFormat_callsServiceAndSetsContentType() throws Exception {
        GenerateQrCodeRequest request = createDefaultRequest();
        request.setOutputFormat("SVG");
        byte[] dummySvgBytes = "<svg>...</svg>".getBytes();

        given(qrCodeService.generateQRCode(any(QrCodeType.class), anyMap(), anyInt(), anyInt(), 
                                           any(ErrorCorrectionLevelEnum.class), anyString(), anyString(), 
                                           eq(OutputFormatEnum.SVG), any(), any(), any(), anyString(), any()))
            .willReturn(dummySvgBytes);

        mockMvc.perform(post("/api/qrcode/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.valueOf("image/svg+xml")))
                .andExpect(content().bytes(dummySvgBytes));
    }
    
    @Test
    void generateQrCode_serviceThrowsUnsupportedOperationException_returnsNotImplemented() throws Exception {
        GenerateQrCodeRequest request = createDefaultRequest();
        // Use a QrCodeType that will be converted to enum for the service call
        QrCodeType typeToMakeUnsupported = QrCodeType.VCARD; // Example
        request.setQrCodeType(typeToMakeUnsupported.name()); 

        given(qrCodeService.generateQRCode(
                eq(typeToMakeUnsupported), 
                anyMap(), anyInt(), anyInt(), any(ErrorCorrectionLevelEnum.class),
                anyString(), anyString(), any(OutputFormatEnum.class), any(), any(), any(), anyString(), any()
        )).willThrow(new UnsupportedOperationException("Test: Type not supported"));

        mockMvc.perform(post("/api/qrcode/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotImplemented()) 
                .andExpect(jsonPath("$.error").value("Test: Type not supported"));
    }
}
