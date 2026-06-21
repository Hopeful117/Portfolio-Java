package com.hopefull117.portfolio.java.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="Technologies")
@Getter
@Setter
public class Technologies implements ModelEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    private String name;

    private String iconeUrl;
}
