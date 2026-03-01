package com.example.projet2024.DTO;

import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MessageDTO {

    private Long id;
    private String content;
    private Long senderId;
    private String senderName;
    private String senderEmail;
    private Long receiverId;
    private String receiverName;
    private String receiverEmail;
    private LocalDateTime timestamp;
    private boolean isRead;

    // File attachment fields
    private String fileName;
    private String filePath;
    private String fileType;
    private String originalFileName;
}
