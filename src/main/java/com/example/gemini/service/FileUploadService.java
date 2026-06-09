package com.example.gemini.service;

import com.example.gemini.dto.FileUploadResponseDTO;
import com.example.gemini.entity.FileUpload;
import com.example.gemini.repository.FileUploadRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class FileUploadService {

    @Autowired
    private FileUploadRepository fileUploadRepository;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private EmailService emailService;

    @Autowired
    private FileExtractionService fileExtractionService;

    public FileUploadResponseDTO uploadFileToS3(MultipartFile file) throws Exception {
        // Validate file
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        // Create FileUpload entity
        String fileKey = "uploads/" + UUID.randomUUID() + "/" + file.getOriginalFilename();
        FileUpload fileUpload = FileUpload.builder()
                .fileName(file.getOriginalFilename())
                .fileKey(fileKey)
                .fileSize(file.getSize())
                .uploadStatus(FileUpload.FileUploadStatus.PENDING)
                .destinationStatus(FileUpload.DestinationStatus.NOT_SENT)
                .uploadDateTime(LocalDateTime.now())
                .s3BucketName(s3Service.getBucketName())
                .originalFileName(file.getOriginalFilename())
                .build();

        try {
            // Convert MultipartFile to File
            File tempFile = convertMultipartFileToFile(file);

            // Upload to S3
            s3Service.uploadFile(tempFile, fileKey);

            // Update upload status
            fileUpload.setUploadStatus(FileUpload.FileUploadStatus.UPLOADED);
            fileUpload.setDestinationStatus(FileUpload.DestinationStatus.SENT);
            fileUpload.setSentToDestinationDateTime(LocalDateTime.now());

            // Clean up temp file
            tempFile.delete();

        } catch (Exception e) {
            fileUpload.setUploadStatus(FileUpload.FileUploadStatus.FAILED);
            fileUpload.setErrorMessage(e.getMessage());
        }

        // Save to database
        fileUploadRepository.save(fileUpload);

        return mapToResponseDTO(fileUpload);
    }

    public FileUploadResponseDTO getFileDetails(Long fileId) throws Exception {
        FileUpload fileUpload = fileUploadRepository.findById(fileId)
                .orElseThrow(() -> new Exception("File not found with id: " + fileId));

        return mapToResponseDTO(fileUpload);
    }

    public FileUploadResponseDTO extractAndEmailFile(String fileKey, String recipientEmail) throws Exception {
        // Validate file exists in S3
        if (!s3Service.fileExists(fileKey)) {
            throw new Exception("File not found in S3: " + fileKey);
        }

        // Find file record in database
        FileUpload fileUpload = fileUploadRepository.findByFileKey(fileKey)
                .orElseThrow(() -> new Exception("File record not found: " + fileKey));

        try {
            // Download file from S3
            byte[] fileBytes = s3Service.downloadFile(fileKey);

            // Extract zip file
            File extractionDir = fileExtractionService.extractZipFile(fileBytes, fileUpload.getOriginalFileName());

            // Get first extracted file (or combine all if multiple)
            File fileToEmail = fileExtractionService.getFirstExtractedFile(extractionDir);

            // Send email with attachment
            String subject = "File Delivery - " + fileUpload.getFileName();
            String htmlBody = emailService.getDefaultEmailTemplate(fileToEmail.getName());

            emailService.sendHtmlEmailWithAttachment(recipientEmail, subject, htmlBody, fileToEmail);

            // Cleanup extraction directory
            fileExtractionService.cleanupExtractionDirectory(extractionDir);

            // Update file upload status
            fileUpload.setDestinationStatus(FileUpload.DestinationStatus.SENT);
            fileUpload.setSentToDestinationDateTime(LocalDateTime.now());

        } catch (Exception e) {
            fileUpload.setDestinationStatus(FileUpload.DestinationStatus.FAILED);
            fileUpload.setErrorMessage(e.getMessage());
        }

        fileUploadRepository.save(fileUpload);
        return mapToResponseDTO(fileUpload);
    }

    private File convertMultipartFileToFile(MultipartFile multipartFile) throws Exception {
        File tempDir = new File("/tmp/posthere-upload");
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        File file = new File(tempDir, multipartFile.getOriginalFilename());
        multipartFile.transferTo(file);
        return file;
    }

    private FileUploadResponseDTO mapToResponseDTO(FileUpload fileUpload) {
        return FileUploadResponseDTO.builder()
                .id(fileUpload.getId())
                .fileName(fileUpload.getFileName())
                .fileKey(fileUpload.getFileKey())
                .fileSize(fileUpload.getFileSize())
                .uploadStatus(fileUpload.getUploadStatus().toString())
                .destinationStatus(fileUpload.getDestinationStatus().toString())
                .uploadDateTime(fileUpload.getUploadDateTime())
                .sentToDestinationDateTime(fileUpload.getSentToDestinationDateTime())
                .s3BucketName(fileUpload.getS3BucketName())
                .errorMessage(fileUpload.getErrorMessage())
                .message("Operation completed successfully")
                .build();
    }
}
