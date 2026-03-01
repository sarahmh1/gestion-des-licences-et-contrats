package com.example.projet2024.controller;

import com.example.projet2024.entite.Bitdefender;
import com.example.projet2024.entite.LicenceFortinet;
import com.example.projet2024.entite.vmware.VMware;
import com.example.projet2024.service.BitdefenderServiceImpl;
import com.example.projet2024.service.EmailService;
import com.example.projet2024.service.VMwareServiceImpl;
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
@RequestMapping("/Bitdefender")
@CrossOrigin(origins = "http://localhost:4200")
public class BitdefenderController {

    @Autowired
    private BitdefenderServiceImpl  bitdefenderService;
    @Autowired
    private EmailService emailService;

    private final Path fileStorageLocation = Paths.get("uploads/bitdefender-files").toAbsolutePath().normalize();

    public BitdefenderController() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory for file uploads.", ex);
        }
    }
    @PostMapping("/addBitdefender")
    public Bitdefender addBitdefender(@RequestBody Bitdefender bitdefender) {
        return bitdefenderService.addBitdefender(bitdefender);
    }

    @PutMapping("/updateBitdefender")
    public Bitdefender updateBitdefender(@RequestBody Bitdefender bitdefender) {
        return bitdefenderService.updateBitdefender(bitdefender);
    }

    @GetMapping("/get/{id-Bitdefender}")
    public Bitdefender getById(@PathVariable("id-Bitdefender") Long bitdefenderId) {
        return bitdefenderService.retrieveBitdefender(bitdefenderId);
    }

    @GetMapping("/allBitdefender")
    public List<Bitdefender> getAllBitdefenders() {
        return bitdefenderService.retrieveAllBitdefenders();
    }

    @DeleteMapping("/delete/{Bitdefender-id}")
    public void deleteById(@PathVariable("Bitdefender-id") Long bitdefenderId) {
        bitdefenderService.deleteById(bitdefenderId);
    }

    @PutMapping("/approuve/{id}")
    public void activateBitdefender(@PathVariable("id") Long id) {
        bitdefenderService.activate(id);
    }

    @PostMapping("/{id}/upload")
    public ResponseEntity<Bitdefender> uploadFile(@PathVariable("id") Long id, @RequestParam("file") MultipartFile file) {
        try {
            Bitdefender bitdefender = bitdefenderService.retrieveBitdefender(id);
            if (bitdefender == null) {
                return ResponseEntity.notFound().build();
            }

            // Supprimer l'ancien fichier s'il existe
            if (bitdefender.getFichier() != null) {
                Path oldFilePath = this.fileStorageLocation.resolve(bitdefender.getFichier()).normalize();
                Files.deleteIfExists(oldFilePath);
            }

            // Générer un nom unique pour le fichier
            String originalFileName = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String newFileName = UUID.randomUUID().toString() + fileExtension;

            // Sauvegarder le fichier
            Path targetLocation = this.fileStorageLocation.resolve(newFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Mettre à jour l'entité
            Bitdefender updated = bitdefenderService.updateBitdefenderFile(id, newFileName, originalFileName);
            return ResponseEntity.ok(updated);
        } catch (IOException ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable("id") Long id) {
        try {
            Bitdefender bitdefender = bitdefenderService.retrieveBitdefender(id);
            if (bitdefender == null || bitdefender.getFichier() == null) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = this.fileStorageLocation.resolve(bitdefender.getFichier()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + bitdefender.getFichierOriginalName() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}/delete-file")
    public ResponseEntity<Bitdefender> deleteFile(@PathVariable("id") Long id) {
        try {
            Bitdefender bitdefender = bitdefenderService.retrieveBitdefender(id);
            if (bitdefender == null) {
                return ResponseEntity.notFound().build();
            }

            // Supprimer le fichier physique
            if (bitdefender.getFichier() != null) {
                Path filePath = this.fileStorageLocation.resolve(bitdefender.getFichier()).normalize();
                Files.deleteIfExists(filePath);
            }

            // Mettre à jour l'entité
            Bitdefender updated = bitdefenderService.updateBitdefenderFile(id, null, null);
            return ResponseEntity.ok(updated);
        } catch (IOException ex) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
