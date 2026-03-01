package com.example.projet2024.service;

import com.example.projet2024.entite.vmware.VMware;

import java.util.List;

public interface IVMwareService {
    VMware addVMware(VMware vmware);
    VMware updateVMware(VMware vmware);
    VMware updateVMwareFile(VMware vmware);
    VMware retrieveVMware(Long vmwareId);
    List<VMware> retrieveAllVMwares();
    void deleteById(Long vmwareId);
    void activate(Long id);
}
