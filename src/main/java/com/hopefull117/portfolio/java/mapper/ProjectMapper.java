package com.hopefull117.portfolio.java.mapper;

import com.hopefull117.portfolio.java.dto.ProjectEditDTO;
import com.hopefull117.portfolio.java.dto.ProjectsDTO;
import com.hopefull117.portfolio.java.model.Project;

import com.hopefull117.portfolio.java.model.Technology;
import com.hopefull117.portfolio.java.repository.TechnologieRepository;
import com.hopefull117.portfolio.java.service.TechnologieService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;


@RequiredArgsConstructor
@Service
public class ProjectMapper {

    private final TechnologieService technologieService;

    public Project toEntity(ProjectsDTO dto) {
        Project project = new Project();

        project.setTitle(dto.getTitle());
        project.setDescription(dto.getDescription());
        project.setGithubUrl(dto.getGithubUrl());

        return project;
    }

    public ProjectsDTO toDto(Project project) {
        ProjectsDTO dto = new ProjectsDTO();

        dto.setTitle(project.getTitle());
        dto.setDescription(project.getDescription());
        dto.setGithubUrl(project.getGithubUrl());

        return dto;
    }
    public ProjectEditDTO toEditDto(Project project){
        ProjectEditDTO dto = new ProjectEditDTO();
        dto.setId(project.getId());
        dto.setTitle(project.getTitle());
        dto.setDescription(project.getDescription());
        dto.setImagePath(project.getImagePath());
        dto.setGithubUrl(project.getGithubUrl());
        dto.setTechnologyIds(
                project.getTechnologies()
                        .stream()
                        .map(Technology::getId)
                        .toList()
        );
        return dto;
    }
}
