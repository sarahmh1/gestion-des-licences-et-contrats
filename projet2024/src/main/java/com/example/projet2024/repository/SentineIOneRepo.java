package com.example.projet2024.repository;

import com.example.projet2024.entite.SentineIOne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SentineIOneRepo extends JpaRepository<SentineIOne,Long> {
    @Query("SELECT COUNT(l) FROM SecPoint se JOIN se.licences l " +
            "WHERE YEAR(l.dateEx) = :year AND MONTH(l.dateEx) = :month")
    Long countExpiringInMonth(@Param("year") int year, @Param("month") int month);
}
