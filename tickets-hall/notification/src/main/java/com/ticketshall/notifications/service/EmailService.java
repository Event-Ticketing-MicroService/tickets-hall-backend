package com.ticketshall.notifications.service;

import java.util.Map;

import jakarta.mail.MessagingException;

public interface EmailService {
    void sendTemplate(String templateName,String to, String subject, Map<String, Object> variables) throws MessagingException;
}
