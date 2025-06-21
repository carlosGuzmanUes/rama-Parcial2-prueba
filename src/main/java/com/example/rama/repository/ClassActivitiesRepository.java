package com.example.rama.repository;

import com.example.rama.model.ClassActivities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ClassActivitiesRepository extends JpaRepository<ClassActivities, Long> {
    
    // Búsquedas por materia
    List<ClassActivities> findByMateriaOrderByDateDesc(String materia);
    
    // Búsquedas por grupo
    List<ClassActivities> findByGrupoOrderByDateDesc(String grupo);
    
    // Búsquedas por materia y grupo
    List<ClassActivities> findByMateriaAndGrupoOrderByDateDesc(String materia, String grupo);
    
    // Búsquedas por docente
    List<ClassActivities> findByDocenteOrderByDateDesc(String docente);
    
    // Búsquedas por tipo de actividad
    List<ClassActivities> findByTipoActividadOrderByDateDesc(ClassActivities.TipoActividad tipoActividad);
    
    // Búsquedas por rango de fechas
    List<ClassActivities> findByDateBetweenOrderByDateDesc(LocalDate startDate, LocalDate endDate);
    
    // ✅ MÉTODOS QUE FALTAN - Búsquedas combinadas con filtros
    @Query("SELECT a FROM ClassActivities a WHERE " +
           "(:materia IS NULL OR a.materia = :materia) AND " +
           "(:grupo IS NULL OR a.grupo = :grupo) AND " +
           "(:tipoActividad IS NULL OR a.tipoActividad = :tipoActividad) AND " +
           "(:startDate IS NULL OR a.date >= :startDate) AND " +
           "(:endDate IS NULL OR a.date <= :endDate) AND " +
           "(:docente IS NULL OR a.docente LIKE %:docente%) " +
           "ORDER BY a.date DESC")
    List<ClassActivities> findActivitiesWithFilters(
            @Param("materia") String materia,
            @Param("grupo") String grupo,
            @Param("tipoActividad") ClassActivities.TipoActividad tipoActividad,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("docente") String docente
    );
    
    // ✅ OBTENER VALORES ÚNICOS PARA FILTROS
    @Query("SELECT DISTINCT a.materia FROM ClassActivities a WHERE a.materia IS NOT NULL ORDER BY a.materia")
    List<String> findDistinctMaterias();
    
    @Query("SELECT DISTINCT a.grupo FROM ClassActivities a WHERE a.grupo IS NOT NULL ORDER BY a.grupo")
    List<String> findDistinctGrupos();
    
    @Query("SELECT DISTINCT a.docente FROM ClassActivities a WHERE a.docente IS NOT NULL ORDER BY a.docente")
    List<String> findDistinctDocentes();
    
    // Estadísticas
    @Query("SELECT a.materia, COUNT(a) FROM ClassActivities a GROUP BY a.materia ORDER BY COUNT(a) DESC")
    List<Object[]> getActivitiesCountByMateria();
    
    @Query("SELECT a.tipoActividad, COUNT(a) FROM ClassActivities a GROUP BY a.tipoActividad ORDER BY COUNT(a) DESC")
    List<Object[]> getActivitiesCountByTipo();
}