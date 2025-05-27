package com.example.qrcodegenerator.service;

import com.example.qrcodegenerator.model.UrlEntry;
import com.example.qrcodegenerator.repository.UrlShortenerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Important for DB operations

import java.security.SecureRandom;
import java.util.Optional;
import java.util.Random;

@Service
public class UrlShortenerService {

    private static final String ALPHANUMERIC_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int SHORT_CODE_LENGTH = 7; // Length of the generated short code
    private final Random random = new SecureRandom(); // Use SecureRandom for better randomness

    private final UrlShortenerRepository urlRepository;

    public UrlShortenerService(UrlShortenerRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    @Transactional
    public UrlEntry shortenUrl(String originalUrl) {
        if (originalUrl == null || originalUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Original URL cannot be null or empty.");
        }
        // A more robust URL validation could be added here, e.g., using Apache Commons Validator

        // Check if the URL is already shortened
        Optional<UrlEntry> existingEntry = urlRepository.findByOriginalUrl(originalUrl.trim());
        if (existingEntry.isPresent()) {
            return existingEntry.get();
        }

        // Generate a unique short code
        String shortCode;
        do {
            shortCode = generateRandomShortCode();
        } while (urlRepository.findByShortCode(shortCode).isPresent());

        UrlEntry newEntry = new UrlEntry();
        newEntry.setOriginalUrl(originalUrl.trim());
        newEntry.setShortCode(shortCode);

        return urlRepository.save(newEntry);
    }

    @Transactional(readOnly = true) // readOnly for GET operations
    public Optional<String> getOriginalUrl(String shortCode) {
        if (shortCode == null || shortCode.trim().isEmpty()) {
            return Optional.empty();
        }
        Optional<UrlEntry> entry = urlRepository.findByShortCode(shortCode.trim());
        return entry.map(UrlEntry::getOriginalUrl);
    }

    private String generateRandomShortCode() {
        StringBuilder sb = new StringBuilder(SHORT_CODE_LENGTH);
        for (int i = 0; i < SHORT_CODE_LENGTH; i++) {
            sb.append(ALPHANUMERIC_CHARACTERS.charAt(random.nextInt(ALPHANUMERIC_CHARACTERS.length())));
        }
        return sb.toString();
    }
}
