package com.hopefull117.portfolio.java.repository;

import com.hopefull117.portfolio.java.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findTop3ByOrderByCreatedAtDesc();
}
