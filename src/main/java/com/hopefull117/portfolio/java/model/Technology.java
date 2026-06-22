package com.hopefull117.portfolio.java.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name="Technology")
@Getter
@Setter
public class Technology implements ModelEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    private String name;

    private String iconeUrl;

    @ManyToMany(mappedBy = "technologies")
    private List<Project> projects;
}

