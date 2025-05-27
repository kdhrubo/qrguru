package com.example.qrcodegenerator.service.formatter;

import com.example.qrcodegenerator.model.QrCodeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GeoLocationQrDataFormatterTest {

    private GeoLocationQrDataFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = new GeoLocationQrDataFormatter();
    }

    @Test
    void supports_shouldReturnTrueForGeoType() {
        assertTrue(formatter.supports(QrCodeType.GEO));
    }

    @ParameterizedTest
    @EnumSource(value = QrCodeType.class, names = {"TEXT", "URL", "VCARD", "WIFI", "EMAIL", "SMS", "CALENDAR"})
    void supports_shouldReturnFalseForNonGeoTypes(QrCodeType type) {
        assertFalse(formatter.supports(type));
    }

    @Test
    void format_validLatLon_shouldProduceCorrectGeoString() {
        Map<String, String> params = new HashMap<>();
        params.put(GeoLocationQrDataFormatter.KEY_GEO_LATITUDE, "34.0522");
        params.put(GeoLocationQrDataFormatter.KEY_GEO_LONGITUDE, "-118.2437");
        String expected = "geo:34.0522,-118.2437";
        assertEquals(expected, formatter.format(params));
    }

    @Test
    void format_validLatLonWithQuery_shouldProduceCorrectGeoStringAndEncodeQuery() {
        Map<String, String> params = new HashMap<>();
        params.put(GeoLocationQrDataFormatter.KEY_GEO_LATITUDE, "40.7128");
        params.put(GeoLocationQrDataFormatter.KEY_GEO_LONGITUDE, "-74.0060");
        params.put(GeoLocationQrDataFormatter.KEY_GEO_QUERY, "New York City Hall");
        String expected = "geo:40.7128,-74.0060?q=New+York+City+Hall";
        assertEquals(expected, formatter.format(params));
    }
    
    @Test
    void format_validLatLonWithQueryContainingSpecialChars_shouldEncodeQuery() {
        Map<String, String> params = new HashMap<>();
        params.put(GeoLocationQrDataFormatter.KEY_GEO_LATITUDE, "40.7128");
        params.put(GeoLocationQrDataFormatter.KEY_GEO_LONGITUDE, "-74.0060");
        params.put(GeoLocationQrDataFormatter.KEY_GEO_QUERY, "Query with spaces & symbols?");
        String expected = "geo:40.7128,-74.0060?q=Query+with+spaces+%26+symbols%3F";
        assertEquals(expected, formatter.format(params));
    }

    @ParameterizedTest
    @CsvSource({
            "90.0, 180.0",    // Max valid
            "-90.0, -180.0",  // Min valid
            "0, 0"            // Zero values
    })
    void format_boundaryLatLonValues_accepted(String lat, String lon) {
        Map<String, String> params = new HashMap<>();
        params.put(GeoLocationQrDataFormatter.KEY_GEO_LATITUDE, lat);
        params.put(GeoLocationQrDataFormatter.KEY_GEO_LONGITUDE, lon);
        String expected = "geo:" + lat + "," + lon;
        assertEquals(expected, formatter.format(params));
    }

    @ParameterizedTest
    @CsvSource({
            "90.000001, 0.0, Latitude must be between -90 and 90",   // Lat too high
            "-90.000001, 0.0, Latitude must be between -90 and 90",  // Lat too low
            "0.0, 180.000001, Longitude must be between -180 and 180", // Lon too high
            "0.0, -180.000001, Longitude must be between -180 and 180" // Lon too low
    })
    void format_invalidLatLonOutOfRange_throwsIllegalArgumentException(String lat, String lon, String expectedMessagePart) {
        Map<String, String> params = new HashMap<>();
        params.put(GeoLocationQrDataFormatter.KEY_GEO_LATITUDE, lat);
        params.put(GeoLocationQrDataFormatter.KEY_GEO_LONGITUDE, lon);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> formatter.format(params));
        assertTrue(exception.getMessage().contains(expectedMessagePart));
    }

    @ParameterizedTest
    @CsvSource({
            "not-a-number, 0.0, Invalid latitude format",
            "0.0, not-a-number, Invalid longitude format"
    })
    void format_invalidLatLonNonNumeric_throwsIllegalArgumentException(String lat, String lon, String expectedMessagePart) {
        Map<String, String> params = new HashMap<>();
        params.put(GeoLocationQrDataFormatter.KEY_GEO_LATITUDE, lat);
        params.put(GeoLocationQrDataFormatter.KEY_GEO_LONGITUDE, lon);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> formatter.format(params));
        assertTrue(exception.getMessage().contains(expectedMessagePart));
    }

    @Test
    void format_missingLatitude_throwsIllegalArgumentException() {
        Map<String, String> params = new HashMap<>();
        params.put(GeoLocationQrDataFormatter.KEY_GEO_LONGITUDE, "0.0");
        Exception exception = assertThrows(IllegalArgumentException.class, () -> formatter.format(params));
        assertEquals("Parameters for Geo-location must contain '" + GeoLocationQrDataFormatter.KEY_GEO_LATITUDE +
                "' and '" + GeoLocationQrDataFormatter.KEY_GEO_LONGITUDE + "'.", exception.getMessage());
    }

    @Test
    void format_missingLongitude_throwsIllegalArgumentException() {
        Map<String, String> params = new HashMap<>();
        params.put(GeoLocationQrDataFormatter.KEY_GEO_LATITUDE, "0.0");
        Exception exception = assertThrows(IllegalArgumentException.class, () -> formatter.format(params));
        assertEquals("Parameters for Geo-location must contain '" + GeoLocationQrDataFormatter.KEY_GEO_LATITUDE +
                "' and '" + GeoLocationQrDataFormatter.KEY_GEO_LONGITUDE + "'.", exception.getMessage());
    }
    
    @Test
    void format_nullParams_throwsIllegalArgumentException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> formatter.format(null));
        assertEquals("Parameters for Geo-location must contain '" + GeoLocationQrDataFormatter.KEY_GEO_LATITUDE +
                "' and '" + GeoLocationQrDataFormatter.KEY_GEO_LONGITUDE + "'.", exception.getMessage());
    }

    @Test
    void format_emptyParams_throwsIllegalArgumentException() {
        Map<String, String> params = new HashMap<>();
        Exception exception = assertThrows(IllegalArgumentException.class, () -> formatter.format(params));
        assertEquals("Parameters for Geo-location must contain '" + GeoLocationQrDataFormatter.KEY_GEO_LATITUDE +
                "' and '" + GeoLocationQrDataFormatter.KEY_GEO_LONGITUDE + "'.", exception.getMessage());
    }
}
