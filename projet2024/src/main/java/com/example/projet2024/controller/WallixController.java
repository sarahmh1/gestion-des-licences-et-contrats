package com.example.projet2024.controller;

import com.example.projet2024.entite.LicenceFortinet;
import com.example.projet2024.entite.proofpoint.Proofpoint;
import com.example.projet2024.entite.wallix.Wallix;
import com.example.projet2024.service.EmailService;
import com.example.projet2024.service.WallixServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/Wallix")
@CrossOrigin(origins = "http://localhost:4200")
public class WallixController {
    @Autowired
    private EmailService emailService;
    @Autowired
    private WallixServiceImpl wallixService;

    private final Path fileStorageLocation = Paths.get("uploads/wallix-files").toAbsolutePath().normalize();

    public WallixController() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory for file uploads.", ex);
        }
    }

    @PostMapping("/addWallix")
    public Wallix addWallix(@RequestBody Wallix wallix) {
        return wallixService.addWallix(wallix);
    }

    @PutMapping("/updateWallix")
    public Wallix updateWallix(@RequestBody Wallix wallix) {
        return wallixService.updateWallix(wallix);
    }

    @GetMapping("/get/{id-Wallix}")
    public Wallix getById(@PathVariable("id-Wallix") Long wallixId) {
        return wallixService.retrieveWallix(wallixId);
    }

    @GetMapping("/allWallix")
    public List<Wallix> getAllWallixs() {
        return wallixService.retrieveAllWallixs();
    }

    @DeleteMapping("/delete/{Wallix-id}")
    public void deleteById(@PathVariable("Wallix-id") Long wallixId) {
        wallixService.deleteById(wallixId);
    }

    @PutMapping("/approuve/{id}")
    public void activateWallix(@PathVariable("id") Long id) {
        wallixService.activate(id);
    }

    // Upload file for Wallix
    @PostMapping("/{id}/upload-file")
    public ResponseEntity<?> uploadFile(@PathVariable("id") Long wallixId, @RequestParam("file") MultipartFile file) {
        try {
            Wallix wallix = wallixService.retrieveWallix(wallixId);
            if (wallix == null) {
                return ResponseEntity.notFound().build();
            }

            // Delete old file if exists
            if (wallix.getFichier() != null && !wallix.getFichier().isEmpty()) {
                try {
                    Path oldFilePath = this.fileStorageLocation.resolve(wallix.getFichier()).normalize();
                    Files.deleteIfExists(oldFilePath);
                } catch (IOException e) {
                    System.err.println("Could not delete old file: " + e.getMessage());
                }
            }

            // Generate unique filename
            String originalFileName = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String newFileName = UUID.randomUUID().toString() + fileExtension;

            // Save file
            Path targetLocation = this.fileStorageLocation.resolve(newFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Update Wallix with file info (using updateWallixFile to preserve licences)
            Wallix updatedWallix = wallixService.updateWallixFile(wallixId, newFileName, originalFileName);

            return ResponseEntity.ok(updatedWallix);
        } catch (IOException ex) {
            return ResponseEntity.badRequest().body("Could not upload file: " + ex.getMessage());
        }
    }

    // Download file for Wallix
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable("id") Long wallixId) {
        try {
            Wallix wallix = wallixService.retrieveWallix(wallixId);
            if (wallix == null || wallix.getFichier() == null) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = this.fileStorageLocation.resolve(wallix.getFichier()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                String contentType = "application/octet-stream";
                String originalFileName = wallix.getFichierOriginalName() != null ? wallix.getFichierOriginalName() : wallix.getFichier();

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + originalFileName + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Delete file for Wallix
    @DeleteMapping("/{id}/delete-file")
    public ResponseEntity<?> deleteFile(@PathVariable("id") Long wallixId) {
        try {
            Wallix wallix = wallixService.retrieveWallix(wallixId);
            if (wallix == null) {
                return ResponseEntity.notFound().build();
            }

            if (wallix.getFichier() != null && !wallix.getFichier().isEmpty()) {
                Path filePath = this.fileStorageLocation.resolve(wallix.getFichier()).normalize();
                Files.deleteIfExists(filePath);
            }

            // Update Wallix with null file info (using updateWallixFile to preserve licences)
            Wallix updatedWallix = wallixService.updateWallixFile(wallixId, null, null);

            return ResponseEntity.ok(updatedWallix);
        } catch (IOException ex) {
            return ResponseEntity.badRequest().body("Could not delete file: " + ex.getMessage());
        }
    }
}
