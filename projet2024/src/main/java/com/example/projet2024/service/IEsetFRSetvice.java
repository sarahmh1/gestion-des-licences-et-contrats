package com.example.projet2024.service;
import com.example.projet2024.entite.ESETFR;

import java.util.List;
public interface IEsetFRSetvice {
    ESETFR addESETFR(ESETFR eset);
    ESETFR updateESETFR(ESETFR eset) ;
    ESETFR retrieveFRESET(Long esetid);
    List<ESETFR> retrieveAllESETsFR();
    void deleteByIdFR (Long esetid);
    void activateFR (Long id ) ;



}
