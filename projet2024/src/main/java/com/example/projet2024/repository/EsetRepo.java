package com.example.projet2024.repository;

import com.example.projet2024.entite.ESET;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EsetRepo extends JpaRepository<ESET,Long> {
    @Query("SELECT COUNT(e) FROM ESET e " +
            "WHERE YEAR(e.dateEx) = :year AND MONTH(e.dateEx) = :month")
    Long countExpiringInMonth(@Param("year") int year, @Param("month") int month);
}
