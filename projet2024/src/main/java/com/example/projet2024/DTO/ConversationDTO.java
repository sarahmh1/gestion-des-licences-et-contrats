package com.example.projet2024.DTO;

import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ConversationDTO {

    private Long id;
    private Long otherUserId;
    private String otherUserName;
    private String otherUserEmail;
    private String otherUserProfilePicture;
    private LocalDateTime lastMessageTime;
    private String lastMessage;
    private Long unreadCount;
}
