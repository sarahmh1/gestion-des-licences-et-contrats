package com.example.projet2024.repository;

import com.example.projet2024.entite.microsofto365.MicrosoftO365;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MicrosoftO365Repo extends JpaRepository<MicrosoftO365, Long> {
    @Query("SELECT COUNT(l) FROM MicrosoftO365 m JOIN m.licences l " +
            "WHERE YEAR(l.dateEx) = :year AND MONTH(l.dateEx) = :month")
    Long countExpiringInMonth(@Param("year") int year, @Param("month") int month);
}
