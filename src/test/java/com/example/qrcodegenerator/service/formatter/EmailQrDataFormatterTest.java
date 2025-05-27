package com.example.qrcodegenerator.service.formatter;

import com.example.qrcodegenerator.model.QrCodeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EmailQrDataFormatterTest {

    private EmailQrDataFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = new EmailQrDataFormatter();
    }

    @Test
    void supports_shouldReturnTrueForEmailType() {
        assertTrue(formatter.supports(QrCodeType.EMAIL));
    }

    @ParameterizedTest
    @EnumSource(value = QrCodeType.class, names = {"TEXT", "URL", "VCARD", "WIFI", "SMS"})
    void supports_shouldReturnFalseForNonEmailTypes(QrCodeType type) {
        assertFalse(formatter.supports(type));
    }

    @Test
    void format_withToOnly_shouldProduceCorrectMailtoString() {
        Map<String, String> params = new HashMap<>();
        params.put(EmailQrDataFormatter.KEY_EMAIL_TO, "test@example.com");
        String expected = "mailto:test%40example.com"; // @ is not typically encoded, but URLEncoder might do it.
                                                       // Let's refine based on actual URLEncoder behavior for '@'.
                                                       // URLEncoder.encode typically doesn't change '@', '-', '_', '.', '*'
        expected = "mailto:test@example.com"; // Corrected: @ is not encoded by URLEncoder.encode(s, "UTF-8")
        assertEquals(expected, formatter.format(params));
    }

    @Test
    void format_withAllParameters_shouldProduceCorrectMailtoStringAndEncode() {
        Map<String, String> params = new HashMap<>();
        params.put(EmailQrDataFormatter.KEY_EMAIL_TO, "to@example.com");
        params.put(EmailQrDataFormatter.KEY_EMAIL_CC, "cc@example.com");
        params.put(EmailQrDataFormatter.KEY_EMAIL_BCC, "bcc@example.com");
        params.put(EmailQrDataFormatter.KEY_EMAIL_SUBJECT, "Hello World");
        params.put(EmailQrDataFormatter.KEY_EMAIL_BODY, "This is the body with spaces & special chars!");

        String expected = "mailto:to@example.com" +
                "?cc=cc%40example.com" +
                "&bcc=bcc%40example.com" +
                "&subject=Hello+World" +
                "&body=This+is+the+body+with+spaces+%26+special+chars%21";
        assertEquals(expected, formatter.format(params));
    }
    
    @Test
    void format_withSpacesInToField_shouldBeTrimmedAndEncodedIfNecessary() {
        Map<String, String> params = new HashMap<>();
        params.put(EmailQrDataFormatter.KEY_EMAIL_TO, "  leading@example.com  ");
        String expected = "mailto:leading@example.com";
        assertEquals(expected, formatter.format(params));
    }

    @Test
    void format_withToAndSubjectOnly_shouldProduceCorrectMailtoString() {
        Map<String, String> params = new HashMap<>();
        params.put(EmailQrDataFormatter.KEY_EMAIL_TO, "recipient@mail.com");
        params.put(EmailQrDataFormatter.KEY_EMAIL_SUBJECT, "Test Subject!");

        String expected = "mailto:recipient@mail.com?subject=Test+Subject%21";
        assertEquals(expected, formatter.format(params));
    }
    
    @Test
    void format_withToAndBodyOnly_shouldProduceCorrectMailtoString() {
        Map<String, String> params = new HashMap<>();
        params.put(EmailQrDataFormatter.KEY_EMAIL_TO, "user@domain.com");
        params.put(EmailQrDataFormatter.KEY_EMAIL_BODY, "Line 1\nLine 2");

        String expected = "mailto:user@domain.com?body=Line+1%0ALine+2";
        assertEquals(expected, formatter.format(params));
    }


    @Test
    void format_missingTo_throwsIllegalArgumentException() {
        Map<String, String> params = new HashMap<>();
        params.put(EmailQrDataFormatter.KEY_EMAIL_SUBJECT, "No Recipient");
        Exception exception = assertThrows(IllegalArgumentException.class, () -> formatter.format(params));
        assertEquals("Parameters for Email must contain '" + EmailQrDataFormatter.KEY_EMAIL_TO + "'.", exception.getMessage());
    }
    
    @Test
    void format_emptyTo_throwsIllegalArgumentException() {
        Map<String, String> params = new HashMap<>();
        params.put(EmailQrDataFormatter.KEY_EMAIL_TO, "   ");
        Exception exception = assertThrows(IllegalArgumentException.class, () -> formatter.format(params));
        assertEquals("Email 'to' address cannot be null or empty.", exception.getMessage());
    }

    @Test
    void format_nullParams_throwsIllegalArgumentException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> formatter.format(null));
        assertEquals("Parameters for Email must contain '" + EmailQrDataFormatter.KEY_EMAIL_TO + "'.", exception.getMessage());
    }
}
