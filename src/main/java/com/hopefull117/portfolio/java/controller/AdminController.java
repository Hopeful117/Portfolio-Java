package com.hopefull117.portfolio.java.controller;

import com.hopefull117.portfolio.java.dto.ProjectsDTO;
import com.hopefull117.portfolio.java.model.Project;

import com.hopefull117.portfolio.java.model.Technology;
import com.hopefull117.portfolio.java.service.ProjectService;
import com.hopefull117.portfolio.java.service.TechnologieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@Slf4j
@RequiredArgsConstructor
public class AdminController {
    private final TechnologieService technologieService;
    private final ProjectService projectService;

    @GetMapping("/dashboard")
    public String getDashBoard(Model model){
        log.info("Tentative d'accès au panneau administrateur");
        model.addAttribute("title","Dashboard");
        return "dashboard";
    }

    @GetMapping("/projects")
    public String getDashBoardProjects(Model model){
        log.info("Tentative d'accès au panneau de gestion des projets");
        model.addAttribute("projects",new ProjectsDTO());
        model.addAttribute("technologies",technologieService.getAll());
        return "form-projects";
    }

    @PostMapping("/projects")
    public String createProjects(@Valid ProjectsDTO projectsDTO){
        log.info("Tentative d'ajout d'un projet");
        Project project = ProjectService.projectToEntity(projectsDTO);
        projectService.create(project);
        return "redirect:/admin/projects";

    }

    @GetMapping("/technologies")
    public String getDashBoardTechnologies(Model model){
        log.info("Tentative d'accès au panneau de gestion des technologies");
        model.addAttribute("technologies",new Technology());
        return "form-technologies";
    }
    @PostMapping("/technologies")
    public String createTechnologies(@Valid Technology technology){
        log.info("Tentative d'ajout d'une technologie");
        technologieService.create(technology);
        return "redirect:/admin/technologies";
    }


}
