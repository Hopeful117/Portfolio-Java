package com.hopefull117.portfolio.java.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class PageController {
    @GetMapping("/projects")
    public String projects(Model model) {
        log.info("Accès page projets");
        model.addAttribute("title","Projets");

        return "projects";
    }

    @GetMapping("/skills")
    public String skills(Model model) {
        log.info("Accès page skills");
        model.addAttribute("title","Skills");
        return "skills";
    }

    @GetMapping("/journey")
    public String journey(Model model) {
        log.info("Accès page journey");
        model.addAttribute("title","Journey");
        return "journey";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        log.info("Accès page contact");
        model.addAttribute("title","Contact");
        return "contact";
    }
}
