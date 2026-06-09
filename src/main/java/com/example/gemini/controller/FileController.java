package com.example.gemini.controller;

import com.example.gemini.dto.FileExtractionEmailRequestDTO;
import com.example.gemini.dto.FileUploadResponseDTO;
import com.example.gemini.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileUploadService fileUploadService;

    /**
     * POST endpoint to upload zip file to S3
     * Requires JWT token in Authorization header: Bearer <token>
     * @param file Zip file to upload (max 5MB)
     * @return FileUploadResponseDTO with upload status and S3 details
     */
    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponseDTO> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // Validate file size (5MB limit)
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body(FileUploadResponseDTO.builder()
                                .message("File size exceeds 5MB limit")
                                .errorMessage("File too large")
                                .build());
            }

            // Validate file type
            if (!file.getContentType().equals("application/zip") && !file.getContentType().equals("application/x-zip-compressed")) {
                return ResponseEntity.badRequest()
                        .body(FileUploadResponseDTO.builder()
                                .message("Only ZIP files are allowed")
                                .errorMessage("Invalid file type")
                                .build());
            }

            FileUploadResponseDTO response = fileUploadService.uploadFileToS3(file);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(FileUploadResponseDTO.builder()
                            .message("Error uploading file")
                            .errorMessage(e.getMessage())
                            .build());
        }
    }

    /**
     * GET endpoint to fetch file details and upload status
     * @param fileId Database ID of the file
     * @return FileUploadResponseDTO with file details and status
     */
    @GetMapping("/details/{fileId}")
    public ResponseEntity<FileUploadResponseDTO> getFileDetails(@PathVariable Long fileId) {
        try {
            FileUploadResponseDTO response = fileUploadService.getFileDetails(fileId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(FileUploadResponseDTO.builder()
                            .message("File not found")
                            .errorMessage(e.getMessage())
                            .build());
        }
    }

    /**
     * POST endpoint to extract zip file from S3 and send via email
     * Requires JWT token in Authorization header: Bearer <token>
     * Validates file exists in S3 before extraction
     * @param requestDTO Contains fileKey (S3 path) and recipient email
     * @return FileUploadResponseDTO with extraction and email status
     */
    @PostMapping("/extract-and-email")
    public ResponseEntity<FileUploadResponseDTO> extractAndEmailFile(@RequestBody FileExtractionEmailRequestDTO requestDTO) {
        try {
            // Validate inputs
            if (requestDTO.getFileKey() == null || requestDTO.getFileKey().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(FileUploadResponseDTO.builder()
                                .message("File key is required")
                                .errorMessage("Missing file key")
                                .build());
            }

            if (requestDTO.getRecipientEmail() == null || requestDTO.getRecipientEmail().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(FileUploadResponseDTO.builder()
                                .message("Recipient email is required")
                                .errorMessage("Missing recipient email")
                                .build());
            }

            // Use provided recipient email or default
            String recipientEmail = requestDTO.getRecipientEmail();

            FileUploadResponseDTO response = fileUploadService.extractAndEmailFile(
                    requestDTO.getFileKey(),
                    recipientEmail
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(FileUploadResponseDTO.builder()
                            .message("Error extracting and sending file")
                            .errorMessage(e.getMessage())
                            .build());
        }
    }
}
