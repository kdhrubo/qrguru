package com.example.qrcodegenerator.controller;

import com.example.qrcodegenerator.service.QRCodeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QRCodeController.class)
public class QRCodeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QRCodeService qrCodeService;

    @Test
    void generate_success_returnsImagePng() throws Exception {
        String data = "testData";
        int width = 200;
        int height = 200;
        byte[] dummyBytes = "dummy-qr-code-image-bytes".getBytes();

        given(qrCodeService.generateQRCode(data, width, height)).willReturn(dummyBytes);

        mockMvc.perform(get("/api/qrcode/generate")
                        .param("data", data)
                        .param("width", String.valueOf(width))
                        .param("height", String.valueOf(height)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(dummyBytes));
    }

    @Test
    void generate_defaultWidthAndHeight_success() throws Exception {
        String data = "testDataDefault";
        byte[] dummyBytes = "dummy-qr-code-image-bytes-default".getBytes();

        // Default width and height are 250
        given(qrCodeService.generateQRCode(data, 250, 250)).willReturn(dummyBytes);

        mockMvc.perform(get("/api/qrcode/generate")
                        .param("data", data))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(dummyBytes));
    }
    
    @Test
    void generate_missingDataParam_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/qrcode/generate")
                        .param("width", "200")
                        .param("height", "200"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generate_serviceThrowsIllegalArgumentException_returnsBadRequest() throws Exception {
        String data = "badData";
        given(qrCodeService.generateQRCode(anyString(), anyInt(), anyInt()))
                .willThrow(new IllegalArgumentException("Test illegal argument"));

        mockMvc.perform(get("/api/qrcode/generate")
                        .param("data", data))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generate_serviceThrowsRuntimeException_returnsInternalServerError() throws Exception {
        String data = "errorData";
        given(qrCodeService.generateQRCode(anyString(), anyInt(), anyInt()))
                .willThrow(new RuntimeException("Test runtime exception"));

        mockMvc.perform(get("/api/qrcode/generate")
                        .param("data", data))
                .andExpect(status().isInternalServerError());
    }
}
