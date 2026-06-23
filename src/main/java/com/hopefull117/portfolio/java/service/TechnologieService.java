package com.hopefull117.portfolio.java.service;


import com.hopefull117.portfolio.java.model.Technology;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TechnologieService extends AbstractCrudService<Technology> {
    protected TechnologieService(JpaRepository<Technology, Long> jpaRepository) {
        super(jpaRepository);

    }
    public List<Technology> getAllById(List<Long>ids){
        return jpaRepository.findAllById(ids);
    }
}
