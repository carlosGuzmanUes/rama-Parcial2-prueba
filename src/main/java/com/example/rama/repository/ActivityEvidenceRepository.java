package com.example.rama.repository;

import com.example.rama.model.ActivityEvidence;
import com.example.rama.model.ClassActivities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityEvidenceRepository extends JpaRepository<ActivityEvidence, Long> {
    
    // ✅ BÚSQUEDAS BÁSICAS POR ACTIVIDAD
    
    /**
     * Encuentra todas las evidencias de una actividad específica
     */
    List<ActivityEvidence> findByActivity(ClassActivities activity);
    
    /**
     * Encuentra todas las evidencias por ID de actividad
     */
    List<ActivityEvidence> findByActivityId(Long activityId);
    
    /**
     * Encuentra evidencias por ID de actividad ordenadas por fecha de subida (más recientes primero)
     */
    @Query("SELECT e FROM ActivityEvidence e WHERE e.activity.id = :activityId ORDER BY e.uploadedAt DESC")
    List<ActivityEvidence> findByActivityIdOrderByUploadedAtDesc(@Param("activityId") Long activityId);
    
    /**
     * Encuentra evidencias por ID de actividad ordenadas por fecha de subida (más antiguas primero)
     */
    @Query("SELECT e FROM ActivityEvidence e WHERE e.activity.id = :activityId ORDER BY e.uploadedAt ASC")
    List<ActivityEvidence> findByActivityIdOrderByUploadedAtAsc(@Param("activityId") Long activityId);
    
    // ✅ BÚSQUEDAS POR USUARIO
    
    /**
     * Encuentra todas las evidencias subidas por un usuario específico
     */
    List<ActivityEvidence> findByUploadedBy(String uploadedBy);
    
    /**
     * Encuentra evidencias por usuario ordenadas por fecha
     */
    @Query("SELECT e FROM ActivityEvidence e WHERE e.uploadedBy = :uploadedBy ORDER BY e.uploadedAt DESC")
    List<ActivityEvidence> findByUploadedByOrderByUploadedAtDesc(@Param("uploadedBy") String uploadedBy);
    
    // ✅ BÚSQUEDAS POR TIPO DE ARCHIVO
    
    /**
     * Encuentra evidencias por extensión de archivo
     */
    List<ActivityEvidence> findByFileExtension(String fileExtension);
    
    /**
     * Encuentra evidencias por tipo de contenido
     */
    List<ActivityEvidence> findByContentType(String contentType);
    
    /**
     * Encuentra evidencias de imágenes
     */
    @Query("SELECT e FROM ActivityEvidence e WHERE e.fileExtension IN ('.jpg', '.jpeg', '.png', '.gif', '.bmp', '.svg', '.webp')")
    List<ActivityEvidence> findImageFiles();
    
    /**
     * Encuentra evidencias de documentos
     */
    @Query("SELECT e FROM ActivityEvidence e WHERE e.fileExtension IN ('.pdf', '.doc', '.docx', '.xls', '.xlsx', '.ppt', '.pptx')")
    List<ActivityEvidence> findDocumentFiles();
    
    // ✅ BÚSQUEDAS POR RANGO DE FECHAS
    
    /**
     * Encuentra evidencias subidas entre dos fechas
     */
    @Query("SELECT e FROM ActivityEvidence e WHERE e.uploadedAt BETWEEN :startDate AND :endDate ORDER BY e.uploadedAt DESC")
    List<ActivityEvidence> findByUploadedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                                  @Param("endDate") LocalDateTime endDate);
    
    /**
     * Encuentra evidencias de una actividad en un rango de fechas
     */
    @Query("SELECT e FROM ActivityEvidence e WHERE e.activity.id = :activityId AND e.uploadedAt BETWEEN :startDate AND :endDate ORDER BY e.uploadedAt DESC")
    List<ActivityEvidence> findByActivityIdAndUploadedAtBetween(@Param("activityId") Long activityId,
                                                               @Param("startDate") LocalDateTime startDate,
                                                               @Param("endDate") LocalDateTime endDate);
    
    // ✅ BÚSQUEDAS POR TAMAÑO DE ARCHIVO
    
    /**
     * Encuentra archivos mayores a un tamaño específico
     */
    @Query("SELECT e FROM ActivityEvidence e WHERE e.fileSize > :minSize ORDER BY e.fileSize DESC")
    List<ActivityEvidence> findByFileSizeGreaterThan(@Param("minSize") Long minSize);
    
    /**
     * Encuentra archivos menores a un tamaño específico
     */
    @Query("SELECT e FROM ActivityEvidence e WHERE e.fileSize < :maxSize ORDER BY e.fileSize ASC")
    List<ActivityEvidence> findByFileSizeLessThan(@Param("maxSize") Long maxSize);
    
    /**
     * Encuentra archivos en un rango de tamaño
     */
    @Query("SELECT e FROM ActivityEvidence e WHERE e.fileSize BETWEEN :minSize AND :maxSize ORDER BY e.fileSize DESC")
    List<ActivityEvidence> findByFileSizeBetween(@Param("minSize") Long minSize, @Param("maxSize") Long maxSize);
    
    // ✅ BÚSQUEDAS CON FILTROS COMBINADOS
    
    /**
     * Búsqueda avanzada con múltiples filtros
     */
    @Query("SELECT e FROM ActivityEvidence e WHERE " +
           "(:activityId IS NULL OR e.activity.id = :activityId) AND " +
           "(:uploadedBy IS NULL OR e.uploadedBy LIKE %:uploadedBy%) AND " +
           "(:fileExtension IS NULL OR e.fileExtension = :fileExtension) AND " +
           "(:startDate IS NULL OR e.uploadedAt >= :startDate) AND " +
           "(:endDate IS NULL OR e.uploadedAt <= :endDate) AND " +
           "(:minSize IS NULL OR e.fileSize >= :minSize) AND " +
           "(:maxSize IS NULL OR e.fileSize <= :maxSize) " +
           "ORDER BY e.uploadedAt DESC")
    List<ActivityEvidence> findWithFilters(@Param("activityId") Long activityId,
                                          @Param("uploadedBy") String uploadedBy,
                                          @Param("fileExtension") String fileExtension,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate,
                                          @Param("minSize") Long minSize,
                                          @Param("maxSize") Long maxSize);
    
    // ✅ BÚSQUEDAS DE TEXTO
    
    /**
     * Busca en el nombre original del archivo
     */
    @Query("SELECT e FROM ActivityEvidence e WHERE e.originalFilename LIKE %:filename% ORDER BY e.uploadedAt DESC")
    List<ActivityEvidence> findByOriginalFilenameContaining(@Param("filename") String filename);
    
    /**
     * Busca en la descripción
     */
    @Query("SELECT e FROM ActivityEvidence e WHERE e.description LIKE %:description% ORDER BY e.uploadedAt DESC")
    List<ActivityEvidence> findByDescriptionContaining(@Param("description") String description);
    
    /**
     * Búsqueda de texto general (nombre o descripción)
     */
    @Query("SELECT e FROM ActivityEvidence e WHERE " +
           "e.originalFilename LIKE %:searchText% OR e.description LIKE %:searchText% " +
           "ORDER BY e.uploadedAt DESC")
    List<ActivityEvidence> findBySearchText(@Param("searchText") String searchText);
    
    // ✅ OPERACIONES DE ELIMINACIÓN
    
    /**
     * Elimina todas las evidencias de una actividad específica
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM ActivityEvidence e WHERE e.activity.id = :activityId")
    void deleteByActivityId(@Param("activityId") Long activityId);
    
    /**
     * Elimina evidencias por usuario (útil para limpieza de datos)
     */
    @Modifying
    @Transactional
    void deleteByUploadedBy(String uploadedBy);
    
    /**
     * Elimina evidencias anteriores a una fecha específica
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM ActivityEvidence e WHERE e.uploadedAt < :cutoffDate")
    void deleteOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    // ✅ ESTADÍSTICAS Y CONTEOS
    
    /**
     * Cuenta el número de evidencias por actividad
     */
    @Query("SELECT COUNT(e) FROM ActivityEvidence e WHERE e.activity.id = :activityId")
    Long countByActivityId(@Param("activityId") Long activityId);
    
    /**
     * Cuenta evidencias por usuario
     */
    Long countByUploadedBy(String uploadedBy);
    
    /**
     * Cuenta evidencias por tipo de archivo
     */
    Long countByFileExtension(String fileExtension);
    
    /**
     * Calcula el tamaño total de archivos por actividad
     */
    @Query("SELECT COALESCE(SUM(e.fileSize), 0) FROM ActivityEvidence e WHERE e.activity.id = :activityId")
    Long getTotalFileSizeByActivityId(@Param("activityId") Long activityId);
    
    /**
     * Obtiene estadísticas de archivos por extensión
     */
    @Query("SELECT e.fileExtension, COUNT(e), COALESCE(SUM(e.fileSize), 0) FROM ActivityEvidence e " +
           "GROUP BY e.fileExtension ORDER BY COUNT(e) DESC")
    List<Object[]> getFileStatisticsByExtension();
    
    /**
     * Obtiene estadísticas de uploads por usuario
     */
    @Query("SELECT e.uploadedBy, COUNT(e), COALESCE(SUM(e.fileSize), 0) FROM ActivityEvidence e " +
           "GROUP BY e.uploadedBy ORDER BY COUNT(e) DESC")
    List<Object[]> getUploadStatisticsByUser();
    
    // ✅ VERIFICACIONES DE EXISTENCIA
    
    /**
     * Verifica si existe un archivo con el mismo nombre en una actividad
     */
    boolean existsByActivityIdAndOriginalFilename(Long activityId, String originalFilename);
    
    /**
     * Verifica si existe un archivo en una ruta específica
     */
    boolean existsByFilePath(String filePath);
    
    /**
     * Encuentra archivo por nombre original en una actividad específica
     */
    Optional<ActivityEvidence> findByActivityIdAndOriginalFilename(Long activityId, String originalFilename);
    
    // ✅ MÉTODOS DE UTILIDAD
    
    /**
     * Encuentra las evidencias más recientes (últimas N)
     */
    @Query("SELECT e FROM ActivityEvidence e ORDER BY e.uploadedAt DESC")
    List<ActivityEvidence> findMostRecent(org.springframework.data.domain.Pageable pageable);
    
    /**
     * Encuentra los archivos más grandes
     */
    @Query("SELECT e FROM ActivityEvidence e ORDER BY e.fileSize DESC")
    List<ActivityEvidence> findLargestFiles(org.springframework.data.domain.Pageable pageable);
    
    /**
     * Obtiene evidencias por actividades de una materia específica
     */
    @Query("SELECT e FROM ActivityEvidence e WHERE e.activity.materia = :materia ORDER BY e.uploadedAt DESC")
    List<ActivityEvidence> findByActivityMateria(@Param("materia") String materia);
    
    /**
     * Obtiene evidencias por actividades de un grupo específico
     */
    @Query("SELECT e FROM ActivityEvidence e WHERE e.activity.grupo = :grupo ORDER BY e.uploadedAt DESC")
    List<ActivityEvidence> findByActivityGrupo(@Param("grupo") String grupo);
}