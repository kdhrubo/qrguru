package com.example.qrcodegenerator.service.formatter;

import com.example.qrcodegenerator.model.QrCodeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SmsQrDataFormatterTest {

    private SmsQrDataFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = new SmsQrDataFormatter();
    }

    @Test
    void supports_shouldReturnTrueForSmsType() {
        assertTrue(formatter.supports(QrCodeType.SMS));
    }

    @ParameterizedTest
    @EnumSource(value = QrCodeType.class, names = {"TEXT", "URL", "VCARD", "WIFI", "EMAIL"})
    void supports_shouldReturnFalseForNonSmsTypes(QrCodeType type) {
        assertFalse(formatter.supports(type));
    }

    @Test
    void format_withNumberAndMessage_shouldProduceCorrectSmsToString() {
        Map<String, String> params = new HashMap<>();
        params.put(SmsQrDataFormatter.KEY_SMS_NUMBER, "+11234567890");
        params.put(SmsQrDataFormatter.KEY_SMS_MESSAGE, "Hello World! This is a test.");

        String expected = "smsto:+11234567890:Hello+World%21+This+is+a+test.";
        assertEquals(expected, formatter.format(params));
    }

    @Test
    void format_withNumberOnly_shouldProduceCorrectSmsToString() {
        Map<String, String> params = new HashMap<>();
        params.put(SmsQrDataFormatter.KEY_SMS_NUMBER, "12345");
        String expected = "smsto:12345";
        assertEquals(expected, formatter.format(params));
    }
    
    @Test
    void format_withNumberAndEmptyMessage_shouldProduceCorrectSmsToString() {
        Map<String, String> params = new HashMap<>();
        params.put(SmsQrDataFormatter.KEY_SMS_NUMBER, "12345");
        params.put(SmsQrDataFormatter.KEY_SMS_MESSAGE, "   "); // Empty message
        String expected = "smsto:12345"; // Message part should be omitted
        assertEquals(expected, formatter.format(params));
    }

    @Test
    void format_numberWithSpaces_shouldBeTrimmed() {
        Map<String, String> params = new HashMap<>();
        params.put(SmsQrDataFormatter.KEY_SMS_NUMBER, "  +1 800 555 1212  ");
        params.put(SmsQrDataFormatter.KEY_SMS_MESSAGE, "Call me");
        String expected = "smsto:+1 800 555 1212:Call+me"; // Number is trimmed but spaces within are kept
        assertEquals(expected, formatter.format(params));
    }

    @Test
    void format_missingNumber_throwsIllegalArgumentException() {
        Map<String, String> params = new HashMap<>();
        params.put(SmsQrDataFormatter.KEY_SMS_MESSAGE, "No Number");
        Exception exception = assertThrows(IllegalArgumentException.class, () -> formatter.format(params));
        assertEquals("Parameters for SMS must contain '" + SmsQrDataFormatter.KEY_SMS_NUMBER + "'.", exception.getMessage());
    }
    
    @Test
    void format_emptyNumber_throwsIllegalArgumentException() {
        Map<String, String> params = new HashMap<>();
        params.put(SmsQrDataFormatter.KEY_SMS_NUMBER, "   ");
        Exception exception = assertThrows(IllegalArgumentException.class, () -> formatter.format(params));
        assertEquals("SMS number cannot be null or empty.", exception.getMessage());
    }

    @Test
    void format_nullParams_throwsIllegalArgumentException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> formatter.format(null));
        assertEquals("Parameters for SMS must contain '" + SmsQrDataFormatter.KEY_SMS_NUMBER + "'.", exception.getMessage());
    }
    
    @Test
    void format_messageWithSpecialCharacters_shouldBeUrlEncoded() {
        Map<String, String> params = new HashMap<>();
        params.put(SmsQrDataFormatter.KEY_SMS_NUMBER, "555000");
        params.put(SmsQrDataFormatter.KEY_SMS_MESSAGE, "Hi, what's up? Let's meet at 2pm & bring snacks!");

        // Expected encoding:
        // Hi, -> Hi%2C
        // what's up? -> what%27s+up%3F (apostrophe, space, question mark)
        // Let's -> Let%27s
        // at 2pm & -> at+2pm+%26
        // bring snacks! -> bring+snacks%21
        String expected = "smsto:555000:Hi%2C+what%27s+up%3F+Let%27s+meet+at+2pm+%26+bring+snacks%21";
        assertEquals(expected, formatter.format(params));
    }
}
