package com.example.qrcodegenerator.service.formatter;

import com.example.qrcodegenerator.model.QrCodeType;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class NoOpQrDataFormatter implements QrDataFormatter {

    // This formatter acts as a fallback for types not yet implemented.
    // For a more robust system, the selection logic in the service
    // would determine if any specific formatter supports a type,
    // and if not, then this behavior (throwing exception) would apply.
    // Or, this formatter could explicitly list types it "supports" by
    // simply being the one to throw the exception for them.

    private static final Set<QrCodeType> UNSUPPORTED_TYPES = new HashSet<>(Arrays.asList(
            // QrCodeType.VCARD, // Now handled by VCardQrDataFormatter
            // QrCodeType.WIFI,  // Now handled by WifiQrDataFormatter
            // QrCodeType.EMAIL, // Now handled by EmailQrDataFormatter
            // QrCodeType.SMS    // Now handled by SmsQrDataFormatter
            // Add any future types here that are known but not yet implemented.
            // For example, if you add QrCodeType.GEO but don't have a formatter yet:
            // QrCodeType.GEO 
    ));

    @Override
    public String format(Map<String, String> params) {
        // This method will be called if this formatter is selected.
        // It indicates that a QrCodeType was specified for which no dedicated formatter exists,
        // yet this NoOpQrDataFormatter claimed to support it (perhaps for future types).
        throw new UnsupportedOperationException("The specified QR code type is recognized but not currently supported for generation.");
    }

    @Override
    public boolean supports(QrCodeType type) {
        // This formatter now only supports types explicitly listed in UNSUPPORTED_TYPES.
        // If UNSUPPORTED_TYPES is empty, this formatter will not support any type.
        // This is preferred, as QRCodeService itself will throw an exception if no formatter is found.
        return UNSUPPORTED_TYPES.contains(type);
    }
}
