package com.example.qrcodegenerator.controller;

import com.example.qrcodegenerator.model.UrlEntry;
import com.example.qrcodegenerator.service.UrlShortenerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UrlShortenerController.class)
public class UrlShortenerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // For converting request objects to JSON

    @MockBean
    private UrlShortenerService urlShortenerService;

    @Test
    void shortenUrl_success_returnsShortenedUrl() throws Exception {
        String originalUrl = "http://example.com/very/long/url";
        String shortCode = "tst123";
        UrlEntry urlEntry = new UrlEntry(1L, originalUrl, shortCode);
        
        UrlShortenerController.ShortenRequest request = new UrlShortenerController.ShortenRequest();
        request.setUrl(originalUrl);

        given(urlShortenerService.shortenUrl(originalUrl)).willReturn(urlEntry);

        // The controller constructs a full URL. We need to anticipate this.
        // For testing, we can assume a host. The controller uses WebRequest, which is harder to mock here.
        // So, we'll check for the core components in the response (shortCode, originalUrl).
        // A more complex test could involve mocking WebRequest or using a different approach.
        String expectedFullShortenedUrlContains = "/" + shortCode; // Simplified check

        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalUrl").value(originalUrl))
                .andExpect(jsonPath("$.shortCode").value(shortCode))
                .andExpect(jsonPath("$.fullShortenedUrl").isNotEmpty()) // Check it's there
                .andExpect(jsonPath("$.fullShortenedUrl").value(org.hamcrest.Matchers.containsString(expectedFullShortenedUrlContains)));
    }
    
    @Test
    void shortenUrl_nullUrlInRequest_returnsBadRequest() throws Exception {
        UrlShortenerController.ShortenRequest request = new UrlShortenerController.ShortenRequest();
        request.setUrl(null); // Explicitly set null

        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("URL must be provided."));
    }
    
    @Test
    void shortenUrl_emptyUrlInRequest_returnsBadRequest() throws Exception {
        UrlShortenerController.ShortenRequest request = new UrlShortenerController.ShortenRequest();
        request.setUrl(""); 

        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("URL must be provided."));
    }

    @Test
    void shortenUrl_serviceThrowsIllegalArgument_returnsBadRequest() throws Exception {
        String originalUrl = "http://invalid.com";
        UrlShortenerController.ShortenRequest request = new UrlShortenerController.ShortenRequest();
        request.setUrl(originalUrl);

        given(urlShortenerService.shortenUrl(originalUrl))
                .willThrow(new IllegalArgumentException("Invalid URL format"));

        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid URL format"));
    }

    @Test
    void redirectToOriginalUrl_existingShortCode_redirects() throws Exception {
        String shortCode = "tst123";
        String originalUrl = "http://example.com/redirect";
        given(urlShortenerService.getOriginalUrl(shortCode)).willReturn(Optional.of(originalUrl));

        mockMvc.perform(get("/{shortCode}", shortCode))
                .andExpect(status().isFound()) // 302
                .andExpect(header().string("Location", originalUrl));
    }
    
    @Test
    void redirectToOriginalUrl_existingShortCodeWithoutScheme_redirectsWithHttp() throws Exception {
        String shortCode = "tstSch";
        String originalUrlNoScheme = "example.com/redirect";
        String expectedUrlWithScheme = "http://" + originalUrlNoScheme;
        given(urlShortenerService.getOriginalUrl(shortCode)).willReturn(Optional.of(originalUrlNoScheme));

        mockMvc.perform(get("/{shortCode}", shortCode))
                .andExpect(status().isFound()) // 302
                .andExpect(header().string("Location", expectedUrlWithScheme));
    }

    @Test
    void redirectToOriginalUrl_nonExistentShortCode_returnsNotFound() throws Exception {
        String shortCode = "nosuch";
        given(urlShortenerService.getOriginalUrl(shortCode)).willReturn(Optional.empty());

        mockMvc.perform(get("/{shortCode}", shortCode))
                .andExpect(status().isNotFound());
    }
}
