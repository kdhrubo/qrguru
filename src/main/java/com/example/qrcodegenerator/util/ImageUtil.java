package com.example.qrcodegenerator.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

public class ImageUtil {

    /**
     * Extracts the Base64 data part from a data URI string.
     * Example: "data:image/png;base64,iVBOR..." -> "iVBOR..."
     * If the "base64," marker is not found, it assumes the input is already raw Base64.
     *
     * @param dataUri The data URI string.
     * @return The extracted Base64 data string, or the original string if not a typical data URI.
     */
    public static String extractBase64Data(String dataUri) {
        if (dataUri == null) {
            return null;
        }
        int commaIndex = dataUri.indexOf(',');
        if (commaIndex != -1 && dataUri.substring(0, commaIndex).contains(";base64")) {
            return dataUri.substring(commaIndex + 1);
        }
        // If no "data:image/...;base64," prefix, assume it's already raw Base64
        return dataUri;
    }

    /**
     * Decodes a Base64 encoded string into a BufferedImage.
     * The input string should be raw Base64 data, without any "data:image/..." prefix.
     *
     * @param base64String The raw Base64 encoded image string.
     * @return BufferedImage if successful, or null if decoding fails or input is invalid.
     */
    public static BufferedImage decodeBase64ToImage(String base64String) {
        if (base64String == null || base64String.trim().isEmpty()) {
            return null;
        }

        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64String.trim());
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            BufferedImage image = ImageIO.read(bis);
            bis.close();
            if (image == null) {
                System.err.println("Warning: ImageIO.read returned null. The Base64 data might not be a recognized image format or is corrupted.");
            }
            return image;
        } catch (IllegalArgumentException e) {
            // Handles invalid Base64 characters
            System.err.println("Warning: Could not decode Base64 string: " + e.getMessage());
            return null;
        } catch (IOException e) {
            System.err.println("Warning: Could not read image from decoded Base64 bytes: " + e.getMessage());
            return null;
        }
    }
}
