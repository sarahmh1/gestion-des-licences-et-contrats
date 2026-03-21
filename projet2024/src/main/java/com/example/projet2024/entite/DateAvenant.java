package com.example.projet2024.entite;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "Date_Avenant")
public class DateAvenant {
    @Id
    @Column(name = "DateAvenantId")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dateAvenantId;

    @Basic
    @Column(name = "Date_Avenant")
    private LocalDate dateAvenant;

    @Basic
    @Column(name = "Numero_Avenant")
    private Integer numeroAvenant;

    @Basic
    @Column(name = "Details_Avenant", columnDefinition = "TEXT")
    private String details;

    @ManyToOne
    @JoinColumn(name = "ContratId")
    @JsonBackReference
    private Contrat contrat;
}
