package com.hopefull117.portfolio.java.controller;

import com.hopefull117.portfolio.java.exception.ArticlePersistenceException;
import com.hopefull117.portfolio.java.mapper.ProjectMapper;
import com.hopefull117.portfolio.java.model.Article;
import com.hopefull117.portfolio.java.service.ArticleService;
import com.hopefull117.portfolio.java.service.ProjectService;
import com.hopefull117.portfolio.java.service.SkillService;
import com.hopefull117.portfolio.java.service.TechnologieService;
import com.hopefull117.portfolio.java.service.TimelineEntryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class AdminArticleControllerTest {

    @Mock
    private TechnologieService technologieService;
    @Mock
    private ProjectService projectService;
    @Mock
    private ProjectMapper projectMapper;
    @Mock
    private SkillService skillService;
    @Mock
    private TimelineEntryService timelineEntryService;
    @Mock
    private ArticleService articleService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AdminController controller = new AdminController(
                technologieService,
                projectService,
                projectMapper,
                skillService,
                timelineEntryService,
                articleService
        );
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setValidator(validator)
                .build();
    }

    @Test
    void successfulCreateStillRedirects() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", new byte[0]);
        when(articleService.create(any(Article.class), any())).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(multipart("/admin/articles")
                        .file(image)
                        .param("title", "Titre français")
                        .param("slug", "manual-attempt")
                        .param("content", "Content")
                        .param("published", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/articles"));

        verify(articleService).create(any(Article.class), any());
    }

    @Test
    void blankTitleReturnsCreateFormWithFriendlyError() throws Exception {
        mockMvc.perform(multipart("/admin/articles")
                        .file(new MockMultipartFile("image", new byte[0]))
                        .param("title", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/articles/form-articles"))
                .andExpect(model().attributeHasFieldErrors("article", "title"));

        verify(articleService, never()).create(any(), any());
    }

    @Test
    void titleWithoutSlugCharactersReturnsCreateFormWithFriendlyError() throws Exception {
        when(articleService.create(any(Article.class), any()))
                .thenThrow(new IllegalArgumentException("Le titre doit contenir des lettres ou des chiffres"));

        mockMvc.perform(multipart("/admin/articles")
                        .file(new MockMultipartFile("image", new byte[0]))
                        .param("title", "🚀✨"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/articles/form-articles"))
                .andExpect(model().attributeHasFieldErrors("article", "title"));
    }

    @Test
    void persistenceFailureReturnsFormWithoutExposingCause() throws Exception {
        when(articleService.create(any(Article.class), any()))
                .thenThrow(new ArticlePersistenceException("Impossible d'enregistrer l'article pour le moment", new RuntimeException("mongo")));

        mockMvc.perform(multipart("/admin/articles")
                        .file(new MockMultipartFile("image", new byte[0]))
                        .param("title", "Valid title"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/articles/form-articles"))
                .andExpect(model().attributeHasErrors("article"));
    }

    @Test
    void successfulEditStillRedirects() throws Exception {
        mockMvc.perform(multipart("/admin/articles/edit/article-id")
                        .file(new MockMultipartFile("image", new byte[0]))
                        .param("title", "Changed title")
                        .param("slug", "ignored-change")
                        .param("content", "Changed content"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/articles"));

        verify(articleService).update(any(), any(Article.class), any());
    }

    @Test
    void articleFormsHaveNoEditableSlugField() throws IOException {
        String createForm = resource("templates/admin/articles/form-articles.html");
        String editForm = resource("templates/admin/articles/form-edit-articles.html");

        assertFalse(createForm.contains("*{slug}"));
        assertFalse(editForm.contains("*{slug}"));
        assertFalse(createForm.contains("name=\"slug\""));
        assertFalse(editForm.contains("name=\"slug\""));
    }

    private String resource(String path) throws IOException {
        return new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8);
    }
}
