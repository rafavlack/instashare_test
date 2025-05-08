package com.javadev.file_service.controller;

import com.javadev.file_service.entity.Archivo;
import com.javadev.file_service.service.ArchivoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/archivos")
@RequiredArgsConstructor
public class ArchivoController {

    private final ArchivoService archivoService;

    // Subir un archivo
    @PostMapping("/subir")
    public ResponseEntity<Archivo> subirArchivo(@RequestParam("archivo") MultipartFile archivo) throws Exception {
        Archivo resultado = archivoService.subirArchivo(archivo);
        return ResponseEntity.ok(resultado);
    }

    // Listar todos los archivos
    @GetMapping
    public ResponseEntity<List<Archivo>> listarArchivos() {
        return ResponseEntity.ok(archivoService.listarArchivos());
    }

    // Renombrar un archivo
    @PatchMapping("/{id}/renombrar")
    public ResponseEntity<Archivo> renombrarArchivo(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String nuevoNombre = payload.get("nombre");
        return ResponseEntity.ok(archivoService.renombrarArchivo(id, nuevoNombre));
    }

    // Descargar archivo procesado (ZIP)
    @GetMapping("/{id}/descargar")
    public ResponseEntity<byte[]> descargarArchivo(@PathVariable Long id) throws Exception {
        byte[] contenido = archivoService.descargarArchivo(id);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"archivo.zip\"")
                .body(contenido);
    }


    // Cambiar estado del archivo
    @PatchMapping("/{id}/estado")
    public ResponseEntity<Archivo> cambiarEstadoArchivo(@PathVariable Long id) throws Exception {
        Archivo archivo = archivoService.procesarArchivo(id);
        return ResponseEntity.ok(archivo);
    }
}
