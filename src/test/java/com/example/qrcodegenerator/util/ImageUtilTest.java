package com.example.qrcodegenerator.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;

class ImageUtilTest {

    // A very small, valid Base64 encoded PNG image (1x1 red pixel)
    // data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==
    private static final String VALID_PNG_RAW_BASE64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==";
    private static final String VALID_PNG_DATA_URI = "data:image/png;base64," + VALID_PNG_RAW_BASE64;
    private static final String VALID_JPEG_DATA_URI = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAP//////////////////////////////////////////////////////////////////////////////////////wgALCAABAAEBAREA/8QAFBABAAAAAAAAAAAAAAAAAAAAAP/aAAgBAQABPxA="; // 1x1 black JPEG
    private static final String VALID_JPEG_RAW_BASE64 = "/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAP//////////////////////////////////////////////////////////////////////////////////////wgALCAABAAEBAREA/8QAFBABAAAAAAAAAAAAAAAAAAAAAP/aAAgBAQABPxA=";


    // --- Tests for decodeBase64ToImage ---

    @Test
    void decodeBase64ToImage_validPng_returnsBufferedImage() {
        BufferedImage image = ImageUtil.decodeBase64ToImage(VALID_PNG_RAW_BASE64);
        assertNotNull(image, "Decoding valid PNG Base64 should return a non-null BufferedImage.");
        assertEquals(1, image.getWidth(), "Image width should be 1.");
        assertEquals(1, image.getHeight(), "Image height should be 1.");
    }

    @Test
    void decodeBase64ToImage_invalidBase64String_returnsNull() {
        // Contains invalid characters like '!' and '*'
        String invalidBase64 = "iVBORw0KGgo!AAA*NSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==";
        BufferedImage image = ImageUtil.decodeBase64ToImage(invalidBase64);
        assertNull(image, "Decoding invalid Base64 should return null.");
    }
    
    @Test
    void decodeBase64ToImage_emptyString_returnsNull() {
        BufferedImage image = ImageUtil.decodeBase64ToImage("");
        assertNull(image, "Decoding empty string should return null.");
    }

    @Test
    void decodeBase64ToImage_nullString_returnsNull() {
        BufferedImage image = ImageUtil.decodeBase64ToImage(null);
        assertNull(image, "Decoding null string should return null.");
    }

    @Test
    void decodeBase64ToImage_nonImageBase64_returnsNull() {
        // Base64 for "Hello World"
        String textBase64 = "SGVsbG8gV29ybGQ=";
        BufferedImage image = ImageUtil.decodeBase64ToImage(textBase64);
        // ImageIO.read returns null if the format is not recognized
        assertNull(image, "Decoding Base64 of non-image data should return null.");
    }
    
    @Test
    void decodeBase64ToImage_stringWithSpaces_decodesCorrectly() {
        BufferedImage image = ImageUtil.decodeBase64ToImage("  " + VALID_PNG_RAW_BASE64 + "  ");
        assertNotNull(image, "Decoding Base64 with leading/trailing spaces should work.");
    }


    // --- Tests for extractBase64Data ---

    @Test
    void extractBase64Data_pngDataUri_extractsCorrectly() {
        String extracted = ImageUtil.extractBase64Data(VALID_PNG_DATA_URI);
        assertEquals(VALID_PNG_RAW_BASE64, extracted, "Should extract raw Base64 from PNG data URI.");
    }

    @Test
    void extractBase64Data_jpegDataUri_extractsCorrectly() {
        String extracted = ImageUtil.extractBase64Data(VALID_JPEG_DATA_URI);
        assertEquals(VALID_JPEG_RAW_BASE64, extracted, "Should extract raw Base64 from JPEG data URI.");
    }

    @Test
    void extractBase64Data_rawBase64_returnsInput() {
        String rawBase64 = "justSomeRawBase64Data";
        String extracted = ImageUtil.extractBase64Data(rawBase64);
        assertEquals(rawBase64, extracted, "Should return the input string if it's not a data URI.");
    }

    @Test
    void extractBase64Data_malformedPrefix_returnsInput() {
        // Missing "base64" part
        String malformedUri = "data:image/png," + VALID_PNG_RAW_BASE64;
        String extracted = ImageUtil.extractBase64Data(malformedUri);
        assertEquals(malformedUri, extracted, "Should return original for malformed prefix (missing ';base64').");
    }
    
    @Test
    void extractBase64Data_noCommaInPrefix_returnsInput() {
        String malformedUri = "data:image/png;base64" + VALID_PNG_RAW_BASE64; // No comma
        String extracted = ImageUtil.extractBase64Data(malformedUri);
        assertEquals(malformedUri, extracted, "Should return original if no comma in data URI like string.");
    }


    @Test
    void extractBase64Data_emptyString_returnsEmptyString() {
        String extracted = ImageUtil.extractBase64Data("");
        assertEquals("", extracted, "Should return empty string for empty input.");
    }

    @Test
    void extractBase64Data_nullInput_returnsNull() {
        String extracted = ImageUtil.extractBase64Data(null);
        assertNull(extracted, "Should return null for null input.");
    }
}
