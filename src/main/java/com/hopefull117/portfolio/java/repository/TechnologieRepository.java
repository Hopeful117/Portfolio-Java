package com.hopefull117.portfolio.java.repository;

import com.hopefull117.portfolio.java.model.Technologies;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TechnologieRepository extends JpaRepository<Technologies,Long> {
}
