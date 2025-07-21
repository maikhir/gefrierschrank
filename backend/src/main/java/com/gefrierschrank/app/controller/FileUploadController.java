package com.gefrierschrank.app.controller;

import com.gefrierschrank.app.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@Tag(name = "File Upload", description = "APIs for file upload and management")
public class FileUploadController {
    
    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);
    
    private final FileUploadService fileUploadService;
    
    public FileUploadController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }
    
    @PostMapping(value = "/upload/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload item image", 
               description = "Upload and compress an image for an item. Images are automatically compressed to 200x200px and max 50KB")
    public ResponseEntity<Map<String, Object>> uploadImage(
            @Parameter(description = "Image file to upload") @RequestParam("file") MultipartFile file,
            Authentication authentication) throws IOException {
        
        logger.info("POST /api/files/upload/image - Uploading image for user: {}", authentication.getName());
        
        try {
            String filePath = fileUploadService.uploadAndCompressImage(file, authentication.getName());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("filePath", filePath);
            response.put("message", "Image uploaded and compressed successfully");
            
            logger.info("Image uploaded successfully: {}", filePath);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid file upload attempt: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (IOException e) {
            logger.error("Error processing image upload: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to process image upload");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PostMapping(value = "/upload/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload CSV file", 
               description = "Upload a CSV file for bulk item import. Returns preview of items to be imported")
    public ResponseEntity<Map<String, Object>> uploadCsv(
            @Parameter(description = "CSV file to upload") @RequestParam("file") MultipartFile file,
            Authentication authentication) throws IOException {
        
        logger.info("POST /api/files/upload/csv - Uploading CSV for user: {}", authentication.getName());
        
        try {
            Map<String, Object> csvData = fileUploadService.processCsvFile(file, authentication.getName());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", csvData);
            response.put("message", "CSV file processed successfully");
            
            logger.info("CSV file processed successfully with {} items", 
                       ((java.util.List<?>) csvData.get("items")).size());
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid CSV upload attempt: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (IOException e) {
            logger.error("Error processing CSV upload: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to process CSV file");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @DeleteMapping("/image")
    @Operation(summary = "Delete image", description = "Delete an uploaded image file")
    public ResponseEntity<Map<String, Object>> deleteImage(
            @Parameter(description = "File path to delete") @RequestParam String filePath,
            Authentication authentication) {
        
        logger.info("DELETE /api/files/image - Deleting image: {} for user: {}", filePath, authentication.getName());
        
        try {
            fileUploadService.deleteFile(filePath, authentication.getName());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Image deleted successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid file deletion attempt: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (Exception e) {
            logger.error("Error deleting file: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to delete file");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}