package com.example.rama.util;

import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * Wrapper para convertir MemoryBuffer de Vaadin a MultipartFile de Spring
 */
public class MultipartFileWrapper implements MultipartFile {
    
    private final MemoryBuffer buffer;
    private final String filename;
    private final String contentType;

    public MultipartFileWrapper(MemoryBuffer buffer, String filename, String contentType) {
        this.buffer = buffer;
        this.filename = filename;
        this.contentType = contentType;
    }

    @Override
    public String getName() {
        return "file";
    }

    @Override
    public String getOriginalFilename() {
        return filename;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        try {
            return buffer.getInputStream() == null || buffer.getInputStream().available() == 0;
        } catch (IOException e) {
            return true;
        }
    }

    @Override
    public long getSize() {
        try {
            return buffer.getInputStream().available();
        } catch (IOException e) {
            return 0;
        }
    }

    @Override
    public byte[] getBytes() throws IOException {
        return buffer.getInputStream().readAllBytes();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return buffer.getInputStream();
    }

    @Override
    public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
        throw new UnsupportedOperationException("transferTo no está implementado");
    }
}
