package com.hopefull117.portfolio.java.service;

import com.hopefull117.portfolio.java.model.Project;
import com.hopefull117.portfolio.java.model.Technology;
import com.hopefull117.portfolio.java.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicProjectServiceTest {
    @Mock
    private ProjectRepository projectRepository;

    @Test
    void mapsOnlyPublicProjectFieldsAndTechnologyNames() {
        Technology technology = new Technology();
        technology.setName("Java");
        technology.setIconeUrl("/icons/java.svg");

        Project project = new Project();
        project.setTitle("HopeCodeSec");
        project.setDescription("Public system");
        project.setImagePath("/project.webp");
        project.setGithubUrl("https://github.com/example");
        project.setTechnologies(List.of(technology));
        when(projectRepository.findAll()).thenReturn(List.of(project));

        var summaries = new PublicProjectService(projectRepository).findPublicSummaries();

        assertEquals(1, summaries.size());
        assertEquals("HopeCodeSec", summaries.getFirst().title());
        assertEquals("Java", summaries.getFirst().technologies().getFirst().name());
        assertEquals("/icons/java.svg", summaries.getFirst().technologies().getFirst().iconUrl());
        verify(projectRepository).findAll();
    }
}
