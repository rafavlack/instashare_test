package com.javadev.file_service.service;

import io.minio.*;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.annotation.PostConstruct;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

@Service
public class MinioService {

    @Value("${minio.url}")
    private String minioUrl;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.bucket}")
    private String bucket;

    private MinioClient minioClient;

    @PostConstruct
    public void init() {
        minioClient = MinioClient.builder()
                .endpoint(minioUrl)
                .credentials(accessKey, secretKey)
                .build();
    }

    // Subir un archivo a MinIO
    public String uploadFile(MultipartFile file) throws Exception {
        String objectName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        try (InputStream is = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(is, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (Exception e) {
            throw new Exception("Error al subir el archivo a MinIO: " + e.getMessage(), e);
        }
        return objectName;
    }

    // Recuperar un archivo de MinIO
    public InputStream getFile(String objectName) throws Exception {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            throw new Exception("Error al obtener el archivo de MinIO: " + e.getMessage(), e);
        }
    }

    // Subir un archivo comprimido (ZIP) a MinIO
    public String uploadBytes(String fileName, byte[] content) throws Exception {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(content)) {
            String objectName = UUID.randomUUID() + "_" + fileName;
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(bais, content.length, -1)
                    .contentType("application/zip")
                    .build());
            return objectName;
        } catch (Exception e) {
            throw new Exception("Error al subir el archivo comprimido a MinIO: " + e.getMessage(), e);
        }
    }
}
