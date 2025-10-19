package com.ticketshall.notifications;

import java.io.IOException;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import com.google.zxing.WriterException;
import com.ticketshall.notifications.service.EmailService;
import com.ticketshall.notifications.service.QrCodeService;

import jakarta.mail.MessagingException;

@SpringBootApplication
public class NotificationApplication {
    public static void main(String[] args) throws WriterException, IOException {
        ApplicationContext ctx = SpringApplication.run(NotificationApplication.class, args);

        EmailService em = ctx.getBean(EmailService.class);

        QrCodeService qrCodeService = ctx.getBean(QrCodeService.class);

        try {
            // Test ticket email with QR code
            Map<String, Object> variables = Map.of(
                "name", "Ahmed",
                "eventName", "Spring Boot Conference 2025",
                "code", "TICKET-ABC123-XYZ789",
                "startTime", "2025-10-25 14:00",
                "endTime", "2025-10-25 18:00",
                "location", "Convention Center, Main Hall"
            );
            String code = (String) variables.get("code");
            String qrCodeBase64 = qrCodeService.generateQrCodeBase64(code, 150, 150);
            variables.put("qrCode", qrCodeBase64);
            em.sendTemplate("ticket-created", "ahmed@example.com", "Your Ticket for Spring Boot Conference 2025", variables);
            System.out.println("Ticket email sent successfully with QR code!");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}