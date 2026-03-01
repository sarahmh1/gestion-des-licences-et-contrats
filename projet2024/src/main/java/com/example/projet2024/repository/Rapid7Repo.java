package com.example.projet2024.repository;

import com.example.projet2024.entite.rapid7.Rapid7;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface Rapid7Repo extends JpaRepository<Rapid7, Long> {
    @Query("SELECT COUNT(l) FROM Rapid7 r JOIN r.licences l " +
            "WHERE YEAR(l.dateEx) = :year AND MONTH(l.dateEx) = :month")
    Long countExpiringInMonth(@Param("year") int year, @Param("month") int month);
}
