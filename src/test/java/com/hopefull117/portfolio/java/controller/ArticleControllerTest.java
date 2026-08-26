package com.hopefull117.portfolio.java.controller;

import com.hopefull117.portfolio.java.dto.ArticleViewDto;
import com.hopefull117.portfolio.java.service.ArticleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class ArticleControllerTest {

    @Mock
    private ArticleService articleService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ArticleController(articleService)).build();
    }

    @Test
    void generatedSlugRemainsReachableAtExistingPublicRoute() throws Exception {
        ArticleViewDto article = ArticleViewDto.builder()
                .title("Titre français")
                .slug("titre-francais")
                .content("Contenu")
                .build();
        when(articleService.findBySlug("titre-francais")).thenReturn(article);

        mockMvc.perform(get("/blog/titre-francais"))
                .andExpect(status().isOk())
                .andExpect(view().name("public/article"))
                .andExpect(model().attribute("article", article));

        verify(articleService).findBySlug("titre-francais");
    }
}
