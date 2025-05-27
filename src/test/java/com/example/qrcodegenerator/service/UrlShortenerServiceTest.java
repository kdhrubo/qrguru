package com.example.qrcodegenerator.service;

import com.example.qrcodegenerator.model.UrlEntry;
import com.example.qrcodegenerator.repository.UrlShortenerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UrlShortenerServiceTest {

    @Mock
    private UrlShortenerRepository urlRepository;

    @InjectMocks
    private UrlShortenerService urlShortenerService;

    private UrlEntry sampleUrlEntry;

    @BeforeEach
    void setUp() {
        sampleUrlEntry = new UrlEntry(1L, "http://example.com", "test123");
    }

    @Test
    void shortenUrl_newUrl_createsAndReturnsEntry() {
        String originalUrl = "http://newexample.com";
        given(urlRepository.findByOriginalUrl(originalUrl)).willReturn(Optional.empty());
        given(urlRepository.findByShortCode(anyString())).willReturn(Optional.empty()); // For uniqueness check
        given(urlRepository.save(any(UrlEntry.class))).thenAnswer(invocation -> {
            UrlEntry entry = invocation.getArgument(0);
            entry.setId(2L); // Simulate saving by assigning an ID
            return entry;
        });

        UrlEntry result = urlShortenerService.shortenUrl(originalUrl);

        assertNotNull(result);
        assertEquals(originalUrl, result.getOriginalUrl());
        assertNotNull(result.getShortCode());
        assertEquals(7, result.getShortCode().length()); // As per SHORT_CODE_LENGTH in service
        verify(urlRepository).save(any(UrlEntry.class));
    }

    @Test
    void shortenUrl_existingUrl_returnsExistingEntry() {
        String originalUrl = "http://example.com";
        given(urlRepository.findByOriginalUrl(originalUrl)).willReturn(Optional.of(sampleUrlEntry));

        UrlEntry result = urlShortenerService.shortenUrl(originalUrl);

        assertNotNull(result);
        assertEquals(sampleUrlEntry.getId(), result.getId());
        assertEquals(originalUrl, result.getOriginalUrl());
        assertEquals(sampleUrlEntry.getShortCode(), result.getShortCode());
        verify(urlRepository, never()).save(any(UrlEntry.class));
    }

    @Test
    void shortenUrl_nullUrl_throwsIllegalArgumentException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            urlShortenerService.shortenUrl(null);
        });
        assertEquals("Original URL cannot be null or empty.", exception.getMessage());
    }

    @Test
    void shortenUrl_emptyUrl_throwsIllegalArgumentException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            urlShortenerService.shortenUrl("  ");
        });
        assertEquals("Original URL cannot be null or empty.", exception.getMessage());
    }
    
    @Test
    void shortenUrl_generatesUniqueShortCodeOnCollision() {
        String originalUrl = "http://collisiontest.com";
        String firstAttemptCode = "collide";
        String secondAttemptCode = "unique1";

        // Mock the random code generator (indirectly, by controlling findByShortCode)
        // This is tricky as the random part is internal. We test the loop behavior.
        given(urlRepository.findByOriginalUrl(originalUrl)).willReturn(Optional.empty());
        
        // Simulate a collision on the first generated short code, then success on the second
        // This requires knowing/controlling what generateRandomShortCode() produces, which is hard.
        // Instead, we'll verify that findByShortCode is called until a unique one is found.
        // We can't directly control the generated code, but we can ensure it tries again.
        
        // For this test, we'll assume generateRandomShortCode() is robust.
        // A more direct test of the retry loop would require refactoring generateRandomShortCode
        // to be injectable or predictable, which might be over-engineering for this.
        // So, we'll trust the loop and just ensure save is called with some generated code.

        UrlShortenerService spyService = spy(new UrlShortenerService(urlRepository));
        
        // We can't easily mock generateRandomShortCode as it's private.
        // Instead, we'll mock the repository's response to ensure the loop condition is met.
        given(urlRepository.findByShortCode(anyString()))
            .willReturn(Optional.of(new UrlEntry())) // First call, simulate collision
            .willReturn(Optional.empty());          // Second call, simulate unique

        given(urlRepository.save(any(UrlEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UrlEntry result = spyService.shortenUrl(originalUrl);

        assertNotNull(result);
        assertNotNull(result.getShortCode());
        verify(urlRepository, times(2)).findByShortCode(anyString()); // Verifies it tried at least twice
        verify(urlRepository).save(any(UrlEntry.class));
    }


    @Test
    void getOriginalUrl_existingShortCode_returnsOriginalUrl() {
        given(urlRepository.findByShortCode("test123")).willReturn(Optional.of(sampleUrlEntry));

        Optional<String> result = urlShortenerService.getOriginalUrl("test123");

        assertTrue(result.isPresent());
        assertEquals("http://example.com", result.get());
    }

    @Test
    void getOriginalUrl_nonExistentShortCode_returnsEmptyOptional() {
        given(urlRepository.findByShortCode("nonexist")).willReturn(Optional.empty());

        Optional<String> result = urlShortenerService.getOriginalUrl("nonexist");

        assertFalse(result.isPresent());
    }

    @Test
    void getOriginalUrl_nullShortCode_returnsEmptyOptional() {
        Optional<String> result = urlShortenerService.getOriginalUrl(null);
        assertFalse(result.isPresent());
        verify(urlRepository, never()).findByShortCode(any());
    }

    @Test
    void getOriginalUrl_emptyShortCode_returnsEmptyOptional() {
        Optional<String> result = urlShortenerService.getOriginalUrl("  ");
        assertFalse(result.isPresent());
        verify(urlRepository, never()).findByShortCode(any());
    }
}
