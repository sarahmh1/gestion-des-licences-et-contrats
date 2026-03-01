package com.example.projet2024.repository;

import com.example.projet2024.entite.InterventionPreventive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterventionPreventiveRepository extends JpaRepository<InterventionPreventive, Long> {
    List<InterventionPreventive> findByNomClientContainingIgnoreCase(String nomClient);
    List<InterventionPreventive> findByContratContratId(Long contratId);
}
