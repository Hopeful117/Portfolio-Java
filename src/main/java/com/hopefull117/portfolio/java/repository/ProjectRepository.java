package com.hopefull117.portfolio.java.repository;

import com.hopefull117.portfolio.java.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
