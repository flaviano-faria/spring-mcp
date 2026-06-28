package com.mcp.context;

import com.mcp.service.file.FileService;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;

@Component
public class Pattern {

    private static final String VARIABLE_NAMES_FILE = "variablenames.md";

    private final FileService fileService;
    private final String specsPath;

    public Pattern(
            FileService fileService,
            @Value("${mcp.pattern.specs-path:src/main/resources/specs/pattern}") String specsPath) {
        this.fileService = fileService;
        this.specsPath = specsPath;
    }

    @McpTool(name = "pattern", description = "I will return pattern specs from review")
    public String getFileContent() {
        try {
            return fileService.readFileContent(specsPath, VARIABLE_NAMES_FILE);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read pattern specs", e);
        }
    }
}
