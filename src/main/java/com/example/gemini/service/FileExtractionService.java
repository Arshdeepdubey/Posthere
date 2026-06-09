package com.example.gemini.service;

import net.lingala.zip4j.ZipFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FileExtractionService {

    @Value("${file.extraction.temp-dir:/tmp/posthere-extraction}")
    private String tempExtractionDir;

    public File extractZipFile(byte[] zipFileBytes, String originalFileName) throws Exception {
        // Create temp directory if it doesn't exist
        Path tempDirPath = Paths.get(tempExtractionDir);
        Files.createDirectories(tempDirPath);

        // Create unique extraction directory
        String extractionId = UUID.randomUUID().toString();
        Path extractionPath = tempDirPath.resolve(extractionId);
        Files.createDirectories(extractionPath);

        // Save zip file temporarily
        String zipFileName = originalFileName != null ? originalFileName : "upload_" + System.currentTimeMillis() + ".zip";
        Path zipFilePath = extractionPath.resolve(zipFileName);
        Files.write(zipFilePath, zipFileBytes);

        // Extract zip file
        ZipFile zipFile = new ZipFile(zipFilePath.toFile());
        zipFile.extractAll(extractionPath.toString());

        // Delete the zip file after extraction
        Files.delete(zipFilePath);

        // Return the extraction directory
        return extractionPath.toFile();
    }

    public List<File> getExtractedFiles(File extractionDirectory) {
        List<File> extractedFiles = new ArrayList<>();
        File[] files = extractionDirectory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    extractedFiles.add(file);
                }
            }
        }

        return extractedFiles;
    }

    public void cleanupExtractionDirectory(File extractionDirectory) {
        if (extractionDirectory != null && extractionDirectory.exists()) {
            deleteDirectory(extractionDirectory);
        }
    }

    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                }
                file.delete();
            }
        }
        directory.delete();
    }

    public File getFirstExtractedFile(File extractionDirectory) throws Exception {
        List<File> extractedFiles = getExtractedFiles(extractionDirectory);

        if (extractedFiles.isEmpty()) {
            throw new Exception("No files found in extracted directory");
        }

        return extractedFiles.get(0);
    }
}
