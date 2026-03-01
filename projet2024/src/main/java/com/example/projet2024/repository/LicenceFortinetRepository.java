package com.example.projet2024.repository;

import com.example.projet2024.entite.LicenceFortinet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LicenceFortinetRepository extends JpaRepository<LicenceFortinet, Long> {
    @Query("SELECT fl FROM LicenceFortinet fl WHERE fl.dateEx IS NOT NULL AND fl.dateEx BETWEEN :startDate AND :endDate AND fl.fortinet IS NOT NULL")
    List<LicenceFortinet> findFortinetLicencesExpiringBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT fl FROM LicenceFortinet fl WHERE fl.dateEx IS NOT NULL AND fl.dateEx BETWEEN :startDate AND :endDate AND fl.palo IS NOT NULL")
    List<LicenceFortinet> findPaloLicencesExpiringBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT fl FROM LicenceFortinet fl WHERE fl.dateEx IS NOT NULL AND fl.dateEx BETWEEN :startDate AND :endDate AND fl.veeam IS NOT NULL")
    List<LicenceFortinet> findVeeamLicencesExpiringBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT fl FROM LicenceFortinet fl WHERE fl.dateEx IS NOT NULL AND fl.dateEx BETWEEN :startDate AND :endDate AND fl.proofpoint IS NOT NULL")
    List<LicenceFortinet> findProofpointLicencesExpiringBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT fl FROM LicenceFortinet fl WHERE fl.dateEx IS NOT NULL AND fl.dateEx BETWEEN :startDate AND :endDate AND fl.wallix IS NOT NULL")
    List<LicenceFortinet> findWallixLicencesExpiringBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT fl FROM LicenceFortinet fl WHERE fl.dateEx IS NOT NULL AND fl.dateEx BETWEEN :startDate AND :endDate AND fl.rapid7 IS NOT NULL")
    List<LicenceFortinet> findRapid7LicencesExpiringBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT fl FROM LicenceFortinet fl WHERE fl.dateEx IS NOT NULL AND fl.dateEx BETWEEN :startDate AND :endDate AND fl.vmware IS NOT NULL")
    List<LicenceFortinet> findVmwareLicencesExpiringBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT fl FROM LicenceFortinet fl WHERE fl.dateEx IS NOT NULL AND fl.dateEx BETWEEN :startDate AND :endDate AND fl.splunk IS NOT NULL")
    List<LicenceFortinet> findSplunkLicencesExpiringBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT fl FROM LicenceFortinet fl WHERE fl.dateEx IS NOT NULL AND fl.dateEx BETWEEN :startDate AND :endDate AND fl.microsoftO365 IS NOT NULL")
    List<LicenceFortinet> findMicrosoftLicencesExpiringBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT fl FROM LicenceFortinet fl WHERE fl.dateEx IS NOT NULL AND fl.dateEx BETWEEN :startDate AND :endDate AND fl.secPoint IS NOT NULL")
    List<LicenceFortinet> findSecPointLicencesExpiringBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT fl FROM LicenceFortinet fl WHERE fl.dateEx IS NOT NULL AND fl.dateEx BETWEEN :startDate AND :endDate AND fl.oneIdentity IS NOT NULL")
    List<LicenceFortinet> findOneIdentityLicencesExpiringBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT fl FROM LicenceFortinet fl WHERE fl.dateEx IS NOT NULL AND fl.dateEx BETWEEN :startDate AND :endDate AND fl.crowdstrike IS NOT NULL")
    List<LicenceFortinet> findCrowdstrikeLicencesExpiringBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT fl FROM LicenceFortinet fl WHERE fl.dateEx IS NOT NULL AND fl.dateEx BETWEEN :startDate AND :endDate AND fl.bitdefender IS NOT NULL")
    List<LicenceFortinet> findBitdefenderLicencesExpiringBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT fl FROM LicenceFortinet fl WHERE fl.dateEx IS NOT NULL AND fl.dateEx BETWEEN :startDate AND :endDate AND fl.netskope IS NOT NULL")
    List<LicenceFortinet> findNetskopeLicencesExpiringBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT fl FROM LicenceFortinet fl WHERE fl.dateEx IS NOT NULL AND fl.dateEx BETWEEN :startDate AND :endDate AND fl.f5 IS NOT NULL")
    List<LicenceFortinet> findF5LicencesExpiringBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT fl FROM LicenceFortinet fl WHERE fl.dateEx IS NOT NULL AND fl.dateEx BETWEEN :startDate AND :endDate AND fl.fortra IS NOT NULL")
    List<LicenceFortinet> findFortraLicencesExpiringBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT fl FROM LicenceFortinet fl WHERE fl.dateEx IS NOT NULL AND fl.dateEx BETWEEN :startDate AND :endDate AND fl.sentineIOne IS NOT NULL")
    List<LicenceFortinet> findSentineIOneLicencesExpiringBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT fl FROM LicenceFortinet fl WHERE fl.dateEx IS NOT NULL AND fl.dateEx BETWEEN :startDate AND :endDate AND fl.alwarebytes IS NOT NULL")
    List<LicenceFortinet> findAlwarebytesLicencesExpiringBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT fl FROM LicenceFortinet fl WHERE fl.dateEx IS NOT NULL AND fl.dateEx BETWEEN :startDate AND :endDate AND fl.infoblox IS NOT NULL")
    List<LicenceFortinet> findInfobloxLicencesExpiringBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT fl FROM LicenceFortinet fl WHERE fl.dateEx IS NOT NULL AND fl.dateEx BETWEEN :startDate AND :endDate AND fl.varonis IS NOT NULL")
    List<LicenceFortinet> findVaronisLicencesExpiringBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT fl FROM LicenceFortinet fl WHERE fl.dateEx IS NOT NULL AND fl.dateEx BETWEEN :startDate AND :endDate AND fl.imperva IS NOT NULL")
    List<LicenceFortinet> findImpervaLicencesExpiringBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT fl FROM LicenceFortinet fl WHERE fl.dateEx IS NOT NULL AND fl.dateEx BETWEEN :startDate AND :endDate AND fl.cisco IS NOT NULL")
    List<LicenceFortinet> findCiscoLicencesExpiringBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
