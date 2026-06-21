package com.hopefull117.portfolio.java.service;

import com.hopefull117.portfolio.java.model.Technologies;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class TechnologieService extends AbstractCrudService<Technologies> {
    protected TechnologieService(JpaRepository<Technologies, Long> jpaRepository) {
        super(jpaRepository);
    }
}
