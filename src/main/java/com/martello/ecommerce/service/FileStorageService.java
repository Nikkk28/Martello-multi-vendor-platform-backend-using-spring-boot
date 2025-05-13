package com.martello.ecommerce.service;

import com.martello.ecommerce.exception.BadRequestException;
import com.martello.ecommerce.exception.FileStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;
    private final List<String> allowedContentTypes = Arrays.asList("image/jpeg", "image/png");
    private final long maxFileSize = 2 * 1024 * 1024; // 2MB

    public FileStorageService(@Value("${file.upload-dir:uploads}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        // Validate file size
        if (file.getSize() > maxFileSize) {
            throw new BadRequestException("File size exceeds maximum limit of 2MB");
        }
        
        // Validate content type
        if (!allowedContentTypes.contains(file.getContentType())) {
            throw new BadRequestException("Only PNG and JPG images are allowed");
        }
        
        // Generate unique filename
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = UUID.randomUUID().toString() + fileExtension;
        
        try {
            // Copy file to the target location
            Path targetLocation = this.fileStorageLocation.resolve(filename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            return "/uploads/" + filename;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + filename, ex);
        }
    }
}
