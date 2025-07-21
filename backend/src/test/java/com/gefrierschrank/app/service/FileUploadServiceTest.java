package com.gefrierschrank.app.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileUploadServiceTest {

    private FileUploadService fileUploadService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileUploadService = new FileUploadService();
        ReflectionTestUtils.setField(fileUploadService, "uploadDirectory", tempDir.toString());
        ReflectionTestUtils.setField(fileUploadService, "imageDirectory", "images");
        ReflectionTestUtils.setField(fileUploadService, "csvDirectory", "csv");
    }

    @Test
    void validateImageFile_EmptyFile_ShouldThrowException() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[0]);
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ReflectionTestUtils.invokeMethod(fileUploadService, "validateImageFile", emptyFile)
        );
        
        assertEquals("File is empty", exception.getMessage());
    }

    @Test
    void validateImageFile_InvalidFileType_ShouldThrowException() {
        MockMultipartFile invalidFile = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ReflectionTestUtils.invokeMethod(fileUploadService, "validateImageFile", invalidFile)
        );
        
        assertTrue(exception.getMessage().contains("Invalid file type"));
    }

    @Test
    void validateCsvFile_EmptyFile_ShouldThrowException() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "test.csv", "text/csv", new byte[0]);
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ReflectionTestUtils.invokeMethod(fileUploadService, "validateCsvFile", emptyFile)
        );
        
        assertEquals("File is empty", exception.getMessage());
    }

    @Test
    void generateUniqueFilename_ShouldIncludeUsernameAndTimestamp() {
        String username = "testuser";
        String extension = ".jpg";
        
        String filename = ReflectionTestUtils.invokeMethod(fileUploadService, "generateUniqueFilename", username, extension);
        
        assertNotNull(filename);
        assertTrue(filename.startsWith(username + "_"));
        assertTrue(filename.endsWith(extension));
    }

    @Test
    void getFileExtension_ValidFilename_ShouldReturnExtension() {
        String result = ReflectionTestUtils.invokeMethod(fileUploadService, "getFileExtension", "test.jpg");
        assertEquals(".jpg", result);
    }

    @Test
    void getFileExtension_NoExtension_ShouldReturnDefault() {
        String result = ReflectionTestUtils.invokeMethod(fileUploadService, "getFileExtension", "filename");
        assertEquals(".jpg", result);
    }
}