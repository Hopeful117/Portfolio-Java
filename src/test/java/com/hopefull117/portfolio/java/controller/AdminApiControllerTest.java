package com.hopefull117.portfolio.java.controller;

import com.hopefull117.portfolio.java.model.Project;
import com.hopefull117.portfolio.java.model.Technology;
import com.hopefull117.portfolio.java.dto.ProjectsDTO;
import com.hopefull117.portfolio.java.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminApiControllerTest {
    @Mock private ProjectService projectService;
    @Mock private TechnologieService technologieService;
    @Mock private SkillService skillService;
    @Mock private TimelineEntryService timelineEntryService;
    @Mock private ArticleService articleService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminApiController(projectService, technologieService, skillService, timelineEntryService, articleService)).build();
    }

    @Test
    void exposesProjectManagementProjectionWithoutEntityCycles() throws Exception {
        Technology technology = new Technology();
        technology.setId(4L);
        technology.setName("Java");
        Project project = new Project();
        project.setId(2L);
        project.setTitle("Developer OS");
        project.setDescription("Public system");
        project.setGithubUrl("https://github.com/example");
        project.setTechnologies(List.of(technology));
        when(projectService.getAll()).thenReturn(List.of(project));

        mockMvc.perform(get("/api/admin/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].technologies[0].id").value(4))
                .andExpect(jsonPath("$[0].technologies[0].name").value("Java"));
    }

    @Test
    void createsProjectThroughJsonCommand() throws Exception {
        mockMvc.perform(post("/api/admin/projects")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"title":"Developer OS","description":"System","githubUrl":"https://github.com/example","technologyIds":[4]}
                                """))
                .andExpect(status().isCreated());

        verify(projectService).create(any(ProjectsDTO.class));
    }
}
