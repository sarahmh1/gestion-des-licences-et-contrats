package com.example.projet2024.controller;

import com.example.projet2024.entite.LicenceFortinet;
import com.example.projet2024.entite.fortinet.Fortinet;
import com.example.projet2024.entite.vmware.VMware;
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
@RequestMapping("/VMware")
@CrossOrigin(origins = "http://localhost:4200")
public class VMwareController {

    @Autowired
    private VMwareServiceImpl vmwareService;
    @Autowired
    private EmailService emailService;
    
    private final Path fileStorageLocation = Paths.get("uploads/vmware-files").toAbsolutePath().normalize();

    public VMwareController() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Impossible de créer le répertoire de stockage des fichiers VMware.", ex);
        }
    }
    
    @PostMapping("/addVMware")
    public VMware addVMware(@RequestBody VMware vmware) {
        return vmwareService.addVMware(vmware);
    }

    @PutMapping("/updateVMware")
    public VMware updateVMware(@RequestBody VMware vmware) {
        return vmwareService.updateVMware(vmware);
    }

    @GetMapping("/get/{id-VMware}")
    public VMware getById(@PathVariable("id-VMware") Long vmwareId) {
        return vmwareService.retrieveVMware(vmwareId);
    }

    @GetMapping("/allVMware")
    public List<VMware> getAllVMwares() {
        return vmwareService.retrieveAllVMwares();
    }

    @DeleteMapping("/delete/{VMware-id}")
    public void deleteById(@PathVariable("VMware-id") Long vmwareId) {
        vmwareService.deleteById(vmwareId);
    }

    @PutMapping("/approuve/{id}")
    public void activateVMware(@PathVariable("id") Long id) {
        vmwareService.activate(id);
    }

    // Upload fichier pour un VMware existant
    @PostMapping("/{id}/upload")
    public ResponseEntity<VMware> uploadFile(@PathVariable("id") Long id, @RequestParam("file") MultipartFile file) {
        try {
            VMware vmware = vmwareService.retrieveVMware(id);
            if (vmware == null) {
                return ResponseEntity.notFound().build();
            }

            // Supprimer l'ancien fichier s'il existe
            if (vmware.getFichier() != null && !vmware.getFichier().isEmpty()) {
                try {
                    Path oldFilePath = this.fileStorageLocation.resolve(vmware.getFichier()).normalize();
                    Files.deleteIfExists(oldFilePath);
                } catch (IOException e) {
                    System.err.println("Erreur suppression ancien fichier: " + e.getMessage());
                }
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

            // Mettre à jour uniquement les champs fichier
            vmware.setFichier(newFileName);
            vmware.setFichierOriginalName(originalFileName);
            VMware updatedVMware = vmwareService.updateVMwareFile(vmware);

            return ResponseEntity.ok(updatedVMware);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors du téléchargement du fichier", e);
        }
    }

    // Télécharger un fichier
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable("id") Long id) {
        try {
            VMware vmware = vmwareService.retrieveVMware(id);
            if (vmware == null || vmware.getFichier() == null) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = this.fileStorageLocation.resolve(vmware.getFichier()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                String contentType = "application/octet-stream";
                String originalFileName = vmware.getFichierOriginalName() != null ? 
                    vmware.getFichierOriginalName() : vmware.getFichier();

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + originalFileName + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Supprimer un fichier
    @DeleteMapping("/{id}/delete-file")
    public ResponseEntity<VMware> deleteFile(@PathVariable("id") Long id) {
        try {
            VMware vmware = vmwareService.retrieveVMware(id);
            if (vmware == null) {
                return ResponseEntity.notFound().build();
            }

            if (vmware.getFichier() != null && !vmware.getFichier().isEmpty()) {
                Path filePath = this.fileStorageLocation.resolve(vmware.getFichier()).normalize();
                Files.deleteIfExists(filePath);
            }

            vmware.setFichier(null);
            vmware.setFichierOriginalName(null);
            VMware updatedVMware = vmwareService.updateVMwareFile(vmware);

            return ResponseEntity.ok(updatedVMware);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la suppression du fichier", e);
        }
    }
}
