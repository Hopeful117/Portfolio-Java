package com.hopefull117.portfolio.java.service;

import com.hopefull117.portfolio.java.dto.ProjectEditDTO;
import com.hopefull117.portfolio.java.dto.ProjectsDTO;
import com.hopefull117.portfolio.java.mapper.ProjectMapper;
import com.hopefull117.portfolio.java.model.Project;
import com.hopefull117.portfolio.java.model.Technology;
import com.hopefull117.portfolio.java.repository.ProjectRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.ListQueryByExampleExecutor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService extends AbstractCrudService<Project> {
    private final ProjectRepository projectRepository;
    private final FileStorageService fileStorageService;
    private final ProjectMapper projectMapper;
    private final TechnologieService technologieService;



    protected ProjectService(JpaRepository<Project, Long> jpaRepository, ProjectRepository projectRepository, FileStorageService fileStorageService, ProjectMapper projectMapper, TechnologieService technologieService) {
        super(jpaRepository);
        this.projectRepository = projectRepository;
        this.fileStorageService = fileStorageService;
        this.projectMapper = projectMapper;
        this.technologieService = technologieService;
    }


    public void create(ProjectsDTO dto) throws IOException {
        Project project = projectMapper.toEntity(dto);

        project.setTechnologies(
                technologieService.getAllById(dto.getTechnologies())
        );

        if (!dto.getImage().isEmpty()) {
            project.setImagePath(
                    fileStorageService.save(dto.getImage())
            );
        }

        projectRepository.save(project);
    }






    public void updateFromDto(ProjectEditDTO dto) throws IOException {

        Project project = projectRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));

        project.setTitle(dto.getTitle());
        project.setDescription(dto.getDescription());
        project.setGithubUrl(dto.getGithubUrl());

        List<Technology> technologies =
                technologieService.getAllById(dto.getTechnologyIds());

        project.setTechnologies(technologies);


        if (dto.getImage() != null && !dto.getImage().isEmpty()) {

            String imagePath = fileStorageService.save(dto.getImage());
            project.setImagePath(imagePath);
        }

        projectRepository.save(project);
    }

    public List<Project> getLastThreeProject(){
        return projectRepository.findTop3ByOrderByIdDesc() ;
    }


    }







