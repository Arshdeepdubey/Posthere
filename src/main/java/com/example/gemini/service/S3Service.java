package com.example.gemini.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.nio.file.Files;

@Service
public class S3Service {

    @Autowired
    private S3Client s3Client;

    @Value("${aws.s3.bucket-name:posthere-bucket}")
    private String bucketName;

    public String uploadFile(File file, String fileKey) throws Exception {
        byte[] fileBytes = Files.readAllBytes(file.toPath());

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileBytes));

        return fileKey;
    }

    public byte[] downloadFile(String fileKey) throws Exception {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .build();

        return s3Client.getObject(getObjectRequest).readAllBytes();
    }

    public boolean fileExists(String fileKey) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();

            s3Client.headObject(headObjectRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Error checking file existence: " + e.getMessage(), e);
        }
    }

    public void deleteFile(String fileKey) throws Exception {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
    }

    public String getBucketName() {
        return bucketName;
    }
}
