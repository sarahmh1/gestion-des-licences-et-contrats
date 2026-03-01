package com.example.projet2024.entite;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "Intervenant")
public class Intervenant {
    @Id
    @Column(name = "IntervenantId")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long intervenantId;

    @Basic
    @Column(name = "Nom")
    private String nom;

    @ManyToOne
    @JoinColumn(name = "InterventionCurativeId")
    @JsonBackReference
    private InterventionCurative interventionCurative;
}
