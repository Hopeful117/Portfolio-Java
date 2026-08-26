package com.hopefull117.portfolio.java.controller;

import com.hopefull117.portfolio.java.dto.ProjectEditDTO;
import com.hopefull117.portfolio.java.dto.ProjectsDTO;
import com.hopefull117.portfolio.java.exception.ArticlePersistenceException;
import com.hopefull117.portfolio.java.exception.ArticleSlugConflictException;
import com.hopefull117.portfolio.java.helper.Category;
import com.hopefull117.portfolio.java.helper.SkillLevel;
import com.hopefull117.portfolio.java.helper.TimelineType;
import com.hopefull117.portfolio.java.mapper.ProjectMapper;
import com.hopefull117.portfolio.java.model.*;

import com.hopefull117.portfolio.java.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
@Slf4j
@RequiredArgsConstructor
public class AdminController {
    private final TechnologieService technologieService;
    private final ProjectService projectService;
    private final ProjectMapper projectMapper;
    private final SkillService skillService;
    private final TimelineEntryService timelineEntryService;
    private final ArticleService articleService;


    @GetMapping("/dashboard")
    public String getDashBoard(Model model){
        log.info("Tentative d'accès au panneau administrateur");
        model.addAttribute("title","Dashboard");
        return "admin/dashboard";
    }
    @GetMapping("/projects")
    public String getProjectDashboard(Model model){
        log.info("Tentative d'accès au panneau des projets");
        model.addAttribute("projects",projectService.getAll());
        return "admin/projects/dashboard-projects";
    }


    @GetMapping("/projects/add")
    public String getAddProjectsForm(Model model){
        log.info("Tentative d'accès au panneau d'ajouts de projets");
        model.addAttribute("projects",new ProjectsDTO());
        model.addAttribute("technologies",technologieService.getAll());
        return "admin/projects/form-projects";
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
        return "admin/projects/form-edit-projects";


    }
    @PostMapping("/projects/edit/{id}")
    public String editProject (@Valid ProjectEditDTO projectEditDTO,@PathVariable("id") Long id) throws IOException {
        log.info("Tentative de modification du projet {}",id);
        projectService.updateFromDto(projectEditDTO);
        return"redirect:/admin/projects";



    }
    @GetMapping("/projects/delete/{id}")
    public String deleteProject(@PathVariable("id") Long id){
        log.info("Tentative de suppression du projet {}", id);
        projectService.deleteById(id);
        return "redirect:/admin/projects";
    }

    @GetMapping("/technologies")
    public String getTechnologiesDashboard(Model model){
        log.info("Tentative d'accès au panneau de gestion des technologies");
        model.addAttribute("technologies",technologieService.getAll());
        return "admin/technologies/dashboard-technologies";

    }
    @GetMapping("/technologies/add")
    public String getAddTechnologiesForm(Model model){
        log.info("Tentative d'accès au panneau d'ajout de technologies");
        model.addAttribute("technologies",new Technology());
        return "admin/technologies/form-technologies";
    }
    @PostMapping("/technologies")
    public String createTechnologies(@Valid Technology technology){
        log.info("Tentative d'ajout d'une technologie");
        technologieService.create(technology);
        return "redirect:/admin/technologies";
    }
    @GetMapping("/technologies/edit/{id}")
    public String getEditTechnologieForm(Model model, @Valid Technology technology ,@PathVariable("id")Long id){
        log.info("Tentative d'accès au formulaire de modification de la technologie {}",id);
        model.addAttribute("technology",technologieService.findById(id));
        return("admin/technologies/form-edit-technologies");

    }

    @PostMapping("/technologies/edit/{id}")
    public String editTechnologie (@Valid Technology technology,@PathVariable("id") Long id){
        log.info("Tentative de modification de la technologie {}",id);
        technologieService.update(id,technology);
        return "redirect:/admin/technologies";
    }

    @GetMapping("/technologies/delete/{id}")
    public String deleteTechnologie(@PathVariable("id")Long id){
        log.info("Tentative de suppression de la technologie {}",id);
        technologieService.deleteById(id);
        return "redirect:/admin/technologies";
    }

    @GetMapping("/skills")
    public String getSkillDashboard(Model model){
        log.info("Tentative d'accès au panneau de gestion des compétences");
        model.addAttribute("skills",skillService.getAll());
        return "admin/skills/dashboard-skills";

    }

    @GetMapping("/skills/add")
    public String getAddSkillsForm(Model model){
        log.info("Tentative d'accès au panneau d'ajout d'un skill");

        model.addAttribute("skill", new Skill());
        model.addAttribute("categories", Category.values());
        model.addAttribute("levels", SkillLevel.values());

        return "admin/skills/form-skills";

    }

    @PostMapping("/skills")
    public String createSkill(@Valid Skill skill){
        log.info("Tentative de création d'un skill");
        skillService.create(skill);
        return "redirect:/admin/skills";
    }

    @GetMapping("/skills/edit/{id}")
    public String getEditSkillForm(Model model,@PathVariable("id")Long id){
        log.info("Tentative d'accès au formulaire de la compétence {}",id);
        model.addAttribute("skill",skillService.findById(id));
        model.addAttribute("categories", Category.values());
        model.addAttribute("levels", SkillLevel.values());
        return "admin/skills/form-edit-skills.html";

    }

    @PostMapping("/skills/edit/{id}")
    public String editSkill(@Valid Skill skill, @PathVariable("id") Long id){
        log.info("tentative de modification de la compétence {}",id);
        skillService.update(id,skill);
        return "redirect:/admin/skills";
    }

    @GetMapping("/skills/delete/{id}")
    public String deleteSkill(@PathVariable("id") Long id){
        log.info("tentative de suppression de la compétence {}",id);
        skillService.deleteById(id);
        return "redirect:/admin/skills";

        }

    @GetMapping("/timeline")
    public String getTimelineDashboard(Model model){
        log.info("tentative d'accès au panneau de gestion de la timeline" );
        model.addAttribute("timelineEntries",timelineEntryService.getTimeline());
        return "admin/timeline/dashboard-timeline";

    }

    @GetMapping("/timeline/add")
    public String getAddTimelineForm(Model model){
        log.info("tentative d'accès au formulaire de création de timeline");
        model.addAttribute("timelineEntry", new TimelineEntry());
        model.addAttribute("timelineTypes", TimelineType.values());
        return "admin/timeline/form-timeline";
    }
    @PostMapping("/timeline")
    public String createTimeline(@Valid TimelineEntry timelineEntry){
        log.info("tentative d'ajout d'un élément dans la timeline");
        timelineEntryService.create(timelineEntry);
        return "redirect:/admin/timeline";
    }
    @GetMapping("/timeline/edit/{id}")
    public String getEditTimelineForm(Model model,@PathVariable("id")Long id){
        log.info("tentative d'accès au formulaire de la timeline{}", id);
        model.addAttribute("timelineEntry",timelineEntryService.findById(id));
        model.addAttribute("timelineTypes",TimelineType.values());
        return "admin/timeline/form-edit-timeline";

    }
    @PostMapping("/timeline/edit/{id}")
    public String editTimeline(@Valid TimelineEntry timelineEntry, @PathVariable("id")Long id){
        log.info("tentative de modification de la timeline {}", id);
        timelineEntryService.update(id,timelineEntry);
        return "redirect:/admin/timeline";
    }

    @GetMapping("/timeline/delete/{id}")
    public String deleteTimeline(@PathVariable("id") Long id){
        log.info("tentative de suppression de timeline {}", id);
        timelineEntryService.deleteById(id);
        return "redirect:/admin/timeline";
    }

    @GetMapping("/articles")
    public String getArticleDashboard(Model model){

        log.info("Tentative d'accès au panneau de gestion des articles");

        model.addAttribute(
                "articles",
                articleService.getAll()
        );

        return "admin/articles/dashboard-articles";
    }

    @GetMapping("/articles/add")
    public String getAddArticleForm(Model model){

        log.info("Tentative d'accès au formulaire d'ajout d'article");

        model.addAttribute(
                "article",
                new Article()
        );

        return "admin/articles/form-articles";

    }
    @PostMapping("/articles")
    public String createArticle(@Valid Article article,
                                BindingResult bindingResult,
                                @RequestParam("image") MultipartFile image) throws IOException {

        log.info("Tentative de création d'un article");

        if (bindingResult.hasErrors()) {
            return "admin/articles/form-articles";
        }

        try {
            articleService.create(article,image);
        } catch (IllegalArgumentException exception) {
            if (exception.getMessage() != null && exception.getMessage().contains("titre")) {
                bindingResult.rejectValue("title", "article.title.slug", exception.getMessage());
            } else {
                bindingResult.reject("article.image", exception.getMessage());
            }
            return "admin/articles/form-articles";
        } catch (ArticleSlugConflictException | ArticlePersistenceException exception) {
            bindingResult.reject("article.save", exception.getMessage());
            return "admin/articles/form-articles";
        } catch (IOException exception) {
            log.warn("Échec du traitement de l'image de couverture: {}", exception.getMessage());
            bindingResult.reject("article.image", "Impossible de traiter l'image pour le moment");
            return "admin/articles/form-articles";
        }

        return "redirect:/admin/articles";

    }

    @GetMapping("/articles/edit/{id}")
    public String getEditArticleForm(Model model,
                                     @PathVariable String id){

        log.info("Modification article {}", id);


        model.addAttribute(
                "article",
                articleService.findById(id)
        );


        return "admin/articles/form-edit-articles";

    }
    @PostMapping("/articles/edit/{id}")
    public String editArticle(@Valid Article article,
                              BindingResult bindingResult,
                              @PathVariable String id,
                              @RequestParam("image") MultipartFile image) throws IOException {

        if (bindingResult.hasErrors()) {
            return "admin/articles/form-edit-articles";
        }

        try {
            articleService.update(id, article,image);
        } catch (IllegalArgumentException exception) {
            bindingResult.reject("article.image", exception.getMessage());
            return "admin/articles/form-edit-articles";
        } catch (ArticlePersistenceException exception) {
            bindingResult.reject("article.save", exception.getMessage());
            return "admin/articles/form-edit-articles";
        } catch (IOException exception) {
            log.warn("Échec du traitement de l'image de couverture: {}", exception.getMessage());
            bindingResult.reject("article.image", "Impossible de traiter l'image pour le moment");
            return "admin/articles/form-edit-articles";
        }


        return "redirect:/admin/articles";

    }
    @GetMapping("/articles/delete/{id}")
    public String deleteArticle(@PathVariable String id){

        log.info("Suppression article {}", id);


        articleService.deleteById(id);


        return "redirect:/admin/articles";

    }



}
