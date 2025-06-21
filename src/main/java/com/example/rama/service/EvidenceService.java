package com.example.rama.service;

import com.example.rama.model.ActivityEvidence;
import com.example.rama.model.ClassActivities;
import com.example.rama.repository.ActivityEvidenceRepository;
import com.example.rama.util.MultipartFileWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class EvidenceService {

    @Autowired
    private ActivityEvidenceRepository evidenceRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
        ".pdf", ".jpg", ".jpeg", ".png", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public List<ActivityEvidence> findByActivity(ClassActivities activity) {
        try {
            return evidenceRepository.findByActivityIdOrderByUploadedAtDesc(activity.getId());
        } catch (Exception e) {
            System.err.println("Error buscando evidencias: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<ActivityEvidence> findByActivityId(Long activityId) {
        try {
            return evidenceRepository.findByActivityIdOrderByUploadedAtDesc(activityId);
        } catch (Exception e) {
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
            ActivityEvidence evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new RuntimeException("Evidencia no encontrada"));
            
            try {
                Path filePath = Paths.get(evidence.getFilePath());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                System.err.println("Error al eliminar archivo fisico: " + e.getMessage());
            }
            
            evidenceRepository.deleteById(evidenceId);
        } catch (Exception e) {
            System.err.println("Error eliminando evidencia: " + e.getMessage());
            throw new RuntimeException("Error al eliminar evidencia", e);
        }
    }

    public void deleteByActivityId(Long activityId) {
        try {
            List<ActivityEvidence> evidences = evidenceRepository.findByActivityId(activityId);
            
            for (ActivityEvidence evidence : evidences) {
                try {
                    Path filePath = Paths.get(evidence.getFilePath());
                    Files.deleteIfExists(filePath);
                } catch (IOException e) {
                    System.err.println("Error al eliminar archivo: " + evidence.getFilePath());
                }
            }
            
            evidenceRepository.deleteByActivityId(activityId);
        } catch (Exception e) {
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

    public ActivityEvidence uploadFile(MultipartFile file, 
                                     ClassActivities activity, 
                                     String description, 
                                     String uploadedBy) throws IOException {
        
        validateFile(file);
        
        // Verificar duplicados
        if (evidenceRepository.existsByActivityIdAndOriginalFilename(
                activity.getId(), file.getOriginalFilename())) {
            throw new RuntimeException("Ya existe un archivo con ese nombre en esta actividad");
        }
        
        Path uploadPath = createUploadDirectory();
        String uniqueFilename = generateUniqueFilename(file.getOriginalFilename());
        Path filePath = uploadPath.resolve(uniqueFilename);
        
        try {
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IOException("Error al guardar el archivo: " + e.getMessage(), e);
        }
        
        ActivityEvidence evidence = new ActivityEvidence(
            uniqueFilename,
            file.getOriginalFilename(),
            filePath.toString(),
            file.getContentType(),
            file.getSize(),
            activity
        );
        
        evidence.setDescription(description);
        evidence.setUploadedBy(uploadedBy);
        
        return save(evidence);
    }

    public ActivityEvidence uploadFile(MultipartFileWrapper fileWrapper, 
                                     ClassActivities activity, 
                                     String description, 
                                     String uploadedBy) throws IOException {
        
        return uploadFile((MultipartFile) fileWrapper, activity, description, uploadedBy);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("El archivo esta vacio");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("El archivo excede el tamaño maximo permitido (" + getFormattedMaxFileSize() + ")");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new RuntimeException("Nombre de archivo invalido");
        }
        
        String extension = "";
        if (originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }
        
        if (extension.isEmpty()) {
            throw new RuntimeException("El archivo debe tener una extension valida");
        }
        
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new RuntimeException("Tipo de archivo no permitido. Extensiones validas: " + 
                                     String.join(", ", ALLOWED_EXTENSIONS));
        }
    }

    private Path createUploadDirectory() throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        return uploadPath;
    }

    private String generateUniqueFilename(String originalFilename) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        
        String extension = "";
        String baseName = originalFilename;
        
        if (originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            baseName = originalFilename.substring(0, originalFilename.lastIndexOf("."));
        }
        
        baseName = baseName.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (baseName.length() > 20) {
            baseName = baseName.substring(0, 20);
        }
        
        return baseName + "_" + timestamp + "_" + uuid + extension;
    }

    public byte[] getFileContent(Long evidenceId) throws IOException {
        ActivityEvidence evidence = findById(evidenceId);
        
        Path filePath = Paths.get(evidence.getFilePath());
        if (!Files.exists(filePath)) {
            throw new IOException("Archivo fisico no encontrado: " + evidence.getFilePath());
        }
        
        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new IOException("Error al leer el archivo: " + e.getMessage(), e);
        }
    }

    public List<String> getAllowedExtensions() {
        return new ArrayList<>(ALLOWED_EXTENSIONS);
    }

    public long getMaxFileSize() {
        return MAX_FILE_SIZE;
    }

    public String getFormattedMaxFileSize() {
        return String.format("%.1f MB", MAX_FILE_SIZE / (1024.0 * 1024.0));
    }

    public long getEvidenceCountByActivity(Long activityId) {
        try {
            return evidenceRepository.countByActivityId(activityId);
        } catch (Exception e) {
            return 0L;
        }
    }

    public List<ActivityEvidence> findByUploadedBy(String uploadedBy) {
        try {
            return evidenceRepository.findByUploadedByOrderByUploadedAtDesc(uploadedBy);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}