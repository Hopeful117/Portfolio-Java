package com.hopefull117.portfolio.java.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "contact")
public class Contact implements ModelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    private String githubUrl;

    private String githubIconeUrl;

    private String linkedinUrl;

    private String linkedinIconeUrl;


}
