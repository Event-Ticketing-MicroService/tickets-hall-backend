package com.ticketshall.notifications.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

@Service
public class QrCodeService {

    /**
     * Generates a QR code for the given text and returns it as a Base64 encoded string
     * that can be used directly in HTML img src attribute
     * 
     * @param text The text to encode in the QR code
     * @param width Width of the QR code image
     * @param height Height of the QR code image
     * @return Base64 encoded PNG image string
     * @throws WriterException If QR code generation fails
     * @throws IOException If image conversion fails
     */
    public String generateQrCodeBase64(String text, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        
        byte[] qrCodeBytes = outputStream.toByteArray();
        String base64Image = Base64.getEncoder().encodeToString(qrCodeBytes);
        
        return "data:image/png;base64," + base64Image;
    }

    /**
     * Generates a QR code with default size (200x200)
     * 
     * @param text The text to encode in the QR code
     * @return Base64 encoded PNG image string
     * @throws WriterException If QR code generation fails
     * @throws IOException If image conversion fails
     */
    public String generateQrCodeBase64(String text) throws WriterException, IOException {
        return generateQrCodeBase64(text, 200, 200);
    }
}