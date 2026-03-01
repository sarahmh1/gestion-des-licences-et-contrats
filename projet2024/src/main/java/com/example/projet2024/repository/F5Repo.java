package com.example.projet2024.repository;

import com.example.projet2024.entite.F5;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface F5Repo extends JpaRepository<F5,Long> {
    @Query("SELECT COUNT(l) FROM F5 f5 JOIN f5.licences l " +
            "WHERE YEAR(l.dateEx) = :year AND MONTH(l.dateEx) = :month")
    Long countExpiringInMonth(@Param("year") int year, @Param("month") int month);
}
