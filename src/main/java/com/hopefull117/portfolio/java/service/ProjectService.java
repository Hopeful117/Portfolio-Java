package com.hopefull117.portfolio.java.service;

import com.hopefull117.portfolio.java.dto.ProjectsDTO;
import com.hopefull117.portfolio.java.model.Project;
import com.hopefull117.portfolio.java.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService extends AbstractCrudService<Project> {


    protected ProjectService(JpaRepository<Project, Long> jpaRepository) {
        super(jpaRepository);
    }

    public static Project projectToEntity(ProjectsDTO dto){
        Project project = new Project();
        project.setTitle(dto.getTitle());
        project.setDescription(dto.getDescription());
        project.setTechnologies(dto.getTechnologies());
        project.setGithubUrl(dto.getGithubUrl());
        project.setImageUrl(dto.getImageUrl());

        return project;

    }
}
