package com.hopefull117.portfolio.java.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
public class LoginController {
    @RequestMapping("/login")
    public String getLoginPage(Model model){
        log.info("Accès a la page login");
        model.addAttribute("title","Login");
        return "admin/login";
    }
}
