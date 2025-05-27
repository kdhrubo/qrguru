package com.example.qrcodegenerator.service.formatter;

import com.example.qrcodegenerator.model.QrCodeType;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class VCardQrDataFormatter implements QrDataFormatter {

    public static final String KEY_VCARD_FIRST_NAME = "firstName";
    public static final String KEY_VCARD_LAST_NAME = "lastName";
    public static final String KEY_VCARD_ORG = "organization";
    public static final String KEY_VCARD_TITLE = "title"; // Added title for completeness
    public static final String KEY_VCARD_TEL_WORK = "telWork";
    public static final String KEY_VCARD_TEL_HOME = "telHome"; // Added home phone
    public static final String KEY_VCARD_EMAIL = "email";
    public static final String KEY_VCARD_URL = "website";
    public static final String KEY_VCARD_STREET = "street";
    public static final String KEY_VCARD_CITY = "city";
    public static final String KEY_VCARD_STATE = "state";
    public static final String KEY_VCARD_ZIP = "zip";
    public static final String KEY_VCARD_COUNTRY = "country";
    public static final String KEY_VCARD_NOTE = "note"; // Added note field

    @Override
    public String format(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            throw new IllegalArgumentException("Parameters for vCard cannot be null or empty.");
        }

        StringBuilder vCardBuilder = new StringBuilder();
        vCardBuilder.append("BEGIN:VCARD\n");
        vCardBuilder.append("VERSION:3.0\n");

        String firstName = params.get(KEY_VCARD_FIRST_NAME);
        String lastName = params.get(KEY_VCARD_LAST_NAME);

        if (firstName != null || lastName != null) {
            vCardBuilder.append("N:")
                    .append(lastName != null ? escapeVCardString(lastName) : "")
                    .append(";")
                    .append(firstName != null ? escapeVCardString(firstName) : "")
                    .append("\n");
            vCardBuilder.append("FN:")
                    .append(Stream.of(firstName, lastName)
                            .filter(Objects::nonNull)
                            .map(this::escapeVCardString)
                            .collect(Collectors.joining(" ")))
                    .append("\n");
        }

        appendIfPresent(vCardBuilder, "ORG", params.get(KEY_VCARD_ORG));
        appendIfPresent(vCardBuilder, "TITLE", params.get(KEY_VCARD_TITLE));
        appendIfPresent(vCardBuilder, "TEL;TYPE=WORK,VOICE", params.get(KEY_VCARD_TEL_WORK));
        appendIfPresent(vCardBuilder, "TEL;TYPE=HOME,VOICE", params.get(KEY_VCARD_TEL_HOME));
        appendIfPresent(vCardBuilder, "EMAIL", params.get(KEY_VCARD_EMAIL));
        appendIfPresent(vCardBuilder, "URL", params.get(KEY_VCARD_URL));
        appendIfPresent(vCardBuilder, "NOTE", params.get(KEY_VCARD_NOTE));

        // Address components
        String street = params.get(KEY_VCARD_STREET);
        String city = params.get(KEY_VCARD_CITY);
        String state = params.get(KEY_VCARD_STATE);
        String zip = params.get(KEY_VCARD_ZIP);
        String country = params.get(KEY_VCARD_COUNTRY);

        if (Stream.of(street, city, state, zip, country).anyMatch(Objects::nonNull)) {
            vCardBuilder.append("ADR;TYPE=WORK:;;") // P.O. Box and Extended Address are empty
                    .append(street != null ? escapeVCardString(street) : "").append(";")
                    .append(city != null ? escapeVCardString(city) : "").append(";")
                    .append(state != null ? escapeVCardString(state) : "").append(";")
                    .append(zip != null ? escapeVCardString(zip) : "").append(";")
                    .append(country != null ? escapeVCardString(country) : "")
                    .append("\n");
        }

        vCardBuilder.append("END:VCARD");
        return vCardBuilder.toString();
    }

    private void appendIfPresent(StringBuilder builder, String property, String value) {
        if (value != null && !value.trim().isEmpty()) {
            builder.append(property).append(":").append(escapeVCardString(value.trim())).append("\n");
        }
    }

    // Basic vCard string escaping for problematic characters.
    // vCard spec mentions escaping for newline, comma, semicolon, colon, backslash.
    private String escapeVCardString(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace(",", "\\,")
                   .replace(";", "\\;")
                   .replace("\n", "\\n");
        // Colons are generally fine in values unless they are part of structured values like N or ADR.
        // Here, we are escaping them broadly for simplicity, might need refinement for specific fields.
    }


    @Override
    public boolean supports(QrCodeType type) {
        return type == QrCodeType.VCARD;
    }
}
