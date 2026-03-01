package com.example.projet2024.repository;


import com.example.projet2024.entite.user.AccountVerification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountVerificationRepository extends JpaRepository<AccountVerification,Long> {
    AccountVerification getAccountVerificationByCode(String code);
}
