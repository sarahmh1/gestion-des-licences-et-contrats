package com.example.projet2024.service;

import com.example.projet2024.entite.OneIdentity;


import java.util.List;

public interface IOneIdentityService {
    OneIdentity addOneIdentity(OneIdentity oneIdentity);
    OneIdentity updateOneIdentity(OneIdentity oneIdentity);
    OneIdentity updateOneIdentityFile(OneIdentity oneIdentity);
    OneIdentity retrieveOneIdentity(Long oneIdentityId);
    List<OneIdentity> retrieveAllOneIdentitys();
    void deleteById(Long oneIdentityId);
    void activate(Long id);
}
