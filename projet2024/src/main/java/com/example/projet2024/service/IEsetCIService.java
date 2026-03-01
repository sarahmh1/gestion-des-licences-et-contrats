package com.example.projet2024.service;

import com.example.projet2024.entite.ESETCI;

import java.util.List;

public interface IEsetCIService {
    ESETCI addESET(ESETCI eset);
    ESETCI updateESET(ESETCI eset) ;
    ESETCI retrieveESET(Long esetid);
    List<ESETCI> retrieveAllESETs();
    void deleteById (Long esetid);
    void activate (Long id ) ;
}
