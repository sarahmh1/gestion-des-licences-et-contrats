//package com.example.projet2024.entite.user;
//
//
//
//import jakarta.persistence.*;
//import lombok.*;
//import lombok.experimental.FieldDefaults;
//
//
//import java.util.Date;
//
//@Entity
//@Getter
//@Setter
//@ToString
//@NoArgsConstructor
//@AllArgsConstructor
//@FieldDefaults(level=AccessLevel.PRIVATE)
//public class ResetPasswordVerification {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    long id;
//    @Column(unique = true)
//    String url;
//    Date date;
//
//    @ManyToOne(cascade = CascadeType.ALL)
//    User user;
//}
//
