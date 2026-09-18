package com.hopefull117.portfolio.java.controller;

import com.hopefull117.portfolio.java.dto.SkillCategoryDTO;
import com.hopefull117.portfolio.java.helper.Category;
import com.hopefull117.portfolio.java.helper.SkillLevel;
import com.hopefull117.portfolio.java.helper.TimelineType;
import com.hopefull117.portfolio.java.model.Skill;
import com.hopefull117.portfolio.java.model.TimelineEntry;
import com.hopefull117.portfolio.java.service.SkillService;
import com.hopefull117.portfolio.java.service.TimelineEntryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PublicProfileControllerTest {
    @Mock
    private SkillService skillService;
    @Mock
    private TimelineEntryService timelineEntryService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new PublicProfileController(skillService, timelineEntryService)).build();
    }

    @Test
    void returnsPublicSkillsWithoutInternalIds() throws Exception {
        Skill skill = new Skill();
        skill.setName("Java");
        skill.setSkillLevel(SkillLevel.CONFIRME);
        when(skillService.getSkillsGroupedByCategory()).thenReturn(List.of(
                new SkillCategoryDTO(Category.BACKEND, List.of(skill))));

        mockMvc.perform(get("/api/public/skills"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("BACKEND"))
                .andExpect(jsonPath("$[0].skills[0].name").value("Java"))
                .andExpect(jsonPath("$[0].skills[0].id").doesNotExist());

        verify(skillService).getSkillsGroupedByCategory();
    }

    @Test
    void returnsPublicJourneyEntries() throws Exception {
        TimelineEntry entry = new TimelineEntry();
        entry.setTitle("Certification");
        entry.setDate(LocalDate.of(2026, 1, 1));
        entry.setDescription("Description");
        entry.setType(TimelineType.CERTIFICATION);

        when(timelineEntryService.getTimeline()).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/public/journey"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Certification"))
                .andExpect(jsonPath("$[0].date").value("2026-01-01"));

        verify(timelineEntryService).getTimeline();
    }
}
