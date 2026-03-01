/*package com.example.projet2024.entite;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "Group")
public class Group {
    @Id
    @Column(name = "GroupID")
    @GeneratedValue(strategy =GenerationType.IDENTITY)
    private Long GroupID ;
    @Basic
    @Column(name = "Name")
    private String Name ;
    @OneToOne
    private TimeTable timetable;
    @OneToMany(cascade = CascadeType.ALL, mappedBy="group")
    private Set<User> Users;
}*/
