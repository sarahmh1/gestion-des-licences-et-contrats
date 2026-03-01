package com.example.projet2024.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path rootLocation = Paths.get("uploads/profiles");

    public FileStorageServiceImpl() {
        try {
            Files.createDirectories(rootLocation);
            System.out.println("📁 Dossier upload créé: " + rootLocation.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("❌ Impossible de créer le dossier de stockage", e);
        }
    }

    @Override
    public String saveProfilePicture(MultipartFile file, Long userId) throws IOException {
        // Validation 1: Fichier non vide
        if (file.isEmpty()) {
            throw new IOException("❌ Le fichier est vide");
        }

        // Validation 2: Taille du fichier (max 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IOException("❌ Le fichier est trop volumineux (max 10MB)");
        }

        // Validation 3: Type de fichier
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IOException("❌ Le fichier doit être une image (JPEG, PNG, GIF, etc.)");
        }

        // Génération d'un nom de fichier sécurisé
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // Nom unique pour éviter les collisions
        String uniqueFilename = "user_" + userId + "_" + UUID.randomUUID() + fileExtension;
        Path destinationFile = rootLocation.resolve(uniqueFilename).normalize().toAbsolutePath();

        // Validation de sécurité: empêcher les path traversal attacks
        if (!destinationFile.getParent().equals(rootLocation.toAbsolutePath())) {
            throw new IOException("❌ Chemin de fichier non autorisé");
        }

        System.out.println("💾 Sauvegarde du fichier: " + destinationFile);

        // Sauvegarde du fichier
        try (var inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        }

        // Retourne le chemin relatif pour le stockage en base
        return uniqueFilename;
    }

    @Override
    public void deleteProfilePicture(String fileName) throws IOException {
        if (fileName != null && !fileName.isEmpty()) {
            Path filePath = rootLocation.resolve(Paths.get(fileName)).normalize().toAbsolutePath();

            // Validation de sécurité
            if (!filePath.getParent().equals(rootLocation.toAbsolutePath())) {
                throw new IOException("❌ Chemin de fichier non autorisé");
            }

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                System.out.println("🗑️ Fichier supprimé: " + filePath);
            }
        }
    }

    // Méthode utilitaire pour vérifier l'état du dossier
    public String checkStorageStatus() {
        try {
            if (!Files.exists(rootLocation)) {
                Files.createDirectories(rootLocation);
                return "Dossier créé: " + rootLocation.toAbsolutePath();
            }

            if (!Files.isWritable(rootLocation)) {
                return "❌ Dossier non accessible en écriture: " + rootLocation.toAbsolutePath();
            }

            return "✅ Dossier OK: " + rootLocation.toAbsolutePath();

        } catch (IOException e) {
            return "❌ Erreur: " + e.getMessage();
        }
    }
}
