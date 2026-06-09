package com.example.gemini.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import jakarta.mail.internet.MimeMessage;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService Tests")
public class EmailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private EmailService emailService;

    private String testRecipient;
    private String testSubject;
    private String testBody;

    @BeforeEach
    void setUp() {
        testRecipient = "user@example.com";
        testSubject = "Test Subject";
        testBody = "Test Body";
    }

    @Test
    @DisplayName("Should send simple email successfully")
    void testSendSimpleEmail_Success() {
        assertDoesNotThrow(() -> {
            emailService.sendSimpleEmail(testRecipient, testSubject, testBody);
        });

        verify(javaMailSender, times(1)).send(any(org.springframework.mail.SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Should generate default email template")
    void testGetDefaultEmailTemplate() {
        String template = emailService.getDefaultEmailTemplate("test.txt");

        assertNotNull(template);
        assertTrue(template.contains("<!DOCTYPE html>"));
        assertTrue(template.contains("File Delivery Notification"));
        assertTrue(template.contains("test.txt"));
    }

    @Test
    @DisplayName("Should contain HTML structure in email template")
    void testEmailTemplate_ContainsHtmlStructure() {
        String template = emailService.getDefaultEmailTemplate("document.pdf");

        assertTrue(template.contains("<html>"));
        assertTrue(template.contains("</html>"));
        assertTrue(template.contains("<body>"));
        assertTrue(template.contains("</body>"));
    }

    @Test
    @DisplayName("Should handle null file name in template")
    void testGetDefaultEmailTemplate_NullFileName() {
        String template = emailService.getDefaultEmailTemplate(null);

        assertNotNull(template);
        assertTrue(template.contains("null")); // Will contain null string
    }
}
