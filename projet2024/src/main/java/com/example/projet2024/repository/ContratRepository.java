package com.example.projet2024.repository;

import com.example.projet2024.entite.Contrat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContratRepository extends JpaRepository<Contrat, Long> {
    List<Contrat> findByClientContainingIgnoreCase(String client);
    List<Contrat> findByObjetContratContainingIgnoreCase(String objetContrat);
}
