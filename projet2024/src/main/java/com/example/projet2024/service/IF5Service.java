package com.example.projet2024.service;

import com.example.projet2024.entite.F5;

import java.util.List;

public interface IF5Service {
    F5 addF5(F5 f5);
    F5 updateF5(F5 f5) ;
    F5 updateF5File(F5 f5);
    F5 retrieveF5(Long f5id);
    List<F5> retrieveAllF5s();
    void deleteById (Long f5id);
    void activate (Long id ) ;
}
