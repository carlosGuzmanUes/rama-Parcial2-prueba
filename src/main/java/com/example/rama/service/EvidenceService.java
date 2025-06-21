package com.example.rama.service;

import com.example.rama.model.ActivityEvidence;
import com.example.rama.model.ClassActivities;
import com.example.rama.repository.ActivityEvidenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EvidenceService {

    @Autowired
    private ActivityEvidenceRepository evidenceRepository;

    public List<ActivityEvidence> findByActivity(ClassActivities activity) {
        try {
            return evidenceRepository.findByActivityIdOrderByUploadedAtDesc(activity.getId());
        } catch (Exception e) {
            // Si hay error, retornar lista vacía por ahora
            System.err.println("Error buscando evidencias: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<ActivityEvidence> findByActivityId(Long activityId) {
        try {
            return evidenceRepository.findByActivityIdOrderByUploadedAtDesc(activityId);
        } catch (Exception e) {
            // Si hay error, retornar lista vacía por ahora
            System.err.println("Error buscando evidencias por ID: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public ActivityEvidence save(ActivityEvidence evidence) {
        try {
            return evidenceRepository.save(evidence);
        } catch (Exception e) {
            System.err.println("Error guardando evidencia: " + e.getMessage());
            throw new RuntimeException("Error al guardar evidencia", e);
        }
    }

    public void delete(Long evidenceId) {
        try {
            evidenceRepository.deleteById(evidenceId);
        } catch (Exception e) {
            System.err.println("Error eliminando evidencia: " + e.getMessage());
            throw new RuntimeException("Error al eliminar evidencia", e);
        }
    }

    public void deleteByActivityId(Long activityId) {
        try {
            evidenceRepository.deleteByActivityId(activityId);
        } catch (Exception e) {
            // Si hay error, solo log por ahora
            System.err.println("Error eliminando evidencias de actividad " + activityId + ": " + e.getMessage());
        }
    }

    public ActivityEvidence findById(Long id) {
        try {
            return evidenceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evidencia no encontrada"));
        } catch (Exception e) {
            System.err.println("Error buscando evidencia por ID: " + e.getMessage());
            throw new RuntimeException("Evidencia no encontrada", e);
        }
    }

    // Métodos auxiliares para upload (implementación básica)
    public byte[] getFileContent(Long evidenceId) throws Exception {
        // Implementación básica - por ahora retorna array vacío
        System.out.println("getFileContent llamado para evidencia: " + evidenceId);
        return new byte[0];
    }

    public List<String> getAllowedExtensions() {
        return List.of(".pdf", ".jpg", ".jpeg", ".png", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx");
    }

    public long getMaxFileSize() {
        return 10 * 1024 * 1024; // 10MB
    }

    public String getFormattedMaxFileSize() {
        return "10 MB";
    }

    // ✅ MÉTODO FALTANTE - uploadFile
    public ActivityEvidence uploadFile(org.springframework.web.multipart.MultipartFile file, 
                                     ClassActivities activity, 
                                     String description, 
                                     String uploadedBy) throws java.io.IOException {
        
        // Validaciones básicas
        if (file.isEmpty()) {
            throw new RuntimeException("El archivo está vacío");
        }
        
        if (file.getSize() > getMaxFileSize()) {
            throw new RuntimeException("El archivo excede el tamaño máximo permitido (10MB)");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new RuntimeException("Nombre de archivo inválido");
        }
        
        // Validar extensión
        String extension = "";
        if (originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }
        
        if (!getAllowedExtensions().contains(extension)) {
            throw new RuntimeException("Tipo de archivo no permitido. Extensiones válidas: " + 
                                     String.join(", ", getAllowedExtensions()));
        }
        
        // Por ahora, crear la evidencia sin guardar el archivo físico
        ActivityEvidence evidence = new ActivityEvidence();
        evidence.setOriginalFilename(originalFilename);
        evidence.setFilename(originalFilename); // Simplificado
        evidence.setFilePath("/temp/" + originalFilename); // Ruta temporal
        evidence.setContentType(file.getContentType());
        evidence.setFileSize(file.getSize());
        evidence.setActivity(activity);
        evidence.setDescription(description);
        evidence.setUploadedBy(uploadedBy);
        evidence.setFileExtension(extension);
        
        // Guardar en base de datos
        return save(evidence);
    }
}