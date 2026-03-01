package com.example.projet2024.service;

import com.example.projet2024.entite.splunk.Splunk;

import java.util.List;

public interface ISplunkService {
    Splunk addSplunk(Splunk splunk);
    Splunk updateSplunk(Splunk splunk);
    Splunk updateSplunkFile(Splunk splunk);
    Splunk retrieveSplunk(Long splunkId);
    List<Splunk> retrieveAllSplunks();
    void deleteById(Long splunkId);
    void activate(Long id);
}
