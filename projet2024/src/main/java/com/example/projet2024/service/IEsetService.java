package com.example.projet2024.service;
import com.example.projet2024.entite.ESET;

import java.util.List;
public interface IEsetService {
    ESET addESET(ESET eset);
    ESET updateESET(ESET eset) ;
    ESET retrieveESET(Long esetid);
    List<ESET> retrieveAllESETs();
    void deleteById (Long esetid);
    void activate (Long id ) ;
}
