package com.example.qrcodegenerator.service.formatter;

import com.example.qrcodegenerator.model.QrCodeType;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class GeoLocationQrDataFormatter implements QrDataFormatter {

    public static final String KEY_GEO_LATITUDE = "lat";
    public static final String KEY_GEO_LONGITUDE = "lon";
    public static final String KEY_GEO_QUERY = "query"; // Optional

    @Override
    public String format(Map<String, String> params) {
        if (params == null || !params.containsKey(KEY_GEO_LATITUDE) || !params.containsKey(KEY_GEO_LONGITUDE)) {
            throw new IllegalArgumentException("Parameters for Geo-location must contain '" + KEY_GEO_LATITUDE +
                    "' and '" + KEY_GEO_LONGITUDE + "'.");
        }

        String latStr = params.get(KEY_GEO_LATITUDE);
        String lonStr = params.get(KEY_GEO_LONGITUDE);
        String query = params.get(KEY_GEO_QUERY);

        double latitude;
        double longitude;

        try {
            latitude = Double.parseDouble(latStr);
            if (latitude < -90.0 || latitude > 90.0) {
                throw new IllegalArgumentException("Latitude must be between -90 and 90. Received: " + latitude);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid latitude format: '" + latStr + "'. Must be a valid number.", e);
        }

        try {
            longitude = Double.parseDouble(lonStr);
            if (longitude < -180.0 || longitude > 180.0) {
                throw new IllegalArgumentException("Longitude must be between -180 and 180. Received: " + longitude);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid longitude format: '" + lonStr + "'. Must be a valid number.", e);
        }

        StringBuilder geoBuilder = new StringBuilder("geo:");
        geoBuilder.append(latitude).append(",").append(longitude);

        if (query != null && !query.trim().isEmpty()) {
            try {
                geoBuilder.append("?q=").append(URLEncoder.encode(query.trim(), StandardCharsets.UTF_8.name()));
            } catch (UnsupportedEncodingException e) {
                // This should not happen with UTF-8
                throw new RuntimeException("Error encoding query parameter for geo-location: " + e.getMessage(), e);
            }
        }

        return geoBuilder.toString();
    }

    @Override
    public boolean supports(QrCodeType type) {
        return type == QrCodeType.GEO;
    }
}
