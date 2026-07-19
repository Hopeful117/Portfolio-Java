package com.hopefull117.portfolio.java.service;

import com.hopefull117.portfolio.java.exception.EntityNotFoundException;
import com.hopefull117.portfolio.java.model.Article;
import com.hopefull117.portfolio.java.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleService {


    private final ArticleRepository articleRepository;
    private final FileStorageService fileStorageService;



    public List<Article> getAll(){

        return articleRepository.findAll();

    }



    public Article findById(String id){

        return articleRepository.findById(id)
                .orElseThrow(
                        () -> new EntityNotFoundException("Article non trouvé")
                );

    }



    public Article create(Article article, MultipartFile file) throws IOException {
        if(!file.isEmpty()){
            article.setCoverImage(fileStorageService.save(file));
        }
        Instant now = Instant.now();

        article.setCreatedAt(now);
        article.setUpdatedAt(now);

        return articleRepository.save(article);

    }



    public void update(String id, Article article, MultipartFile file) throws IOException {

        Article existingArticle = findById(id);

        if(!file.isEmpty()){
            existingArticle.setCoverImage(fileStorageService.save(file));
        }


        existingArticle.setTitle(article.getTitle());

        existingArticle.setSlug(article.getSlug());

        existingArticle.setExcerpt(article.getExcerpt());

        existingArticle.setContent(article.getContent());
        

        existingArticle.setTags(article.getTags());

        existingArticle.setPublished(article.isPublished());

        existingArticle.setUpdatedAt(java.time.Instant.now());


        articleRepository.save(existingArticle);

    }



    public void deleteById(String id){

        if(!articleRepository.existsById(id)){

            throw new EntityNotFoundException(
                    "Article non trouvé"
            );

        }


        articleRepository.deleteById(id);

    }



    public List<Article> findPublished(){

        return articleRepository.findByPublishedTrueOrderByCreatedAtDesc();

    }



    public Article findBySlug(String slug){

        return articleRepository.findBySlug(slug)
                .orElseThrow(
                        () -> new EntityNotFoundException(
                                "Article non trouvé"
                        )
                );

    }

}
