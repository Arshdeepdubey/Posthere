package com.example.gemini.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.File;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendSimpleEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom("noreply@posthere.com");

        javaMailSender.send(message);
    }

    public void sendHtmlEmailWithAttachment(String to, String subject, String htmlBody, File attachment)
            throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true); // true indicates HTML
        helper.setFrom("noreply@posthere.com");

        if (attachment != null && attachment.exists()) {
            helper.addAttachment(attachment.getName(), attachment);
        }

        javaMailSender.send(mimeMessage);
    }

    public String getDefaultEmailTemplate(String fileName) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; }\n" +
                "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }\n" +
                "        .header { background-color: #4CAF50; color: white; padding: 10px; text-align: center; }\n" +
                "        .content { padding: 20px; border: 1px solid #ddd; }\n" +
                "        .footer { text-align: center; padding: 10px; font-size: 12px; color: #666; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <div class=\"header\">\n" +
                "            <h2>File Delivery Notification</h2>\n" +
                "        </div>\n" +
                "        <div class=\"content\">\n" +
                "            <p>Dear Recipient,</p>\n" +
                "            <p>Your requested file <strong>" + fileName + "</strong> has been extracted and is attached to this email.</p>\n" +
                "            <p>Please find the extracted file(s) in the attachment below.</p>\n" +
                "            <hr/>\n" +
                "            <p><strong>File Details:</strong></p>\n" +
                "            <ul>\n" +
                "                <li><strong>File Name:</strong> " + fileName + "</li>\n" +
                "            <li><strong>Delivered At:</strong> " + java.time.LocalDateTime.now() + "</li>\n" +
                "                <li><strong>Status:</strong> Successfully Extracted and Sent</li>\n" +
                "            </ul>\n" +
                "            <hr/>\n" +
                "            <p>If you have any questions, please contact support.</p>\n" +
                "        </div>\n" +
                "        <div class=\"footer\">\n" +
                "            <p>&copy; 2024 Posthere. All rights reserved.</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }
}
