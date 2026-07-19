package com.hopefull117.portfolio.java.controller;

import com.hopefull117.portfolio.java.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;
    @GetMapping("/projects")
    public String projects(Model model) {
        log.info("Accès page projets");
        model.addAttribute("title","Projets");
        model.addAttribute("projects",projectService.getAll());

        return "public/projects";
    }
}
