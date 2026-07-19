package com.hopefull117.portfolio.java.controller;

import com.hopefull117.portfolio.java.model.Article;
import com.hopefull117.portfolio.java.service.ArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/blog")
@Slf4j
@RequiredArgsConstructor
public class ArticleController {
    private final ArticleService articleService;


    @GetMapping
    public String blog(Model model){

        List<Article> articles = articleService.findPublished();

        model.addAttribute("articles", articles);

        return "public/blog";
    }





    @GetMapping("/{slug}")
    public String article(@PathVariable String slug,
                          Model model){


        Article article = articleService.findBySlug(slug);


        model.addAttribute("article", article);


        return "public/article";

    }
}
