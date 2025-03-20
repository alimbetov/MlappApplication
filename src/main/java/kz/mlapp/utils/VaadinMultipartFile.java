package kz.mlapp.utils;

import org.springframework.web.multipart.MultipartFile;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class VaadinMultipartFile implements MultipartFile {

    private final String fileName;
    private final String mimeType;
    private final byte[] content;

    public VaadinMultipartFile(MemoryBuffer buffer, String mimeType) throws IOException {
        this.fileName = buffer.getFileName() != null ? buffer.getFileName() : "unknown_file";
        this.mimeType = mimeType != null ? mimeType : "application/octet-stream";
        this.content = buffer.getInputStream().readAllBytes();
    }

    @Override
    public String getName() {
        return "file";
    }

    @Override
    public String getOriginalFilename() {
        return fileName;
    }

    @Override
    public String getContentType() {
        return mimeType;
    }

    @Override
    public boolean isEmpty() {
        return content.length == 0;
    }

    @Override
    public long getSize() {
        return content.length;
    }

    @Override
    public byte[] getBytes() {
        return content;
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(content);
    }

    @Override
    public void transferTo(java.io.File dest) throws IOException {
        Files.write(dest.toPath(), content);
    }
}
