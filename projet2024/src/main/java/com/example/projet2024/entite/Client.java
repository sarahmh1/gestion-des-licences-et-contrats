package com.example.projet2024.entite;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "client")
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_client", nullable = false)
    private String nomClient;

    @Column(name = "nos_vis_a_vis", columnDefinition = "TEXT")
    @Convert(converter = ListToStringConverter.class)
    private List<String> nosVisAVis = new ArrayList<>();

    @Column(name = "adresses_mail", columnDefinition = "TEXT")
    @Convert(converter = ListToStringConverter.class)
    private List<String> adressesMail = new ArrayList<>();

    @Column(name = "num_tel", columnDefinition = "TEXT")
    @Convert(converter = ListToStringConverter.class)
    private List<String> numTel = new ArrayList<>();

    @Column(name = "adresses", columnDefinition = "TEXT")
    @Convert(converter = ListToStringConverter.class)
    private List<String> adresses = new ArrayList<>();
}
