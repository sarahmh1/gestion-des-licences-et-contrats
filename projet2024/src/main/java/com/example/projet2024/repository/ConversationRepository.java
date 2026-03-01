package com.example.projet2024.repository;

import com.example.projet2024.entite.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    /**
     * Trouve une conversation entre deux utilisateurs
     */
    @Query("SELECT c FROM Conversation c WHERE " +
            "(c.user1Id = :user1Id AND c.user2Id = :user2Id) OR " +
            "(c.user1Id = :user2Id AND c.user2Id = :user1Id)")
    Optional<Conversation> findConversationBetweenUsers(@Param("user1Id") Long user1Id,
            @Param("user2Id") Long user2Id);

    /**
     * Récupère toutes les conversations d'un utilisateur
     */
    @Query("SELECT c FROM Conversation c WHERE c.user1Id = :userId OR c.user2Id = :userId " +
            "ORDER BY c.lastMessageTime DESC")
    List<Conversation> findUserConversations(@Param("userId") Long userId);
}
