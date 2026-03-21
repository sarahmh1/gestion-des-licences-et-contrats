package com.example.projet2024.controller;

import com.example.projet2024.DTO.ConversationDTO;
import com.example.projet2024.DTO.MessageDTO;
import com.example.projet2024.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class MessageRestController {

    @Autowired
    private MessageService messageService;

    private final String uploadDir = "uploads/messages/";

    /**
     * Envoie un message texte simple
     */
    @PostMapping("/send")
    public ResponseEntity<MessageDTO> sendMessage(@RequestBody Map<String, Object> payload) {
        Long senderId = Long.valueOf(payload.get("senderId").toString());
        Long receiverId = Long.valueOf(payload.get("receiverId").toString());
        String content = payload.get("content").toString();

        MessageDTO message = messageService.sendMessage(senderId, receiverId, content);
        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }

    /**
     * Envoie un message avec fichier joint
     */
    @PostMapping("/send-with-file")
    public ResponseEntity<MessageDTO> sendMessageWithFile(
            @RequestParam("senderId") Long senderId,
            @RequestParam("receiverId") Long receiverId,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam("file") MultipartFile file) {

        try {
            // Créer le dossier s'il n'existe pas
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Générer un nom unique pour le fichier
            String originalFileName = file.getOriginalFilename();
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            Path filePath = uploadPath.resolve(uniqueFileName);

            // Sauvegarder le fichier
            Files.copy(file.getInputStream(), filePath);

            // Déterminer le type de fichier
            String fileType = file.getContentType();

            // Envoyer le message avec le fichier
            MessageDTO message = messageService.sendMessageWithFile(
                    senderId, receiverId, content,
                    uniqueFileName, filePath.toString(), fileType, originalFileName);

            return new ResponseEntity<>(message, HttpStatus.CREATED);

        } catch (IOException e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Récupère les messages d'une conversation
     */
    @GetMapping("/conversation/{currentUserId}/{otherUserId}")
    public ResponseEntity<List<MessageDTO>> getConversationMessages(
            @PathVariable Long currentUserId,
            @PathVariable Long otherUserId) {
        List<MessageDTO> messages = messageService.getConversationMessages(currentUserId, otherUserId);
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }

    /**
     * Récupère toutes les conversations d'un utilisateur
     */
    @GetMapping("/conversations/{userId}")
    public ResponseEntity<List<ConversationDTO>> getUserConversations(@PathVariable Long userId) {
        List<ConversationDTO> conversations = messageService.getUserConversations(userId);
        return new ResponseEntity<>(conversations, HttpStatus.OK);
    }

    /**
     * Marque les messages comme lus
     */
    @PostMapping("/mark-read")
    public ResponseEntity<Void> markMessagesAsRead(@RequestBody Map<String, Object> payload) {
        Long senderId = Long.valueOf(payload.get("senderId").toString());
        Long receiverId = Long.valueOf(payload.get("receiverId").toString());

        messageService.markMessagesAsRead(senderId, receiverId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Compte les messages non lus
     */
    @GetMapping("/unread-count/{userId}")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@PathVariable Long userId) {
        Long count = messageService.getUnreadCount(userId);
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Supprime un message pour moi seulement
     */
    @DeleteMapping("/delete-for-me/{messageId}/{userId}")
    public ResponseEntity<Void> deleteMessageForMe(@PathVariable Long messageId, @PathVariable Long userId) {
        messageService.deleteMessageForMe(messageId, userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Supprime un message pour tout le monde
     */
    @DeleteMapping("/delete-for-everyone/{messageId}/{userId}")
    public ResponseEntity<Void> deleteMessageForEveryone(@PathVariable Long messageId, @PathVariable Long userId) {
        messageService.deleteMessageForEveryone(messageId, userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Supprime une conversation pour un utilisateur (soft delete)
     */
    @DeleteMapping("/conversation/{conversationId}/user/{userId}")
    public ResponseEntity<Void> deleteConversationForUser(
            @PathVariable Long conversationId,
            @PathVariable Long userId) {
        messageService.deleteConversationForUser(conversationId, userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
