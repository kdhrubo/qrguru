package com.example.qrcodegenerator.service.formatter;

import com.example.qrcodegenerator.model.QrCodeType;

import java.util.Map;

public interface QrDataFormatter {

    /**
     * Formats the input parameters into a string suitable for QR code encoding.
     *
     * @param params A map containing the data needed for the specific QR code type.
     *               For example, for TEXT type, it might expect a "text" key.
     *               For URL type, it might expect a "url" key.
     * @return The formatted string to be encoded in the QR code.
     * @throws IllegalArgumentException if required parameters are missing or invalid.
     */
    String format(Map<String, String> params);

    /**
     * Checks if this formatter supports the given QR code type.
     *
     * @param type The QrCodeType enum value.
     * @return true if the formatter supports the type, false otherwise.
     */
    boolean supports(QrCodeType type);
}
