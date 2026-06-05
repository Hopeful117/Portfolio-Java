package com.hopefull117.portfolio.java.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class HomeController {

        @GetMapping("/")
        public String home(Model model) {
            log.info("Accès à la page d'accueil");
            model.addAttribute("title", "Portfolio");
            return "home";
        }
}
