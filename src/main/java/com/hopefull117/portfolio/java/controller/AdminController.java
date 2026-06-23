package com.hopefull117.portfolio.java.controller;

import com.hopefull117.portfolio.java.dto.ProjectEditDTO;
import com.hopefull117.portfolio.java.dto.ProjectsDTO;
import com.hopefull117.portfolio.java.mapper.ProjectMapper;
import com.hopefull117.portfolio.java.model.Project;

import com.hopefull117.portfolio.java.model.Technology;
import com.hopefull117.portfolio.java.service.ProjectService;
import com.hopefull117.portfolio.java.service.TechnologieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequestMapping("/admin")
@Slf4j
@RequiredArgsConstructor
public class AdminController {
    private final TechnologieService technologieService;
    private final ProjectService projectService;
    private final ProjectMapper projectMapper;


    @GetMapping("/dashboard")
    public String getDashBoard(Model model){
        log.info("Tentative d'accès au panneau administrateur");
        model.addAttribute("title","Dashboard");
        return "dashboard";
    }
    @GetMapping("/projects")
    public String getProjectDashboard(Model model){
        log.info("Tentative d'accès au panneau des projets");
        model.addAttribute("projects",projectService.getAll());
        return "dashboard-projects";
    }


    @GetMapping("/projects/add")
    public String getAddProjectsForm(Model model){
        log.info("Tentative d'accès au panneau d'ajouts de projets");
        model.addAttribute("projects",new ProjectsDTO());
        model.addAttribute("technologies",technologieService.getAll());
        return "form-projects";
    }

    @PostMapping("/projects")
    public String createProjects(@Valid ProjectsDTO projectsDTO ,@RequestParam("image") MultipartFile image) throws IOException {
        log.info("Tentative d'ajout d'un projet");
        projectService.create(projectsDTO);
        return "redirect:/projects";

    }
    @GetMapping("/projects/edit/{id}")
    public String getEditProjectForm(Model model, @PathVariable("id") Long id){
        log.info("Tentative d'accès au formulaire de modification du projet {}", id);
        Project project= projectService.findById(id);
        model.addAttribute("project",projectMapper.toEditDto(project));
        model.addAttribute("technologies",technologieService.getAll());
        return "form-edit-projects";


    }
    @PostMapping("/projects/edit/{id}")
    public String editProject (@Valid ProjectEditDTO projectEditDTO,@PathVariable("id") Long id) throws IOException {
        log.info("Tentative de modification du projet {}",id);
        projectService.updateFromDto(projectEditDTO);
        return"redirect:/admin/projects";



    }

    @GetMapping("/technologies")
    public String getTechnologiesDashboard(Model model){
        log.info("Tentative d'accès au panneau de gestion des technologies");
        model.addAttribute("technologies",technologieService.getAll());
        return "dashboard-technologies";

    }
    @GetMapping("/technologies/add")
    public String getAddTechnologiesForm(Model model){
        log.info("Tentative d'accès au panneau d'ajout de technologies");
        model.addAttribute("technologies",new Technology());
        return "form-technologies";
    }
    @PostMapping("/technologies")
    public String createTechnologies(@Valid Technology technology){
        log.info("Tentative d'ajout d'une technologie");
        technologieService.create(technology);
        return "redirect:/admin/projects";
    }
    @GetMapping("/technologies/edit/{id}")
    public String getEditTechnologieForm(Model model, @Valid Technology technology ,@PathVariable("id")Long id){
        log.info("Tentative d'accès au formulaire de modification de la technologie {}",id);
        model.addAttribute("technology",technologieService.findById(id));
        return("form-edit-technologies");

    }

    @PostMapping("/technologies/edit/{id}")
    public String editTechnologie (@Valid Technology technology,@PathVariable("id") Long id){
        log.info("Tentative de modification de la technologie {}",id);
        technologieService.update(id,technology);
        return "redirect:/admin/dashboard";
    }


}
