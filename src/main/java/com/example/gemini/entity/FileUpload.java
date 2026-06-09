package com.example.gemini.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_uploads")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class FileUpload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    private String fileKey; // S3 object key

    private Long fileSize;

    @Enumerated(EnumType.STRING)
    private FileUploadStatus uploadStatus;

    @Enumerated(EnumType.STRING)
    private DestinationStatus destinationStatus;

    private LocalDateTime uploadDateTime;

    private LocalDateTime sentToDestinationDateTime;

    private String s3BucketName;

    private String errorMessage;

    private String originalFileName;

    public enum FileUploadStatus {
        PENDING,
        UPLOADED,
        FAILED
    }

    public enum DestinationStatus {
        NOT_SENT,
        SENT,
        FAILED
    }
}
