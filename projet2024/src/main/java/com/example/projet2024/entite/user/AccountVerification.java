package com.example.projet2024.entite.user;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level=AccessLevel.PRIVATE)
public class AccountVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    String code;
    Date date;
    Boolean active;
    String email;
}

