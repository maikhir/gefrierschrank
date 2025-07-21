package com.gefrierschrank.app.service;

import com.gefrierschrank.app.dto.CsvItemDto;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;

@Service
public class FileUploadService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileUploadService.class);
    
    private static final int MAX_IMAGE_SIZE = 200; // 200x200 pixels
    private static final long MAX_FILE_SIZE_KB = 50; // 50KB
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/jpeg", "image/jpg", "image/png", "image/webp");
    private static final Set<String> ALLOWED_CSV_TYPES = Set.of("text/csv", "application/csv", "text/plain");
    
    @Value("${app.upload.directory:./uploads}")
    private String uploadDirectory;
    
    @Value("${app.upload.images.directory:images}")
    private String imageDirectory;
    
    @Value("${app.upload.csv.directory:csv}")
    private String csvDirectory;
    
    public String uploadAndCompressImage(MultipartFile file, String username) throws IOException {
        validateImageFile(file);
        
        // Create user-specific upload directory
        Path userUploadPath = createUserDirectory(username, imageDirectory);
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String uniqueFilename = generateUniqueFilename(username, extension);
        Path targetPath = userUploadPath.resolve(uniqueFilename);
        
        // Read and compress image
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        if (originalImage == null) {
            throw new IllegalArgumentException("Could not read image file");
        }
        
        BufferedImage compressedImage = compressImage(originalImage);
        
        // Save compressed image
        String formatName = extension.substring(1); // Remove the dot
        if ("jpg".equals(formatName)) {
            formatName = "jpeg";
        }
        
        ImageIO.write(compressedImage, formatName, targetPath.toFile());
        
        // Verify file size
        long fileSizeKB = Files.size(targetPath) / 1024;
        if (fileSizeKB > MAX_FILE_SIZE_KB) {
            // If still too large, compress further with lower quality
            compressedImage = compressImageWithQuality(originalImage, 0.7f);
            ImageIO.write(compressedImage, formatName, targetPath.toFile());
        }
        
        String relativePath = Paths.get(imageDirectory, username, uniqueFilename).toString();
        logger.info("Image uploaded and compressed: {} ({}KB)", relativePath, Files.size(targetPath) / 1024);
        
        return relativePath;
    }
    
    public Map<String, Object> processCsvFile(MultipartFile file, String username) throws IOException {
        validateCsvFile(file);
        
        // Create user-specific upload directory
        Path userUploadPath = createUserDirectory(username, csvDirectory);
        
        // Save CSV file temporarily
        String originalFilename = file.getOriginalFilename();
        String uniqueFilename = generateUniqueFilename(username, ".csv");
        Path targetPath = userUploadPath.resolve(uniqueFilename);
        
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        try {
            // Parse CSV and return preview
            List<CsvItemDto> items = parseCsvFile(targetPath);
            
            Map<String, Object> result = new HashMap<>();
            result.put("items", items);
            result.put("totalItems", items.size());
            result.put("filePath", Paths.get(csvDirectory, username, uniqueFilename).toString());
            result.put("validItems", items.stream().mapToInt(item -> item.isValid() ? 1 : 0).sum());
            
            return result;
            
        } catch (Exception e) {
            // Clean up file if parsing failed
            Files.deleteIfExists(targetPath);
            throw new IllegalArgumentException("Failed to parse CSV file: " + e.getMessage());
        }
    }
    
    public void deleteFile(String filePath, String username) {
        try {
            Path fullPath = Paths.get(uploadDirectory, filePath);
            
            // Security check: ensure file belongs to user
            if (!filePath.contains(username)) {
                throw new IllegalArgumentException("Access denied: File does not belong to user");
            }
            
            if (Files.exists(fullPath)) {
                Files.delete(fullPath);
                logger.info("File deleted: {}", filePath);
            } else {
                logger.warn("File not found for deletion: {}", filePath);
            }
            
        } catch (IOException e) {
            logger.error("Error deleting file: {}", filePath, e);
            throw new RuntimeException("Failed to delete file");
        }
    }
    
    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        if (!ALLOWED_IMAGE_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Invalid file type. Only JPEG, PNG and WebP images are allowed");
        }
        
        if (file.getSize() > 10 * 1024 * 1024) { // 10MB max original size
            throw new IllegalArgumentException("File too large. Maximum size is 10MB");
        }
    }
    
    private void validateCsvFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        
        if (!ALLOWED_CSV_TYPES.contains(contentType) && 
            (filename == null || !filename.toLowerCase().endsWith(".csv"))) {
            throw new IllegalArgumentException("Invalid file type. Only CSV files are allowed");
        }
        
        if (file.getSize() > 5 * 1024 * 1024) { // 5MB max
            throw new IllegalArgumentException("File too large. Maximum size is 5MB");
        }
    }
    
    private Path createUserDirectory(String username, String subDirectory) throws IOException {
        Path userPath = Paths.get(uploadDirectory, subDirectory, username);
        Files.createDirectories(userPath);
        return userPath;
    }
    
    private String generateUniqueFilename(String username, String extension) {
        return username + "_" + System.currentTimeMillis() + extension;
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg"; // Default extension
        }
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }
    
    private BufferedImage compressImage(BufferedImage originalImage) {
        return compressImageWithQuality(originalImage, 0.9f);
    }
    
    private BufferedImage compressImageWithQuality(BufferedImage originalImage, float quality) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        
        // Calculate new dimensions maintaining aspect ratio
        int newWidth, newHeight;
        if (originalWidth > originalHeight) {
            newWidth = MAX_IMAGE_SIZE;
            newHeight = (int) ((double) originalHeight / originalWidth * MAX_IMAGE_SIZE);
        } else {
            newHeight = MAX_IMAGE_SIZE;
            newWidth = (int) ((double) originalWidth / originalHeight * MAX_IMAGE_SIZE);
        }
        
        // Create new compressed image
        BufferedImage compressedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = compressedImage.createGraphics();
        
        // Set high quality rendering hints
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw scaled image
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();
        
        return compressedImage;
    }
    
    private List<CsvItemDto> parseCsvFile(Path csvPath) throws IOException, CsvException {
        List<CsvItemDto> items = new ArrayList<>();
        
        try (FileReader fileReader = new FileReader(csvPath.toFile());
             CSVReader csvReader = new CSVReader(fileReader)) {
            
            List<String[]> records = csvReader.readAll();
            
            if (records.isEmpty()) {
                throw new IllegalArgumentException("CSV file is empty");
            }
            
            // Skip header row if it exists
            boolean hasHeader = isHeaderRow(records.get(0));
            int startIndex = hasHeader ? 1 : 0;
            
            for (int i = startIndex; i < records.size(); i++) {
                String[] record = records.get(i);
                if (record.length >= 4) { // Minimum required fields
                    CsvItemDto item = parseCsvRecord(record, i + 1);
                    items.add(item);
                }
            }
        }
        
        return items;
    }
    
    private boolean isHeaderRow(String[] firstRow) {
        if (firstRow.length < 4) return false;
        
        // Check if first row contains typical header words
        String firstCell = firstRow[0].toLowerCase().trim();
        return firstCell.contains("name") || firstCell.contains("artikel") || 
               firstCell.contains("item") || firstCell.contains("bezeichnung");
    }
    
    private CsvItemDto parseCsvRecord(String[] record, int rowNumber) {
        CsvItemDto item = new CsvItemDto();
        item.setRowNumber(rowNumber);
        
        try {
            // Required fields: name, category, quantity, unit
            item.setName(record[0].trim());
            item.setCategoryName(record[1].trim());
            
            // Parse quantity
            try {
                item.setQuantity(new BigDecimal(record[2].trim()));
            } catch (NumberFormatException e) {
                item.addError("Invalid quantity: " + record[2]);
            }
            
            item.setUnit(record[3].trim());
            
            // Optional fields
            if (record.length > 4 && !record[4].trim().isEmpty()) {
                try {
                    item.setExpiryDate(parseDate(record[4].trim()));
                } catch (DateTimeParseException e) {
                    item.addError("Invalid expiry date: " + record[4]);
                }
            }
            
            if (record.length > 5 && !record[5].trim().isEmpty()) {
                item.setDescription(record[5].trim());
            }
            
            // Validate required fields
            if (item.getName().isEmpty()) {
                item.addError("Name is required");
            }
            if (item.getCategoryName().isEmpty()) {
                item.addError("Category is required");
            }
            if (item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                item.addError("Valid quantity is required");
            }
            if (item.getUnit().isEmpty()) {
                item.addError("Unit is required");
            }
            
        } catch (ArrayIndexOutOfBoundsException e) {
            item.addError("Missing required fields (name, category, quantity, unit)");
        }
        
        return item;
    }
    
    private LocalDate parseDate(String dateStr) {
        DateTimeFormatter[] formatters = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd.MM.yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy")
        };
        
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(dateStr, formatter);
            } catch (DateTimeParseException ignored) {
                // Try next formatter
            }
        }
        
        throw new DateTimeParseException("Unable to parse date: " + dateStr, dateStr, 0);
    }
}