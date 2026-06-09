package com.example.gemini.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileExtractionEmailRequestDTO {
    private String fileKey; // S3 object key
    private String recipientEmail;
    private String subject;
    private String message;
}
