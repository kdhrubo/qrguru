package com.example.qrcodegenerator.service.formatter;

import com.example.qrcodegenerator.model.QrCodeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CalendarEventQrDataFormatterTest {

    private CalendarEventQrDataFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = new CalendarEventQrDataFormatter();
    }

    @Test
    void supports_shouldReturnTrueForCalendarType() {
        assertTrue(formatter.supports(QrCodeType.CALENDAR));
    }

    @ParameterizedTest
    @EnumSource(value = QrCodeType.class, names = {"TEXT", "URL", "VCARD", "WIFI", "EMAIL", "SMS", "GEO"})
    void supports_shouldReturnFalseForNonCalendarTypes(QrCodeType type) {
        assertFalse(formatter.supports(type));
    }

    private Map<String, String> getValidBaseParams() {
        Map<String, String> params = new HashMap<>();
        params.put(CalendarEventQrDataFormatter.KEY_CALENDAR_SUMMARY, "Team Meeting");
        params.put(CalendarEventQrDataFormatter.KEY_CALENDAR_DTSTART, "20240704T100000Z");
        params.put(CalendarEventQrDataFormatter.KEY_CALENDAR_DTEND, "20240704T110000Z");
        return params;
    }

    @Test
    void format_withRequiredParameters_shouldProduceCorrectVEventString() {
        Map<String, String> params = getValidBaseParams();
        String result = formatter.format(params);

        assertTrue(result.startsWith("BEGIN:VEVENT\n"));
        assertTrue(result.contains("SUMMARY:Team Meeting\n"));
        assertTrue(result.contains("DTSTART:20240704T100000Z\n"));
        assertTrue(result.contains("DTEND:20240704T110000Z\n"));
        assertTrue(result.contains("UID:")); // UID is auto-generated
        assertTrue(result.contains("DTSTAMP:")); // DTSTAMP is auto-generated
        assertTrue(result.endsWith("\nEND:VEVENT"));
    }

    @Test
    void format_withAllParameters_shouldProduceCorrectVEventString() {
        Map<String, String> params = getValidBaseParams();
        String customUid = UUID.randomUUID().toString();
        String customDtstamp = "20240701T120000Z";
        params.put(CalendarEventQrDataFormatter.KEY_CALENDAR_UID, customUid);
        params.put(CalendarEventQrDataFormatter.KEY_CALENDAR_DTSTAMP, customDtstamp);
        params.put(CalendarEventQrDataFormatter.KEY_CALENDAR_LOCATION, "Conference Room 1; Building A");
        params.put(CalendarEventQrDataFormatter.KEY_CALENDAR_DESCRIPTION, "Discuss project milestones.\nBring snacks, please!");

        String result = formatter.format(params);

        assertTrue(result.contains("UID:" + customUid + "\n"));
        assertTrue(result.contains("DTSTAMP:" + customDtstamp + "\n"));
        assertTrue(result.contains("SUMMARY:Team Meeting\n"));
        assertTrue(result.contains("DTSTART:20240704T100000Z\n"));
        assertTrue(result.contains("DTEND:20240704T110000Z\n"));
        assertTrue(result.contains("LOCATION:Conference Room 1\\; Building A\n")); // Escaped semicolon
        assertTrue(result.contains("DESCRIPTION:Discuss project milestones.\\nBring snacks\\, please!\n")); // Escaped newline and comma
        assertTrue(result.endsWith("\nEND:VEVENT"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"20240704", "20240704T100000", "20240704T100000Z"})
    void format_validDateTimeFormats_accepted(String dateTime) {
        Map<String, String> params = getValidBaseParams();
        params.put(CalendarEventQrDataFormatter.KEY_CALENDAR_DTSTART, dateTime);
        params.put(CalendarEventQrDataFormatter.KEY_CALENDAR_DTEND, dateTime); // Using same for simplicity
        
        assertDoesNotThrow(() -> formatter.format(params));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2024-07-04T10:00:00Z", "20240704100000", "invalid-date"})
    void format_invalidDateTimeFormats_throwsIllegalArgumentException(String invalidDateTime) {
        Map<String, String> params = getValidBaseParams();
        params.put(CalendarEventQrDataFormatter.KEY_CALENDAR_DTSTART, invalidDateTime);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> formatter.format(params));
        assertTrue(exception.getMessage().contains("Invalid format for '" + CalendarEventQrDataFormatter.KEY_CALENDAR_DTSTART + "'"));
    }

    @Test
    void format_missingSummary_throwsIllegalArgumentException() {
        Map<String, String> params = getValidBaseParams();
        params.remove(CalendarEventQrDataFormatter.KEY_CALENDAR_SUMMARY);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> formatter.format(params));
        assertEquals("'" + CalendarEventQrDataFormatter.KEY_CALENDAR_SUMMARY + "' is required for Calendar event.", exception.getMessage());
    }

    @Test
    void format_missingDtStart_throwsIllegalArgumentException() {
        Map<String, String> params = getValidBaseParams();
        params.remove(CalendarEventQrDataFormatter.KEY_CALENDAR_DTSTART);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> formatter.format(params));
        assertEquals("'" + CalendarEventQrDataFormatter.KEY_CALENDAR_DTSTART + "' is required for Calendar event.", exception.getMessage());
    }

    @Test
    void format_missingDtEnd_throwsIllegalArgumentException() {
        Map<String, String> params = getValidBaseParams();
        params.remove(CalendarEventQrDataFormatter.KEY_CALENDAR_DTEND);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> formatter.format(params));
        assertEquals("'" + CalendarEventQrDataFormatter.KEY_CALENDAR_DTEND + "' is required for Calendar event.", exception.getMessage());
    }
    
    @Test
    void format_emptySummary_throwsIllegalArgumentException() {
        Map<String, String> params = getValidBaseParams();
        params.put(CalendarEventQrDataFormatter.KEY_CALENDAR_SUMMARY, "   ");
        Exception exception = assertThrows(IllegalArgumentException.class, () -> formatter.format(params));
        assertEquals("'" + CalendarEventQrDataFormatter.KEY_CALENDAR_SUMMARY + "' is required for Calendar event.", exception.getMessage());
    }

    @Test
    void format_specialCharactersInTextFields_areEscaped() {
        Map<String, String> params = getValidBaseParams();
        params.put(CalendarEventQrDataFormatter.KEY_CALENDAR_SUMMARY, "Sum;mary,Text\\");
        params.put(CalendarEventQrDataFormatter.KEY_CALENDAR_LOCATION, "Loc;ation,Text\\");
        params.put(CalendarEventQrDataFormatter.KEY_CALENDAR_DESCRIPTION, "Desc;ription,Text\\\nNew Line");

        String result = formatter.format(params);
        assertTrue(result.contains("SUMMARY:Sum\\;mary\\,Text\\\\\n"));
        assertTrue(result.contains("LOCATION:Loc\\;ation\\,Text\\\\\n"));
        assertTrue(result.contains("DESCRIPTION:Desc\\;ription\\,Text\\\\\\nNew Line\n"));
    }

    @Test
    void format_autoGeneratedUidAndDtstamp_arePresent() {
        Map<String, String> params = getValidBaseParams();
        String result = formatter.format(params);
        
        assertTrue(result.matches("(?s).*UID:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\n.*"), "Should contain auto-generated UID.");
        assertTrue(result.matches("(?s).*DTSTAMP:\\d{8}T\\d{6}Z\n.*"), "Should contain auto-generated DTSTAMP in UTC format.");
    }
    
    @Test
    void format_nullParams_throwsIllegalArgumentException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> formatter.format(null));
        assertEquals("Parameters for Calendar event cannot be null or empty.", exception.getMessage());
    }

    @Test
    void format_emptyParams_throwsIllegalArgumentException() {
        Map<String, String> params = new HashMap<>();
        Exception exception = assertThrows(IllegalArgumentException.class, () -> formatter.format(params));
        assertEquals("Parameters for Calendar event cannot be null or empty.", exception.getMessage());
    }
}
