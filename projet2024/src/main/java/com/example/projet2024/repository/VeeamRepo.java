package com.example.projet2024.repository;

import com.example.projet2024.entite.veeam.Veeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VeeamRepo extends JpaRepository<Veeam, Long> {
    @Query("SELECT COUNT(l) FROM Veeam v JOIN v.licences l " +
            "WHERE YEAR(l.dateEx) = :year AND MONTH(l.dateEx) = :month")
    Long countExpiringInMonth(@Param("year") int year, @Param("month") int month);
}
