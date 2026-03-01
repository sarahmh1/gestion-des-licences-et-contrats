package com.example.projet2024.service;


import com.example.projet2024.entite.ESETNFR;

import java.util.List;

public interface IEsetNFRService {
    ESETNFR addESET(ESETNFR eset);
    ESETNFR updateESET(ESETNFR eset) ;
    ESETNFR retrieveESET(Long esetid);
    List<ESETNFR>retrieveAllESETs();
    void deleteById (Long esetid);
    void activate (Long id ) ;
}
