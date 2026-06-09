package com.example.gemini.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileExtractionService Tests")
public class FileExtractionServiceTest {

    @InjectMocks
    private FileExtractionService fileExtractionService;

    private byte[] testZipBytes;
    private String tempDir;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = System.getProperty("java.io.tmpdir") + "/test-extraction-" + System.currentTimeMillis();
        Files.createDirectories(Paths.get(tempDir));

        // Create test ZIP file in memory
        testZipBytes = createTestZipFile();

        // Set temp directory in service
        ReflectionTestUtils.setField(fileExtractionService, "tempExtractionDir", tempDir);
    }

    @Test
    @DisplayName("Should extract ZIP file successfully")
    void testExtractZipFile_Success() throws Exception {
        File extractedDir = fileExtractionService.extractZipFile(testZipBytes, "test.zip");

        assertNotNull(extractedDir);
        assertTrue(extractedDir.exists());
        assertTrue(extractedDir.isDirectory());

        // Cleanup
        deleteDirectory(extractedDir);
    }

    @Test
    @DisplayName("Should get extracted files from directory")
    void testGetExtractedFiles_Success() throws Exception {
        File extractedDir = fileExtractionService.extractZipFile(testZipBytes, "test.zip");
        List<File> files = fileExtractionService.getExtractedFiles(extractedDir);

        assertNotNull(files);
        assertTrue(files.size() > 0);

        // Cleanup
        deleteDirectory(extractedDir);
    }

    @Test
    @DisplayName("Should cleanup extraction directory")
    void testCleanupExtractionDirectory_Success() throws Exception {
        File extractedDir = fileExtractionService.extractZipFile(testZipBytes, "test.zip");
        assertTrue(extractedDir.exists());

        fileExtractionService.cleanupExtractionDirectory(extractedDir);

        assertFalse(extractedDir.exists());
    }

    @Test
    @DisplayName("Should handle null directory in cleanup")
    void testCleanupExtractionDirectory_NullDirectory() {
        assertDoesNotThrow(() -> {
            fileExtractionService.cleanupExtractionDirectory(null);
        });
    }

    @Test
    @DisplayName("Should get first extracted file")
    void testGetFirstExtractedFile_Success() throws Exception {
        File extractedDir = fileExtractionService.extractZipFile(testZipBytes, "test.zip");
        File firstFile = fileExtractionService.getFirstExtractedFile(extractedDir);

        assertNotNull(firstFile);
        assertTrue(firstFile.exists());

        // Cleanup
        deleteDirectory(extractedDir);
    }

    // Helper methods
    private byte[] createTestZipFile() throws IOException {
        Path tempZip = Files.createTempFile("test", ".zip");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(tempZip))) {
            ZipEntry entry = new ZipEntry("test.txt");
            zos.putNextEntry(entry);
            zos.write("Test content".getBytes());
            zos.closeEntry();
        }
        return Files.readAllBytes(tempZip);
    }

    private void deleteDirectory(File dir) {
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    }
                    file.delete();
                }
            }
            dir.delete();
        }
    }
}
