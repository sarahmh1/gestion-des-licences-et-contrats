package com.example.projet2024.service;

import com.example.projet2024.DTO.ConversationDTO;
import com.example.projet2024.DTO.MessageDTO;
import com.example.projet2024.entite.Conversation;
import com.example.projet2024.entite.Message;
import com.example.projet2024.entite.User;
import com.example.projet2024.repository.ConversationRepository;
import com.example.projet2024.repository.MessageRepository;
import com.example.projet2024.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Envoie un message et met à jour la conversation
     */
    @Transactional
    public MessageDTO sendMessage(Long senderId, Long receiverId, String content) {
        return sendMessageWithFile(senderId, receiverId, content, null, null, null, null);
    }

    /**
     * Envoie un message avec ou sans fichier joint
     */
    @Transactional
    public MessageDTO sendMessageWithFile(Long senderId, Long receiverId, String content,
            String fileName, String filePath, String fileType, String originalFileName) {
        // Créer le message
        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content != null ? content : "");
        message.setTimestamp(LocalDateTime.now());
        message.setRead(false);

        // Ajouter les informations du fichier si présent
        if (fileName != null) {
            message.setFileName(fileName);
            message.setFilePath(filePath);
            message.setFileType(fileType);
            message.setOriginalFileName(originalFileName);
        }

        Message savedMessage = messageRepository.save(message);

        // Mettre à jour ou créer la conversation
        String lastMessage = content;
        if ((content == null || content.isEmpty()) && originalFileName != null) {
            lastMessage = "📎 " + originalFileName;
        }
        updateOrCreateConversation(senderId, receiverId, lastMessage != null ? lastMessage : "");

        // Convertir en DTO
        MessageDTO dto = convertToDTO(savedMessage);

        // Envoyer en temps réel via WebSocket au destinataire ET à l'expéditeur
        messagingTemplate.convertAndSend("/topic/messages/" + receiverId, dto);
        messagingTemplate.convertAndSend("/topic/messages/" + senderId, dto);

        return dto;
    }

    /**
     * Récupère tous les messages d'une conversation pour un utilisateur
     * Filtre les messages antérieurs à la date de suppression si applicable
     */
    public List<MessageDTO> getConversationMessages(Long currentUserId, Long otherUserId) {
        List<Message> messages = messageRepository.findMessagesBetweenUsers(currentUserId, otherUserId);
        List<MessageDTO> messageDTOs = new ArrayList<>();

        // Récupérer la conversation pour vérifier la date de suppression
        Optional<Conversation> conversationOpt = conversationRepository.findConversationBetweenUsers(currentUserId,
                otherUserId);
        LocalDateTime deletedAt = null;

        if (conversationOpt.isPresent()) {
            Conversation conversation = conversationOpt.get();
            if (conversation.getUser1Id().equals(currentUserId)) {
                deletedAt = conversation.getDeletedAtUser1();
            } else if (conversation.getUser2Id().equals(currentUserId)) {
                deletedAt = conversation.getDeletedAtUser2();
            }
        }

        for (Message message : messages) {
            // Filtrer les messages antérieurs à la date de suppression
            if (deletedAt != null && message.getTimestamp().isBefore(deletedAt)) {
                continue;
            }
            // Filtrer les messages supprimés pour cet utilisateur
            if (message.getSenderId().equals(currentUserId) && message.isDeletedBySender()) {
                continue;
            }
            if (message.getReceiverId().equals(currentUserId) && message.isDeletedByReceiver()) {
                continue;
            }
            messageDTOs.add(convertToDTO(message));
        }

        return messageDTOs;
    }

    /**
     * Marque les messages comme lus
     */
    @Transactional
    public void markMessagesAsRead(Long senderId, Long receiverId) {
        messageRepository.markMessagesAsRead(senderId, receiverId);
    }

    /**
     * Compte les messages non lus pour un utilisateur
     */
    public Long getUnreadCount(Long userId) {
        return messageRepository.countUnreadMessages(userId);
    }

    /**
     * Récupère toutes les conversations d'un utilisateur
     */
    public List<ConversationDTO> getUserConversations(Long userId) {
        List<Conversation> conversations = conversationRepository.findUserConversations(userId);
        List<ConversationDTO> conversationDTOs = new ArrayList<>();

        for (Conversation conversation : conversations) {
            // Filtrer les conversations supprimées pour cet utilisateur
            boolean isDeletedForUser = (conversation.getUser1Id().equals(userId) && conversation.isDeletedByUser1())
                    || (conversation.getUser2Id().equals(userId) && conversation.isDeletedByUser2());

            if (isDeletedForUser) {
                continue; // Ignorer cette conversation
            }

            ConversationDTO dto = convertConversationToDTO(conversation, userId);

            // Compter les messages non lus de cette conversation
            Long otherUserId = conversation.getUser1Id().equals(userId) ? conversation.getUser2Id()
                    : conversation.getUser1Id();

            Long unreadCount = messageRepository.findMessagesBetweenUsers(userId, otherUserId)
                    .stream()
                    .filter(m -> m.getReceiverId().equals(userId) && !m.isRead())
                    .count();

            dto.setUnreadCount(unreadCount);
            conversationDTOs.add(dto);
        }

        return conversationDTOs;

    }

    /**
     * Met à jour ou crée une conversation
     * Réinitialise aussi le soft delete pour le destinataire si nécessaire
     */
    private void updateOrCreateConversation(Long senderId, Long receiverId, String lastMessage) {
        Optional<Conversation> existingConversation = conversationRepository.findConversationBetweenUsers(senderId,
                receiverId);

        Conversation conversation;
        if (existingConversation.isPresent()) {
            conversation = existingConversation.get();

            // Réinitialiser le soft delete pour le destinataire (pour qu'il voie la
            // conversation)
            // MAIS garder la date deletedAt pour filtrer les anciens messages
            if (conversation.getUser1Id().equals(receiverId)) {
                conversation.setDeletedByUser1(false);
            } else if (conversation.getUser2Id().equals(receiverId)) {
                conversation.setDeletedByUser2(false);
            }
        } else {
            conversation = new Conversation();
            conversation.setUser1Id(senderId);
            conversation.setUser2Id(receiverId);
        }

        conversation.setLastMessage(lastMessage.length() > 100 ? lastMessage.substring(0, 100) + "..." : lastMessage);
        conversation.setLastMessageTime(LocalDateTime.now());

        conversationRepository.save(conversation);
    }

    /**
     * Convertit un Message en MessageDTO
     */
    private MessageDTO convertToDTO(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setContent(message.getContent());
        dto.setSenderId(message.getSenderId());
        dto.setReceiverId(message.getReceiverId());
        dto.setTimestamp(message.getTimestamp());
        dto.setRead(message.isRead());

        // Ajouter les informations du fichier
        dto.setFileName(message.getFileName());
        dto.setFilePath(message.getFilePath());
        dto.setFileType(message.getFileType());
        dto.setOriginalFileName(message.getOriginalFileName());
        dto.setDeletedForEveryone(message.isDeletedForEveryone());

        // Récupérer les informations de l'expéditeur
        Optional<User> sender = userRepository.findById(message.getSenderId());
        sender.ifPresent(user -> {
            dto.setSenderName(user.getFirstname() + " " + user.getLastname());
            dto.setSenderEmail(user.getEmail());
        });

        // Récupérer les informations du destinataire
        Optional<User> receiver = userRepository.findById(message.getReceiverId());
        receiver.ifPresent(user -> {
            dto.setReceiverName(user.getFirstname() + " " + user.getLastname());
            dto.setReceiverEmail(user.getEmail());
        });

        return dto;
    }

    /**
     * Convertit une Conversation en ConversationDTO
     */
    private ConversationDTO convertConversationToDTO(Conversation conversation, Long currentUserId) {
        ConversationDTO dto = new ConversationDTO();
        dto.setId(conversation.getId());
        dto.setLastMessage(conversation.getLastMessage());
        dto.setLastMessageTime(conversation.getLastMessageTime());

        // Déterminer l'autre utilisateur
        Long otherUserId = conversation.getUser1Id().equals(currentUserId) ? conversation.getUser2Id()
                : conversation.getUser1Id();
        dto.setOtherUserId(otherUserId);

        // Récupérer les informations de l'autre utilisateur
        Optional<User> otherUser = userRepository.findById(otherUserId);
        otherUser.ifPresent(user -> {
            // Gérer les noms null
            String firstName = user.getFirstname() != null ? user.getFirstname() : "";
            String lastName = user.getLastname() != null ? user.getLastname() : "";
            String fullName = (firstName + " " + lastName).trim();
            // Si le nom est vide, utiliser l'email comme fallback
            if (fullName.isEmpty()) {
                fullName = user.getEmail();
            }
            dto.setOtherUserName(fullName);
            dto.setOtherUserEmail(user.getEmail());
            dto.setOtherUserProfilePicture(user.getProfilePicture());
        });

        return dto;
    }

    /**
     * Supprime un message pour l'utilisateur courant seulement
     */
    @Transactional
    public void deleteMessageForMe(Long messageId, Long userId) {
        Optional<Message> msgOpt = messageRepository.findById(messageId);
        if (msgOpt.isPresent()) {
            Message msg = msgOpt.get();
            if (msg.getSenderId().equals(userId)) {
                msg.setDeletedBySender(true);
            } else if (msg.getReceiverId().equals(userId)) {
                msg.setDeletedByReceiver(true);
            }
            // Si les deux ont supprimé, supprimer réellement
            if (msg.isDeletedBySender() && msg.isDeletedByReceiver()) {
                messageRepository.delete(msg);
            } else {
                messageRepository.save(msg);
            }
        }
    }

    /**
     * Supprime un message pour tout le monde
     */
    @Transactional
    public void deleteMessageForEveryone(Long messageId, Long userId) {
        Optional<Message> msgOpt = messageRepository.findById(messageId);
        if (msgOpt.isPresent()) {
            Message msg = msgOpt.get();
            // Seul l'expéditeur peut supprimer pour tout le monde
            if (msg.getSenderId().equals(userId)) {
                msg.setDeletedForEveryone(true);
                msg.setContent("");
                msg.setFileName(null);
                msg.setFilePath(null);
                msg.setFileType(null);
                msg.setOriginalFileName(null);
                messageRepository.save(msg);
            }
        }
    }

    /**
     * Soft delete d'une conversation pour un utilisateur spécifique
     * L'autre utilisateur peut toujours voir la conversation
     * Les messages avant cette date ne seront plus visibles pour cet utilisateur
     */
    @Transactional
    public void deleteConversationForUser(Long conversationId, Long userId) {
        Optional<Conversation> conversationOpt = conversationRepository.findById(conversationId);
        if (conversationOpt.isPresent()) {
            Conversation conversation = conversationOpt.get();
            LocalDateTime now = LocalDateTime.now();

            // Marquer comme supprimé pour cet utilisateur et enregistrer la date
            if (conversation.getUser1Id().equals(userId)) {
                conversation.setDeletedByUser1(true);
                conversation.setDeletedAtUser1(now);
            } else if (conversation.getUser2Id().equals(userId)) {
                conversation.setDeletedByUser2(true);
                conversation.setDeletedAtUser2(now);
            }

            conversationRepository.save(conversation);

            // Si les deux utilisateurs ont supprimé, supprimer réellement
            if (conversation.isDeletedByUser1() && conversation.isDeletedByUser2()) {
                // Supprimer tous les messages de la conversation
                List<Message> messages = messageRepository.findMessagesBetweenUsers(
                        conversation.getUser1Id(), conversation.getUser2Id());
                messageRepository.deleteAll(messages);
                // Supprimer la conversation
                conversationRepository.delete(conversation);
            }
        }
    }
}
