package com.example.projet2024.repository;

import com.example.projet2024.entite.Varonis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public interface VaronisRepo extends JpaRepository<Varonis, Long> {
    @Query("SELECT COUNT(l) FROM Varonis v JOIN v.licences l " +
            "WHERE YEAR(l.dateEx) = :year AND MONTH(l.dateEx) = :month")
    Long countExpiringInMonth(@Param("year") int year, @Param("month") int month);



}
