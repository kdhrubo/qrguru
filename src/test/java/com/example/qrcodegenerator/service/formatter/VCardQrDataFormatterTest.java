package com.example.qrcodegenerator.service.formatter;

import com.example.qrcodegenerator.model.QrCodeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class VCardQrDataFormatterTest {

    private VCardQrDataFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = new VCardQrDataFormatter();
    }

    @Test
    void supports_shouldReturnTrueForVCardType() {
        assertTrue(formatter.supports(QrCodeType.VCARD));
    }

    @ParameterizedTest
    @EnumSource(value = QrCodeType.class, names = {"TEXT", "URL", "WIFI", "EMAIL", "SMS"})
    void supports_shouldReturnFalseForNonVCardTypes(QrCodeType type) {
        assertFalse(formatter.supports(type));
    }

    @Test
    void format_withAllParameters_shouldProduceCorrectVCardString() {
        Map<String, String> params = new HashMap<>();
        params.put(VCardQrDataFormatter.KEY_VCARD_FIRST_NAME, "John");
        params.put(VCardQrDataFormatter.KEY_VCARD_LAST_NAME, "Doe");
        params.put(VCardQrDataFormatter.KEY_VCARD_ORG, "Example Corp; Sales Division");
        params.put(VCardQrDataFormatter.KEY_VCARD_TITLE, "Senior Tester");
        params.put(VCardQrDataFormatter.KEY_VCARD_TEL_WORK, "+1-123-555-1212");
        params.put(VCardQrDataFormatter.KEY_VCARD_TEL_HOME, "+1-123-555-0000");
        params.put(VCardQrDataFormatter.KEY_VCARD_EMAIL, "john.doe@example.com");
        params.put(VCardQrDataFormatter.KEY_VCARD_URL, "http://www.example.com");
        params.put(VCardQrDataFormatter.KEY_VCARD_STREET, "123 Main St, Apt 4B");
        params.put(VCardQrDataFormatter.KEY_VCARD_CITY, "Anytown");
        params.put(VCardQrDataFormatter.KEY_VCARD_STATE, "CA");
        params.put(VCardQrDataFormatter.KEY_VCARD_ZIP, "90210");
        params.put(VCardQrDataFormatter.KEY_VCARD_COUNTRY, "USA");
        params.put(VCardQrDataFormatter.KEY_VCARD_NOTE, "A test note with special chars: \\,;,\\nnewline.");

        String expected = "BEGIN:VCARD\n" +
                "VERSION:3.0\n" +
                "N:Doe;John\n" +
                "FN:John Doe\n" +
                "ORG:Example Corp\\; Sales Division\n" +
                "TITLE:Senior Tester\n" +
                "TEL;TYPE=WORK,VOICE:+1-123-555-1212\n" +
                "TEL;TYPE=HOME,VOICE:+1-123-555-0000\n" +
                "EMAIL:john.doe@example.com\n" +
                "URL:http://www.example.com\n" +
                "NOTE:A test note with special chars: \\\\\\,\\;\\;\\\\nnewline.\n" +
                "ADR;TYPE=WORK:;;123 Main St\\, Apt 4B;Anytown;CA;90210;USA\n" +
                "END:VCARD";
        
        String actual = formatter.format(params);
        assertEquals(expected, actual);
    }

    @Test
    void format_withMinimalParameters_shouldProduceCorrectVCardString() {
        Map<String, String> params = new HashMap<>();
        params.put(VCardQrDataFormatter.KEY_VCARD_FIRST_NAME, "Jane");
        params.put(VCardQrDataFormatter.KEY_VCARD_LAST_NAME, "Doe");

        String expected = "BEGIN:VCARD\n" +
                "VERSION:3.0\n" +
                "N:Doe;Jane\n" +
                "FN:Jane Doe\n" +
                "END:VCARD";
        assertEquals(expected, formatter.format(params));
    }
    
    @Test
    void format_onlyLastName_shouldProduceCorrectVCardString() {
        Map<String, String> params = new HashMap<>();
        params.put(VCardQrDataFormatter.KEY_VCARD_LAST_NAME, "Doe");

        String expected = "BEGIN:VCARD\n" +
                "VERSION:3.0\n" +
                "N:Doe;\n" + // Semicolon is important
                "FN:Doe\n" +
                "END:VCARD";
        assertEquals(expected, formatter.format(params));
    }

    @Test
    void format_emptyParams_throwsIllegalArgumentException() {
        Map<String, String> params = new HashMap<>();
        Exception exception = assertThrows(IllegalArgumentException.class, () -> formatter.format(params));
        assertEquals("Parameters for vCard cannot be null or empty.", exception.getMessage());
    }
    
    @Test
    void format_nullParams_throwsIllegalArgumentException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> formatter.format(null));
        assertEquals("Parameters for vCard cannot be null or empty.", exception.getMessage());
    }

    @Test
    void format_addressFieldsPartiallyPresent_shouldFormatAddressCorrectly() {
        Map<String, String> params = new HashMap<>();
        params.put(VCardQrDataFormatter.KEY_VCARD_FIRST_NAME, "Just");
        params.put(VCardQrDataFormatter.KEY_VCARD_LAST_NAME, "Aname");
        params.put(VCardQrDataFormatter.KEY_VCARD_STREET, "123 Main St");
        params.put(VCardQrDataFormatter.KEY_VCARD_CITY, "Anytown");

        String expected = "BEGIN:VCARD\n" +
                "VERSION:3.0\n" +
                "N:Aname;Just\n" +
                "FN:Just Aname\n" +
                "ADR;TYPE=WORK:;;123 Main St;Anytown;;;\n" + // Note the empty fields for state, zip, country
                "END:VCARD";
        assertEquals(expected, formatter.format(params));
    }
    
    @Test
    void format_specialCharactersInNamesAndFields_shouldBeEscaped() {
        Map<String, String> params = new HashMap<>();
        params.put(VCardQrDataFormatter.KEY_VCARD_FIRST_NAME, "First\\Name");
        params.put(VCardQrDataFormatter.KEY_VCARD_LAST_NAME, "Last;Name");
        params.put(VCardQrDataFormatter.KEY_VCARD_ORG, "Org, Inc.\nNextLine");

        String expected = "BEGIN:VCARD\n" +
                "VERSION:3.0\n" +
                "N:Last\\;Name;First\\\\Name\n" +
                "FN:First\\\\Name Last\\;Name\n" +
                "ORG:Org\\, Inc.\\nNextLine\n" +
                "END:VCARD";
        assertEquals(expected, formatter.format(params));
    }
}
