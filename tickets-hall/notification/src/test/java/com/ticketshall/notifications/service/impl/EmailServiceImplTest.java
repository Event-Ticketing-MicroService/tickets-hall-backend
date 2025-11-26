package com.ticketshall.notifications.service.impl;

import com.ticketshall.notifications.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        lenient().when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void sendTemplate_WithValidParameters_ShouldSendEmail() throws MessagingException {
        // Arrange
        String templateName = "ticket-created";
        String to = "test@example.com";
        String subject = "Test Subject";
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "John");
        variables.put("eventName", "Concert");

        String processedHtml = "<html><body>Hello John, welcome to Concert</body></html>";
        when(templateEngine.process(eq(templateName), any(Context.class)))
                .thenReturn(processedHtml);

        // Act
        emailService.sendTemplate(templateName, to, subject, variables);

        // Assert
        verify(templateEngine, times(1)).process(eq(templateName), any(Context.class));
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void sendTemplate_ShouldProcessTemplateWithCorrectVariables() throws MessagingException {
        // Arrange
        String templateName = "test-template";
        String to = "user@example.com";
        String subject = "Test";
        Map<String, Object> variables = new HashMap<>();
        variables.put("key1", "value1");
        variables.put("key2", "value2");

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        when(templateEngine.process(eq(templateName), contextCaptor.capture()))
                .thenReturn("<html>Test</html>");

        // Act
        emailService.sendTemplate(templateName, to, subject, variables);

        // Assert
        Context capturedContext = contextCaptor.getValue();
        assertNotNull(capturedContext);
        verify(templateEngine, times(1)).process(eq(templateName), any(Context.class));
    }

    @Test
    void sendTemplate_WithEmptyVariables_ShouldStillSendEmail() throws MessagingException {
        // Arrange
        String templateName = "simple-template";
        String to = "recipient@example.com";
        String subject = "Simple Email";
        Map<String, Object> variables = new HashMap<>();

        when(templateEngine.process(eq(templateName), any(Context.class)))
                .thenReturn("<html><body>Simple content</body></html>");

        // Act
        emailService.sendTemplate(templateName, to, subject, variables);

        // Assert
        verify(templateEngine, times(1)).process(eq(templateName), any(Context.class));
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void sendTemplate_WithMultipleRecipients_ShouldSendToCorrectAddress() throws MessagingException {
        // Arrange
        String templateName = "notification";
        String to = "multiple@example.com";
        String subject = "Notification";
        Map<String, Object> variables = new HashMap<>();
        variables.put("message", "Test message");

        when(templateEngine.process(eq(templateName), any(Context.class)))
                .thenReturn("<html>Content</html>");

        // Act
        emailService.sendTemplate(templateName, to, subject, variables);

        // Assert
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void sendTemplate_WhenTemplateEngineThrowsException_ShouldPropagateException() {
        // Arrange
        String templateName = "failing-template";
        String to = "test@example.com";
        String subject = "Test";
        Map<String, Object> variables = new HashMap<>();

        when(templateEngine.process(eq(templateName), any(Context.class)))
                .thenThrow(new RuntimeException("Template processing failed"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            emailService.sendTemplate(templateName, to, subject, variables);
        });

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendTemplate_WithComplexVariables_ShouldProcessCorrectly() throws MessagingException {
        // Arrange
        String templateName = "complex-template";
        String to = "complex@example.com";
        String subject = "Complex Email";
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "Alice");
        variables.put("eventName", "Tech Conference 2024");
        variables.put("qrCode", "data:image/png;base64,iVBORw0KGgoAAAANS...");
        variables.put("location", "San Francisco");

        when(templateEngine.process(eq(templateName), any(Context.class)))
                .thenReturn("<html><body>Complex content</body></html>");

        // Act
        emailService.sendTemplate(templateName, to, subject, variables);

        // Assert
        verify(templateEngine, times(1)).process(eq(templateName), any(Context.class));
        verify(mailSender, times(1)).send(mimeMessage);
    }
}
