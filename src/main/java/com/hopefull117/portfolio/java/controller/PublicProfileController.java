package com.hopefull117.portfolio.java.controller;

import com.hopefull117.portfolio.java.dto.PublicSkillCategoryDto;
import com.hopefull117.portfolio.java.dto.PublicSkillDto;
import com.hopefull117.portfolio.java.dto.PublicTimelineEntryDto;
import com.hopefull117.portfolio.java.service.SkillService;
import com.hopefull117.portfolio.java.service.TimelineEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicProfileController {
    private final SkillService skillService;
    private final TimelineEntryService timelineEntryService;

    @GetMapping("/skills")
    public ResponseEntity<List<PublicSkillCategoryDto>> skills() {
        return ResponseEntity.ok(skillService.getSkillsGroupedByCategory().stream()
                .map(category -> new PublicSkillCategoryDto(
                        category.category().name(),
                        category.skills().stream()
                                .map(skill -> new PublicSkillDto(
                                        skill.getName(), skill.getSkillLevel().name()))
                                .toList()))
                .toList());
    }

    @GetMapping("/journey")
    public ResponseEntity<List<PublicTimelineEntryDto>> journey() {
        return ResponseEntity.ok(timelineEntryService.getTimeline().stream()
                .map(entry -> new PublicTimelineEntryDto(
                        entry.getTitle(), entry.getDate(), entry.getDescription(), entry.getLink(), entry.getType().name()))
                .toList());
    }
}
