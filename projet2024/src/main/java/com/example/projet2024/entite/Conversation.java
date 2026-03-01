package com.example.projet2024.entite;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "conversations")
public class Conversation implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user1_id", nullable = false)
    private Long user1Id;

    @Column(name = "user2_id", nullable = false)
    private Long user2Id;

    @Column(name = "last_message_time")
    private LocalDateTime lastMessageTime;

    @Column(name = "last_message", length = 500)
    private String lastMessage;

    // Soft delete flags per user
    @Column(name = "deleted_by_user1", nullable = false)
    private boolean deletedByUser1 = false;

    @Column(name = "deleted_by_user2", nullable = false)
    private boolean deletedByUser2 = false;

    // Timestamp when each user deleted - messages before this are hidden
    @Column(name = "deleted_at_user1")
    private LocalDateTime deletedAtUser1;

    @Column(name = "deleted_at_user2")
    private LocalDateTime deletedAtUser2;

    @PrePersist
    protected void onCreate() {
        lastMessageTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastMessageTime = LocalDateTime.now();
    }
}
