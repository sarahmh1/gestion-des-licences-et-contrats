package com.example.projet2024.repository;

import com.example.projet2024.entite.Fortra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FortraRepo extends JpaRepository<Fortra,Long> {
    @Query("SELECT COUNT(l) FROM Fortra fo JOIN fo.licences l " +
            "WHERE YEAR(l.dateEx) = :year AND MONTH(l.dateEx) = :month")
    Long countExpiringInMonth(@Param("year") int year, @Param("month") int month);
}
