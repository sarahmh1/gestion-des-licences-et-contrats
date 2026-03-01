package com.example.projet2024.controller;

import com.example.projet2024.entite.LicenceFortinet;
import com.example.projet2024.entite.fortinet.Fortinet;
import com.example.projet2024.entite.veeam.Veeam;
import com.example.projet2024.service.EmailService;
import com.example.projet2024.service.VeeamServiceImpl;
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
@RequestMapping("/Veeam")
@CrossOrigin(origins = "http://localhost:4200")
public class VeeamController {

    @Autowired
    private VeeamServiceImpl veeamService;
    @Autowired
    private EmailService emailService;

    private final Path fileStorageLocation = Paths.get("uploads/veeam-files").toAbsolutePath().normalize();

    public VeeamController() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory for file uploads.", ex);
        }
    }

    @PostMapping("/addVeeam")
    public Veeam addVeeam(@RequestBody Veeam veeam) {
        return veeamService.addVeeam(veeam);
    }

    @PutMapping("/updateVeeam")
    public Veeam updateVeeam(@RequestBody Veeam veeam) {
        return veeamService.updateVeeam(veeam);
    }

    @GetMapping("/get/{id-Veeam}")
    public Veeam getById(@PathVariable("id-Veeam") Long veeamId) {
        return veeamService.retrieveVeeam(veeamId);
    }

    @GetMapping("/allVeeam")
    public List<Veeam> getAllVeeams() {
        return veeamService.retrieveAllVeeams();
    }

    @DeleteMapping("/delete/{Veeam-id}")
    public void deleteById(@PathVariable("Veeam-id") Long veeamId) {
        veeamService.deleteById(veeamId);
    }

    @PutMapping("/approuve/{id}")
    public void activateVeeam(@PathVariable("id") Long id) {
        veeamService.activate(id);
    }

    // Upload file for Veeam
    @PostMapping("/{id}/upload-file")
    public ResponseEntity<?> uploadFile(@PathVariable("id") Long veeamId, @RequestParam("file") MultipartFile file) {
        try {
            Veeam veeam = veeamService.retrieveVeeam(veeamId);
            if (veeam == null) {
                return ResponseEntity.notFound().build();
            }

            // Delete old file if exists
            if (veeam.getFichier() != null && !veeam.getFichier().isEmpty()) {
                try {
                    Path oldFilePath = this.fileStorageLocation.resolve(veeam.getFichier()).normalize();
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

            // Update Veeam with file info (using updateVeeamFile to preserve licences)
            Veeam updatedVeeam = veeamService.updateVeeamFile(veeamId, newFileName, originalFileName);

            return ResponseEntity.ok(updatedVeeam);
        } catch (IOException ex) {
            return ResponseEntity.badRequest().body("Could not upload file: " + ex.getMessage());
        }
    }

    // Download file for Veeam
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable("id") Long veeamId) {
        try {
            Veeam veeam = veeamService.retrieveVeeam(veeamId);
            if (veeam == null || veeam.getFichier() == null) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = this.fileStorageLocation.resolve(veeam.getFichier()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                String contentType = "application/octet-stream";
                String originalFileName = veeam.getFichierOriginalName() != null ? veeam.getFichierOriginalName() : veeam.getFichier();

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

    // Delete file for Veeam
    @DeleteMapping("/{id}/delete-file")
    public ResponseEntity<?> deleteFile(@PathVariable("id") Long veeamId) {
        try {
            Veeam veeam = veeamService.retrieveVeeam(veeamId);
            if (veeam == null) {
                return ResponseEntity.notFound().build();
            }

            if (veeam.getFichier() != null && !veeam.getFichier().isEmpty()) {
                Path filePath = this.fileStorageLocation.resolve(veeam.getFichier()).normalize();
                Files.deleteIfExists(filePath);
            }

            // Update Veeam with null file info (using updateVeeamFile to preserve licences)
            Veeam updatedVeeam = veeamService.updateVeeamFile(veeamId, null, null);

            return ResponseEntity.ok(updatedVeeam);
        } catch (IOException ex) {
            return ResponseEntity.badRequest().body("Could not delete file: " + ex.getMessage());
        }
    }
}
