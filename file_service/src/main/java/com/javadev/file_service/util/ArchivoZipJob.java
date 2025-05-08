package com.javadev.file_service.util;

import com.javadev.file_service.entity.Archivo;
import com.javadev.file_service.repository.ArchivoRepository;
import com.javadev.file_service.service.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class ArchivoZipJob {

    private final ArchivoRepository archivoRepository;
    private final MinioService minioService;

    @Scheduled(fixedDelay = 10000) // cada 10 segundos
    public void procesarArchivos() {
        List<Archivo> pendientes = archivoRepository.findAll().stream()
                .filter(a -> a.getEstado().equals("PENDIENTE"))
                .toList();

        for (Archivo archivo : pendientes) {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 ZipOutputStream zos = new ZipOutputStream(baos);
                 InputStream original = minioService.getFile(archivo.getMinioObjectName())) {

                ZipEntry entry = new ZipEntry(archivo.getNombreOriginal());
                zos.putNextEntry(entry);
                original.transferTo(zos);
                zos.closeEntry();
                zos.finish();

                // Subir archivo ZIP
                String zipKey = minioService.uploadBytes(archivo.getNombreOriginal() + ".zip", baos.toByteArray());

                archivo.setMinioObjectName(zipKey);
                archivo.setEstado("PROCESADO");
                archivo.setFechaProcesado(LocalDateTime.now());
                archivoRepository.save(archivo);

                log.info("Archivo procesado y comprimido: {}", archivo.getNombreOriginal());

            } catch (Exception e) {
                log.error("Error al comprimir archivo {}: {}", archivo.getId(), e.getMessage());
            }
        }
    }
}
