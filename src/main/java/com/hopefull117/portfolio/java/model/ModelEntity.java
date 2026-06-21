package com.hopefull117.portfolio.java.model;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public interface ModelEntity <M extends ModelEntity<M>>{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;


}
