package com.example.qrcodegenerator.service.formatter;

import com.example.qrcodegenerator.model.QrCodeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WifiQrDataFormatterTest {

    private WifiQrDataFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = new WifiQrDataFormatter();
    }

    @Test
    void supports_shouldReturnTrueForWifiType() {
        assertTrue(formatter.supports(QrCodeType.WIFI));
    }

    @ParameterizedTest
    @EnumSource(value = QrCodeType.class, names = {"TEXT", "URL", "VCARD", "EMAIL", "SMS"})
    void supports_shouldReturnFalseForNonWifiTypes(QrCodeType type) {
        assertFalse(formatter.supports(type));
    }

    @Test
    void format_wpaEncryption_shouldProduceCorrectWifiString() {
        Map<String, String> params = new HashMap<>();
        params.put(WifiQrDataFormatter.KEY_WIFI_SSID, "MyNetwork");
        params.put(WifiQrDataFormatter.KEY_WIFI_PASSWORD, "MyPassword123");
        params.put(WifiQrDataFormatter.KEY_WIFI_ENCRYPTION, "WPA");

        String expected = "WIFI:S:MyNetwork;T:WPA;P:MyPassword123;;";
        assertEquals(expected, formatter.format(params));
    }
    
    @Test
    void format_wpa2Encryption_shouldUseWPAInOutput() {
        Map<String, String> params = new HashMap<>();
        params.put(WifiQrDataFormatter.KEY_WIFI_SSID, "MyNetworkWPA2");
        params.put(WifiQrDataFormatter.KEY_WIFI_PASSWORD, "PassWPA2");
        params.put(WifiQrDataFormatter.KEY_WIFI_ENCRYPTION, "WPA2"); // User might specify WPA2

        String expected = "WIFI:S:MyNetworkWPA2;T:WPA2;P:PassWPA2;;"; // Service currently uses encryption type as is
        assertEquals(expected, formatter.format(params));
    }

    @Test
    void format_wepEncryption_shouldProduceCorrectWifiString() {
        Map<String, String> params = new HashMap<>();
        params.put(WifiQrDataFormatter.KEY_WIFI_SSID, "WEPNet");
        params.put(WifiQrDataFormatter.KEY_WIFI_PASSWORD, "wepkey");
        params.put(WifiQrDataFormatter.KEY_WIFI_ENCRYPTION, "WEP");

        String expected = "WIFI:S:WEPNet;T:WEP;P:wepkey;;";
        assertEquals(expected, formatter.format(params));
    }

    @Test
    void format_noPasswordEncryption_shouldProduceCorrectWifiString() {
        Map<String, String> params = new HashMap<>();
        params.put(WifiQrDataFormatter.KEY_WIFI_SSID, "OpenNet");
        params.put(WifiQrDataFormatter.KEY_WIFI_ENCRYPTION, "nopass");

        String expected = "WIFI:S:OpenNet;T:NOPASS;;"; // Password field is omitted
        assertEquals(expected, formatter.format(params));
    }
    
    @Test
    void format_noPasswordEncryptionExplicitEmptyPassword_shouldProduceCorrectWifiString() {
        Map<String, String> params = new HashMap<>();
        params.put(WifiQrDataFormatter.KEY_WIFI_SSID, "OpenNet");
        params.put(WifiQrDataFormatter.KEY_WIFI_PASSWORD, ""); // Explicit empty password
        params.put(WifiQrDataFormatter.KEY_WIFI_ENCRYPTION, "nopass");

        String expected = "WIFI:S:OpenNet;T:NOPASS;;";
        assertEquals(expected, formatter.format(params));
    }

    @Test
    void format_hiddenNetwork_shouldIncludeHiddenFlag() {
        Map<String, String> params = new HashMap<>();
        params.put(WifiQrDataFormatter.KEY_WIFI_SSID, "HiddenSSID");
        params.put(WifiQrDataFormatter.KEY_WIFI_PASSWORD, "hiddenpass");
        params.put(WifiQrDataFormatter.KEY_WIFI_ENCRYPTION, "WPA");
        params.put(WifiQrDataFormatter.KEY_WIFI_HIDDEN, "true");

        String expected = "WIFI:S:HiddenSSID;T:WPA;P:hiddenpass;H:true;;";
        assertEquals(expected, formatter.format(params));
    }
    
    @Test
    void format_hiddenNetworkFalse_shouldOmitHiddenFlag() {
        Map<String, String> params = new HashMap<>();
        params.put(WifiQrDataFormatter.KEY_WIFI_SSID, "VisibleSSID");
        params.put(WifiQrDataFormatter.KEY_WIFI_PASSWORD, "visiblepass");
        params.put(WifiQrDataFormatter.KEY_WIFI_ENCRYPTION, "WPA");
        params.put(WifiQrDataFormatter.KEY_WIFI_HIDDEN, "false"); // Explicitly false

        String expected = "WIFI:S:VisibleSSID;T:WPA;P:visiblepass;;";
        assertEquals(expected, formatter.format(params));
    }


    @Test
    void format_missingSsid_throwsIllegalArgumentException() {
        Map<String, String> params = new HashMap<>();
        params.put(WifiQrDataFormatter.KEY_WIFI_PASSWORD, "password");
        Exception exception = assertThrows(IllegalArgumentException.class, () -> formatter.format(params));
        assertEquals("Parameters for WiFi must contain '" + WifiQrDataFormatter.KEY_WIFI_SSID + "'.", exception.getMessage());
    }
    
    @Test
    void format_emptySsid_throwsIllegalArgumentException() {
        Map<String, String> params = new HashMap<>();
        params.put(WifiQrDataFormatter.KEY_WIFI_SSID, "  ");
        Exception exception = assertThrows(IllegalArgumentException.class, () -> formatter.format(params));
        assertEquals("SSID cannot be null or empty.", exception.getMessage());
    }

    @Test
    void format_encryptedWithMissingPassword_throwsIllegalArgumentException() {
        Map<String, String> params = new HashMap<>();
        params.put(WifiQrDataFormatter.KEY_WIFI_SSID, "SecureNet");
        params.put(WifiQrDataFormatter.KEY_WIFI_ENCRYPTION, "WPA");
        // Missing password
        Exception exception = assertThrows(IllegalArgumentException.class, () -> formatter.format(params));
        assertEquals("Password is required for WPA encryption.", exception.getMessage());
    }
    
    @Test
    void format_encryptedWithEmptyPassword_throwsIllegalArgumentException() {
        Map<String, String> params = new HashMap<>();
        params.put(WifiQrDataFormatter.KEY_WIFI_SSID, "SecureNet");
        params.put(WifiQrDataFormatter.KEY_WIFI_ENCRYPTION, "WEP");
        params.put(WifiQrDataFormatter.KEY_WIFI_PASSWORD, ""); // Empty password
        Exception exception = assertThrows(IllegalArgumentException.class, () -> formatter.format(params));
        assertEquals("Password is required for WEP encryption.", exception.getMessage());
    }

    @Test
    void format_invalidEncryptionType_throwsIllegalArgumentException() {
        Map<String, String> params = new HashMap<>();
        params.put(WifiQrDataFormatter.KEY_WIFI_SSID, "TestNet");
        params.put(WifiQrDataFormatter.KEY_WIFI_PASSWORD, "password");
        params.put(WifiQrDataFormatter.KEY_WIFI_ENCRYPTION, "INVALID_TYPE");
        Exception exception = assertThrows(IllegalArgumentException.class, () -> formatter.format(params));
        assertEquals("Invalid WiFi encryption type: INVALID_TYPE. Supported types are WPA, WPA2, WEP, nopass.", exception.getMessage());
    }

    @Test
    void format_specialCharactersInSsidAndPassword_shouldBeEscaped() {
        Map<String, String> params = new HashMap<>();
        params.put(WifiQrDataFormatter.KEY_WIFI_SSID, "My;Net\\work, SSID");
        params.put(WifiQrDataFormatter.KEY_WIFI_PASSWORD, "Pass:\"word\"");
        params.put(WifiQrDataFormatter.KEY_WIFI_ENCRYPTION, "WPA");

        String expectedSsid = "My\\;Net\\\\work\\, SSID";
        String expectedPass = "Pass\\:\\\"word\\\"";
        String expected = "WIFI:S:" + expectedSsid + ";T:WPA;P:" + expectedPass + ";;";
        assertEquals(expected, formatter.format(params));
    }
}
