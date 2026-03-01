package com.example.projet2024.controller;

import com.example.projet2024.DTO.PasswordChangeRequest;
import com.example.projet2024.DTO.UserDTO;
import com.example.projet2024.DTO.UserUpdateRequest;
import com.example.projet2024.Enum.Role_Enum;
import com.example.projet2024.entite.User;
import com.example.projet2024.service.FileStorageService;
import com.example.projet2024.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/Users")
@CrossOrigin(origins = "http://localhost:4200")
public class UserRestController {

    @Autowired
    private IUserService userService;
    @Autowired
    private  FileStorageService fileStorageService;

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/dto")
    public List<UserDTO> getAllUsersDTO() {
        return userService.getAllUsersDTO();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        return userService.updateUser(id, updatedUser);
    }

    // ✅ FONCTION COMPLÈTE POUR UPLOAD DE PHOTO
    @PutMapping(value = "/{id}/profile-picture")
    public ResponseEntity<Map<String, Object>> updateProfilePicture(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("=== DÉBUT UPLOAD PHOTO ===");
            System.out.println("📥 ID Utilisateur: " + id);

            // 1. VALIDATION DU FICHIER
            if (file == null) {
                System.out.println("❌ ERREUR: Aucun fichier reçu");
                response.put("error", "Aucun fichier fourni");
                return ResponseEntity.badRequest().body(response);
            }

            if (file.isEmpty()) {
                System.out.println("❌ ERREUR: Fichier vide");
                response.put("error", "Le fichier est vide");
                return ResponseEntity.badRequest().body(response);
            }

            // 2. INFORMATIONS DU FICHIER
            System.out.println("📁 Fichier reçu:");
            System.out.println("   Nom: " + file.getOriginalFilename());
            System.out.println("   Taille: " + file.getSize() + " bytes");
            System.out.println("   Type: " + file.getContentType());

            // 3. VÉRIFICATION AUTHENTIFICATION
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                System.out.println("❌ ERREUR: Utilisateur non authentifié");
                response.put("error", "Non authentifié");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String currentUserEmail = authentication.getName();
            System.out.println("👤 Utilisateur authentifié: " + currentUserEmail);

            // 4. RÉCUPÉRATION UTILISATEUR
            User userToUpdate = userService.getUserById(id);
            if (userToUpdate == null) {
                System.out.println("❌ ERREUR: Utilisateur non trouvé ID: " + id);
                response.put("error", "Utilisateur non trouvé");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            System.out.println("🎯 Utilisateur à modifier: " + userToUpdate.getEmail());

            // 5. VÉRIFICATION PERMISSIONS
            if (!userToUpdate.getEmail().equals(currentUserEmail)) {
                System.out.println("❌ ERREUR: Permission refusée");
                System.out.println("   Current: " + currentUserEmail);
                System.out.println("   Target: " + userToUpdate.getEmail());
                response.put("error", "Vous ne pouvez modifier que votre propre profil");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            // 6. SUPPRESSION ANCIENNE PHOTO (si existe)
            if (userToUpdate.getProfilePicture() != null && !userToUpdate.getProfilePicture().isEmpty()) {
                try {
                    System.out.println("🗑️ Suppression ancienne photo: " + userToUpdate.getProfilePicture());
                    fileStorageService.deleteProfilePicture(userToUpdate.getProfilePicture());
                } catch (Exception e) {
                    System.out.println("⚠️ Impossible de supprimer l'ancienne photo: " + e.getMessage());
                    // On continue quand même
                }
            }

            // 7. SAUVEGARDE NOUVELLE PHOTO

            System.out.println("💾 Sauvegarde nouvelle photo...");
            String filename = fileStorageService.saveProfilePicture(file, id);
            String profilePictureUrl = "/Users/serve-img/" + filename; // ✅ URL CORRIGÉE
            System.out.println("✅ Photo sauvegardée: " + profilePictureUrl);

            // 8. MISE À JOUR UTILISATEUR
            System.out.println("🔄 Mise à jour de l'utilisateur...");
            User updatedUser = userService.updateProfilePicture(id, profilePictureUrl);

            // 9. RÉPONSE SUCCÈS
            System.out.println("🎉 Upload réussi !");
            response.put("success", true);
            response.put("message", "Photo de profil mise à jour avec succès");
            response.put("profilePicture", profilePictureUrl);
            response.put("user", Map.of(
                    "id", updatedUser.getId(),
                    "email", updatedUser.getEmail(),
                    "firstname", updatedUser.getFirstname(),
                    "lastname", updatedUser.getLastname()
            ));

            System.out.println("=== FIN UPLOAD PHOTO - SUCCÈS ===");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("❌ ERREUR CRITIQUE: " + e.getMessage());
            e.printStackTrace();
            response.put("error", "Erreur lors de l'upload: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    // ✅ MÉTHODE POUR SERVIR LES IMAGES (à ajouter dans UserRestController)
    @GetMapping(value = "/serve-img/{filename:.+}", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_GIF_VALUE})
    public ResponseEntity<byte[]> serveImage(@PathVariable String filename) {
        try {
            // Chemin relatif au projet (même que FileStorageServiceImpl)
            Path filePath = Paths.get("uploads/profiles")
                    .resolve(filename)
                    .toAbsolutePath();

            System.out.println("🎯 SERVING IMAGE: " + filename);
            System.out.println("📁 Absolute path: " + filePath.toString());
            System.out.println("📁 File exists: " + Files.exists(filePath));

            if (!Files.exists(filePath)) {
                System.out.println("❌ FILE NOT FOUND: " + filename);
                return ResponseEntity.notFound().build();
            }

            // Lire le fichier
            byte[] imageBytes = Files.readAllBytes(filePath);

            // Déterminer le type MIME
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                if (filename.toLowerCase().endsWith(".png")) contentType = "image/png";
                else if (filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".jpeg")) contentType = "image/jpeg";
                else if (filename.toLowerCase().endsWith(".gif")) contentType = "image/gif";
                else contentType = "application/octet-stream";
            }

            System.out.println("✅ Image served: " + filename + " (" + imageBytes.length + " bytes) as " + contentType);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(imageBytes);

        } catch (Exception e) {
            System.out.println("❌ Error serving image: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    @GetMapping("/debug-storage")
    public ResponseEntity<String> debugStorage() {
        try {
            Path uploadsPath = Paths.get("uploads").toAbsolutePath();
            Path profilesPath = Paths.get("uploads/profiles").toAbsolutePath();

            StringBuilder response = new StringBuilder();
            response.append("📁 Uploads path: ").append(uploadsPath).append("\n");
            response.append("📁 Profiles path: ").append(profilesPath).append("\n");
            response.append("📁 Uploads exists: ").append(Files.exists(uploadsPath)).append("\n");
            response.append("📁 Profiles exists: ").append(Files.exists(profilesPath)).append("\n");

            if (Files.exists(profilesPath)) {
                try (var files = Files.list(profilesPath)) {
                    response.append("📄 Files in profiles: ").append(files.count()).append("\n");
                }
            }

            return ResponseEntity.ok(response.toString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // Dans UserRestController.java
    @GetMapping("/test-image/{filename}")
    public ResponseEntity<byte[]> testImage(@PathVariable String filename) {
        try {
            Path filePath = Paths.get("uploads/profiles/").toAbsolutePath().resolve(filename);

            System.out.println("🔍 TEST - Looking for: " + filePath);
            System.out.println("📁 TEST - Exists: " + Files.exists(filePath));

            if (Files.exists(filePath)) {
                byte[] imageBytes = Files.readAllBytes(filePath);
                return ResponseEntity.ok().body(imageBytes);
            }
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    @GetMapping("/debug-ultimate")
    public ResponseEntity<String> debugUltimate() {
        StringBuilder result = new StringBuilder();

        result.append("=== ULTIMATE DEBUG ===\n\n");

        try {
            // 1. Test du chemin physique
            Path profilesPath = Paths.get("uploads/profiles").toAbsolutePath();
            result.append("📁 Physical path: ").append(profilesPath).append("\n");
            result.append("📁 Exists: ").append(Files.exists(profilesPath)).append("\n");
            result.append("📁 Readable: ").append(Files.isReadable(profilesPath)).append("\n\n");

            // 2. Liste des fichiers
            if (Files.exists(profilesPath)) {
                result.append("📄 Files in directory:\n");
                Files.list(profilesPath).forEach(file -> {
                    try {
                        result.append("   - ").append(file.getFileName())
                                .append(" (").append(Files.size(file)).append(" bytes)")
                                .append("\n");
                    } catch (Exception e) {
                        result.append("   - ").append(file.getFileName()).append(" [error]\n");
                    }
                });
            }

            // 3. Test d'accès à un fichier spécifique
            result.append("\n🔍 Testing specific file access:\n");
            File testFile = new File("uploads/profiles/avatar.png");
            result.append("   File: ").append(testFile.getAbsolutePath()).append("\n");
            result.append("   Exists: ").append(testFile.exists()).append("\n");
            result.append("   Readable: ").append(testFile.canRead()).append("\n");

            // 4. Test de lecture
            if (testFile.exists()) {
                try {
                    byte[] testBytes = Files.readAllBytes(testFile.toPath());
                    result.append("   Can read: ").append(testBytes.length > 0).append("\n");
                    result.append("   Size: ").append(testBytes.length).append(" bytes\n");
                } catch (Exception e) {
                    result.append("   Read error: ").append(e.getMessage()).append("\n");
                }
            }

            // 5. URLs de test
            result.append("\n🌐 Test URLs:\n");
            result.append("   - http://localhost:8089/Users/test-image/avatar.png\n");
            result.append("   - http://localhost:8089/api/files/profiles/avatar.png\n");
            result.append("   - http://localhost:8089/Users/debug-ultimate\n");

        } catch (Exception e) {
            result.append("❌ Error: ").append(e.getMessage());
        }

        return ResponseEntity.ok(result.toString());
    }

    @PutMapping("/{id}/role")
    public User assignUserRole(@PathVariable Long id, @RequestParam Role_Enum role) {
        return userService.assignUserRole(id, role);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateUser(@PathVariable Long id) {
        userService.activateUser(id);
        return ResponseEntity.ok(Map.of("message", "User activated successfully"));
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok(Map.of("message", "User deactivated successfully"));
    }


    @GetMapping("/verify")
    public ResponseEntity<String> verifyUser(@RequestParam String token) {
        boolean verified = userService.verifyUser(token);
        if (verified) {
            return ResponseEntity.ok("User verified successfully");
        } else {
            return ResponseEntity.badRequest().body("Invalid token");
        }
    }

    // Ajoutez ces endpoints
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();

            // Si vous avez une méthode findByEmail dans votre service
            User user = userService.findByEmail(currentUsername);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        try {
            User user = userService.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @PathVariable Long id,
            @RequestBody PasswordChangeRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Vérifier l'authentification
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                response.put("success", false);
                response.put("message", "Non authentifié");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String currentUserEmail = authentication.getName();
            User userToUpdate = userService.getUserById(id);
            
            // Vérifier que l'utilisateur modifie son propre mot de passe
            if (!userToUpdate.getEmail().equals(currentUserEmail)) {
                response.put("success", false);
                response.put("message", "Vous ne pouvez modifier que votre propre mot de passe");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            // Changer le mot de passe
            boolean success = userService.changePassword(id, request);
            
            if (success) {
                response.put("success", true);
                response.put("message", "Mot de passe changé avec succès");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Mot de passe actuel incorrect");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors du changement de mot de passe: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
