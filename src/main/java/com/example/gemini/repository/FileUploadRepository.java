package com.example.gemini.repository;

import com.example.gemini.entity.FileUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileUploadRepository extends JpaRepository<FileUpload, Long> {
    Optional<FileUpload> findByFileKey(String fileKey);

    Optional<FileUpload> findByFileName(String fileName);

    List<FileUpload> findByUploadStatus(FileUpload.FileUploadStatus status);

    List<FileUpload> findByDestinationStatus(FileUpload.DestinationStatus status);
}
