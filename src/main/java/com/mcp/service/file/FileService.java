package com.mcp.service.file;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class FileService {

    public String readFileContent(String filePath, String fileName) throws IOException {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("filePath must not be blank");
        }
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("fileName must not be blank");
        }

        Path basePath = Path.of(filePath).toAbsolutePath().normalize();
        Path resolvedPath = basePath.resolve(fileName).normalize();

        if (!resolvedPath.startsWith(basePath)) {
            throw new IllegalArgumentException("fileName must not escape the provided filePath");
        }
        if (!Files.isRegularFile(resolvedPath)) {
            throw new IOException("File not found: " + resolvedPath);
        }

        return Files.readString(resolvedPath, StandardCharsets.UTF_8);
    }
}
