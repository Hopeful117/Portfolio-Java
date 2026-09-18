package com.hopefull117.portfolio.java.controller;

import com.hopefull117.portfolio.java.dto.PublicProjectSummaryDto;
import com.hopefull117.portfolio.java.service.PublicProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/projects")
@RequiredArgsConstructor
public class PublicProjectController {
    private final PublicProjectService projectService;

    @GetMapping
    public ResponseEntity<List<PublicProjectSummaryDto>> list() {
        return ResponseEntity.ok(projectService.findPublicSummaries());
    }
}
