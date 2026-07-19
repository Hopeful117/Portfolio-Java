package com.hopefull117.portfolio.java.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class ContactController {
    @GetMapping("/contact")
    public String contact(Model model) {
        log.info("Accès page contact");
        model.addAttribute("title","Contact");
        return "public/contact";
    }
}
