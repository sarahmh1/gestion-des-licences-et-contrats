package com.example.projet2024.service;

import com.example.projet2024.entite.Netskope;
import com.example.projet2024.entite.vmware.VMware;

import java.util.List;

public interface INetskopeService {
    Netskope addNetskope(Netskope netskope);
    Netskope updateNetskope(Netskope netskope);
    Netskope retrieveNetskope(Long netskopeId);
    List<Netskope> retrieveAllNetskopes();
    void deleteById(Long netskopeId);
    void activate(Long id);
    Netskope updateNetskopeFile(Long netskopeId, String fichier, String fichierOriginalName);
}
