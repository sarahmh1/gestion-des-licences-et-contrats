package com.example.projet2024.service;

import com.example.projet2024.entite.Notification;
import com.example.projet2024.entite.User;
import com.example.projet2024.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public Notification createNotification(User user, String message, Long interventionPreventiveId) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setInterventionPreventiveId(interventionPreventiveId);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        Notification saved = notificationRepository.save(notification);

        // Envoyer la notification en temps réel via WebSocket
        Map<String, Object> wsPayload = new HashMap<>();
        wsPayload.put("id", saved.getId());
        wsPayload.put("message", saved.getMessage());
        wsPayload.put("read", false);
        wsPayload.put("createdAt", saved.getCreatedAt().toString());
        wsPayload.put("interventionPreventiveId", saved.getInterventionPreventiveId());
        messagingTemplate.convertAndSend("/topic/notifications/" + user.getId(), wsPayload);

        return saved;
    }

    public List<Notification> getNotificationsForUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification != null) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
    }

    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalse(userId);
        for (Notification n : unread) {
            n.setRead(true);
        }
        notificationRepository.saveAll(unread);
    }

    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }
}
