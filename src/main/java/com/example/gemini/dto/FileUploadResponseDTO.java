package com.example.gemini.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileUploadResponseDTO {
    private Long id;
    private String fileName;
    private String fileKey;
    private Long fileSize;
    private String uploadStatus;
    private String destinationStatus;
    private LocalDateTime uploadDateTime;
    private LocalDateTime sentToDestinationDateTime;
    private String s3BucketName;
    private String errorMessage;
    private String message;
}
