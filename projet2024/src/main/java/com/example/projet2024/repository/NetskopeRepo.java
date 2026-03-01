package com.example.projet2024.repository;

import com.example.projet2024.entite.Netskope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NetskopeRepo extends JpaRepository<Netskope, Long> {
    @Query("SELECT COUNT(l) FROM Netskope n JOIN n.licences l " +
            "WHERE YEAR(l.dateEx) = :year AND MONTH(l.dateEx) = :month")
    Long countExpiringInMonth(@Param("year") int year, @Param("month") int month);
}
