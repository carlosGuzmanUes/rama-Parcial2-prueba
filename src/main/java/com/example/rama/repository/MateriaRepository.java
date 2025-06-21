package com.example.rama.repository;

import com.example.rama.model.Materia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MateriaRepository extends JpaRepository<Materia, Long> {
    
    List<Materia> findByDocente(String docente);
    
    Optional<Materia> findByNombre(String nombre);
    
    List<Materia> findByNombreContainingIgnoreCase(String nombre);
    
    List<Materia> findByDocenteContainingIgnoreCase(String docente);
}