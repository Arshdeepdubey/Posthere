package com.example.gemini.service;

import com.example.gemini.dto.FileUploadResponseDTO;
import com.example.gemini.entity.FileUpload;
import com.example.gemini.repository.FileUploadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileUploadService Tests")
public class FileUploadServiceTest {

    @Mock
    private FileUploadRepository fileUploadRepository;

    // Use real service instance instead of mocking
    private S3Service s3Service;
    private EmailService emailService;
    private FileExtractionService fileExtractionService;

    @InjectMocks
    private FileUploadService fileUploadService;

    private FileUpload testFileUpload;
    private MockMultipartFile testFile;

    @BeforeEach
    void setUp() {
        testFile = new MockMultipartFile(
                "file",
                "test.zip",
                "application/zip",
                "PK".getBytes()
        );

        testFileUpload = FileUpload.builder()
                .id(1L)
                .fileName("test.zip")
                .fileKey("uploads/uuid/test.zip")
                .fileSize(1024000L)
                .uploadStatus(FileUpload.FileUploadStatus.UPLOADED)
                .destinationStatus(FileUpload.DestinationStatus.SENT)
                .uploadDateTime(LocalDateTime.now())
                .s3BucketName("posthere-bucket")
                .build();
    }

    @Test
    @DisplayName("Should throw exception for null file")
    void testUploadFileToS3_NullFile() {
        assertThrows(IllegalArgumentException.class, () -> {
            fileUploadService.uploadFileToS3(null);
        });
    }

    @Test
    @DisplayName("Should throw exception for empty file")
    void testUploadFileToS3_EmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.zip",
                "application/zip",
                new byte[0]
        );

        assertThrows(IllegalArgumentException.class, () -> {
            fileUploadService.uploadFileToS3(emptyFile);
        });
    }

    @Test
    @DisplayName("Should retrieve file details by ID")
    void testGetFileDetails_Success() throws Exception {
        when(fileUploadRepository.findById(1L)).thenReturn(Optional.of(testFileUpload));

        FileUploadResponseDTO response = fileUploadService.getFileDetails(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("test.zip", response.getFileName());
    }

    @Test
    @DisplayName("Should throw exception for non-existent file")
    void testGetFileDetails_NotFound() {
        when(fileUploadRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> {
            fileUploadService.getFileDetails(999L);
        });
    }
}
