package com.example.rama.service;

import com.example.rama.model.ActivityEvidence;
import com.example.rama.model.ClassActivities;
import com.example.rama.repository.ActivityEvidenceRepository;
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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class EvidenceService {

    @Autowired
    private ActivityEvidenceRepository evidenceRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // Extensiones permitidas
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
        ".pdf", ".jpg", ".jpeg", ".png", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx"
    );

    // Tamaño máximo de archivo (10MB)
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    public List<ActivityEvidence> findByActivity(ClassActivities activity) {
        return evidenceRepository.findByActivityIdOrderByUploadedAtDesc(activity.getId());
    }

    public List<ActivityEvidence> findByActivityId(Long activityId) {
        return evidenceRepository.findByActivityIdOrderByUploadedAtDesc(activityId);
    }

    public ActivityEvidence save(ActivityEvidence evidence) {
        return evidenceRepository.save(evidence);
    }

    public void delete(Long evidenceId) {
        ActivityEvidence evidence = evidenceRepository.findById(evidenceId)
            .orElseThrow(() -> new RuntimeException("Evidencia no encontrada"));
        
        // Eliminar archivo físico
        try {
            Path filePath = Paths.get(evidence.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Error al eliminar archivo físico: " + e.getMessage());
        }
        
        // Eliminar registro de base de datos
        evidenceRepository.deleteById(evidenceId);
    }

    public ActivityEvidence uploadFile(MultipartFile file, ClassActivities activity, 
                                     String description, String uploadedBy) throws IOException {
        
        // Validaciones
        validateFile(file);
        
        // Crear directorio si no existe
        Path uploadPath = createUploadDirectory();
        
        // Generar nombre único para el archivo
        String uniqueFilename = generateUniqueFilename(file.getOriginalFilename());
        
        // Ruta completa del archivo
        Path filePath = uploadPath.resolve(uniqueFilename);
        
        // Copiar archivo al directorio de uploads
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Crear entidad ActivityEvidence
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
        
        return evidenceRepository.save(evidence);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("El archivo está vacío");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("El archivo excede el tamaño máximo permitido (10MB)");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new RuntimeException("Nombre de archivo inválido");
        }
        
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new RuntimeException("Tipo de archivo no permitido. Extensiones válidas: " + 
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
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        return timestamp + "_" + uuid + extension;
    }

    public byte[] getFileContent(Long evidenceId) throws IOException {
        ActivityEvidence evidence = evidenceRepository.findById(evidenceId)
            .orElseThrow(() -> new RuntimeException("Evidencia no encontrada"));
        
        Path filePath = Paths.get(evidence.getFilePath());
        if (!Files.exists(filePath)) {
            throw new RuntimeException("Archivo físico no encontrado");
        }
        
        return Files.readAllBytes(filePath);
    }

    public ActivityEvidence findById(Long id) {
        return evidenceRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Evidencia no encontrada"));
    }

    public void deleteByActivityId(Long activityId) {
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
    }

    public List<String> getAllowedExtensions() {
        return ALLOWED_EXTENSIONS;
    }

    public long getMaxFileSize() {
        return MAX_FILE_SIZE;
    }

    public String getFormattedMaxFileSize() {
        return String.format("%.1f MB", MAX_FILE_SIZE / (1024.0 * 1024.0));
    }
}