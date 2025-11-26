package com.ticketshall.notifications.service;

import com.google.zxing.WriterException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class QrCodeServiceTest {

    private QrCodeService qrCodeService;

    @BeforeEach
    void setUp() {
        qrCodeService = new QrCodeService();
    }

    @Test
    void generateQrCodeBase64_WithCustomDimensions_ShouldReturnBase64String() throws WriterException, IOException {
        // Arrange
        String text = "TICKET-12345";
        int width = 300;
        int height = 300;

        // Act
        String result = qrCodeService.generateQrCodeBase64(text, width, height);

        // Assert
        assertNotNull(result);
        assertTrue(result.startsWith("data:image/png;base64,"));
        assertTrue(result.length() > 100); // Base64 encoded image should be substantial
    }

    @Test
    void generateQrCodeBase64_WithDefaultDimensions_ShouldReturnBase64String() throws WriterException, IOException {
        // Arrange
        String text = "TICKET-67890";

        // Act
        String result = qrCodeService.generateQrCodeBase64(text);

        // Assert
        assertNotNull(result);
        assertTrue(result.startsWith("data:image/png;base64,"));
        assertTrue(result.length() > 100);
    }

    @Test
    void generateQrCodeBase64_WithEmptyString_ShouldThrowException() {
        // Arrange
        String text = "";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            qrCodeService.generateQrCodeBase64(text);
        });
    }

    @Test
    void generateQrCodeBase64_WithLongText_ShouldReturnBase64String() throws WriterException, IOException {
        // Arrange
        String text = "This is a very long text that should still be encoded into a QR code successfully";

        // Act
        String result = qrCodeService.generateQrCodeBase64(text);

        // Assert
        assertNotNull(result);
        assertTrue(result.startsWith("data:image/png;base64,"));
        assertTrue(result.length() > 100);
    }

    @Test
    void generateQrCodeBase64_WithSpecialCharacters_ShouldReturnBase64String() throws WriterException, IOException {
        // Arrange
        String text = "TICKET-!@#$%^&*()_+-=[]{}|;:',.<>?";

        // Act
        String result = qrCodeService.generateQrCodeBase64(text);

        // Assert
        assertNotNull(result);
        assertTrue(result.startsWith("data:image/png;base64,"));
        assertTrue(result.length() > 100);
    }

    @Test
    void generateQrCodeBase64_WithDifferentTexts_ShouldReturnDifferentResults() throws WriterException, IOException {
        // Arrange
        String text1 = "TICKET-111";
        String text2 = "TICKET-222";

        // Act
        String result1 = qrCodeService.generateQrCodeBase64(text1);
        String result2 = qrCodeService.generateQrCodeBase64(text2);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotEquals(result1, result2);
    }

    @Test
    void generateQrCodeBase64_WithSmallDimensions_ShouldReturnBase64String() throws WriterException, IOException {
        // Arrange
        String text = "TICKET-SMALL";
        int width = 50;
        int height = 50;

        // Act
        String result = qrCodeService.generateQrCodeBase64(text, width, height);

        // Assert
        assertNotNull(result);
        assertTrue(result.startsWith("data:image/png;base64,"));
    }

    @Test
    void generateQrCodeBase64_WithLargeDimensions_ShouldReturnBase64String() throws WriterException, IOException {
        // Arrange
        String text = "TICKET-LARGE";
        int width = 1000;
        int height = 1000;

        // Act
        String result = qrCodeService.generateQrCodeBase64(text, width, height);

        // Assert
        assertNotNull(result);
        assertTrue(result.startsWith("data:image/png;base64,"));
        assertTrue(result.length() > 1000); // Larger dimensions should produce larger base64 string
    }
}
