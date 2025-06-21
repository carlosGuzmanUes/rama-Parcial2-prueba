package com.example.rama.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "activity_evidences")
public class ActivityEvidence {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "filename", nullable = false)
    private String filename;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "file_extension")
    private String fileExtension;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "uploaded_by")
    private String uploadedBy;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relación con la actividad
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private ClassActivities activity;

    // Constructores
    public ActivityEvidence() {
        this.uploadedAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public ActivityEvidence(String filename, String originalFilename, String filePath, 
                           String contentType, Long fileSize, ClassActivities activity) {
        this();
        this.filename = filename;
        this.originalFilename = originalFilename;
        this.filePath = filePath;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.activity = activity;
        this.fileExtension = extractFileExtension(originalFilename);
    }

    // Métodos de ciclo de vida JPA
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.uploadedAt == null) {
            this.uploadedAt = now;
        }
        this.updatedAt = now;
    }

    // Métodos de utilidad
    private String extractFileExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf(".")).toLowerCase();
        }
        return "";
    }

    public boolean isImageFile() {
        if (fileExtension == null) return false;
        return fileExtension.matches("\\.(jpg|jpeg|png|gif|bmp|svg|webp)$");
    }

    public boolean isPdfFile() {
        return ".pdf".equals(fileExtension);
    }

    public boolean isDocumentFile() {
        if (fileExtension == null) return false;
        return fileExtension.matches("\\.(doc|docx|xls|xlsx|ppt|pptx|odt|ods|odp)$");
    }

    public boolean isVideoFile() {
        if (fileExtension == null) return false;
        return fileExtension.matches("\\.(mp4|avi|mov|wmv|flv|webm|mkv)$");
    }

    public boolean isAudioFile() {
        if (fileExtension == null) return false;
        return fileExtension.matches("\\.(mp3|wav|flac|aac|ogg|wma)$");
    }

    public boolean isTextFile() {
        if (fileExtension == null) return false;
        return fileExtension.matches("\\.(txt|md|csv|log|xml|json|html|css|js)$");
    }

    public String getFileIcon() {
        if (isImageFile()) return "🖼️";
        if (isPdfFile()) return "📄";
        if (isDocumentFile()) return "📋";
        if (isVideoFile()) return "🎥";
        if (isAudioFile()) return "🎵";
        if (isTextFile()) return "📝";
        return "📎";
    }

    public String getFileCategory() {
        if (isImageFile()) return "Imagen";
        if (isPdfFile()) return "PDF";
        if (isDocumentFile()) return "Documento";
        if (isVideoFile()) return "Video";
        if (isAudioFile()) return "Audio";
        if (isTextFile()) return "Texto";
        return "Archivo";
    }

    public String getFormattedFileSize() {
        if (fileSize == null || fileSize <= 0) return "N/A";
        
        double bytes = fileSize.doubleValue();
        
        if (bytes < 1024) {
            return String.format("%.0f B", bytes);
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024 * 1024));
        } else {
            return String.format("%.1f GB", bytes / (1024 * 1024 * 1024));
        }
    }

    public boolean isValidFile() {
        return originalFilename != null && 
               !originalFilename.trim().isEmpty() && 
               fileSize != null && 
               fileSize > 0 && 
               filePath != null && 
               !filePath.trim().isEmpty();
    }

    public String getShortDescription() {
        if (description == null || description.trim().isEmpty()) {
            return "Sin descripción";
        }
        return description.length() > 50 ? 
            description.substring(0, 47) + "..." : 
            description;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
        if (originalFilename != null) {
            this.fileExtension = extractFileExtension(originalFilename);
        }
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public ClassActivities getActivity() {
        return activity;
    }

    public void setActivity(ClassActivities activity) {
        this.activity = activity;
    }

    // equals y hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivityEvidence that = (ActivityEvidence) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // toString
    @Override
    public String toString() {
        return "ActivityEvidence{" +
                "id=" + id +
                ", originalFilename='" + originalFilename + '\'' +
                ", fileSize=" + fileSize +
                ", contentType='" + contentType + '\'' +
                ", uploadedBy='" + uploadedBy + '\'' +
                ", uploadedAt=" + uploadedAt +
                ", activityId=" + (activity != null ? activity.getId() : null) +
                '}';
    }

    // Métodos de conveniencia para la UI
    public String getDisplayName() {
        return originalFilename != null ? originalFilename : "Archivo sin nombre";
    }

    public String getUploadInfo() {
        StringBuilder info = new StringBuilder();
        if (uploadedBy != null) {
            info.append("Subido por ").append(uploadedBy);
        }
        if (uploadedAt != null) {
            if (info.length() > 0) info.append(" el ");
            info.append(uploadedAt.toLocalDate());
        }
        return info.toString();
    }

    public boolean canBeDeleted() {
        // Lógica de negocio: un archivo puede ser eliminado si...
        // Por ejemplo, solo por quien lo subió, o por administradores
        return true; // Por ahora, todos pueden eliminar
    }

    public boolean canBeDownloaded() {
        // Lógica de negocio: un archivo puede ser descargado si...
        return isValidFile();
    }
}
