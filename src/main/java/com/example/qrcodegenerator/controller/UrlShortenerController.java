package com.example.qrcodegenerator.controller;

import com.example.qrcodegenerator.model.UrlEntry;
import com.example.qrcodegenerator.service.UrlShortenerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController; // Note: No base /api path for redirect

import java.net.URI;
import java.util.Map;
import java.util.Optional;

@RestController
public class UrlShortenerController {

    private final UrlShortenerService urlShortenerService;

    public UrlShortenerController(UrlShortenerService urlShortenerService) {
        this.urlShortenerService = urlShortenerService;
    }

    // DTO for the shorten request to make it more explicit
    // Could also be a simple Map<String, String> as initially planned
    public static class ShortenRequest {
        private String url;
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }
    
    // DTO for the shorten response
    public static class ShortenResponse {
        private String shortCode;
        private String originalUrl;
        private String fullShortenedUrl; // e.g. http://localhost:8080/<code>

        public ShortenResponse(String shortCode, String originalUrl, String fullShortenedUrl) {
            this.shortCode = shortCode;
            this.originalUrl = originalUrl;
            this.fullShortenedUrl = fullShortenedUrl;
        }
        public String getShortCode() { return shortCode; }
        public String getOriginalUrl() { return originalUrl; }
        public String getFullShortenedUrl() { return fullShortenedUrl; }
    }


    @PostMapping("/api/shorten")
    public ResponseEntity<?> shortenUrl(@RequestBody ShortenRequest request,
                                        org.springframework.web.context.request.WebRequest webRequest) {
        if (request == null || request.getUrl() == null || request.getUrl().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "URL must be provided."));
        }
        try {
            UrlEntry urlEntry = urlShortenerService.shortenUrl(request.getUrl());
            // Construct the full shortened URL. This is a bit simplistic as it assumes http and localhost.
            // In a real app, this would come from configuration or request headers.
            String scheme = webRequest.getHeader("X-Forwarded-Proto"); // Check for reverse proxy
            if (scheme == null) scheme = webRequest.getContextPath().startsWith("https") ? "https" : "http"; // Basic guess
             String host = webRequest.getHeader("X-Forwarded-Host");
            if (host == null) host = webRequest.getHeader("Host"); // Host header
            if (host == null) host = "localhost:8080"; // Fallback

            String fullShortenedUrl = scheme + "://" + host + "/" + urlEntry.getShortCode();
            
            ShortenResponse response = new ShortenResponse(
                urlEntry.getShortCode(), 
                urlEntry.getOriginalUrl(),
                fullShortenedUrl
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // Log exception e
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An unexpected error occurred."));
        }
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirectToOriginalUrl(@PathVariable String shortCode) {
        Optional<String> originalUrlOpt = urlShortenerService.getOriginalUrl(shortCode);

        if (originalUrlOpt.isPresent()) {
            String originalUrl = originalUrlOpt.get();
            // Basic check if URL includes scheme, add http:// if not, for URI.create to work robustly
            if (!originalUrl.matches("^[a-zA-Z]+://.*")) {
                originalUrl = "http://" + originalUrl;
            }
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(originalUrl))
                    .build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
