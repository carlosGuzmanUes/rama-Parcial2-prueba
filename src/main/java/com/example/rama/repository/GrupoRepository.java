package com.example.rama.repository;

import com.example.rama.model.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GrupoRepository extends JpaRepository<Grupo, Long> {
    
    List<Grupo> findByMateria(String materia);
    
    Optional<Grupo> findByGrupo(String grupoName);
    
    List<Grupo> findByGrupoContainingIgnoreCase(String grupoName);
    
    List<Grupo> findByMateriaContainingIgnoreCase(String materia);
}