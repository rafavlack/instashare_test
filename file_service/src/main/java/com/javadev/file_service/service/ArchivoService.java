package com.javadev.file_service.service;

import com.javadev.file_service.entity.Archivo;
import com.javadev.file_service.exception.ArchivoNoEncontradoException;
import com.javadev.file_service.exception.ArchivoNoProcesadoException;
import com.javadev.file_service.repository.ArchivoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class ArchivoService {

    private final ArchivoRepository archivoRepository;
    private final MinioService minioService;

    // Método para subir archivos
    public Archivo subirArchivo(MultipartFile file) throws Exception {
        String minioKey = minioService.uploadFile(file);

        Archivo archivo = Archivo.builder()
                .nombreOriginal(file.getOriginalFilename())
                .nombreActual(file.getOriginalFilename())
                .estado("PENDIENTE")
                .size(file.getSize())
                .minioObjectName(minioKey)
                .fechaSubida(LocalDateTime.now())
                .build();

        return archivoRepository.save(archivo);
    }

    // Método para listar todos los archivos
    public List<Archivo> listarArchivos() {
        return archivoRepository.findAll();
    }

    // Método para renombrar archivos
    public Archivo renombrarArchivo(Long id, String nuevoNombre) {
        Archivo archivo = archivoRepository.findById(id)
                .orElseThrow(() -> new ArchivoNoEncontradoException("Archivo no encontrado con id: " + id));
        archivo.setNombreActual(nuevoNombre);
        return archivoRepository.save(archivo);
    }

    // Método para descargar el archivo
    public byte[] descargarArchivo(Long id) throws Exception {
        Archivo archivo = archivoRepository.findById(id)
                .orElseThrow(() -> new ArchivoNoEncontradoException("Archivo no encontrado con id: " + id));

        if (!"PROCESADO".equals(archivo.getEstado())) {
            throw new ArchivoNoProcesadoException("El archivo aún no ha sido procesado");
        }

        try (InputStream is = minioService.getFile(archivo.getMinioObjectName())) {
            return is.readAllBytes();
        }
    }

    // Método para procesar el archivo: comprimirlo y subirlo a MinIO
    public Archivo procesarArchivo(Long id) throws Exception {
        Archivo archivo = archivoRepository.findById(id)
                .orElseThrow(() -> new ArchivoNoEncontradoException("Archivo no encontrado con id: " + id));

        if ("PROCESADO".equals(archivo.getEstado())) {
            return archivo;
        }


        try (InputStream is = minioService.getFile(archivo.getMinioObjectName());
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            // Crear una entrada en el ZIP
            ZipEntry entry = new ZipEntry(archivo.getNombreActual());
            zos.putNextEntry(entry);

            // Leer contenido y escribirlo en el ZIP
            is.transferTo(zos);
            zos.closeEntry();
            zos.finish();

            // Subir ZIP a MinIO
            String zipFileName = archivo.getId() + "_" + archivo.getNombreActual().replaceAll(" ", "_") + ".zip";
            InputStream zipStream = new ByteArrayInputStream(baos.toByteArray());
            String zipMinioKey = minioService.uploadBytes(zipFileName, baos.toByteArray());

            // Actualizar entidad
            archivo.setMinioObjectName(zipMinioKey);
            archivo.setEstado("PROCESADO");
            archivo.setFechaProcesado(LocalDateTime.now());

            return archivoRepository.save(archivo);
        }
    }
}
