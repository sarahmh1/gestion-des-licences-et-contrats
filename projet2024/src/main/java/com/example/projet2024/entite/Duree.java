package com.example.projet2024.entite;
import com.fasterxml.jackson.annotation.JsonProperty;
public enum Duree {

    @JsonProperty("1_an")
    UN_AN ,
    @JsonProperty("2_ans")
    DEUX_ANS ,
    @JsonProperty("3_ans")
    TROIS_ANS
}
