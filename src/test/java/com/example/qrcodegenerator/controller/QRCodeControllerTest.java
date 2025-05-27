package com.example.qrcodegenerator.controller;

import com.example.qrcodegenerator.dto.GenerateQrCodeRequest;
import com.example.qrcodegenerator.model.ErrorCorrectionLevelEnum;
import com.example.qrcodegenerator.model.OutputFormatEnum;
import com.example.qrcodegenerator.model.QrCodeType;
import com.example.qrcodegenerator.service.QRCodeService;
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

    private GenerateQrCodeRequest createDefaultRequest() {
        GenerateQrCodeRequest request = new GenerateQrCodeRequest();
        request.setQrCodeType("TEXT");
        request.setData("Default Data");
        // Other fields will use their defaults (width, height, errorCorrectionLevel, colors, outputFormat)
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
                eq(expectedParams), // Verifying how params map is constructed
                eq(request.getWidth()),
                eq(request.getHeight()),
                eq(ErrorCorrectionLevelEnum.M),
                eq(request.getForegroundColor()),
                eq(request.getBackgroundColor()),
                eq(OutputFormatEnum.PNG)
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
                any(OutputFormatEnum.class)
        );
    }
    
    @Test
    void generateQrCode_vCardType_success() throws Exception {
        GenerateQrCodeRequest request = createDefaultRequest();
        request.setQrCodeType("VCARD");
        request.setData(""); // Data field might be ignored or used differently for complex types
        Map<String, String> vcardParams = new HashMap<>();
        vcardParams.put("firstName", "John");
        vcardParams.put("lastName", "Doe");
        request.setParams(vcardParams);

        byte[] dummyPngBytes = "vcard-bytes".getBytes();
        given(qrCodeService.generateQRCode(
                eq(QrCodeType.VCARD),
                eq(vcardParams), 
                anyInt(), anyInt(), any(ErrorCorrectionLevelEnum.class),
                anyString(), anyString(), any(OutputFormatEnum.class)
        )).willReturn(dummyPngBytes);

        mockMvc.perform(post("/api/qrcode/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        
        verify(qrCodeService).generateQRCode(
                eq(QrCodeType.VCARD),
                eq(vcardParams),
                anyInt(), anyInt(), any(ErrorCorrectionLevelEnum.class),
                anyString(), anyString(), any(OutputFormatEnum.class)
        );
    }
    
    // Similar tests for WIFI, EMAIL, SMS types would follow the VCARD pattern,
    // setting appropriate qrCodeType and params.

    @ParameterizedTest
    @CsvSource({"L,L", "M,M", "Q,Q", "H,H", " M ,M", ",M"}) // Test with spaces and empty (default)
    void generateQrCode_validErrorCorrectionLevels_callsServiceWithCorrectEnum(String inputLevel, String expectedEnumName) throws Exception {
        GenerateQrCodeRequest request = createDefaultRequest();
        if (inputLevel != null && inputLevel.isEmpty()) { // Simulate empty string from CSV source for default case
             request.setErrorCorrectionLevel(""); // Test default behavior in enum parsing
        } else if (inputLevel != null) {
            request.setErrorCorrectionLevel(inputLevel);
        }
        // else inputLevel is null, DTO default 'M' applies. CsvSource doesn't make nulls easily.
        // For null, we would need a separate test or different source.

        ErrorCorrectionLevelEnum expectedEnum = ErrorCorrectionLevelEnum.valueOf(expectedEnumName);
        
        given(qrCodeService.generateQRCode(any(QrCodeType.class), anyMap(), anyInt(), anyInt(), 
                                           eq(expectedEnum), 
                                           anyString(), anyString(), any(OutputFormatEnum.class)))
            .willReturn("dummy".getBytes());


        mockMvc.perform(post("/api/qrcode/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(qrCodeService).generateQRCode(any(QrCodeType.class), anyMap(), anyInt(), anyInt(), 
                                           eq(expectedEnum), 
                                           anyString(), anyString(), any(OutputFormatEnum.class));
    }

    @Test
    void generateQrCode_invalidErrorCorrectionLevel_returnsBadRequest() throws Exception {
        GenerateQrCodeRequest request = createDefaultRequest();
        request.setErrorCorrectionLevel("X"); // Invalid level

        mockMvc.perform(post("/api/qrcode/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid error correction level: 'X'. Allowed values are L, M, Q, H (case-insensitive)."));
    }

    @Test
    void generateQrCode_validColors_callsServiceWithColors() throws Exception {
        GenerateQrCodeRequest request = createDefaultRequest();
        request.setForegroundColor("#112233");
        request.setBackgroundColor("#AABBCC");

        mockMvc.perform(post("/api/qrcode/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(qrCodeService).generateQRCode(any(QrCodeType.class), anyMap(), anyInt(), anyInt(), 
                                           any(ErrorCorrectionLevelEnum.class), 
                                           eq("#112233"), eq("#AABBCC"), 
                                           any(OutputFormatEnum.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"#12345", "invalidColor", "#GGHHII"})
    void generateQrCode_invalidForegroundColor_returnsBadRequestDueToDtoValidation(String invalidColor) throws Exception {
        GenerateQrCodeRequest request = createDefaultRequest();
        request.setForegroundColor(invalidColor);

        ResultActions result = mockMvc.perform(post("/api/qrcode/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));
        
        result.andExpect(status().isBadRequest());
        // For DTO validation, the error message structure might be different (e.g. fieldErrors)
        // Depending on global exception handler setup. Spring default is usually a list of errors.
        // For simplicity, we just check for bad request. A more specific check might be:
        // .andExpect(jsonPath("$.errors[0].defaultMessage").value(org.hamcrest.Matchers.containsString("Invalid hex color format for foregroundColor")));
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"#12345", "invalidColor", "#GGHHII"})
    void generateQrCode_invalidBackgroundColor_returnsBadRequestDueToDtoValidation(String invalidColor) throws Exception {
        GenerateQrCodeRequest request = createDefaultRequest();
        request.setBackgroundColor(invalidColor);

        mockMvc.perform(post("/api/qrcode/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void generateQrCode_svgOutputFormat_callsServiceAndSetsContentType() throws Exception {
        GenerateQrCodeRequest request = createDefaultRequest();
        request.setOutputFormat("SVG");
        byte[] dummySvgBytes = "<svg>...</svg>".getBytes();

        given(qrCodeService.generateQRCode(any(QrCodeType.class), anyMap(), anyInt(), anyInt(), 
                                           any(ErrorCorrectionLevelEnum.class), anyString(), anyString(), 
                                           eq(OutputFormatEnum.SVG)))
            .willReturn(dummySvgBytes);

        mockMvc.perform(post("/api/qrcode/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.valueOf("image/svg+xml")))
                .andExpect(content().bytes(dummySvgBytes));

        verify(qrCodeService).generateQRCode(any(QrCodeType.class), anyMap(), anyInt(), anyInt(), 
                                           any(ErrorCorrectionLevelEnum.class), anyString(), anyString(), 
                                           eq(OutputFormatEnum.SVG));
    }
    
    @Test
    void generateQrCode_pngOutputFormatExplicit_callsServiceAndSetsContentType() throws Exception {
        GenerateQrCodeRequest request = createDefaultRequest();
        request.setOutputFormat("PNG"); // Explicitly PNG
        byte[] dummyPngBytes = "dummy-png".getBytes();

        given(qrCodeService.generateQRCode(any(QrCodeType.class), anyMap(), anyInt(), anyInt(), 
                                           any(ErrorCorrectionLevelEnum.class), anyString(), anyString(), 
                                           eq(OutputFormatEnum.PNG)))
            .willReturn(dummyPngBytes);

        mockMvc.perform(post("/api/qrcode/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(dummyPngBytes));
    }
    
    @Test
    void generateQrCode_invalidOutputFormat_defaultsToPngAndSetsPngContentType() throws Exception {
        GenerateQrCodeRequest request = createDefaultRequest();
        request.setOutputFormat("INVALID_FORMAT"); // Invalid, should default to PNG
        byte[] dummyPngBytes = "default-png".getBytes();

        // Controller defaults to PNG if OutputFormatEnum.fromString provides default
        given(qrCodeService.generateQRCode(any(QrCodeType.class), anyMap(), anyInt(), anyInt(), 
                                           any(ErrorCorrectionLevelEnum.class), anyString(), anyString(), 
                                           eq(OutputFormatEnum.PNG))) // Expecting PNG due to default
            .willReturn(dummyPngBytes);

        mockMvc.perform(post("/api/qrcode/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(dummyPngBytes));
    }


    @Test
    void generateQrCode_serviceThrowsUnsupportedOperationException_returnsNotImplemented() throws Exception {
        GenerateQrCodeRequest request = createDefaultRequest();
        request.setQrCodeType("UNSUPPORTED_TYPE_FOR_TEST"); // A type that will cause the exception

        given(qrCodeService.generateQRCode(
                eq(QrCodeType.valueOf(request.getQrCodeType())), // Use the actual enum value
                anyMap(), anyInt(), anyInt(), any(ErrorCorrectionLevelEnum.class),
                anyString(), anyString(), any(OutputFormatEnum.class)
        )).willThrow(new UnsupportedOperationException("Test: Type not supported"));

        mockMvc.perform(post("/api/qrcode/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotImplemented()) // HTTP 501
                .andExpect(jsonPath("$.error").value("Test: Type not supported"));
    }
    
    @Test
    void generateQrCode_serviceThrowsIllegalArgumentException_returnsBadRequest() throws Exception {
        GenerateQrCodeRequest request = createDefaultRequest();
        request.setData(""); // This might cause an IAE from a formatter or service pre-check

        given(qrCodeService.generateQRCode(any(QrCodeType.class), anyMap(), anyInt(), anyInt(), 
                                           any(ErrorCorrectionLevelEnum.class), anyString(), anyString(), 
                                           any(OutputFormatEnum.class)))
            .willThrow(new IllegalArgumentException("Test: Invalid argument from service"));

        mockMvc.perform(post("/api/qrcode/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Test: Invalid argument from service"));
    }

    @Test
    void generateQrCode_dtoValidationFailsForData_returnsBadRequest() throws Exception {
        GenerateQrCodeRequest request = createDefaultRequest();
        request.setData(null); // @NotBlank constraint

        mockMvc.perform(post("/api/qrcode/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        // More specific check for DTO validation error message can be added here
    }
}
