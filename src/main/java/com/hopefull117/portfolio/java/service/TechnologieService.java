package com.hopefull117.portfolio.java.service;


import com.hopefull117.portfolio.java.model.Technology;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class TechnologieService extends AbstractCrudService<Technology> {
    protected TechnologieService(JpaRepository<Technology, Long> jpaRepository) {
        super(jpaRepository);
    }
}
