package com.example.rama.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

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

    @Column(name = "description")
    private String description;

    @Column(name = "uploaded_by")
    private String uploadedBy;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    // Relación con la actividad
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private ClassActivities activity;

    // Constructores
    public ActivityEvidence() {
        this.uploadedAt = LocalDateTime.now();
    }

    public ActivityEvidence(String filename, String originalFilename, String filePath, 
                           String contentType, Long fileSize, ClassActivities activity) {
        this.filename = filename;
        this.originalFilename = originalFilename;
        this.filePath = filePath;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.activity = activity;
        this.uploadedAt = LocalDateTime.now();
        this.fileExtension = extractFileExtension(originalFilename);
    }

    // Métodos de utilidad
    private String extractFileExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf(".")).toLowerCase();
        }
        return "";
    }

    public boolean isImageFile() {
        return fileExtension.matches("\\.(jpg|jpeg|png|gif|bmp)$");
    }

    public boolean isPdfFile() {
        return ".pdf".equals(fileExtension);
    }

    public boolean isDocumentFile() {
        return fileExtension.matches("\\.(doc|docx|xls|xlsx|ppt|pptx)$");
    }

    public String getFileIcon() {
        if (isImageFile()) return "🖼️";
        if (isPdfFile()) return "📄";
        if (isDocumentFile()) return "📋";
        return "📎";
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
        this.fileExtension = extractFileExtension(originalFilename);
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

    public ClassActivities getActivity() {
        return activity;
    }

    public void setActivity(ClassActivities activity) {
        this.activity = activity;
    }

    public String getFormattedFileSize() {
        if (fileSize == null) return "N/A";
        
        double bytes = fileSize.doubleValue();
        if (bytes < 1024) return String.format("%.0f B", bytes);
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024 * 1024));
        return String.format("%.1f GB", bytes / (1024 * 1024 * 1024));
    }

    @Override
    public String toString() {
        return "ActivityEvidence{" +
                "id=" + id +
                ", originalFilename='" + originalFilename + '\'' +
                ", fileSize=" + fileSize +
                ", contentType='" + contentType + '\'' +
                ", uploadedAt=" + uploadedAt +
                '}';
    }
}
