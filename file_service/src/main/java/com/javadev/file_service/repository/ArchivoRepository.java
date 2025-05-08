package com.javadev.file_service.repository;

import com.javadev.file_service.entity.Archivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArchivoRepository extends JpaRepository<Archivo, Long> {
}
