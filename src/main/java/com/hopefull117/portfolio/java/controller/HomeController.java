package com.hopefull117.portfolio.java.controller;

import com.hopefull117.portfolio.java.service.ProjectService;
import com.hopefull117.portfolio.java.service.TechnologieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
@RequiredArgsConstructor
public class HomeController {
    private final TechnologieService technologieService;
    private final ProjectService projectService;

        @GetMapping("/")
        public String home(Model model) {
            log.info("Accès à la page d'accueil");
            model.addAttribute("title", "Portfolio");
            model.addAttribute("projects",projectService.getLastThreeProject());
            model.addAttribute("technologies",technologieService.getAll().stream().limit(5));
            return "public/home";
        }
}
