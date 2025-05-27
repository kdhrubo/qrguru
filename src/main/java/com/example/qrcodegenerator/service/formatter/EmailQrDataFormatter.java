package com.example.qrcodegenerator.service.formatter;

import com.example.qrcodegenerator.model.QrCodeType;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class EmailQrDataFormatter implements QrDataFormatter {

    public static final String KEY_EMAIL_TO = "to";
    public static final String KEY_EMAIL_CC = "cc";
    public static final String KEY_EMAIL_BCC = "bcc";
    public static final String KEY_EMAIL_SUBJECT = "subject";
    public static final String KEY_EMAIL_BODY = "body";

    @Override
    public String format(Map<String, String> params) {
        if (params == null || !params.containsKey(KEY_EMAIL_TO)) {
            throw new IllegalArgumentException("Parameters for Email must contain '" + KEY_EMAIL_TO + "'.");
        }

        String to = params.get(KEY_EMAIL_TO);
        if (to == null || to.trim().isEmpty()) {
            throw new IllegalArgumentException("Email 'to' address cannot be null or empty.");
        }

        StringBuilder mailtoBuilder = new StringBuilder("mailto:");
        mailtoBuilder.append(encodeValue(to.trim()));

        String cc = params.get(KEY_EMAIL_CC);
        String bcc = params.get(KEY_EMAIL_BCC);
        String subject = params.get(KEY_EMAIL_SUBJECT);
        String body = params.get(KEY_EMAIL_BODY);

        Stream.Builder<String> queryParams = Stream.builder();
        if (cc != null && !cc.trim().isEmpty()) {
            queryParams.add("cc=" + encodeValue(cc.trim()));
        }
        if (bcc != null && !bcc.trim().isEmpty()) {
            queryParams.add("bcc=" + encodeValue(bcc.trim()));
        }
        if (subject != null && !subject.trim().isEmpty()) {
            queryParams.add("subject=" + encodeValue(subject.trim()));
        }
        if (body != null && !body.trim().isEmpty()) {
            queryParams.add("body=" + encodeValue(body.trim()));
        }
        
        String queryString = queryParams.build().collect(Collectors.joining("&"));
        if (!queryString.isEmpty()) {
            mailtoBuilder.append("?").append(queryString);
        }

        return mailtoBuilder.toString();
    }

    private String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            // This should not happen with UTF-8
            throw new RuntimeException("Error encoding URL parameter for email: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean supports(QrCodeType type) {
        return type == QrCodeType.EMAIL;
    }
}
