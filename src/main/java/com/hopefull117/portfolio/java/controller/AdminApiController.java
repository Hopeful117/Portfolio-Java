package com.hopefull117.portfolio.java.controller;

import com.hopefull117.portfolio.java.dto.*;
import com.hopefull117.portfolio.java.model.*;
import com.hopefull117.portfolio.java.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminApiController {
    private final ProjectService projectService;
    private final TechnologieService technologieService;
    private final SkillService skillService;
    private final TimelineEntryService timelineEntryService;
    private final ArticleService articleService;

    @GetMapping("/summary")
    public AdminSummaryDto summary() {
        return new AdminSummaryDto(projectService.getAll().size(), articleService.getAll().size(), technologieService.getAll().size(), skillService.getAll().size(), timelineEntryService.getAll().size());
    }

    @GetMapping("/projects")
    @Transactional(readOnly = true)
    public List<AdminProjectDto> projects() {
        return projectService.getAll().stream().map(this::projectDto).toList();
    }

    @PostMapping(value = "/projects", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createProject(@Valid @RequestBody AdminProjectRequest request) throws IOException {
        projectService.create(new ProjectsDTO(request.title(), request.description(), request.technologyIds() == null ? List.of() : request.technologyIds(), null, request.githubUrl()));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping(value = "/projects", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> createProjectWithImage(@RequestPart("data") @Valid AdminProjectRequest request, @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
        projectService.create(new ProjectsDTO(request.title(), request.description(), request.technologyIds() == null ? List.of() : request.technologyIds(), image, request.githubUrl()));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping(value = "/projects/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateProject(@PathVariable Long id, @Valid @RequestBody AdminProjectRequest request) throws IOException {
        projectService.updateFromDto(new ProjectEditDTO(id, request.title(), request.description(), request.githubUrl(), null, null, request.technologyIds() == null ? List.of() : request.technologyIds()));
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/projects/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateProjectWithImage(@PathVariable Long id, @RequestPart("data") @Valid AdminProjectRequest request, @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
        projectService.updateFromDto(new ProjectEditDTO(id, request.title(), request.description(), request.githubUrl(), null, image, request.technologyIds() == null ? List.of() : request.technologyIds()));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/projects/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/technologies")
    public List<AdminTechnologyDto> technologies() {
        return technologieService.getAll().stream().map(this::technologyDto).toList();
    }

    @PostMapping("/technologies")
    public ResponseEntity<AdminTechnologyDto> createTechnology(@Valid @RequestBody AdminTechnologyRequest request) {
        Technology technology = new Technology();
        technology.setName(request.name());
        technology.setIconeUrl(request.iconUrl());
        technologieService.create(technology);
        return ResponseEntity.status(HttpStatus.CREATED).body(technologyDto(technology));
    }

    @PutMapping("/technologies/{id}")
    public ResponseEntity<AdminTechnologyDto> updateTechnology(@PathVariable Long id, @Valid @RequestBody AdminTechnologyRequest request) {
        Technology technology = technologieService.findById(id);
        technology.setName(request.name());
        technology.setIconeUrl(request.iconUrl());
        technologieService.update(id, technology);
        return ResponseEntity.ok(technologyDto(technology));
    }

    @DeleteMapping("/technologies/{id}")
    public ResponseEntity<Void> deleteTechnology(@PathVariable Long id) {
        technologieService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/skills")
    public List<AdminSkillDto> skills() {
        return skillService.getAll().stream().map(this::skillDto).toList();
    }

    @PostMapping("/skills")
    public ResponseEntity<AdminSkillDto> createSkill(@Valid @RequestBody AdminSkillRequest request) {
        Skill skill = new Skill();
        apply(skill, request);
        skillService.create(skill);
        return ResponseEntity.status(HttpStatus.CREATED).body(skillDto(skill));
    }

    @PutMapping("/skills/{id}")
    public ResponseEntity<AdminSkillDto> updateSkill(@PathVariable Long id, @Valid @RequestBody AdminSkillRequest request) {
        Skill skill = skillService.findById(id);
        apply(skill, request);
        skillService.update(id, skill);
        return ResponseEntity.ok(skillDto(skill));
    }

    @DeleteMapping("/skills/{id}")
    public ResponseEntity<Void> deleteSkill(@PathVariable Long id) {
        skillService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/timeline")
    public List<AdminTimelineDto> timeline() {
        return timelineEntryService.getTimeline().stream().map(this::timelineDto).toList();
    }

    @PostMapping("/timeline")
    public ResponseEntity<AdminTimelineDto> createTimeline(@Valid @RequestBody AdminTimelineRequest request) {
        TimelineEntry entry = new TimelineEntry();
        apply(entry, request);
        timelineEntryService.create(entry);
        return ResponseEntity.status(HttpStatus.CREATED).body(timelineDto(entry));
    }

    @PutMapping("/timeline/{id}")
    public ResponseEntity<AdminTimelineDto> updateTimeline(@PathVariable Long id, @Valid @RequestBody AdminTimelineRequest request) {
        TimelineEntry entry = new TimelineEntry();
        apply(entry, request);
        timelineEntryService.update(id, entry);
        return ResponseEntity.ok(timelineDto(timelineEntryService.findById(id)));
    }

    @DeleteMapping("/timeline/{id}")
    public ResponseEntity<Void> deleteTimeline(@PathVariable Long id) {
        timelineEntryService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/articles")
    public List<AdminArticleDto> articles() {
        return articleService.getAll().stream().map(this::articleDto).toList();
    }

    @PostMapping(value = "/articles", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AdminArticleDto> createArticle(@Valid @RequestBody AdminArticleRequest request) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED).body(articleDto(articleService.create(article(request), null)));
    }

    @PostMapping(value = "/articles", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AdminArticleDto> createArticleWithImage(@RequestPart("data") @Valid AdminArticleRequest request, @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED).body(articleDto(articleService.create(article(request), image)));
    }

    @PutMapping(value = "/articles/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AdminArticleDto> updateArticle(@PathVariable String id, @Valid @RequestBody AdminArticleRequest request) throws IOException {
        articleService.update(id, article(request), null);
        return ResponseEntity.ok(articleDto(articleService.findById(id)));
    }

    @PutMapping(value = "/articles/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AdminArticleDto> updateArticleWithImage(@PathVariable String id, @RequestPart("data") @Valid AdminArticleRequest request, @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
        articleService.update(id, article(request), image);
        return ResponseEntity.ok(articleDto(articleService.findById(id)));
    }

    @DeleteMapping("/articles/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable String id) {
        articleService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private AdminProjectDto projectDto(Project project) {
        return new AdminProjectDto(project.getId(), project.getTitle(), project.getDescription(), project.getGithubUrl(), project.getImagePath(), project.getTechnologies() == null ? List.of() : project.getTechnologies().stream().map(this::technologyDto).toList());
    }

    private AdminTechnologyDto technologyDto(Technology technology) {
        return new AdminTechnologyDto(technology.getId(), technology.getName(), technology.getIconeUrl());
    }

    private AdminSkillDto skillDto(Skill skill) {
        return new AdminSkillDto(skill.getId(), skill.getName(), skill.getCategory() == null ? null : skill.getCategory().name(), skill.getSkillLevel() == null ? null : skill.getSkillLevel().name());
    }

    private AdminTimelineDto timelineDto(TimelineEntry entry) {
        return new AdminTimelineDto(entry.getId(), entry.getTitle(), entry.getDate(), entry.getDescription(), entry.getLink(), entry.getDisplayOrder(), entry.getType() == null ? null : entry.getType().name());
    }

    private AdminArticleDto articleDto(Article article) {
        return new AdminArticleDto(article.getId(), article.getTitle(), article.getSlug(), article.getExcerpt(), article.getContent(), article.getCoverImage(), article.getTags() == null ? List.of() : article.getTags(), article.isPublished(), article.getCreatedAt(), article.getUpdatedAt());
    }

    private void apply(Skill skill, AdminSkillRequest request) {
        skill.setName(request.name());
        skill.setCategory(request.category());
        skill.setSkillLevel(request.skillLevel());
    }

    private void apply(TimelineEntry entry, AdminTimelineRequest request) {
        entry.setTitle(request.title());
        entry.setDate(request.date());
        entry.setDescription(request.description());
        entry.setLink(request.link());
        entry.setDisplayOrder(request.displayOrder());
        entry.setType(request.type());
    }

    private Article article(AdminArticleRequest request) {
        Article article = new Article();
        article.setTitle(request.title());
        article.setExcerpt(request.excerpt());
        article.setContent(request.content());
        article.setTags(request.tags());
        article.setPublished(request.published());
        return article;
    }
}
