package com.hopefull117.portfolio.java.controller;

import com.hopefull117.portfolio.java.service.SkillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
@RequiredArgsConstructor
public class SkillController {
    private final SkillService skillService;
    @GetMapping("/skills")
    public String skills(Model model) {
        log.info("Accès page skills");
        model.addAttribute("title","Skills");
        model.addAttribute("skillCategories",skillService.getSkillsGroupedByCategory());
        return "public/skills";
    }
}
