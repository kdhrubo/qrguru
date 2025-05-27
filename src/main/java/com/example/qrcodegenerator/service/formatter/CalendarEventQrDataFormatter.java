package com.example.qrcodegenerator.service.formatter;

import com.example.qrcodegenerator.model.QrCodeType;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
public class CalendarEventQrDataFormatter implements QrDataFormatter {

    public static final String KEY_CALENDAR_UID = "uid";
    public static final String KEY_CALENDAR_SUMMARY = "summary";
    public static final String KEY_CALENDAR_DTSTART = "dtstart";
    public static final String KEY_CALENDAR_DTEND = "dtend";
    public static final String KEY_CALENDAR_LOCATION = "location";
    public static final String KEY_CALENDAR_DESCRIPTION = "description";
    public static final String KEY_CALENDAR_DTSTAMP = "dtstamp";

    // Regex for YYYYMMDD, YYYYMMDDTHHmmSS, or YYYYMMDDTHHmmSSZ
    private static final Pattern ICAL_DATE_TIME_PATTERN = Pattern.compile(
            "^\\d{8}(T\\d{6}Z?)?$"
    );

    @Override
    public String format(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            throw new IllegalArgumentException("Parameters for Calendar event cannot be null or empty.");
        }

        String summary = params.get(KEY_CALENDAR_SUMMARY);
        String dtStart = params.get(KEY_CALENDAR_DTSTART);
        String dtEnd = params.get(KEY_CALENDAR_DTEND);

        if (summary == null || summary.trim().isEmpty()) {
            throw new IllegalArgumentException("'" + KEY_CALENDAR_SUMMARY + "' is required for Calendar event.");
        }
        if (dtStart == null || dtStart.trim().isEmpty()) {
            throw new IllegalArgumentException("'" + KEY_CALENDAR_DTSTART + "' is required for Calendar event.");
        }
        if (dtEnd == null || dtEnd.trim().isEmpty()) {
            throw new IllegalArgumentException("'" + KEY_CALENDAR_DTEND + "' is required for Calendar event.");
        }

        validateDateTimeFormat(dtStart, KEY_CALENDAR_DTSTART);
        validateDateTimeFormat(dtEnd, KEY_CALENDAR_DTEND);

        String uid = params.getOrDefault(KEY_CALENDAR_UID, UUID.randomUUID().toString());
        String dtStamp = params.getOrDefault(KEY_CALENDAR_DTSTAMP, getCurrentUtcTimestamp());

        validateDateTimeFormat(dtStamp, KEY_CALENDAR_DTSTAMP); // Also validate provided dtstamp

        String location = params.get(KEY_CALENDAR_LOCATION);
        String description = params.get(KEY_CALENDAR_DESCRIPTION);

        StringBuilder veventBuilder = new StringBuilder();
        veventBuilder.append("BEGIN:VEVENT\n");
        appendIfPresent(veventBuilder, "UID", uid, false); // UID is not typically escaped in the same way
        appendIfPresent(veventBuilder, "DTSTAMP", dtStamp, false); // Timestamps are not typically escaped
        appendIfPresent(veventBuilder, "SUMMARY", summary, true);
        appendIfPresent(veventBuilder, "DTSTART", dtStart, false); // Dates are not typically escaped
        appendIfPresent(veventBuilder, "DTEND", dtEnd, false);   // Dates are not typically escaped
        appendIfPresent(veventBuilder, "LOCATION", location, true);
        appendIfPresent(veventBuilder, "DESCRIPTION", description, true);
        veventBuilder.append("END:VEVENT");

        return veventBuilder.toString();
    }

    private void validateDateTimeFormat(String dateTime, String fieldName) {
        if (dateTime != null && !dateTime.trim().isEmpty()) {
            if (!ICAL_DATE_TIME_PATTERN.matcher(dateTime.trim()).matches()) {
                throw new IllegalArgumentException("Invalid format for '" + fieldName + "': '" + dateTime +
                        "'. Expected YYYYMMDD, YYYYMMDDTHHmmSS, or YYYYMMDDTHHmmSSZ.");
            }
        }
    }

    private String getCurrentUtcTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());
    }
    
    private void appendIfPresent(StringBuilder builder, String property, String value, boolean escape) {
        if (value != null && !value.trim().isEmpty()) {
            builder.append(property).append(":")
                   .append(escape ? escapeVEventString(value.trim()) : value.trim())
                   .append("\n");
        }
    }

    private String escapeVEventString(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace(",", "\\,")
                   .replace(";", "\\;")
                   .replace("\n", "\\n");
    }

    @Override
    public boolean supports(QrCodeType type) {
        return type == QrCodeType.CALENDAR;
    }
}
