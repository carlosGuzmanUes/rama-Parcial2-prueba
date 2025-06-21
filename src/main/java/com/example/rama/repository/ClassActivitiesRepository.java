package com.example.rama.repository;

import com.example.rama.model.ClassActivities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClassActivitiesRepository extends JpaRepository<ClassActivities, Long> {
    
    // ✅ BÚSQUEDAS BÁSICAS POR CAMPOS INDIVIDUALES
    
    /**
     * Encuentra actividades por materia ordenadas por fecha descendente
     */
    List<ClassActivities> findByMateriaOrderByDateDesc(String materia);
    
    /**
     * Encuentra actividades por grupo ordenadas por fecha descendente
     */
    List<ClassActivities> findByGrupoOrderByDateDesc(String grupo);
    
    /**
     * Encuentra actividades por docente ordenadas por fecha descendente
     */
    List<ClassActivities> findByDocenteOrderByDateDesc(String docente);
    
    /**
     * Encuentra actividades por tipo ordenadas por fecha descendente
     */
    List<ClassActivities> findByTipoActividadOrderByDateDesc(ClassActivities.TipoActividad tipoActividad);
    
    // ✅ BÚSQUEDAS COMBINADAS
    
    /**
     * Encuentra actividades por materia y grupo
     */
    List<ClassActivities> findByMateriaAndGrupoOrderByDateDesc(String materia, String grupo);
    
    /**
     * Encuentra actividades por materia y docente
     */
    List<ClassActivities> findByMateriaAndDocenteOrderByDateDesc(String materia, String docente);
    
    /**
     * Encuentra actividades por grupo y docente
     */
    List<ClassActivities> findByGrupoAndDocenteOrderByDateDesc(String grupo, String docente);
    
    /**
     * Encuentra actividades por materia y tipo
     */
    List<ClassActivities> findByMateriaAndTipoActividadOrderByDateDesc(String materia, ClassActivities.TipoActividad tipoActividad);
    
    // ✅ BÚSQUEDAS POR RANGO DE FECHAS
    
    /**
     * Encuentra actividades entre dos fechas
     */
    List<ClassActivities> findByDateBetweenOrderByDateDesc(LocalDate startDate, LocalDate endDate);
    
    /**
     * Encuentra actividades posteriores a una fecha
     */
    List<ClassActivities> findByDateGreaterThanEqualOrderByDateDesc(LocalDate date);
    
    /**
     * Encuentra actividades anteriores a una fecha
     */
    List<ClassActivities> findByDateLessThanEqualOrderByDateDesc(LocalDate date);
    
    /**
     * Encuentra actividades de una materia en un rango de fechas
     */
    List<ClassActivities> findByMateriaAndDateBetweenOrderByDateDesc(String materia, LocalDate startDate, LocalDate endDate);
    
    /**
     * Encuentra actividades de un grupo en un rango de fechas
     */
    List<ClassActivities> findByGrupoAndDateBetweenOrderByDateDesc(String grupo, LocalDate startDate, LocalDate endDate);
    
    // ✅ BÚSQUEDAS CON FILTROS AVANZADOS
    
    /**
     * Búsqueda avanzada con múltiples filtros opcionales
     */
    @Query("SELECT a FROM ClassActivities a WHERE " +
           "(:materia IS NULL OR :materia = '' OR a.materia = :materia) AND " +
           "(:grupo IS NULL OR :grupo = '' OR a.grupo = :grupo) AND " +
           "(:tipoActividad IS NULL OR a.tipoActividad = :tipoActividad) AND " +
           "(:startDate IS NULL OR a.date >= :startDate) AND " +
           "(:endDate IS NULL OR a.date <= :endDate) AND " +
           "(:docente IS NULL OR :docente = '' OR LOWER(a.docente) LIKE LOWER(CONCAT('%', :docente, '%'))) " +
           "ORDER BY a.date DESC")
    List<ClassActivities> findActivitiesWithFilters(
            @Param("materia") String materia,
            @Param("grupo") String grupo,
            @Param("tipoActividad") ClassActivities.TipoActividad tipoActividad,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("docente") String docente
    );
    
    /**
     * Búsqueda de texto en descripción y observaciones
     */
    @Query("SELECT a FROM ClassActivities a WHERE " +
           "LOWER(a.description) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(a.observaciones) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
           "ORDER BY a.date DESC")
    List<ClassActivities> findBySearchText(@Param("searchText") String searchText);
    
    /**
     * Búsqueda combinada con texto y filtros
     */
    @Query("SELECT a FROM ClassActivities a WHERE " +
           "(:searchText IS NULL OR :searchText = '' OR " +
           " LOWER(a.description) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           " LOWER(a.observaciones) LIKE LOWER(CONCAT('%', :searchText, '%'))) AND " +
           "(:materia IS NULL OR :materia = '' OR a.materia = :materia) AND " +
           "(:grupo IS NULL OR :grupo = '' OR a.grupo = :grupo) AND " +
           "(:tipoActividad IS NULL OR a.tipoActividad = :tipoActividad) " +
           "ORDER BY a.date DESC")
    List<ClassActivities> findBySearchTextAndFilters(
            @Param("searchText") String searchText,
            @Param("materia") String materia,
            @Param("grupo") String grupo,
            @Param("tipoActividad") ClassActivities.TipoActividad tipoActividad
    );
    
    // ✅ MÉTODOS PARA OBTENER VALORES ÚNICOS (NECESARIOS PARA FILTROS EN UI)
    
    /**
     * Obtiene todas las materias únicas ordenadas alfabéticamente
     */
    @Query("SELECT DISTINCT a.materia FROM ClassActivities a WHERE a.materia IS NOT NULL AND a.materia != '' ORDER BY a.materia")
    List<String> findDistinctMaterias();
    
    /**
     * Obtiene todos los grupos únicos ordenados alfabéticamente
     */
    @Query("SELECT DISTINCT a.grupo FROM ClassActivities a WHERE a.grupo IS NOT NULL AND a.grupo != '' ORDER BY a.grupo")
    List<String> findDistinctGrupos();
    
    /**
     * Obtiene todos los docentes únicos ordenados alfabéticamente
     */
    @Query("SELECT DISTINCT a.docente FROM ClassActivities a WHERE a.docente IS NOT NULL AND a.docente != '' ORDER BY a.docente")
    List<String> findDistinctDocentes();
    
    /**
     * Obtiene todos los tipos de actividad únicos en uso
     */
    @Query("SELECT DISTINCT a.tipoActividad FROM ClassActivities a WHERE a.tipoActividad IS NOT NULL ORDER BY a.tipoActividad")
    List<ClassActivities.TipoActividad> findDistinctTipoActividades();
    
    /**
     * Obtiene materias por docente
     */
    @Query("SELECT DISTINCT a.materia FROM ClassActivities a WHERE a.docente = :docente AND a.materia IS NOT NULL ORDER BY a.materia")
    List<String> findDistinctMateriasByDocente(@Param("docente") String docente);
    
    /**
     * Obtiene grupos por materia
     */
    @Query("SELECT DISTINCT a.grupo FROM ClassActivities a WHERE a.materia = :materia AND a.grupo IS NOT NULL ORDER BY a.grupo")
    List<String> findDistinctGruposByMateria(@Param("materia") String materia);
    
    // ✅ ESTADÍSTICAS Y CONTEOS
    
    /**
     * Cuenta actividades por materia
     */
    @Query("SELECT a.materia, COUNT(a) FROM ClassActivities a WHERE a.materia IS NOT NULL GROUP BY a.materia ORDER BY COUNT(a) DESC")
    List<Object[]> getActivitiesCountByMateria();
    
    /**
     * Cuenta actividades por grupo
     */
    @Query("SELECT a.grupo, COUNT(a) FROM ClassActivities a WHERE a.grupo IS NOT NULL GROUP BY a.grupo ORDER BY COUNT(a) DESC")
    List<Object[]> getActivitiesCountByGrupo();
    
    /**
     * Cuenta actividades por tipo
     */
    @Query("SELECT a.tipoActividad, COUNT(a) FROM ClassActivities a WHERE a.tipoActividad IS NOT NULL GROUP BY a.tipoActividad ORDER BY COUNT(a) DESC")
    List<Object[]> getActivitiesCountByTipo();
    
    /**
     * Cuenta actividades por docente
     */
    @Query("SELECT a.docente, COUNT(a) FROM ClassActivities a WHERE a.docente IS NOT NULL GROUP BY a.docente ORDER BY COUNT(a) DESC")
    List<Object[]> getActivitiesCountByDocente();
    
    /**
     * Obtiene estadísticas por mes
     */
    @Query("SELECT YEAR(a.date), MONTH(a.date), COUNT(a) FROM ClassActivities a WHERE a.date IS NOT NULL GROUP BY YEAR(a.date), MONTH(a.date) ORDER BY YEAR(a.date) DESC, MONTH(a.date) DESC")
    List<Object[]> getActivitiesCountByMonth();
    
    /**
     * Cuenta total de actividades
     */
    @Query("SELECT COUNT(a) FROM ClassActivities a")
    Long getTotalActivitiesCount();
    
    /**
     * Cuenta actividades por materia específica
     */
    Long countByMateria(String materia);
    
    /**
     * Cuenta actividades por grupo específico
     */
    Long countByGrupo(String grupo);
    
    /**
     * Cuenta actividades por docente específico
     */
    Long countByDocente(String docente);
    
    /**
     * Cuenta actividades por tipo específico
     */
    Long countByTipoActividad(ClassActivities.TipoActividad tipoActividad);
    
    // ✅ BÚSQUEDAS RECIENTES Y POPULARES
    
    /**
     * Encuentra las actividades más recientes
     */
    @Query("SELECT a FROM ClassActivities a ORDER BY a.date DESC, a.id DESC")
    List<ClassActivities> findMostRecent(org.springframework.data.domain.Pageable pageable);
    
    /**
     * Encuentra actividades de la última semana
     */
    @Query("SELECT a FROM ClassActivities a WHERE a.date >= :oneWeekAgo ORDER BY a.date DESC")
    List<ClassActivities> findFromLastWeek(@Param("oneWeekAgo") LocalDate oneWeekAgo);
    
    /**
     * Encuentra actividades del último mes
     */
    @Query("SELECT a FROM ClassActivities a WHERE a.date >= :oneMonthAgo ORDER BY a.date DESC")
    List<ClassActivities> findFromLastMonth(@Param("oneMonthAgo") LocalDate oneMonthAgo);
    
    /**
     * Encuentra actividades programadas (futuras)
     */
    @Query("SELECT a FROM ClassActivities a WHERE a.date > :today ORDER BY a.date ASC")
    List<ClassActivities> findScheduledActivities(@Param("today") LocalDate today);
    
    // ✅ VALIDACIONES Y VERIFICACIONES
    
    /**
     * Verifica si existe una actividad con la misma descripción en la misma fecha
     */
    boolean existsByDescriptionAndDate(String description, LocalDate date);
    
    /**
     * Verifica si existe una actividad similar (misma materia, grupo y fecha)
     */
    boolean existsByMateriaAndGrupoAndDate(String materia, String grupo, LocalDate date);
    
    /**
     * Encuentra actividades duplicadas por descripción
     */
    @Query("SELECT a FROM ClassActivities a WHERE LOWER(a.description) = LOWER(:description) AND a.id != :excludeId")
    List<ClassActivities> findDuplicatesByDescription(@Param("description") String description, @Param("excludeId") Long excludeId);
    
    // ✅ MÉTODOS DE UTILIDAD PARA REPORTES
    
    /**
     * Obtiene actividades para reporte por materia
     */
    @Query("SELECT a FROM ClassActivities a WHERE " +
           "(:materia IS NULL OR a.materia = :materia) AND " +
           "(:startDate IS NULL OR a.date >= :startDate) AND " +
           "(:endDate IS NULL OR a.date <= :endDate) " +
           "ORDER BY a.materia, a.date DESC")
    List<ClassActivities> findForMateriaReport(@Param("materia") String materia,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);
    
    /**
     * Obtiene actividades para reporte por grupo
     */
    @Query("SELECT a FROM ClassActivities a WHERE " +
           "(:grupo IS NULL OR a.grupo = :grupo) AND " +
           "(:startDate IS NULL OR a.date >= :startDate) AND " +
           "(:endDate IS NULL OR a.date <= :endDate) " +
           "ORDER BY a.grupo, a.date DESC")
    List<ClassActivities> findForGrupoReport(@Param("grupo") String grupo,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);
    
    /**
     * Obtiene actividades para reporte por docente
     */
    @Query("SELECT a FROM ClassActivities a WHERE " +
           "(:docente IS NULL OR LOWER(a.docente) LIKE LOWER(CONCAT('%', :docente, '%'))) AND " +
           "(:startDate IS NULL OR a.date >= :startDate) AND " +
           "(:endDate IS NULL OR a.date <= :endDate) " +
           "ORDER BY a.docente, a.date DESC")
    List<ClassActivities> findForDocenteReport(@Param("docente") String docente,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);
    
    // ✅ BÚSQUEDAS ESPECÍFICAS ADICIONALES
    
    /**
     * Encuentra actividades sin observaciones
     */
    @Query("SELECT a FROM ClassActivities a WHERE a.observaciones IS NULL OR a.observaciones = '' ORDER BY a.date DESC")
    List<ClassActivities> findWithoutObservaciones();
    
    /**
     * Encuentra actividades con observaciones
     */
    @Query("SELECT a FROM ClassActivities a WHERE a.observaciones IS NOT NULL AND a.observaciones != '' ORDER BY a.date DESC")
    List<ClassActivities> findWithObservaciones();
    
    /**
     * Encuentra actividades por descripción parcial
     */
    @Query("SELECT a FROM ClassActivities a WHERE LOWER(a.description) LIKE LOWER(CONCAT('%', :description, '%')) ORDER BY a.date DESC")
    List<ClassActivities> findByDescriptionContaining(@Param("description") String description);
}