package com.example.projet2024.repository;

import com.example.projet2024.entite.InterventionCurative;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterventionCurativeRepository extends JpaRepository<InterventionCurative, Long> {
    List<InterventionCurative> findByNomClientContainingIgnoreCase(String nomClient);
    List<InterventionCurative> findByIntervenantContainingIgnoreCase(String intervenant);
    List<InterventionCurative> findByContratContratId(Long contratId);
}
