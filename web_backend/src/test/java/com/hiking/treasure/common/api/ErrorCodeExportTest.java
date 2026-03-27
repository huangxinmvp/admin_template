package com.hiking.treasure.common.api;

import com.hiking.treasure.tools.ErrorCodeTsExporter;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErrorCodeExportTest {

    @Test
    void frontendTsFileMatchesGeneratedContent() throws IOException {
        String content = Files.readString(Path.of("docs/frontend-error-keys.ts"));
        assertEquals(ErrorCodeTsExporter.generateTsContent(), content);
    }
}
