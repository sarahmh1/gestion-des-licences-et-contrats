package com.example.projet2024.repository;

import com.example.projet2024.entite.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

        /**
         * Récupère tous les messages entre deux utilisateurs, triés par date
         */
        @Query("SELECT m FROM Message m WHERE " +
                        "(m.senderId = :user1Id AND m.receiverId = :user2Id) OR " +
                        "(m.senderId = :user2Id AND m.receiverId = :user1Id) " +
                        "ORDER BY m.timestamp ASC")
        List<Message> findMessagesBetweenUsers(@Param("user1Id") Long user1Id,
                        @Param("user2Id") Long user2Id);

        /**
         * Compte les messages non lus pour un utilisateur (exclut les supprimés)
         */
        @Query("SELECT COUNT(m) FROM Message m WHERE m.receiverId = :userId AND m.isRead = false AND m.deletedByReceiver = false AND m.deletedForEveryone = false")
        Long countUnreadMessages(@Param("userId") Long userId);

        /**
         * Récupère tous les messages non lus pour un utilisateur
         */
        @Query("SELECT m FROM Message m WHERE m.receiverId = :userId AND m.isRead = false")
        List<Message> findUnreadMessages(@Param("userId") Long userId);

        /**
         * Marque tous les messages d'une conversation comme lus
         */
        @Modifying
        @Query("UPDATE Message m SET m.isRead = true WHERE m.senderId = :senderId AND m.receiverId = :receiverId AND m.isRead = false")
        void markMessagesAsRead(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);
}
