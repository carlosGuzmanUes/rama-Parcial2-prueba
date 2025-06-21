package com.example.rama.util;

import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Wrapper para convertir MemoryBuffer de Vaadin a MultipartFile de Spring
 * Esta clase permite usar el sistema de upload de Vaadin con los servicios de Spring
 */
public class MultipartFileWrapper implements MultipartFile {
    
    private final MemoryBuffer buffer;
    private final String originalFilename;
    private final String contentType;
    private byte[] cachedBytes;
    private boolean bytesLoaded = false;

    /**
     * Constructor principal
     * @param buffer MemoryBuffer de Vaadin que contiene el archivo
     * @param originalFilename Nombre original del archivo
     * @param contentType Tipo MIME del archivo
     */
    public MultipartFileWrapper(MemoryBuffer buffer, String originalFilename, String contentType) {
        this.buffer = buffer;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
    }

    /**
     * Constructor alternativo con detección automática de content type
     */
    public MultipartFileWrapper(MemoryBuffer buffer, String originalFilename) {
        this.buffer = buffer;
        this.originalFilename = originalFilename;
        this.contentType = detectContentType(originalFilename);
    }

    @Override
    public String getName() {
        return "file"; // Nombre del campo en el formulario
    }

    @Override
    public String getOriginalFilename() {
        return originalFilename;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        try {
            return getSize() == 0;
        } catch (Exception e) {
            return true;
        }
    }

    @Override
    public long getSize() {
        try {
            if (!bytesLoaded) {
                loadBytes();
            }
            return cachedBytes != null ? cachedBytes.length : 0;
        } catch (IOException e) {
            return 0;
        }
    }

    @Override
    public byte[] getBytes() throws IOException {
        if (!bytesLoaded) {
            loadBytes();
        }
        return cachedBytes != null ? cachedBytes.clone() : new byte[0];
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (!bytesLoaded) {
            loadBytes();
        }
        return cachedBytes != null ? 
            new ByteArrayInputStream(cachedBytes) : 
            new ByteArrayInputStream(new byte[0]);
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        transferTo(dest.toPath());
    }

    /**
     * Transfiere el archivo a un Path (método adicional para Java NIO)
     */
    public void transferTo(Path dest) throws IOException, IllegalStateException {
        try (InputStream inputStream = getInputStream()) {
            Files.copy(inputStream, dest, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Carga los bytes del archivo desde el MemoryBuffer y los cachea
     */
    private void loadBytes() throws IOException {
        if (bytesLoaded) {
            return;
        }

        try (InputStream inputStream = buffer.getInputStream()) {
            if (inputStream != null) {
                cachedBytes = inputStream.readAllBytes();
            } else {
                cachedBytes = new byte[0];
            }
        } catch (Exception e) {
            cachedBytes = new byte[0];
            throw new IOException("Error al leer el archivo desde MemoryBuffer", e);
        } finally {
            bytesLoaded = true;
        }
    }

    /**
     * Detecta el content type basado en la extensión del archivo
     */
    private String detectContentType(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "application/octet-stream";
        }

        String extension = filename.substring(filename.lastIndexOf(".")).toLowerCase();
        
        return switch (extension) {
            // Imágenes
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".png" -> "image/png";
            case ".gif" -> "image/gif";
            case ".bmp" -> "image/bmp";
            case ".svg" -> "image/svg+xml";
            case ".webp" -> "image/webp";
            
            // Documentos
            case ".pdf" -> "application/pdf";
            case ".doc" -> "application/msword";
            case ".docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case ".xls" -> "application/vnd.ms-excel";
            case ".xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case ".ppt" -> "application/vnd.ms-powerpoint";
            case ".pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            
            // Texto
            case ".txt" -> "text/plain";
            case ".csv" -> "text/csv";
            case ".html" -> "text/html";
            case ".xml" -> "application/xml";
            case ".json" -> "application/json";
            
            // Audio
            case ".mp3" -> "audio/mpeg";
            case ".wav" -> "audio/wav";
            case ".flac" -> "audio/flac";
            case ".aac" -> "audio/aac";
            
            // Video
            case ".mp4" -> "video/mp4";
            case ".avi" -> "video/x-msvideo";
            case ".mov" -> "video/quicktime";
            case ".wmv" -> "video/x-ms-wmv";
            
            // Comprimidos
            case ".zip" -> "application/zip";
            case ".rar" -> "application/vnd.rar";
            case ".7z" -> "application/x-7z-compressed";
            
            default -> "application/octet-stream";
        };
    }

    /**
     * Obtiene la extensión del archivo
     */
    public String getFileExtension() {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
    }

    /**
     * Verifica si el archivo es una imagen
     */
    public boolean isImage() {
        String ext = getFileExtension();
        return ext.matches("\\.(jpg|jpeg|png|gif|bmp|svg|webp)$");
    }

    /**
     * Verifica si el archivo es un documento
     */
    public boolean isDocument() {
        String ext = getFileExtension();
        return ext.matches("\\.(pdf|doc|docx|xls|xlsx|ppt|pptx|odt|ods|odp)$");
    }

    /**
     * Verifica si el archivo es de texto
     */
    public boolean isText() {
        String ext = getFileExtension();
        return ext.matches("\\.(txt|csv|html|xml|json|md|log)$");
    }

    /**
     * Obtiene una descripción legible del tamaño del archivo
     */
    public String getFormattedSize() {
        long size = getSize();
        if (size == 0) return "0 B";
        
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double sizeDouble = size;
        
        while (sizeDouble >= 1024 && unitIndex < units.length - 1) {
            sizeDouble /= 1024;
            unitIndex++;
        }
        
        return String.format("%.1f %s", sizeDouble, units[unitIndex]);
    }

    /**
     * Valida que el archivo tenga un tamaño válido
     */
    public boolean isValidSize(long maxSizeBytes) {
        return getSize() > 0 && getSize() <= maxSizeBytes;
    }

    /**
     * Valida que el archivo tenga una extensión permitida
     */
    public boolean hasValidExtension(String... allowedExtensions) {
        String fileExt = getFileExtension();
        for (String allowedExt : allowedExtensions) {
            if (fileExt.equals(allowedExt.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Genera un nombre único para el archivo basado en timestamp
     */
    public String generateUniqueFilename() {
        if (originalFilename == null) {
            return "file_" + System.currentTimeMillis();
        }
        
        String extension = getFileExtension();
        String baseName = originalFilename;
        
        if (!extension.isEmpty()) {
            baseName = originalFilename.substring(0, originalFilename.lastIndexOf("."));
        }
        
        // Limpiar el nombre base de caracteres especiales
        baseName = baseName.replaceAll("[^a-zA-Z0-9._-]", "_");
        
        return baseName + "_" + System.currentTimeMillis() + extension;
    }

    /**
     * Información de depuración del archivo
     */
    public String getDebugInfo() {
        return String.format(
            "MultipartFileWrapper{originalFilename='%s', contentType='%s', size=%d, extension='%s'}",
            originalFilename, contentType, getSize(), getFileExtension()
        );
    }

    /**
     * Verifica si el wrapper es válido y puede ser usado
     */
    public boolean isValid() {
        return buffer != null && 
               originalFilename != null && 
               !originalFilename.trim().isEmpty() && 
               !isEmpty();
    }

    @Override
    public String toString() {
        return String.format("MultipartFileWrapper[%s, %s, %s]", 
                           originalFilename, contentType, getFormattedSize());
    }

    /**
     * Limpia los recursos cacheados
     */
    public void cleanup() {
        cachedBytes = null;
        bytesLoaded = false;
    }

    // Métodos de conveniencia para validaciones comunes
    
    /**
     * Valida que sea un archivo de imagen válido
     */
    public boolean isValidImage(long maxSizeBytes) {
        return isImage() && isValidSize(maxSizeBytes);
    }

    /**
     * Valida que sea un documento válido
     */
    public boolean isValidDocument(long maxSizeBytes) {
        return isDocument() && isValidSize(maxSizeBytes);
    }

    /**
     * Obtiene el MemoryBuffer original (usar con cuidado)
     */
    public MemoryBuffer getBuffer() {
        return buffer;
    }
}
