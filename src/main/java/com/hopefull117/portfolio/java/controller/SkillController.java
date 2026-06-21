package com.hopefull117.portfolio.java.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class SkillController {
    @GetMapping("/skills")
    public String skills(Model model) {
        log.info("Accès page skills");
        model.addAttribute("title","Skills");
        return "skills";
    }
}
