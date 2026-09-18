package com.hopefull117.portfolio.java.controller;

import com.hopefull117.portfolio.java.dto.PublicProjectSummaryDto;
import com.hopefull117.portfolio.java.dto.PublicTechnologyDto;
import com.hopefull117.portfolio.java.service.PublicProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PublicProjectControllerTest {
    @Mock
    private PublicProjectService projectService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new PublicProjectController(projectService)).build();
    }

    @Test
    void returnsBoundedProjectSummaryProjection() throws Exception {
        when(projectService.findPublicSummaries()).thenReturn(List.of(
                new PublicProjectSummaryDto(
                        "HopeCodeSec", "Public system", "/project.webp", "https://github.com/example",
                        List.of(new PublicTechnologyDto("Java", "/icons/java.svg")))));

        mockMvc.perform(get("/api/public/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("HopeCodeSec"))
                .andExpect(jsonPath("$[0].technologies[0].name").value("Java"))
                .andExpect(jsonPath("$[0].id").doesNotExist())
                .andExpect(jsonPath("$[0].technologies[0].id").doesNotExist());

        verify(projectService).findPublicSummaries();
    }
}
